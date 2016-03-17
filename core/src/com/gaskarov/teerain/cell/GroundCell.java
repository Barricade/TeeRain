package com.gaskarov.teerain.cell;

import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.organoid.PhysicsWallOrganoid;
import com.gaskarov.teerain.util.GraphicsUtils;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class GroundCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TILE_N_X = Settings.TILE_W * 0;
	public static final float TILE_N_Y = Settings.TILE_H * 0;
	public static final float TILE_S_X = Settings.TILE_W * 1;
	public static final float TILE_S_Y = Settings.TILE_H * 0;
	public static final float TILE_H_X = Settings.TILE_W * 2;
	public static final float TILE_H_Y = Settings.TILE_H * 0;
	public static final float TILE_V_X = Settings.TILE_W * 3;
	public static final float TILE_V_Y = Settings.TILE_H * 0;
	public static final float TILE_B_X = Settings.TILE_W * 4;
	public static final float TILE_B_Y = Settings.TILE_H * 0;

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

	private GroundCell() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, 0, Settings.SOLID_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
		mPhysicsWallOrganoid.attach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, 0, Settings.NO_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
		mPhysicsWallOrganoid.detach(pCellularity, pX, pY, pZ, this);
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
	public void recycle() {
		recycle(this);
	}

	@Override
	public Cell cpy() {
		return obtain();
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
		return pCell instanceof GroundCell;
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
	public void render(Cellularity pCellularity, int pX, int pY, int pZ) {
		for (int i = 0; i < Settings.LAYERS_PER_DEPTH; ++i)
			switch (i) {
			case LAYER: {
				int count =
						GraphicsUtils.render(this, pCellularity, pX, pY, pZ, TILE_N_X, TILE_N_Y,
								TILE_S_X, TILE_S_Y, TILE_H_X, TILE_H_Y, TILE_V_X, TILE_V_Y,
								TILE_B_X, TILE_B_Y);
				GraphicsUtils.count(pCellularity, count);
				break;
			}
			default:
				GraphicsUtils.count(pCellularity, 0);
				break;
			}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static GroundCell obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (GroundCell.class) {
				return sPool.size() == 0 ? new GroundCell() : (GroundCell) sPool.pop();
			}
		return new GroundCell();
	}

	private static void recyclePure(GroundCell pObj) {
		if (GlobalConstants.POOL)
			synchronized (GroundCell.class) {
				sPool.push(pObj);
			}
	}

	public static GroundCell obtain() {
		GroundCell obj = obtainPure();
		obj.mPhysicsWallOrganoid = PhysicsWallOrganoid.obtain();
		obj.mTilesConnectedMask = 0;
		return obj;
	}

	public static void recycle(GroundCell pObj) {
		PhysicsWallOrganoid.recycle(pObj.mPhysicsWallOrganoid);
		pObj.mPhysicsWallOrganoid = null;
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
