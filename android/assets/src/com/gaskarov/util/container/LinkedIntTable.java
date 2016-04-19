package com.gaskarov.util.container;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryIntArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class LinkedIntTable {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int INVALID_ID = -1;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int[] mUsedId;
	private int[] mUsed;
	private int mSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private LinkedIntTable() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	private static LinkedIntTable obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (LinkedIntTable.class) {
				return sPool.size() == 0 ? new LinkedIntTable() : (LinkedIntTable) sPool.pop();
			}
		return new LinkedIntTable();
	}

	private static void recyclePure(LinkedIntTable pObj) {
		if (GlobalConstants.POOL)
			synchronized (LinkedIntTable.class) {
				sPool.push(pObj);
			}
	}

	public static LinkedIntTable obtain(int pN) {

		LinkedIntTable obj = obtainPure();

		obj.mUsedId = BinaryIntArrayPool.obtain(pN);
		for (int i = 0; i < obj.mUsedId.length; ++i)
			obj.mUsedId[i] = INVALID_ID;
		obj.mUsed = BinaryIntArrayPool.obtain(pN);
		obj.mSize = 0;

		return obj;
	}

	public static void recycle(LinkedIntTable pObj) {
		BinaryIntArrayPool.recycle(pObj.mUsedId);
		pObj.mUsedId = null;
		BinaryIntArrayPool.recycle(pObj.mUsed);
		pObj.mUsed = null;
		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public boolean get(int pObj) {
		return mUsedId[pObj] != INVALID_ID;
	}

	public boolean set(int pObj) {
		if (mUsedId[pObj] != INVALID_ID)
			return true;
		mUsedId[pObj] = mSize;
		mUsed[mSize++] = pObj;
		return false;
	}

	public boolean remove(int pObj) {
		if (mUsedId[pObj] != INVALID_ID) {
			int id = mUsedId[pObj];
			mUsedId[pObj] = INVALID_ID;
			if (id != --mSize) {
				mUsed[id] = mUsed[mSize];
				mUsedId[mUsed[id]] = id;
			}
			return true;
		}
		return false;
	}

	public int key(int pNode) {
		return mUsed[pNode];
	}

	public int pop() {
		int id = mUsed[--mSize];
		mUsedId[id] = INVALID_ID;
		return id;
	}

	public void clear() {
		while (mSize > 0)
			mUsedId[mUsed[--mSize]] = INVALID_ID;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
