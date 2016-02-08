package com.gaskarov.teerain.cell;

import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.organoid.PhysicsWallOrganoid;
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
public final class TreeCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TILE_WOOD_N_X = Settings.TILE_W * 3;
	public static final float TILE_WOOD_N_Y = Settings.TILE_H * 3;
	public static final float TILE_WOOD_S_X = Settings.TILE_W * 3;
	public static final float TILE_WOOD_S_Y = Settings.TILE_H * 3;
	public static final float TILE_WOOD_H_X = Settings.TILE_W * 4;
	public static final float TILE_WOOD_H_Y = Settings.TILE_H * 3;
	public static final float TILE_WOOD_V_X = Settings.TILE_W * 3;
	public static final float TILE_WOOD_V_Y = Settings.TILE_H * 3;
	public static final float TILE_WOOD_B_X = Settings.TILE_W * 4;
	public static final float TILE_WOOD_B_Y = Settings.TILE_H * 3;
	public static final boolean TILE_WOOD_N_TR = true;
	public static final boolean TILE_WOOD_S_TR = true;
	public static final boolean TILE_WOOD_H_TR = true;
	public static final boolean TILE_WOOD_V_TR = true;
	public static final boolean TILE_WOOD_B_TR = true;

	public static final int LAYER_WOOD = 1;

	public static final float TILE_LEAVES_N_X = Settings.TILE_W * 0;
	public static final float TILE_LEAVES_N_Y = Settings.TILE_H * 2;
	public static final float TILE_LEAVES_S_X = Settings.TILE_W * 1;
	public static final float TILE_LEAVES_S_Y = Settings.TILE_H * 2;
	public static final float TILE_LEAVES_H_X = Settings.TILE_W * 2;
	public static final float TILE_LEAVES_H_Y = Settings.TILE_H * 2;
	public static final float TILE_LEAVES_V_X = Settings.TILE_W * 3;
	public static final float TILE_LEAVES_V_Y = Settings.TILE_H * 2;
	public static final float TILE_LEAVES_B_X = Settings.TILE_W * 4;
	public static final float TILE_LEAVES_B_Y = Settings.TILE_H * 2;
	public static final boolean TILE_LEAVES_N_TR = false;
	public static final boolean TILE_LEAVES_S_TR = true;
	public static final boolean TILE_LEAVES_H_TR = true;
	public static final boolean TILE_LEAVES_V_TR = true;
	public static final boolean TILE_LEAVES_B_TR = true;

	public static final int LAYER_LEAVES = 0;

	public static final float TILE_SAPLING_X = Settings.TILE_W * 8;
	public static final float TILE_SAPLING_Y = Settings.TILE_H * 0;
	public static final boolean TILE_SAPLING_TR = true;

	public static final int LAYER_SAPLING = 1;

	public static final float BORDER_SIZE = 0.02f;
	public static final float CORNER_SIZE = 0.1f;
	public static final float CORNER_VALUE = 0.01f;
	public static final float FRICTION = 0.6f;
	public static final float DENSITY = 1.0f;
	public static final float RESTITUTION = 0.0f;

	public static final int MAX_TREE_SIZE = 8;

	public static final int UPDATE_ANOTHER_PRIORITY = 8;

	public static final int GROW_DELAY = 60;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mTreeSize;
	private int mLeavesSize;

	private PhysicsWallOrganoid mPhysicsWallOrganoid;
	private int mTilesConnectedMask;

	// ===========================================================
	// Constructors
	// ===========================================================

	private TreeCell() {
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
		return false;
	}

	@Override
	public boolean isSquare() {
		return false;
	}

	@Override
	public boolean isTileConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY) {
		return false;
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
		return pVY == -1;
	}

	@Override
	public boolean render(Cellularity pCellularity, int pX, int pY, int pZ, float pOffsetX,
			float pOffsetY, int pTileX, int pTileY, float pWidth, float pHeight, float pCos,
			float pSin, FloatArray[] pRenderBuffers) {
		FloatArray renderBuffer = pRenderBuffers[Settings.LAYERS_PER_DEPTH * pZ + LAYER_SAPLING];
		GraphicsUtils.render(this, pCellularity, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
				pWidth, pHeight, pCos, pSin, renderBuffer, TILE_SAPLING_X, TILE_SAPLING_Y);
		return TILE_SAPLING_TR;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static TreeCell obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (TreeCell.class) {
				return sPool.size() == 0 ? new TreeCell() : (TreeCell) sPool.pop();
			}
		return new TreeCell();
	}

	private static void recyclePure(TreeCell pObj) {
		if (GlobalConstants.POOL)
			synchronized (TreeCell.class) {
				sPool.push(pObj);
			}
	}

	public static TreeCell obtain(int pTreeSize, int pLeavesSize) {
		TreeCell obj = obtainPure();
		obj.mTreeSize = pTreeSize;
		obj.mLeavesSize = pLeavesSize;
		obj.mPhysicsWallOrganoid = PhysicsWallOrganoid.obtain();
		obj.mTilesConnectedMask = 0;
		return obj;
	}

	public static TreeCell obtain() {
		return obtain(0, 0);
	}

	public static void recycle(TreeCell pObj) {
		PhysicsWallOrganoid.recycle(pObj.mPhysicsWallOrganoid);
		pObj.mPhysicsWallOrganoid = null;
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
