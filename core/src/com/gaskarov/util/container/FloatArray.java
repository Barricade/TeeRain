package com.gaskarov.util.container;

import com.gaskarov.util.common.ArrayUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryFloatArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class FloatArray {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private float[] mData;
	private int mSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private FloatArray() {
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

	private static FloatArray obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (FloatArray.class) {
				return sPool.size() == 0 ? new FloatArray() : (FloatArray) sPool.pop();
			}
		return new FloatArray();
	}

	private static void recyclePure(FloatArray pObj) {
		if (GlobalConstants.POOL)
			synchronized (FloatArray.class) {
				sPool.push(pObj);
			}
	}

	public static FloatArray obtain() {

		FloatArray obj = obtainPure();

		obj.mSize = 0;
		obj.mData = BinaryFloatArrayPool.ZERO_SIZE_ARRAY;

		return obj;
	}

	public static void recycle(FloatArray pObj) {
		BinaryFloatArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public float get(int pId) {
		return mData[pId];
	}

	public void set(int pId, float pVal) {
		mData[pId] = pVal;
	}

	public float back() {
		return mData[mSize - 1];
	}

	public void push(float pVal) {
		while (mSize == mData.length) {
			float[] oldPool = mData;
			mData = ArrayUtils.copyOf(oldPool, oldPool.length == 0 ? 1 : oldPool.length << 1);
			BinaryFloatArrayPool.recycle(oldPool);
		}
		mData[mSize++] = pVal;
	}

	public void pushArray(int pN) {
		mSize += pN;
		while (mSize > mData.length) {
			float[] oldPool = mData;
			mData = ArrayUtils.copyOf(oldPool, oldPool.length == 0 ? 1 : oldPool.length << 1);
			BinaryFloatArrayPool.recycle(oldPool);
		}
	}

	public void pushArray(FloatArray pArray) {
		int size = mSize;
		pushArray(pArray.mSize);
		System.arraycopy(pArray.mData, 0, mData, size, pArray.mSize);
	}

	public float pop() {
		return mData[--mSize];
	}

	public void popArray(int pN) {
		mSize -= pN;
	}

	public void clear() {
		mSize = 0;
	}

	public float[] data() {
		return mData;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
