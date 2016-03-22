package com.gaskarov.teerain.core;

import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.GraphicsUtils;
import com.gaskarov.teerain.game.Player;
import com.gaskarov.teerain.game.game.ControlOrganoid;

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

	public void render(Cellularity pCellularity, int pX, int pY, int pZ, float pCos, float pSin) {
		for (int i = 0; i < Settings.LAYERS_PER_DEPTH; ++i)
			switch (i) {
			default:
				GraphicsUtils.count(pCellularity, 0);
				break;
			}
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

	public int renderWeapon(Cellularity pCellularity, int pX, int pY, int pZ, float pCos,
			float pSin, ControlOrganoid pControlOrganoid) {
		return 0;
	}

	public void touchDown(Cellularity pCellularity, int pX, int pY, int pZ, float pClickX,
			float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer, int pItemId) {
	}

	public void touchUp(Cellularity pCellularity, int pX, int pY, int pZ, float pClickX,
			float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer, int pItemId) {
	}

	public void touchDragged(Cellularity pCellularity, int pX, int pY, int pZ, float pClickX,
			float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer, int pItemId) {
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
