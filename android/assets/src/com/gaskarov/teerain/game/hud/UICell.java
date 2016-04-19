package com.gaskarov.teerain.game.hud;

import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.GraphicsUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

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

	public static final int LAYER = 0;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

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
		pCellularity.setCellLightData(pX, pY, pZ, Settings.AIR_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
	}

	@Override
	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, Settings.NO_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
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
		return obj;
	}

	public static void recycle(UICell pObj) {
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
