package com.gaskarov.util.container;

import com.gaskarov.util.common.ArrayUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryIntArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class IntArray {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int[] mData;
	private int mSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private IntArray() {
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

	private static IntArray obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (IntArray.class) {
				return sPool.size() == 0 ? new IntArray() : (IntArray) sPool.pop();
			}
		return new IntArray();
	}

	private static void recyclePure(IntArray pObj) {
		if (GlobalConstants.POOL)
			synchronized (IntArray.class) {
				sPool.push(pObj);
			}
	}

	public static IntArray obtain(int pCapacity) {

		IntArray obj = obtainPure();

		obj.mSize = 0;
		obj.mData = BinaryIntArrayPool.obtain(pCapacity);

		return obj;
	}

	public static IntArray obtain() {
		return obtain(0);
	}

	public static void recycle(IntArray pObj) {
		BinaryIntArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public int get(int pId) {
		return mData[pId];
	}

	public void set(int pId, int pVal) {
		mData[pId] = pVal;
	}

	public int back() {
		return mData[mSize - 1];
	}

	public void push(int pVal) {
		while (mSize == mData.length) {
			int[] oldPool = mData;
			mData = ArrayUtils.copyOf(oldPool, oldPool.length == 0 ? 1 : oldPool.length << 1);
			BinaryIntArrayPool.recycle(oldPool);
		}
		mData[mSize++] = pVal;
	}

	public int pop() {
		return mData[--mSize];
	}

	public void clear() {
		mSize = 0;
	}

	public void clear(int pCapacity) {
		mSize = 0;
		if (mData.length != pCapacity) {
			BinaryIntArrayPool.recycle(mData);
			mData = BinaryIntArrayPool.obtain(pCapacity);
		}
	}

	public int[] data() {
		return mData;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
