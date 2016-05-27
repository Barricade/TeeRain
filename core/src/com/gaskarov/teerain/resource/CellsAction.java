package com.gaskarov.teerain.resource;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.gaskarov.teerain.core.PhysicsWall;
import com.gaskarov.teerain.core.Player;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.Cellularity.CellData;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.cellularity.DynamicCellularity;
import com.gaskarov.teerain.core.util.Collidable;
import com.gaskarov.teerain.core.util.GraphicsUtils;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.IntArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class CellsAction {

	// ===========================================================
	// Constants
	// ===========================================================

	protected static final float FLOAT_EPS = 1e-6f;

	private static final int TREE_X = 2;
	private static final int TREE_Y = 7;
	private static final int TREE_Z = 1;
	private static final int[][][] TREE = new int[][][] { //
	{ { 0, 1, 1, 1, 0 }, //
			{ 1, 1, 1, 1, 1 }, //
			{ 1, 1, 1, 1, 1 }, //
			{ 1, 1, 1, 1, 1 }, //
			{ 0, 1, 1, 1, 0 }, //
			{ 0, 0, 0, 0, 0 }, //
			{ 0, 0, 0, 0, 0 }, //
			{ 0, 0, 0, 0, 0 } },

	{ { 0, 1, 1, 1, 0 }, //
			{ 1, 1, 1, 1, 1 }, //
			{ 1, 1, 2, 1, 1 }, //
			{ 1, 1, 2, 1, 1 }, //
			{ 0, 1, 2, 1, 0 }, //
			{ 0, 0, 2, 0, 0 }, //
			{ 0, 0, 2, 0, 0 }, //
			{ 0, 0, 2, 0, 0 } } };

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

	// ===========================================================
	// Methods
	// ===========================================================

	public static boolean checkTree(Cellularity pCellularity, int pX, int pY,
			int pZ) {
		for (int k = 0; k < TREE.length; ++k)
			for (int i = 0; i < TREE[k].length; ++i)
				for (int j = 0; j < TREE[k][i].length; ++j) {
					int x = pX + j - TREE_X;
					int y = pY - (i - TREE_Y);
					int z = pZ + k - TREE_Z;
					if (TREE[k][i][j] != 0
							&& pCellularity.getCell(x, y, z) != Cells.CELL_TYPE_AIR)
						return false;
				}
		return true;
	}

	public static void genTree(Cellularity pCellularity, int pX, int pY, int pZ) {
		for (int k = 0; k < TREE.length; ++k)
			for (int i = 0; i < TREE[k].length; ++i)
				for (int j = 0; j < TREE[k][i].length; ++j) {
					int x = pX + j - TREE_X;
					int y = pY - (i - TREE_Y);
					int z = pZ + k - TREE_Z;
					if (TREE[k][i][j] == 1) {
						if (pCellularity.getCell(x, y, z) == Cells.CELL_TYPE_AIR)
							pCellularity.setCell(x, y, z,
									Cells.CELL_TYPE_LEAVES, null);
					} else if (TREE[k][i][j] == 2) {
						pCellularity.setCell(x, y, z, Cells.CELL_TYPE_WOOD,
								null);
					}
				}
	}

	public static void attach(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_DYNAMIC_LIGHT_SOURCE[cellType]
				&& !pCellularity.isChunk())
			pCellularity.tickEnable(pX, pY, pZ);

		if (Cells.CELLS_IS_TEE[cellType]) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).attach(pCellularity, pX, pY, pZ);
		}
		if (cellType == Cells.CELL_TYPE_LOOT) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof LootCellData)
				((LootCellData) cellData).attach(pCellularity, pX, pY, pZ);
		}
		if (Cells.CELLS_TICK[cellType])
			pCellularity.tickEnable(pX, pY, pZ);
	}

	public static void detach(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {

		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_DYNAMIC_LIGHT_SOURCE[cellType]
				&& !pCellularity.isChunk())
			pCellularity.tickDisable(pX, pY, pZ);

		switch (Cells.CELLS_PHYSICS_TYPE[pCell & Cells.CELL_TYPES_MAX_MASK]) {
		case Cells.CELL_PHYSICS_TYPE_NO:
			break;
		case Cells.CELL_PHYSICS_TYPE_WALL: {
			PhysicsWall physicsWall = pCellularity.getPhysicsWall(pX, pY, pZ);
			if (physicsWall != null) {
				physicsWall.destroyPhysics(pCellularity, pX, pY, pZ);
				pCellularity.setPhysicsWall(pX, pY, pZ, null);
			}
			break;
		}
		default:
			break;
		}

		if (Cells.CELLS_IS_TEE[cellType]) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).detach(pCellularity, pX, pY, pZ);
		}
		if (cellType == Cells.CELL_TYPE_LOOT) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof LootCellData)
				((LootCellData) cellData).detach(pCellularity, pX, pY, pZ);
		}
		if (Cells.CELLS_TICK[cellType])
			pCellularity.tickDisable(pX, pY, pZ);
	}

	public static void tissularedAttach(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_TEE[cellType]) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).tissularedAttach(pCellularity, pX, pY,
						pZ);
		}
	}

	public static void tissularedDetach(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_TEE[cellType]) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).tissularedDetach(pCellularity, pX, pY,
						pZ);
		}
	}

	public static int update(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		return 0;
	}

	public static void refresh(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_PHYSICS_TYPE[cellType]) {
		case Cells.CELL_PHYSICS_TYPE_NO:
			break;
		case Cells.CELL_PHYSICS_TYPE_WALL: {
			int flags = PhysicsWall.near(pCellularity, pX, pY, pZ);
			boolean shellable = Cells.CELLS_IS_SHELLABLE[cellType];
			boolean flag = flags != 31 || !pCellularity.isChunk() || !shellable;
			PhysicsWall physicsWall = pCellularity.getPhysicsWall(pX, pY, pZ);
			if (physicsWall != null) {
				if (physicsWall.getNear() != flags) {
					physicsWall.destroyPhysics(pCellularity, pX, pY, pZ);
					if (!flag) {
						pCellularity.setPhysicsWall(pX, pY, pZ, null);
						physicsWall = null;
					}
				} else
					physicsWall = null;
			} else if (flag) {
				physicsWall = PhysicsWall.obtain(pCellularity, pX, pY, pZ);
				pCellularity.setPhysicsWall(pX, pY, pZ, physicsWall);
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
				physicsWall.createPhysics(pCellularity, pX, pY, pZ, borderSize,
						cornerValue, cornerSize, density, friction,
						restitution, solid, square);
			}
			break;
		}
		default:
			break;
		}
	}

	public static void tick(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		if (Cells.CELLS_IS_DYNAMIC_LIGHT_SOURCE[cellType]
				&& !pCellularity.isChunk())
			tickDynamicLightSource(pCellularity, pX, pY, pZ, pCell);

		if (Cells.CELLS_IS_TEE[cellType]) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).tick(pCellularity, pX, pY, pZ);
		}
		if (cellType == Cells.CELL_TYPE_LOOT) {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof LootCellData)
				((LootCellData) cellData).tick(pCellularity, pX, pY, pZ);
		}
		if (cellType == Cells.CELL_TYPE_SAPLING) {
			if (pCellularity.isChunk()) {
				long h = pCellularity.random();
				if (MathUtils.mod(h, 60 * 60 * 12) == 0
						&& checkTree(pCellularity, pX, pY, pZ)) {
					genTree(pCellularity, pX, pY, pZ);
				}
			}
		}
	}

	public static void render(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ, float pOffsetX, float pOffsetY, int pTileX,
			int pTileY, float pSize, float pCos, float pSin,
			FloatArray[] pRenderBuffers) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_RENDER_TYPE[cellType]) {
		case Cells.CELL_RENDER_TYPE_NO:
			break;
		case Cells.CELL_RENDER_TYPE_TILE: {
			float[] data = Cells.CELLS_RENDER_TILE_DATA[cellType];
			GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pOffsetX,
					pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
					pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
							+ Cells.CELLS_RENDER_LAYER[cellType]], data[0],
					data[1]);
			break;
		}
		case Cells.CELL_RENDER_TYPE_WALL: {
			float[] data = Cells.CELLS_RENDER_TILE_DATA[cellType];
			GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pOffsetX,
					pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
					pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
							+ Cells.CELLS_RENDER_LAYER[cellType]], data[0],
					data[1], data[2], data[3], data[4], data[5], data[6],
					data[7], data[8], data[9], data[10], data[11]);
			break;
		}
		case Cells.CELL_RENDER_TYPE_TEE: {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData)
				((TeeCellData) cellData).render(pCell, pCellularity, pX, pY,
						pZ, pOffsetX, pOffsetY, pTileX, pTileY, pSize, pCos,
						pSin, pRenderBuffers);
			break;
		}
		case Cells.CELL_RENDER_TYPE_UI: {
			float[] data = Cells.CELLS_RENDER_TILE_DATA[cellType];
			GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pOffsetX,
					pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
					pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
							+ Cells.CELLS_RENDER_LAYER[cellType]], data[0],
					data[1], data[2], data[3], data[4], data[5], data[6],
					data[7], data[8], data[9], data[10], data[11]);
			int symbol = (pCell >>> Cells.CELL_TYPES_MAX_LOG) & 255;
			if (symbol != 0)
				GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]],
						Cells.SYMBOLS_TILE_X[symbol],
						Cells.SYMBOLS_TILE_Y[symbol]);
			break;
		}
		case Cells.CELL_RENDER_TYPE_INVENTORY: {
			float[] data = Cells.CELLS_RENDER_TILE_DATA[cellType];
			int cellParam = (pCell >>> Cells.CELL_TYPES_MAX_LOG) & 255;
			FloatArray renderBuffer = pRenderBuffers[pZ
					* Settings.LAYERS_PER_DEPTH
					+ (cellParam == Cells.INVENTORY_PARAM_SELECT_ITEM ? Cells.CELL_RENDER_LAYER_SPECIAL
							: Cells.CELLS_RENDER_LAYER[cellType])];
			if (cellParam != Cells.INVENTORY_PARAM_SELECT_ITEM)
				GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						renderBuffer, data[0], data[1], data[2], data[3],
						data[4], data[5], data[6], data[7], data[8], data[9],
						data[10], data[11]);
			if (Cells.INVENTORY_PARAM_CRAFT_ITEM_MIN <= cellParam
					&& cellParam <= Cells.INVENTORY_PARAM_CRAFT_ITEM_MAX)
				GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						renderBuffer, Settings.TILE_W * 10,
						Settings.TILE_H * 12);
			else if (Cells.INVENTORY_PARAM_CRAFT_OUT_ITEM_MIN <= cellParam
					&& cellParam <= Cells.INVENTORY_PARAM_CRAFT_OUT_ITEM_MAX)
				GraphicsUtils
						.render(pCell, pCellularity, pX, pY, pZ, pOffsetX,
								pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
								renderBuffer, Settings.TILE_W * 9,
								Settings.TILE_H * 11);
			pCellularity.getTissularity().renderInventory(pCell, pCellularity,
					pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY, pSize,
					pCos, pSin, renderBuffer);
			break;
		}
		case Cells.CELL_RENDER_TYPE_LOOT: {
			if (cellType == Cells.CELL_TYPE_LOOT) {
				CellData cellData = pCellularity.getCellData(pX, pY, pZ);
				if (cellData instanceof LootCellData)
					((LootCellData) cellData).render(pCell, pCellularity, pX,
							pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY, pSize,
							pCos, pSin, pRenderBuffers);
			}
			break;
		}
		default:
			break;
		}
		if (Settings.AI_DEBUG_RENDER) {
			if (pZ == 0 && pCellularity.isChunk()) {
				ChunkCellularity chunk = pCellularity.getChunk();
				GraphicsUtils.renderDebug(pCell, pCellularity, pX, pY, pZ,
						pOffsetX, pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]],
						15 * Settings.TILE_W, 0 * Settings.TILE_H, 255, 255,
						255, ((chunk.getAI(pX, pY, pZ) >> 8) & 15) * 42);
				GraphicsUtils.renderDebug(pCell, pCellularity, pX, pY, pZ,
						pOffsetX, pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]],
						11 * Settings.TILE_W, 5 * Settings.TILE_H, 255, 255,
						255, (chunk.getAI(pX, pY, pZ) >>> 12) * 16);
				GraphicsUtils.renderDebug(pCell, pCellularity, pX, pY, pZ,
						pOffsetX, pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELLS_RENDER_LAYER[cellType]],
						0 * Settings.TILE_W, 1 * Settings.TILE_H, 255, 0, 0,
						(chunk.getAI(pX, pY, pZ) & 255) / 2);
			}
		}
	}

	public static boolean isTileCornerFix(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		return Cells.CELLS_TILE_CORNER_FIX[cellType];
	}

	public static boolean isTileConnected(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ, int pVCell, int pVX, int pVY) {
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

	public static int aiResistance(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		return Cells.CELLS_AI_RESISTANCE[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static int aiDiagonalResistance(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ) {
		return Cells.CELLS_AI_DIAGONAL_RESISTANCE[pCell
				& Cells.CELL_TYPES_MAX_MASK];
	}

	public static boolean isSolid(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		return Cells.CELLS_IS_SOLID[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static boolean isShellable(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ) {
		return Cells.CELLS_IS_SHELLABLE[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static int lightSourceR(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		return Cells.CELLS_LIGHT_SOURCE_R[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static int lightSourceG(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		return Cells.CELLS_LIGHT_SOURCE_G[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static int lightSourceB(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		return Cells.CELLS_LIGHT_SOURCE_B[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static int lightResistance(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ, int pId) {
		return Cells.CELLS_LIGHT_RESISTANCE[pCell & Cells.CELL_TYPES_MAX_MASK][pId];
	}

	public static boolean isDroppable(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ) {
		return Cells.CELLS_IS_DROPPABLE[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static boolean isConnected(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ, int pVCell, int pVX, int pVY, int pVZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		int vCellType = pVCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_CONNECTION_TYPE[cellType]) {
		case Cells.CELL_CONNECTION_TYPE_NO:
			return false;
		case Cells.CELL_CONNECTION_TYPE_ALL:
			return true;
		case Cells.CELL_CONNECTION_TYPE_BOTTOM:
			return pVY == -1;
		case Cells.CELL_CONNECTION_TYPE_FILTER:
			for (int i = 0; i < Cells.CELLS_CONNECTION_FILTER[cellType].length; ++i)
				if (vCellType == Cells.CELLS_CONNECTION_FILTER[cellType][i])
					return true;
			return false;
		default:
			return false;
		}
	}

	public static boolean isBlocking(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ) {
		return Cells.CELLS_IS_BLOCKING[pCell & Cells.CELL_TYPES_MAX_MASK];
	}

	public static void renderWeapon(Cellularity pCellularity, int pItem,
			CellData pItemData, int pX, int pY, int pZ, float pOffsetX,
			float pOffsetY, int pTileX, int pTileY, float pSize, float pCos,
			float pSin, FloatArray pRenderBuffer,
			ControlOrganoid pControlOrganoid) {
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
				GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffer, renderData[0], renderData[1],
						Settings.TILE_W, Settings.TILE_H, eyesX, eyesY, eyesW,
						eyesH, 1f, 0f);
				break;
			}
			case Cells.CELL_RENDER_TYPE_WALL: {
				float[] renderData = Cells.CELLS_RENDER_TILE_DATA[cellType];
				float eyesW = 0.5f;
				float eyesH = 0.5f;
				float eyesX = 0.5f + pControlOrganoid.getEyesX() / 2;
				float eyesY = 0.5f + pControlOrganoid.getEyesY() / 2;
				GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffer, renderData[8], renderData[9],
						Settings.TILE_W, Settings.TILE_H, eyesX, eyesY, eyesW,
						eyesH, 1f, 0f);
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
			float eyesH = pControlOrganoid.getEyesX() < 0 ? -renderData[1]
					: renderData[1];
			float eyesX = 0.5f + pControlOrganoid.getEyesX() / 1.4f;
			float eyesY = 0.5f + pControlOrganoid.getEyesY() / 1.4f;
			GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX,
					pOffsetY, pTileX, pTileY, pSize, pCos, pSin, pRenderBuffer,
					renderData[2], renderData[3], renderData[4], renderData[5],
					eyesX, eyesY, eyesW, eyesH, pControlOrganoid.getEyesX(),
					pControlOrganoid.getEyesY());
			break;
		}
		default:
			break;
		}
	}

	public static void renderItem(Cellularity pCellularity, int pItem,
			CellData pItemData, int pX, int pY, int pZ, float pOffsetX,
			float pOffsetY, int pTileX, int pTileY, float pSize, float pCos,
			float pSin, FloatArray pRenderBuffer, float pPositionX,
			float pPositionY) {
		int cellType = pItem & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_RENDER_ITEM_TYPE[cellType]) {
		case Cells.CELL_RENDER_ITEM_TYPE_NO:
			break;
		case Cells.CELL_RENDER_ITEM_TYPE_DEFAULT: {
			switch (Cells.CELLS_RENDER_TYPE[cellType]) {
			case Cells.CELL_RENDER_TYPE_NO:
				break;
			case Cells.CELL_RENDER_TYPE_TILE: {
				float[] renderData = Cells.CELLS_RENDER_TILE_DATA[cellType];
				GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffer, renderData[0], renderData[1],
						Settings.TILE_W, Settings.TILE_H, pPositionX,
						pPositionY, 0.7f, 0.7f, 1f, 0f);
				break;
			}
			case Cells.CELL_RENDER_TYPE_WALL: {
				float[] renderData = Cells.CELLS_RENDER_TILE_DATA[cellType];
				GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffer, renderData[8], renderData[9],
						Settings.TILE_W, Settings.TILE_H, pPositionX,
						pPositionY, 0.7f, 0.7f, 1f, 0f);
				break;
			}
			default:
				break;
			}
			break;
		}
		default:
			break;
		}
	}

	public static void destroyCell(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_DESTROY_ACTION[cellType]) {
		case Cells.CELL_DESTROY_ACTION_NO:
			break;
		case Cells.CELL_DESTROY_ACTION_DEFAULT: {
			Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
			float x = p.x;
			float y = p.y;
			DynamicCellularity dynamic = DynamicCellularity.obtain(
					pCellularity.random(), x + Settings.MAX_DROP_HSIZE - 0.5f,
					y + Settings.MAX_DROP_HSIZE - 0.5f, 0);
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData != null)
				cellData = cellData.cpy();
			LootCellData loot = LootCellData.obtain();
			loot.push(pCell, cellData, 1);
			dynamic.setCell(0, 0, 0, Cells.CELL_TYPE_LOOT, loot);
			pCellularity.setCell(pX, pY, pZ,
					pCellularity.getDropDefaultCell(pX, pY, pZ), null);
			pCellularity.getChunk().pushCellularity(dynamic);
			break;
		}
		case Cells.CELL_DESTROY_ACTION_LOOT: {
			pCellularity.setCell(pX, pY, pZ,
					pCellularity.getDropDefaultCell(pX, pY, pZ), null);
			break;
		}
		default:
			break;
		}
	}

	public static void touchDown(Cellularity pCellularity, int pItem,
			CellData pItemData, int pCell, int pX, int pY, int pZ,
			float pClickX, float pClickY, ControlOrganoid pControlOrganoid,
			Player pPlayer, int pUseItem) {
		int cellType = pItem & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_ITEM_ACTION[cellType]) {
		case Cells.CELL_ITEM_ACTION_NO:
			break;
		case Cells.CELL_ITEM_ACTION_DEFAULT: {
			ChunkCellularity chunk = pCellularity.getChunk();
			Tissularity tissularity = chunk.getTissularity();
			Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
			MetaBody body = pCellularity.getBody();
			float x = body.getOffsetX() + p.x;
			float y = body.getOffsetY() + p.y;
			tissularity.raycastCell(pZ, x, y, pClickX, pClickY, pCellularity
					.isChunk() ? null : (DynamicCellularity) pCellularity);

			if (tissularity.getRayCastType() != Tissularity.RAYCAST_TYPE_START
					&& tissularity.getRayCastDistance2() <= Settings.MAX_ACTION_RADIUS_2) {

				int posX, posY;

				if (tissularity.getRayCastType() == Tissularity.RAYCAST_TYPE_END) {
					posX = tissularity.getRayCastBlockCellX();
					posY = tissularity.getRayCastBlockCellY();
				} else {
					posX = tissularity.getRayCastPreBlockCellX();
					posY = tissularity.getRayCastPreBlockCellY();
				}

				Cellularity dynamic = tissularity.getRayCastBlockCellularity();
				if (dynamic == null) {
					ChunkCellularity clickChunk = tissularity.getChunk(
							posX >> Settings.CHUNK_SIZE_LOG,
							posY >> Settings.CHUNK_SIZE_LOG);
					if (clickChunk != null) {
						int localX = posX & Settings.CHUNK_SIZE_MASK;
						int localY = posY & Settings.CHUNK_SIZE_MASK;
						clickChunk.setCell(localX, localY, pZ, pItem,
								pItemData != null ? pItemData.cpy() : null);
						int count = pPlayer.getItemCount(pUseItem);
						if (--count == 0)
							pPlayer.setItem(pUseItem, Cells.CELL_TYPE_VACUUM,
									null, 0);
						else
							pPlayer.setItemCount(pUseItem, count);
					}
				}
			}

			pControlOrganoid.fastLookTo(pClickX - x, pClickY - y);
			break;
		}
		case Cells.CELL_ITEM_ACTION_HAMMER: {
			ChunkCellularity chunk = pCellularity.getChunk();
			Tissularity tissularity = chunk.getTissularity();
			Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
			MetaBody body = pCellularity.getBody();
			float x = body.getOffsetX() + p.x;
			float y = body.getOffsetY() + p.y;
			tissularity.raycastCell(pZ, x, y, pClickX, pClickY, pCellularity
					.isChunk() ? null : (DynamicCellularity) pCellularity);

			if (tissularity.getRayCastDistance2() <= Settings.MAX_ACTION_RADIUS_2) {

				int z = pZ;

				if (tissularity.getRayCastType() == Tissularity.RAYCAST_TYPE_END) {
					z = tissularity.raycastDepth(pZ, pClickX, pClickY,
							pCellularity.isChunk() ? null
									: (DynamicCellularity) pCellularity);
				}

				if (z != Settings.CHUNK_DEPTH_SKY) {
					Cellularity dynamic = tissularity
							.getRayCastBlockCellularity();
					if (dynamic == null) {
						int posX = tissularity.getRayCastBlockCellX();
						int posY = tissularity.getRayCastBlockCellY();
						ChunkCellularity clickChunk = tissularity.getChunk(
								posX >> Settings.CHUNK_SIZE_LOG,
								posY >> Settings.CHUNK_SIZE_LOG);
						if (clickChunk != null) {
							int localX = posX & Settings.CHUNK_SIZE_MASK;
							int localY = posY & Settings.CHUNK_SIZE_MASK;
							destroyCell(clickChunk,
									clickChunk.getCell(localX, localY, z), posX
											& Settings.CHUNK_SIZE_MASK, posY
											& Settings.CHUNK_SIZE_MASK, z);
						}
					} else {
						int posX = tissularity.getRayCastBlockCellX();
						int posY = tissularity.getRayCastBlockCellY();
						int cell = dynamic.getCell(posX, posY, z);
						destroyCell(dynamic, cell, posX, posY, z);
					}
				}
			}

			float vx = pClickX - x;
			float vy = pClickY - y;
			pControlOrganoid.fastLookTo(vx, vy);
			break;
		}
		case Cells.CELL_ITEM_ACTION_GUN: {
			ChunkCellularity chunk = pCellularity.getChunk();
			Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
			float localX = p.x;
			float localY = p.y;
			MetaBody body = pCellularity.getBody();
			float x = body.getOffsetX() + localX;
			float y = body.getOffsetY() + localY;
			float vx = pClickX - x;
			float vy = pClickY - y;
			pControlOrganoid.fastLookTo(vx, vy);
			float k = (float) Math.sqrt(vx * vx + vy * vy);
			vx /= k;
			vy /= k;
			DynamicCellularity cellularity = DynamicCellularity.obtain(
					pCellularity.random(), localX + vx
							+ Settings.MAX_DROP_HSIZE - 0.5f, localY + vy
							+ Settings.MAX_DROP_HSIZE - 0.5f, 0);
			chunk.pushCellularity(cellularity);
			cellularity.setCell(0, 0, 0, Cells.CELL_TYPE_LAMP, null);
			MetaBody bulletBody = cellularity.getBody();
			float velocity = 30f;
			bulletBody.setVelocity(vx * velocity, vy * velocity);
			break;
		}
		}
	}

	public static void touchUp(Cellularity pCellularity, int pItem,
			CellData pItemData, int pCell, int pX, int pY, int pZ,
			float pClickX, float pClickY, ControlOrganoid pControlOrganoid,
			Player pPlayer, int pUseItem) {
	}

	public static void touchDragged(Cellularity pCellularity, int pItem,
			CellData pItemData, int pCell, int pX, int pY, int pZ,
			float pClickX, float pClickY, ControlOrganoid pControlOrganoid,
			Player pPlayer, int pUseItem) {
	}

	private static void tickDynamicLightSource(Cellularity pCellularity,
			int pX, int pY, int pZ, int pCell) {

		ChunkCellularity chunk = pCellularity.getChunk();

		Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
		int x = MathUtils.floor(p.x);
		int y = MathUtils.floor(p.y);
		int r = lightSourceR(pCellularity, pCell, pX, pY, pZ);
		int g = lightSourceG(pCellularity, pCell, pX, pY, pZ);
		int b = lightSourceB(pCellularity, pCell, pX, pY, pZ);
		int[] color = chunk.getLight(x, y, pZ);
		if (color[0] < r || color[1] < g || color[2] < b)
			chunk.setLight(x, y, pZ, Math.max(r, color[0]),
					Math.max(g, color[1]), Math.max(b, color[2]));
	}

	public static void beginContact(Cellularity pCellularity, int pCell,
			int pX, int pY, int pZ, Contact pContact, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap, Collidable pData,
			PhysicsWall pPhysicsWall) {
		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		switch (Cells.CELLS_PHYSICS_COLLISION_TYPE[cellType]) {
		case Cells.CELL_PHYSICS_COLLISION_TYPE_NO:
			break;
		case Cells.CELL_PHYSICS_COLLISION_TYPE_DEFAULT:
			break;
		case Cells.CELL_PHYSICS_COLLISION_TYPE_TEE: {
			CellData cellData = pCellularity.getCellData(pX, pY, pZ);
			if (cellData instanceof TeeCellData) {
				TeeCellData teeCellData = (TeeCellData) cellData;
				Player player = pCellularity.getTissularity().getPlayer(
						teeCellData.getId());
				if (teeCellData.getDropDelayLeft() <= 0 && player != null
						&& pData instanceof LootCellData) {
					LootCellData loot = (LootCellData) pData;
					IntArray items = loot.getItems();
					Array itemsData = loot.getItemsData();
					IntArray itemsCount = loot.getItemsCount();

					for (int i = 0; i < items.size();) {
						CellData itemData = (CellData) itemsData.get(i);
						int count = player.pushItem(items.get(i), itemData,
								itemsCount.get(i));
						if (count > 0) {
							itemsCount.set(i, count);
							++i;
						} else {
							if (itemData != null)
								itemData.recycle();
							items.set(i, items.back());
							items.pop();
							itemsData.set(i, itemsData.back());
							itemsData.pop();
							itemsCount.set(i, itemsCount.back());
							itemsCount.pop();
						}
					}

					if (items.size() == 0)
						loot.getCellularity().destroyCell(loot.getX(),
								loot.getY(), loot.getZ());
				}
			}
			break;
		}
		default:
			break;
		}
	}

	public static void endContact(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ, Contact pContact, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap, Collidable pData,
			PhysicsWall pPhysicsWall) {
	}

	public static void preSolve(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ, Contact pContact, Manifold pOldManifold,
			Fixture pFixture, Fixture pThisFixture, boolean pSwap,
			Collidable pData, PhysicsWall pPhysicsWall) {
		int near = pPhysicsWall.getNear();
		if (pCellularity.isChunk() && near != 31) {
			boolean flag = false;
			WorldManifold manifold = pContact.getWorldManifold();
			float[] separations = manifold.getSeparations();
			for (int i = manifold.getNumberOfContactPoints() - 1; i >= 0; --i)
				if (separations[i] < -0.01f) {
					flag = true;
					break;
				}
			if (!flag) {
				float normalX = manifold.getNormal().x;
				float normalY = manifold.getNormal().y;
				if (pSwap) {
					normalX *= -1;
					normalY *= -1;
				}
				if ((near & 1) != 0 && normalX >= FLOAT_EPS) {
					pContact.setEnabled(false);
					return;
				} else if ((near & 2) != 0 && normalY >= FLOAT_EPS) {
					pContact.setEnabled(false);
					return;
				} else if ((near & 4) != 0 && normalX <= -FLOAT_EPS) {
					pContact.setEnabled(false);
					return;
				} else if ((near & 8) != 0 && normalY <= -FLOAT_EPS) {
					pContact.setEnabled(false);
					return;
				}
			}
		}
	}

	public static void postSolve(Cellularity pCellularity, int pCell, int pX,
			int pY, int pZ, Contact pContact, ContactImpulse pContactImpulse,
			Fixture pFixture, Fixture pThisFixture, boolean pSwap,
			Collidable pData, PhysicsWall pPhysicsWall) {
	}

	public static int stuckSize(int pItem, CellData pItemData) {
		return Cells.CELLS_ITEM_STUCK_SIZE[pItem & Cells.CELL_TYPES_MAX_MASK];
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
