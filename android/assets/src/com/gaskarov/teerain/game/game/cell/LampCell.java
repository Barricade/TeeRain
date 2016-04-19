package com.gaskarov.teerain.game.game.cell;

import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.GraphicsUtils;
import com.gaskarov.teerain.game.Player;
import com.gaskarov.teerain.game.game.ControlOrganoid;
import com.gaskarov.teerain.game.game.DynamicLight;
import com.gaskarov.teerain.game.game.PhysicsWallOrganoid;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class LampCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TILE_N_X = Settings.TILE_W * 0;
	public static final float TILE_N_Y = Settings.TILE_H * 4;
	public static final float TILE_S_X = Settings.TILE_W * 1;
	public static final float TILE_S_Y = Settings.TILE_H * 4;
	public static final float TILE_H_X = Settings.TILE_W * 2;
	public static final float TILE_H_Y = Settings.TILE_H * 4;
	public static final float TILE_V_X = Settings.TILE_W * 3;
	public static final float TILE_V_Y = Settings.TILE_H * 4;
	public static final float TILE_B_X = Settings.TILE_W * 4;
	public static final float TILE_B_Y = Settings.TILE_H * 4;

	public static final int LAYER = 0;

	public static final float BORDER_SIZE = 0.02f;
	public static final float CORNER_SIZE = 0.1f;
	public static final float CORNER_VALUE = 0.01f;
	public static final float FRICTION = 0.6f;
	public static final float DENSITY = 1.0f;
	public static final float RESTITUTION = 0.0f;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private PhysicsWallOrganoid mPhysicsWallOrganoid;

	// ===========================================================
	// Constructors
	// ===========================================================

	private LampCell() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, Settings.AIR_LIGHT_RESISTANCE_ID,
				Settings.MAGIC_LAMP_LIGHT_SOURCE_ID);
		DynamicLight.attach(pCellularity, pX, pY, pZ, this);
		mPhysicsWallOrganoid.attach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, Settings.NO_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
		mPhysicsWallOrganoid.detach(pCellularity, pX, pY, pZ, this);
		DynamicLight.detach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void refresh(Cellularity pCellularity, int pX, int pY, int pZ) {
		mPhysicsWallOrganoid.refresh(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void tick(Cellularity pCellularity, int pX, int pY, int pZ) {
		DynamicLight.tick(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void recycle() {
		recycle(this);
	}

	@Override
	public Cell cpy() {
		return obtain();
	}

	@Override
	public boolean isDynamicLightSource() {
		return true;
	}

	@Override
	public float getBorderSize() {
		return BORDER_SIZE;
	}

	@Override
	public float getCornerValue() {
		return CORNER_VALUE;
	}

	@Override
	public float getCornerSize() {
		return CORNER_SIZE;
	}

	@Override
	public float getFriction() {
		return FRICTION;
	}

	@Override
	public float getDensity() {
		return DENSITY;
	}

	@Override
	public float getRestitution() {
		return RESTITUTION;
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public boolean isSquare() {
		return true;
	}

	@Override
	public boolean isTileConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY) {
		return pCell instanceof LampCell;
	}

	@Override
	public boolean isDroppable(Cellularity pCellularity, int pX, int pY, int pZ) {
		return false;
	}

	@Override
	public boolean isConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY, int pVZ) {
		return false;
	}

	@Override
	public void render(Cellularity pCellularity, int pX, int pY, int pZ, float pCos, float pSin) {
		for (int i = 0; i < Settings.LAYERS_PER_DEPTH; ++i)
			switch (i) {
			case LAYER: {
				int count =
						GraphicsUtils.render(this, pCellularity, pX, pY, pZ, pCos, pSin, TILE_N_X,
								TILE_N_Y, TILE_S_X, TILE_S_Y, TILE_H_X, TILE_H_Y, TILE_V_X,
								TILE_V_Y, TILE_B_X, TILE_B_Y);
				GraphicsUtils.count(pCellularity, count);
				break;
			}
			default:
				GraphicsUtils.count(pCellularity, 0);
				break;
			}
	}

	@Override
	public int renderWeapon(Cellularity pCellularity, int pX, int pY, int pZ, float pCos,
			float pSin, ControlOrganoid pControlOrganoid) {
		float eyesW = 0.5f;
		float eyesH = 0.5f;
		float eyesX = 0.5f + pControlOrganoid.getEyesX() / 2;
		float eyesY = 0.5f + pControlOrganoid.getEyesY() / 2;
		return GraphicsUtils.renderTexture(this, pCellularity, pX, pY, pZ, pCos, pSin, TILE_B_X,
				TILE_B_Y, Settings.TILE_W, Settings.TILE_H, eyesX, eyesY, eyesW, eyesH, 1f, 0f);
	}

	@Override
	public void touchDown(Cellularity pCellularity, int pX, int pY, int pZ, float pClickX,
			float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer, int pUseItem) {
		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int posX = tissularity.getOffsetX() + MathUtils.floor(pClickX);
		int posY = tissularity.getOffsetY() + MathUtils.floor(pClickY);
		Cellularity clickChunk =
				tissularity.getChunk(posX >> Settings.CHUNK_SIZE_LOG,
						posY >> Settings.CHUNK_SIZE_LOG);
		if (clickChunk != null)
			clickChunk.setCell(posX & Settings.CHUNK_SIZE_MASK, posY & Settings.CHUNK_SIZE_MASK, 0,
					cpy());
		MetaBody body = pCellularity.getBody();
		float c = (float) Math.cos(body.getAngle());
		float s = (float) Math.sin(body.getAngle());
		float offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
		float x =
				body.getOffsetX() + body.getPositionX() + (pX - offset + 0.5f) * c
						- (pY - offset + 0.5f) * s;
		float y =
				body.getOffsetY() + body.getPositionY() + (pX - offset + 0.5f) * s
						+ (pY - offset + 0.5f) * c;
		pControlOrganoid.fastLookTo(pClickX - x, pClickY - y);
	}

	@Override
	public boolean isBlocking() {
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static LampCell obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (LampCell.class) {
				return sPool.size() == 0 ? new LampCell() : (LampCell) sPool.pop();
			}
		return new LampCell();
	}

	private static void recyclePure(LampCell pObj) {
		if (GlobalConstants.POOL)
			synchronized (LampCell.class) {
				sPool.push(pObj);
			}
	}

	public static LampCell obtain() {
		LampCell obj = obtainPure();
		obj.mPhysicsWallOrganoid = PhysicsWallOrganoid.obtain();
		return obj;
	}

	public static void recycle(LampCell pObj) {
		PhysicsWallOrganoid.recycle(pObj.mPhysicsWallOrganoid);
		pObj.mPhysicsWallOrganoid = null;
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
