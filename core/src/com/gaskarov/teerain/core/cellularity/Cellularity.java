package com.gaskarov.teerain.core.cellularity;

import com.badlogic.gdx.math.Vector2;
import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.PhysicsWall;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.GraphicsUtils;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.Player;
import com.gaskarov.teerain.game.game.ControlOrganoid;
import com.gaskarov.teerain.game.game.TeeCellData;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.container.FloatArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public abstract class Cellularity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public abstract boolean isChunk();

	public abstract ChunkCellularity getChunk();

	public abstract int getChunkX();

	public abstract int getChunkY();

	public abstract Tissularity getTissularity();

	public abstract Vector2 localToChunk(float pX, float pY);

	public abstract int localToGlobalX(int pX);

	public abstract int localToGlobalY(int pY);

	public abstract MetaBody getBody();

	public abstract MetaBody getBody(int pX, int pY);

	public abstract void tickEnable(int pX, int pY, int pZ);

	public abstract void tickDisable(int pX, int pY, int pZ);

	public abstract void tissularedEnable(int pX, int pY, int pZ);

	public abstract void tissularedDisable(int pX, int pY, int pZ);

	public abstract PhysicsWall getPhysicsWall(int pX, int pY, int pZ);

	public abstract void setPhysicsWall(int pX, int pY, int pZ, PhysicsWall pPhysicsWall);

	public abstract int getCell(int pX, int pY, int pZ);

	public abstract void setCell(int pX, int pY, int pZ, int pCell, CellData pCellData);

	public abstract CellData getCellData(int pX, int pY, int pZ);

	public abstract boolean isShellable(int pX, int pY, int pZ);

	// ===========================================================
	// Methods
	// ===========================================================

	protected void attach(int pCell, int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_DYNAMIC_LIGHT_SOURCE[cellType] && !isChunk())
			tickEnable(pX, pY, pZ);

		if (cellType == Cells.CELL_TYPE_TEE) {
			CellData cellData = getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).attach(this, pX, pY, pZ);
		}
	}

	protected void detach(int pCell, int pX, int pY, int pZ) {

		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_DYNAMIC_LIGHT_SOURCE[cellType] && !isChunk())
			tickDisable(pX, pY, pZ);

		switch (Cells.CELLS_PHYSICS_TYPE[pCell & Cells.CELL_TYPES_MAX_MASK]) {
		case Cells.CELL_PHYSICS_TYPE_NO:
			break;
		case Cells.CELL_PHYSICS_TYPE_WALL: {
			PhysicsWall physicsWall = getPhysicsWall(pX, pY, pZ);
			if (physicsWall != null) {
				physicsWall.destroyPhysics(this, pX, pY, pZ);
				setPhysicsWall(pX, pY, pZ, null);
			}
			break;
		}
		default:
			break;
		}

		if (cellType == Cells.CELL_TYPE_TEE) {
			CellData cellData = getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).detach(this, pX, pY, pZ);
		}
	}

	protected void tissularedAttach(int pCell, int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (cellType == Cells.CELL_TYPE_TEE) {
			CellData cellData = getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).tissularedAttach(this, pX, pY, pZ);
		}
	}

	protected void tissularedDetach(int pCell, int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (cellType == Cells.CELL_TYPE_TEE) {
			CellData cellData = getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).tissularedDetach(this, pX, pY, pZ);
		}
	}

	protected int update(int pCell, int pX, int pY, int pZ) {
		return 0;
	}

	protected void refresh(int pCell, int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_PHYSICS_TYPE[cellType]) {
		case Cells.CELL_PHYSICS_TYPE_NO:
			break;
		case Cells.CELL_PHYSICS_TYPE_WALL: {
			int flags = PhysicsWall.near(this, pX, pY, pZ);
			boolean shellable = Cells.CELLS_IS_SHELLABLE[cellType];
			boolean flag = flags != 31 || !isChunk() || !shellable;
			PhysicsWall physicsWall = getPhysicsWall(pX, pY, pZ);
			if (physicsWall != null) {
				if (physicsWall.getNear() != flags) {
					physicsWall.destroyPhysics(this, pX, pY, pZ);
					if (!flag) {
						setPhysicsWall(pX, pY, pZ, null);
						physicsWall = null;
					}
				} else
					physicsWall = null;
			} else if (flag) {
				physicsWall = PhysicsWall.obtain();
				setPhysicsWall(pX, pY, pZ, physicsWall);
			}
			if (physicsWall != null) {
				physicsWall.setNear(flags);
				float borderSize = Cells.CELLS_PHYSICS_BORDER_SIZE[cellType];
				float cornerValue = Cells.CELLS_PHYSICS_CORNER_VALUE[cellType];
				float cornerSize = Cells.CELLS_PHYSICS_CORNER_SIZE[cellType];
				float density = Cells.CELLS_PHYSICS_DENSITY[cellType];
				float friction = Cells.CELLS_PHYSICS_FRICTION[cellType];
				float restitution = Cells.CELLS_PHYSICS_RESTITUTION[cellType];
				boolean solid = Cells.CELLS_IS_SOLID[cellType];
				boolean square = Cells.CELLS_IS_SQUARE[cellType];
				physicsWall.createPhysics(this, pX, pY, pZ, borderSize, cornerValue, cornerSize,
						density, friction, restitution, solid, square);
			}
			break;
		}
		default:
			break;
		}
	}

	protected void tick(int pCell, int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_DYNAMIC_LIGHT_SOURCE[cellType] && !isChunk())
			tickDynamicLightSource(pX, pY, pZ, pCell);

		if (cellType == Cells.CELL_TYPE_TEE) {
			CellData cellData = getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).tick(this, pX, pY, pZ);
		}
	}

	protected void render(int pCell, int pX, int pY, int pZ, float pOffsetX, float pOffsetY,
			int pTileX, int pTileY, float pSize, float pCos, float pSin, FloatArray[] pRenderBuffers) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_RENDER_TYPE[cellType]) {
		case Cells.CELL_RENDER_TYPE_NO:
			break;
		case Cells.CELL_RENDER_TYPE_TILE: {
			float[] data = Cells.CELLS_RENDER_TILE_DATA[cellType];
			GraphicsUtils.render(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
					pSize, pCos, pSin, pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
							+ Cells.CELLS_RENDER_LAYER[cellType]], data[0], data[1]);
			break;
		}
		case Cells.CELL_RENDER_TYPE_WALL: {
			float[] data = Cells.CELLS_RENDER_TILE_DATA[cellType];
			GraphicsUtils.render(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
					pSize, pCos, pSin, pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
							+ Cells.CELLS_RENDER_LAYER[cellType]], data[0], data[1], data[2],
					data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
			break;
		}
		case Cells.CELL_RENDER_TYPE_TEE: {
			CellData cellData = getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).render(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY,
						pTileX, pTileY, pSize, pCos, pSin, pRenderBuffers);
			break;
		}
		case Cells.CELL_RENDER_TYPE_UI: {
			float[] data = Cells.CELLS_RENDER_TILE_DATA[cellType];
			GraphicsUtils.render(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
					pSize, pCos, pSin, pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
							+ Cells.CELLS_RENDER_LAYER[cellType]], data[0], data[1], data[2],
					data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
			int symbol = (pCell >>> Cells.CELL_TYPES_MAX_LOG) & 255;
			if (symbol != 0)
				GraphicsUtils.render(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
						pSize, pCos, pSin, pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]],
						Cells.SYMBOLS_TILE_X[symbol], Cells.SYMBOLS_TILE_Y[symbol]);
			break;
		}
		default:
			break;
		}
		if (Settings.AI_DEBUG_RENDER) {
			if (pZ == 0 && isChunk()) {
				ChunkCellularity chunk = getChunk();
				GraphicsUtils.renderDebug(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX,
						pTileY, pSize, pCos, pSin, pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]], 8 * Settings.TILE_W,
						0 * Settings.TILE_H, 255, 255, 255,
						((chunk.getAI(pX, pY, pZ) >> 8) & 15) * 42);
				GraphicsUtils.renderDebug(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX,
						pTileY, pSize, pCos, pSin, pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]], 0 * Settings.TILE_W,
						7 * Settings.TILE_H, 255, 255, 255, (chunk.getAI(pX, pY, pZ) >>> 12) * 16);
				GraphicsUtils.renderDebug(pCell, this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX,
						pTileY, pSize, pCos, pSin, pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]], 0 * Settings.TILE_W,
						1 * Settings.TILE_H, 255, 0, 0, (chunk.getAI(pX, pY, pZ) & 255) / 2);
			}
		}
	}

	public boolean isTileConnected(int pCell, int pX, int pY, int pZ, int pVCell, int pVX, int pVY) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		int vCellType = pVCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_TILE_CONNECTED_TYPE[cellType]) {
		case Cells.CELL_TILE_CONNECTED_TYPE_NO:
			return false;
		case Cells.CELL_TILE_CONNECTED_TYPE_SAME:
			return cellType == vCellType;
		default:
			return false;
		}
	}

	public int aiResistance(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_AI_RESISTANCE[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public int aiDiagonalResistance(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_AI_DIAGONAL_RESISTANCE[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public boolean isSolid(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_IS_SOLID[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public boolean isShellable(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_IS_SHELLABLE[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public int lightSourceR(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_LIGHT_SOURCE_R[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public int lightSourceG(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_LIGHT_SOURCE_G[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public int lightSourceB(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_LIGHT_SOURCE_B[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public int lightResistance(int pCell, int pX, int pY, int pZ, int pId) {
		return Cells.CELLS_LIGHT_RESISTANCE[pCell & Cells.CELL_TYPES_MAX_MASK][pId];
	}

	public boolean isDroppable(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_IS_DROPPABLE[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public boolean isConnected(int pCell, int pX, int pY, int pZ, int pVCell, int pVX, int pVY,
			int pVZ) {
		switch (Cells.CELLS_CONNECTION_TYPE[pCell & Cells.CELL_TYPES_MAX_MASK]) {
		case Cells.CELL_CONNECTION_TYPE_NO:
			return false;
		case Cells.CELL_CONNECTION_TYPE_ALL:
			return true;
		default:
			return false;
		}
	}

	public boolean isBlocking(int pCell, int pX, int pY, int pZ) {
		return Cells.CELLS_IS_BLOCKING[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public void renderWeapon(int pItem, CellData pItemData, int pX, int pY, int pZ, float pOffsetX,
			float pOffsetY, int pTileX, int pTileY, float pSize, float pCos, float pSin,
			FloatArray pRenderBuffer, ControlOrganoid pControlOrganoid) {
		int cellType = pItem & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_RENDER_WEAPON_TYPE[cellType]) {
		case Cells.CELL_RENDER_WEAPON_TYPE_NO:
			break;
		case Cells.CELL_RENDER_WEAPON_TYPE_DEFAULT: {
			switch (Cells.CELLS_RENDER_TYPE[cellType]) {
			case Cells.CELL_RENDER_TYPE_NO:
				break;
			case Cells.CELL_RENDER_TYPE_TILE: {
				float[] renderData = Cells.CELLS_RENDER_TILE_DATA[cellType];
				float eyesW = 0.5f;
				float eyesH = 0.5f;
				float eyesX = 0.5f + pControlOrganoid.getEyesX() / 2;
				float eyesY = 0.5f + pControlOrganoid.getEyesY() / 2;
				GraphicsUtils.renderTexture(this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
						pSize, pCos, pSin, pRenderBuffer, renderData[0], renderData[1],
						Settings.TILE_W, Settings.TILE_H, eyesX, eyesY, eyesW, eyesH, 1f, 0f);
				break;
			}
			case Cells.CELL_RENDER_TYPE_WALL: {
				float[] renderData = Cells.CELLS_RENDER_TILE_DATA[cellType];
				float eyesW = 0.5f;
				float eyesH = 0.5f;
				float eyesX = 0.5f + pControlOrganoid.getEyesX() / 2;
				float eyesY = 0.5f + pControlOrganoid.getEyesY() / 2;
				GraphicsUtils.renderTexture(this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
						pSize, pCos, pSin, pRenderBuffer, renderData[8], renderData[9],
						Settings.TILE_W, Settings.TILE_H, eyesX, eyesY, eyesW, eyesH, 1f, 0f);
				break;
			}
			case Cells.CELL_RENDER_TYPE_TEE:
				break;
			default:
				break;
			}
			break;
		}
		case Cells.CELL_RENDER_WEAPON_TYPE_SPECIAL: {
			float[] renderData = Cells.CELLS_RENDER_WEAPON_DATA[cellType];
			float eyesW = renderData[0];
			float eyesH = pControlOrganoid.getEyesX() < 0 ? -renderData[1] : renderData[1];
			float eyesX = 0.5f + pControlOrganoid.getEyesX() / 1.4f;
			float eyesY = 0.5f + pControlOrganoid.getEyesY() / 1.4f;
			GraphicsUtils.renderTexture(this, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
					pSize, pCos, pSin, pRenderBuffer, renderData[2], renderData[3], renderData[4],
					renderData[5], eyesX, eyesY, eyesW, eyesH, pControlOrganoid.getEyesX(),
					pControlOrganoid.getEyesY());
			break;
		}
		default:
			break;
		}
	}

	public void touchDown(int pItem, CellData pItemData, int pCell, int pX, int pY, int pZ,
			float pClickX, float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer) {
		int cellType = pItem & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_ITEM_ACTION[cellType]) {
		case Cells.CELL_ITEM_ACTION_NO:
			break;
		case Cells.CELL_ITEM_ACTION_DEFAULT: {
			ChunkCellularity chunk = getChunk();
			Tissularity tissularity = chunk.getTissularity();
			int posX = tissularity.getOffsetX() + MathUtils.floor(pClickX);
			int posY = tissularity.getOffsetY() + MathUtils.floor(pClickY);
			ChunkCellularity clickChunk =
					tissularity.getChunk(posX >> Settings.CHUNK_SIZE_LOG,
							posY >> Settings.CHUNK_SIZE_LOG);
			if (clickChunk != null)
				clickChunk.setCell(posX & Settings.CHUNK_SIZE_MASK,
						posY & Settings.CHUNK_SIZE_MASK, 0, pItem, pItemData != null ? pItemData
								.cpy() : null);
			Vector2 p = localToChunk(pX + 0.5f, pY + 0.5f);
			MetaBody body = getBody();
			float x = body.getOffsetX() + p.x;
			float y = body.getOffsetY() + p.y;
			pControlOrganoid.fastLookTo(pClickX - x, pClickY - y);
			break;
		}
		case Cells.CELL_ITEM_ACTION_HAMMER: {
			ChunkCellularity chunk = getChunk();
			Tissularity tissularity = chunk.getTissularity();
			Vector2 p = localToChunk(pX + 0.5f, pY + 0.5f);
			MetaBody body = getBody();
			float x = body.getOffsetX() + p.x;
			float y = body.getOffsetY() + p.y;
			tissularity.raycastCell(pZ, x, y, pClickX, pClickY, isChunk() ? null
					: (DynamicCellularity) this);
			Cellularity dynamic = tissularity.getRayCastBlockCellularity();
			if (dynamic == null) {
				int posX = tissularity.getRayCastBlockCellX();
				int posY = tissularity.getRayCastBlockCellY();
				ChunkCellularity clickChunk =
						tissularity.getChunk(posX >> Settings.CHUNK_SIZE_LOG,
								posY >> Settings.CHUNK_SIZE_LOG);
				if (clickChunk != null)
					clickChunk.setCell(posX & Settings.CHUNK_SIZE_MASK, posY
							& Settings.CHUNK_SIZE_MASK, pZ, Cells.CELL_TYPE_AIR, null);
			} else
				dynamic.setCell(tissularity.getRayCastBlockCellX(), tissularity
						.getRayCastBlockCellY(), pZ, Cells.CELL_TYPE_VACUUM, null);
			float vx = pClickX - x;
			float vy = pClickY - y;
			pControlOrganoid.fastLookTo(vx, vy);
			break;
		}
		case Cells.CELL_ITEM_ACTION_GUN: {
			ChunkCellularity chunk = getChunk();
			Vector2 p = localToChunk(pX + 0.5f, pY + 0.5f);
			float localX = p.x;
			float localY = p.y;
			MetaBody body = getBody();
			float x = body.getOffsetX() + localX;
			float y = body.getOffsetY() + localY;
			float vx = pClickX - x;
			float vy = pClickY - y;
			pControlOrganoid.fastLookTo(vx, vy);
			float k = (float) Math.sqrt(vx * vx + vy * vy);
			vx /= k;
			vy /= k;
			DynamicCellularity cellularity =
					DynamicCellularity.obtain(localX + vx + Settings.MAX_DROP_HSIZE - 0.5f, localY
							+ vy + Settings.MAX_DROP_HSIZE - 0.5f, 0);
			chunk.pushCellularity(cellularity);
			cellularity.setCell(0, 0, 0, Cells.CELL_TYPE_LAMP, null);
			MetaBody bulletBody = cellularity.getBody();
			float velocity = 30f;
			bulletBody.setVelocity(vx * velocity, vy * velocity);
			break;
		}
		}
	}

	public void touchUp(int pItem, CellData pItemData, int pCell, int pX, int pY, int pZ,
			float pClickX, float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer) {
	}

	public void touchDragged(int pItem, CellData pItemData, int pCell, int pX, int pY, int pZ,
			float pClickX, float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer) {
	}

	private void tickDynamicLightSource(int pX, int pY, int pZ, int pCell) {

		ChunkCellularity chunk = getChunk();

		Vector2 p = localToChunk(pX + 0.5f, pY + 0.5f);
		int x = MathUtils.floor(p.x);
		int y = MathUtils.floor(p.y);
		int r = lightSourceR(pCell, pX, pY, pZ);
		int g = lightSourceG(pCell, pX, pY, pZ);
		int b = lightSourceB(pCell, pX, pY, pZ);
		int[] color = chunk.getLight(x, y, pZ);
		if (color[0] < r || color[1] < g || color[2] < b)
			chunk.setLight(x, y, pZ, Math.max(r, color[0]), Math.max(g, color[1]), Math.max(b,
					color[2]));
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static abstract class CellData {

		public abstract CellData cpy();

		public abstract void recycle();
	}

}
