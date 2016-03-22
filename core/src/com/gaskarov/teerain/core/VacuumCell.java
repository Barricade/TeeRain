package com.gaskarov.teerain.core;


/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class VacuumCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final VacuumCell sInstance = new VacuumCell();

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
	public void recycle() {
		recycle(this);
	}

	@Override
	public VacuumCell cpy() {
		return VacuumCell.obtain();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static VacuumCell obtain() {
		return sInstance;
	}

	public static void recycle(VacuumCell pObj) {
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
