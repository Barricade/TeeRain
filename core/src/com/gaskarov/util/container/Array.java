package com.gaskarov.util.container;

import com.gaskarov.util.common.ArrayUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryObjectArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Array {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static int sPoolSize = 0;
	private static Object[] sPool = BinaryObjectArrayPool.ZERO_SIZE_ARRAY;

	private Object[] mData;
	private int mSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private Array() {
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

	private static Array obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (Array.class) {
				return sPoolSize == 0 ? new Array() : (Array) sPool[--sPoolSize];
			}
		return new Array();
	}

	private static void recyclePure(Array pObj) {
		if (GlobalConstants.POOL)
			synchronized (Array.class) {
				if (sPoolSize == sPool.length) {
					Object[] oldPool = sPool;
					sPool =
							ArrayUtils
									.copyOf(oldPool, oldPool.length == 0 ? 1 : oldPool.length * 2);
					BinaryObjectArrayPool.recycle(oldPool);
				}
				sPool[sPoolSize++] = pObj;
			}
	}

	public static Array obtain() {

		Array obj = obtainPure();

		obj.mSize = 0;
		obj.mData = BinaryObjectArrayPool.ZERO_SIZE_ARRAY;

		return obj;
	}

	public static void recycle(Array pObj) {
		BinaryObjectArrayPool.recycle(pObj.mData);
		pObj.mData = null;

		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public Object get(int pId) {
		return mData[pId];
	}

	public void set(int pId, Object pVal) {
		mData[pId] = pVal;
	}

	public Object back() {
		return mData[mSize - 1];
	}

	public void push(Object pVal) {
		while (mSize == mData.length) {
			Object[] oldPool = mData;
			mData = ArrayUtils.copyOf(oldPool, oldPool.length == 0 ? 1 : oldPool.length << 1);
			BinaryObjectArrayPool.recycle(oldPool);
		}
		mData[mSize++] = pVal;
	}

	public Object pop() {
		return mData[--mSize];
	}

	public void clear() {
		mSize = 0;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
