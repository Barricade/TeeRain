package com.gaskarov.util.container;

import com.gaskarov.util.common.ArrayUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryByteArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class ByteArray {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private byte[] mData;
	private int mSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private ByteArray() {
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

	private static ByteArray obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (ByteArray.class) {
				return sPool.size() == 0 ? new ByteArray() : (ByteArray) sPool.pop();
			}
		return new ByteArray();
	}

	private static void recyclePure(ByteArray pObj) {
		if (GlobalConstants.POOL)
			synchronized (ByteArray.class) {
				sPool.push(pObj);
			}
	}

	public static ByteArray obtain(int pCapacity) {

		ByteArray obj = obtainPure();

		obj.mSize = 0;
		obj.mData = BinaryByteArrayPool.obtain(pCapacity);

		return obj;
	}

	public static ByteArray obtain() {
		return obtain(0);
	}

	public static void recycle(ByteArray pObj) {
		BinaryByteArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public int get(int pId) {
		return mData[pId];
	}

	public void set(int pId, byte pVal) {
		mData[pId] = pVal;
	}

	public int back() {
		return mData[mSize - 1];
	}

	public void push(byte pVal) {
		while (mSize == mData.length) {
			byte[] oldPool = mData;
			mData = ArrayUtils.copyOf(oldPool, oldPool.length == 0 ? 1 : oldPool.length << 1);
			BinaryByteArrayPool.recycle(oldPool);
		}
		mData[mSize++] = pVal;
	}

	public byte pop() {
		return mData[--mSize];
	}

	public void clear() {
		mSize = 0;
	}

	public void clear(int pCapacity) {
		mSize = 0;
		if (mData.length != pCapacity) {
			BinaryByteArrayPool.recycle(mData);
			mData = BinaryByteArrayPool.obtain(pCapacity);
		}
	}

	public byte[] data() {
		return mData;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
