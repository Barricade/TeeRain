package com.gaskarov.teerain.core.cellularity;

import com.badlogic.gdx.math.Vector2;
import com.gaskarov.teerain.core.PhysicsWall;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.MetaBody;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public abstract class Cellularity {

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

	public abstract boolean isChunk();

	public abstract ChunkCellularity getChunk();

	public abstract int getChunkX();

	public abstract int getChunkY();

	public abstract Tissularity getTissularity();

	public abstract Vector2 localToChunk(float pX, float pY);

	public abstract int localToGlobalX(int pX);

	public abstract int localToGlobalY(int pY);

	public abstract MetaBody getBody();

	public abstract MetaBody getBody(int pX, int pY);

	public abstract void tickEnable(int pX, int pY, int pZ);

	public abstract void tickDisable(int pX, int pY, int pZ);

	public abstract void tissularedEnable(int pX, int pY, int pZ);

	public abstract void tissularedDisable(int pX, int pY, int pZ);

	public abstract void destroyCell(int pX, int pY, int pZ);

	public abstract PhysicsWall getPhysicsWall(int pX, int pY, int pZ);

	public abstract void setPhysicsWall(int pX, int pY, int pZ,
			PhysicsWall pPhysicsWall);

	public abstract int getCell(int pX, int pY, int pZ);

	public abstract void setCell(int pX, int pY, int pZ, int pCell,
			CellData pCellData);

	public abstract CellData getCellData(int pX, int pY, int pZ);

	public abstract boolean isShellable(int pX, int pY, int pZ);

	public abstract boolean isSolid(int pX, int pY, int pZ);

	public abstract int getDefaultCell(int pX, int pY, int pZ);

	public abstract int getDropDefaultCell(int pX, int pY, int pZ);

	public abstract long random();

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static abstract class CellData {

		public abstract CellData cpy();

		public abstract void recycle();
	}

}
