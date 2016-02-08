package com.gaskarov.teerain.cell;

import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cellularity.Cellularity;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class AirCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final AirCell sInstance = new AirCell();

	// ===========================================================
	// Constructors
	// ===========================================================

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
	public void recycle() {
		recycle(this);
	}

	@Override
	public AirCell cpy() {
		return AirCell.obtain();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static AirCell obtain() {
		return sInstance;
	}

	public static void recycle(AirCell pObj) {
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
