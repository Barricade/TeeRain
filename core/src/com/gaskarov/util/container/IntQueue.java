package com.gaskarov.util.container;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryIntArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class IntQueue {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int[] mData;
	private int mHead;
	private int mTail;

	// ===========================================================
	// Constructors
	// ===========================================================

	private IntQueue() {
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

	private static IntQueue obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (IntQueue.class) {
				return sPool.size() == 0 ? new IntQueue() : (IntQueue) sPool.pop();
			}
		return new IntQueue();
	}

	private static void recyclePure(IntQueue pObj) {
		if (GlobalConstants.POOL)
			synchronized (IntQueue.class) {
				sPool.push(pObj);
			}
	}

	public static IntQueue obtain(int pCapacity) {

		IntQueue obj = obtainPure();

		obj.mData = BinaryIntArrayPool.obtain(pCapacity);
		obj.mHead = obj.mTail = 0;

		return obj;
	}

	public static IntQueue obtain() {
		return obtain(0);
	}

	public static void recycle(IntQueue pObj) {
		BinaryIntArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		recyclePure(pObj);
	}

	public int size() {
		return mHead - mTail & mData.length - 1;
	}

	public int get(int pId) {
		return mData[mTail + pId & mData.length - 1];
	}

	public void set(int pId, int pObj) {
		mData[mTail + pId & mData.length - 1] = pObj;
	}

	public void push(int pObj) {
		resize();
		mData[mHead] = pObj;
		mHead = mHead + 1 & mData.length - 1;
	}

	public int pop() {
		int obj = mData[mHead];
		mHead = mHead - 1 & mData.length - 1;
		return obj;
	}

	public void unshift(int pObj) {
		resize();
		mTail = mTail - 1 & mData.length - 1;
		mData[mTail] = pObj;
	}

	public int shift() {
		int obj = mData[mTail];
		mTail = mTail + 1 & mData.length - 1;
		return obj;
	}

	public void clear() {
		mTail = mHead;
	}

	public void clear(int pCapacity) {
		this.mTail = this.mHead = 0;
		if (mData.length != pCapacity) {
			BinaryIntArrayPool.recycle(mData);
			mData = BinaryIntArrayPool.obtain(pCapacity);
		}
	}

	private void resize() {
		if (size() < mData.length - 1)
			return;
		int[] a = BinaryIntArrayPool.obtain(mData.length == 0 ? 2 : mData.length * 2);
		if (mTail <= mHead) {
			System.arraycopy(mData, mTail, a, mTail, mHead - mTail);
		} else {
			System.arraycopy(mData, 0, a, 0, mHead);
			System.arraycopy(mData, mTail, a, mData.length + mTail, mData.length - mTail);
			mTail += mData.length;
		}
		BinaryIntArrayPool.recycle(mData);
		mData = a;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
