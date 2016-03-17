package com.gaskarov.util.container;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryFloatArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class FloatQueue {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private float[] mData;
	private int mHead;
	private int mTail;

	// ===========================================================
	// Constructors
	// ===========================================================

	private FloatQueue() {
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

	private static FloatQueue obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (FloatQueue.class) {
				return sPool.size() == 0 ? new FloatQueue() : (FloatQueue) sPool.pop();
			}
		return new FloatQueue();
	}

	private static void recyclePure(FloatQueue pObj) {
		if (GlobalConstants.POOL)
			synchronized (FloatQueue.class) {
				sPool.push(pObj);
			}
	}

	public static FloatQueue obtain(int pCapacity) {

		FloatQueue obj = obtainPure();

		obj.mData = BinaryFloatArrayPool.obtain(pCapacity);
		obj.mHead = obj.mTail = 0;

		return obj;
	}

	public static FloatQueue obtain() {
		return obtain(0);
	}

	public static void recycle(FloatQueue pObj) {
		BinaryFloatArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		recyclePure(pObj);
	}

	public int size() {
		return mHead - mTail & mData.length - 1;
	}

	public float get(int pId) {
		return mData[mTail + pId & mData.length - 1];
	}

	public void set(int pId, float pObj) {
		mData[mTail + pId & mData.length - 1] = pObj;
	}

	public void push(float pObj) {
		resize();
		mData[mHead] = pObj;
		mHead = mHead + 1 & mData.length - 1;
	}

	public float pop() {
		float obj = mData[mHead];
		mHead = mHead - 1 & mData.length - 1;
		return obj;
	}

	public void unshift(float pObj) {
		resize();
		mTail = mTail - 1 & mData.length - 1;
		mData[mTail] = pObj;
	}

	public float shift() {
		float obj = mData[mTail];
		mTail = mTail + 1 & mData.length - 1;
		return obj;
	}

	public void clear() {
		mTail = mHead;
	}

	public void clear(int pCapacity) {
		this.mTail = this.mHead = 0;
		if (mData.length != pCapacity) {
			BinaryFloatArrayPool.recycle(mData);
			mData = BinaryFloatArrayPool.obtain(pCapacity);
		}
	}

	private void resize() {
		if (size() < mData.length - 1)
			return;
		float[] a = BinaryFloatArrayPool.obtain(mData.length == 0 ? 2 : mData.length * 2);
		if (mTail <= mHead) {
			System.arraycopy(mData, mTail, a, mTail, mHead - mTail);
		} else {
			System.arraycopy(mData, 0, a, 0, mHead);
			System.arraycopy(mData, mTail, a, mData.length + mTail, mData.length - mTail);
			mTail += mData.length;
		}
		BinaryFloatArrayPool.recycle(mData);
		mData = a;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
