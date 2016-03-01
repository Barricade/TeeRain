package com.gaskarov.teerain.cell;

import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cellularity.Cellularity;
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
public final class UICell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TILE_N_X = Settings.TILE_W * 0;
	public static final float TILE_N_Y = Settings.TILE_H * 5;
	public static final float TILE_S_X = Settings.TILE_W * 1;
	public static final float TILE_S_Y = Settings.TILE_H * 5;
	public static final float TILE_H_X = Settings.TILE_W * 2;
	public static final float TILE_H_Y = Settings.TILE_H * 5;
	public static final float TILE_V_X = Settings.TILE_W * 3;
	public static final float TILE_V_Y = Settings.TILE_H * 5;
	public static final float TILE_B_X = Settings.TILE_W * 4;
	public static final float TILE_B_Y = Settings.TILE_H * 5;
	public static final boolean TILE_N_TR = true;
	public static final boolean TILE_S_TR = true;
	public static final boolean TILE_H_TR = true;
	public static final boolean TILE_V_TR = true;
	public static final boolean TILE_B_TR = true;

	public static final int LAYER = 0;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mTilesConnectedMask;

	// ===========================================================
	// Constructors
	// ===========================================================

	private UICell() {
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
				Settings.NO_LIGHT_SOURCE_ID);
	}

	@Override
	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, 0, Settings.NO_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
	}

	@Override
	public void refresh(Cellularity pCellularity, int pX, int pY, int pZ) {
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
	public boolean isTileConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY) {
		return pCell instanceof UICell;
	}

	@Override
	public int tilesConnectedMask() {
		return mTilesConnectedMask;
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

	private static UICell obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (UICell.class) {
				return sPool.size() == 0 ? new UICell() : (UICell) sPool.pop();
			}
		return new UICell();
	}

	private static void recyclePure(UICell pObj) {
		if (GlobalConstants.POOL)
			synchronized (UICell.class) {
				sPool.push(pObj);
			}
	}

	public static UICell obtain() {
		UICell obj = obtainPure();
		obj.mTilesConnectedMask = 0;
		return obj;
	}

	public static void recycle(UICell pObj) {
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
