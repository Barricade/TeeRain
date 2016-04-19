package com.gaskarov.teerain.core;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class VoidCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final VoidCell sInstance = new VoidCell();

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
	public boolean isConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY, int pVZ) {
		return true;
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public void recycle() {
		recycle(this);
	}

	@Override
	public VoidCell cpy() {
		return VoidCell.obtain();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static VoidCell obtain() {
		return sInstance;
	}

	public static void recycle(VoidCell pObj) {
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
