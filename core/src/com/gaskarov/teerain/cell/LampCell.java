package com.gaskarov.teerain.cell;

import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.organoid.PhysicsWallOrganoid;
import com.gaskarov.teerain.util.DynamicLight;
import com.gaskarov.teerain.util.GraphicsUtils;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;

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
	public static final boolean TILE_N_TR = true;
	public static final boolean TILE_S_TR = true;
	public static final boolean TILE_H_TR = true;
	public static final boolean TILE_V_TR = true;
	public static final boolean TILE_B_TR = true;

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
	private int mTilesConnectedMask;

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
		pCellularity.setCellLightData(pX, pY, pZ, 0, Settings.AIR_LIGHT_RESISTANCE_ID,
				Settings.LAMP_LIGHT_SOURCE_ID + (int) (Math.random() * 7));
		DynamicLight.attach(pCellularity, pX, pY, pZ, this);
		mPhysicsWallOrganoid.attach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, 0, Settings.NO_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
		mPhysicsWallOrganoid.detach(pCellularity, pX, pY, pZ, this);
		DynamicLight.detach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void refresh(Cellularity pCellularity, int pX, int pY, int pZ) {
		mPhysicsWallOrganoid.refresh(pCellularity, pX, pY, pZ, this);
		mTilesConnectedMask = 0;
		for (int i = 0; i < ArrayConstants.MOVE_AROUND_X.length; ++i) {
			int vx = ArrayConstants.MOVE_AROUND_X[i];
			int vy = ArrayConstants.MOVE_AROUND_Y[i];
			if (isTileConnected(pCellularity, pX, pY, pZ, pCellularity
					.getCell(pX + vx, pY + vy, pZ), vx, vy))
				mTilesConnectedMask |= 1 << i;
		}
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
	public int tilesConnectedMask() {
		return mTilesConnectedMask;
	}

	@Override
	public boolean isDroppable(Cellularity pCellularity, int pX, int pY, int pZ) {
		return true;
	}

	@Override
	public boolean isConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY, int pVZ) {
		return true;
	}

	@Override
	public boolean render(Cellularity pCellularity, int pX, int pY, int pZ, float pOffsetX,
			float pOffsetY, int pTileX, int pTileY, float pSize, float pCos, float pSin,
			FloatArray[] pRenderBuffers) {
		FloatArray renderBuffer = pRenderBuffers[Settings.LAYERS_PER_DEPTH * pZ + LAYER];
		return GraphicsUtils.render(this, pCellularity, pX, pY, pZ, pOffsetX, pOffsetY, pTileX,
				pTileY, pSize, pCos, pSin, renderBuffer, TILE_N_X, TILE_N_Y, TILE_S_X, TILE_S_Y,
				TILE_H_X, TILE_H_Y, TILE_V_X, TILE_V_Y, TILE_B_X, TILE_B_Y, TILE_N_TR, TILE_S_TR,
				TILE_H_TR, TILE_V_TR, TILE_B_TR);
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
		obj.mTilesConnectedMask = 0;
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
