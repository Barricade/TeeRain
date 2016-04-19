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
public final class ContainerShadow {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mSize;
	private int[] mFree;
	private int mFreeSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private ContainerShadow() {
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

	private static ContainerShadow obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (ContainerShadow.class) {
				return sPool.size() == 0 ? new ContainerShadow() : (ContainerShadow) sPool.pop();
			}
		return new ContainerShadow();
	}

	private static void recyclePure(ContainerShadow pObj) {
		if (GlobalConstants.POOL)
			synchronized (ContainerShadow.class) {
				sPool.push(pObj);
			}
	}

	public static ContainerShadow obtain(int pCapacity) {

		ContainerShadow obj = obtainPure();

		obj.mSize = 0;
		obj.mFree = BinaryIntArrayPool.obtain(pCapacity);
		obj.mFreeSize = 0;

		return obj;
	}

	public static ContainerShadow obtain() {
		return obtain(0);
	}

	public static void recycle(ContainerShadow pObj) {
		BinaryIntArrayPool.recycle(pObj.mFree);
		pObj.mFree = null;
		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public int push() {
		if (mFreeSize == 0 && mSize == mFree.length) {
			int oldCapacity = mFree.length;
			int capacity = oldCapacity == 0 ? 1 : oldCapacity << 1;
			int[] oldFree = mFree;
			mFree = ArrayUtils.copyOf(oldFree, capacity);
			BinaryIntArrayPool.recycle(oldFree);
		}
		int key = mFreeSize == 0 ? mSize : mFree[--mFreeSize];
		++mSize;
		return key;
	}

	public void remove(int pKey) {
		--mSize;
		mFree[mFreeSize++] = pKey;
	}

	public void clear() {
		mFreeSize = 0;
		mSize = 0;
	}

	public void clear(int pCapacity) {
		if (mFree.length != pCapacity) {
			BinaryIntArrayPool.recycle(mFree);
			mFree = BinaryIntArrayPool.obtain(pCapacity);
		}
		mFreeSize = 0;
		mSize = 0;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
