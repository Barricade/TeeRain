package com.gaskarov.teerain.cell;

import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.util.container.FloatArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public abstract class Cell {

	// ===========================================================
	// Constants
	// ===========================================================

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

	public abstract void recycle();

	public abstract Cell cpy();

	// ===========================================================
	// Methods
	// ===========================================================

	public Cell update(Cellularity pCellularity, int pX, int pY, int pZ) {
		return null;
	}

	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
	}

	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
	}

	public void tissularedAttach(Cellularity pCellularity, int pX, int pY, int pZ) {
	}

	public void tissularedDetach(Cellularity pCellularity, int pX, int pY, int pZ) {
	}

	public void refresh(Cellularity pCellularity, int pX, int pY, int pZ) {
	}

	public void tick(Cellularity pCellularity, int pX, int pY, int pZ) {
	}

	public boolean render(Cellularity pCellularity, int pX, int pY, int pZ, float pOffsetX,
			float pOffsetY, int pTileX, int pTileY, float pSize, float pCos, float pSin,
			FloatArray[] pRenderBuffers) {
		return true;
	}

	public boolean isDroppable(Cellularity pCellularity, int pX, int pY, int pZ) {
		return false;
	}

	public boolean isConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY, int pVZ) {
		return false;
	}

	public boolean isDynamicLightSource() {
		return false;
	}

	public float getBorderSize() {
		return 0;
	}

	public float getCornerValue() {
		return 0;
	}

	public float getCornerSize() {
		return 0;
	}

	public float getFriction() {
		return 0;
	}

	public float getDensity() {
		return 0;
	}

	public float getRestitution() {
		return 0;
	}

	public boolean isSolid() {
		return false;
	}

	public boolean isSquare() {
		return true;
	}

	public boolean isTileConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY) {
		return false;
	}

	public int tilesConnectedMask() {
		return 0;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
