package com.gaskarov.util.container;

import com.gaskarov.util.common.ArrayUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryIntArrayPool;
import com.gaskarov.util.pool.BinaryObjectArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Container {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private Object[] mData;
	private int[] mDataId;
	private int mSize;
	private int[] mIndirect;
	private int[] mFree;
	private int mFreeSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private Container() {
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

	private static Container obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (Container.class) {
				return sPool.size() == 0 ? new Container() : (Container) sPool.pop();
			}
		return new Container();
	}

	private static void recyclePure(Container pObj) {
		if (GlobalConstants.POOL)
			synchronized (Container.class) {
				sPool.push(pObj);
			}
	}

	public static Container obtain(int pCapacity) {

		Container obj = obtainPure();

		obj.mSize = 0;
		obj.mData = BinaryObjectArrayPool.obtain(pCapacity);
		obj.mDataId = BinaryIntArrayPool.obtain(pCapacity);
		obj.mIndirect = BinaryIntArrayPool.obtain(pCapacity);
		obj.mFree = BinaryIntArrayPool.obtain(pCapacity);
		obj.mFreeSize = 0;

		return obj;
	}

	public static Container obtain() {
		return obtain(0);
	}

	public static void recycle(Container pObj) {
		BinaryObjectArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		BinaryIntArrayPool.recycle(pObj.mDataId);
		pObj.mDataId = null;
		BinaryIntArrayPool.recycle(pObj.mIndirect);
		pObj.mIndirect = null;
		BinaryIntArrayPool.recycle(pObj.mFree);
		pObj.mFree = null;
		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public Object getVal(int pId) {
		return mData[pId];
	}

	public int getKey(int pId) {
		return mDataId[pId];
	}

	public Object get(int pKey) {
		return mData[mIndirect[pKey]];
	}

	public int push(Object pVal) {
		if (mFreeSize == 0 && mSize == mData.length) {
			int oldCapacity = mData.length;
			int capacity = oldCapacity == 0 ? 1 : oldCapacity << 1;
			Object[] oldData = mData;
			mData = ArrayUtils.copyOf(oldData, capacity);
			BinaryObjectArrayPool.recycle(oldData);
			int[] oldDataId = mDataId;
			mDataId = ArrayUtils.copyOf(oldDataId, capacity);
			BinaryIntArrayPool.recycle(oldDataId);
			int[] oldIndirect = mIndirect;
			mIndirect = ArrayUtils.copyOf(oldIndirect, capacity);
			BinaryIntArrayPool.recycle(oldIndirect);
			int[] oldFree = mFree;
			mFree = ArrayUtils.copyOf(oldFree, capacity);
			BinaryIntArrayPool.recycle(oldFree);
		}
		int key = mFreeSize == 0 ? mSize : mFree[--mFreeSize];
		mIndirect[key] = mSize;
		mData[mSize] = pVal;
		mDataId[mSize++] = key;
		return key;
	}

	public void remove(int pKey) {
		int id = mIndirect[pKey];
		int lastKey = mDataId[--mSize];
		mIndirect[lastKey] = id;
		mData[id] = mData[mSize];
		mDataId[id] = lastKey;
		mFree[mFreeSize++] = pKey;
	}

	public void clear() {
		mFreeSize = 0;
		mSize = 0;
	}

	public void clear(int pCapacity) {
		if (mData.length != pCapacity) {
			BinaryObjectArrayPool.recycle(mData);
			mData = BinaryObjectArrayPool.obtain(pCapacity);
			BinaryIntArrayPool.recycle(mDataId);
			mDataId = BinaryIntArrayPool.obtain(pCapacity);
			BinaryIntArrayPool.recycle(mIndirect);
			mIndirect = BinaryIntArrayPool.obtain(pCapacity);
			BinaryIntArrayPool.recycle(mFree);
			mFree = BinaryIntArrayPool.obtain(pCapacity);
		}
		mFreeSize = 0;
		mSize = 0;
	}

	public Object[] data() {
		return mData;
	}

	public int[] dataKeys() {
		return mDataId;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
