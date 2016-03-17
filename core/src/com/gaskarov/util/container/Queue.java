package com.gaskarov.util.container;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryObjectArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Queue {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private Object[] mData;
	private int mHead;
	private int mTail;

	// ===========================================================
	// Constructors
	// ===========================================================

	private Queue() {
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

	private static Queue obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (Queue.class) {
				return sPool.size() == 0 ? new Queue() : (Queue) sPool.pop();
			}
		return new Queue();
	}

	private static void recyclePure(Queue pObj) {
		if (GlobalConstants.POOL)
			synchronized (Queue.class) {
				sPool.push(pObj);
			}
	}

	public static Queue obtain(int pCapacity) {

		Queue obj = obtainPure();

		obj.mData = BinaryObjectArrayPool.obtain(pCapacity);
		obj.mHead = obj.mTail = 0;

		return obj;
	}

	public static Queue obtain() {
		return obtain(0);
	}

	public static void recycle(Queue pObj) {
		BinaryObjectArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		recyclePure(pObj);
	}

	public int size() {
		return this.mHead - this.mTail & this.mData.length - 1;
	}

	public Object get(int pId) {
		return this.mData[this.mTail + pId & this.mData.length - 1];
	}

	public void set(int pId, Object pObj) {
		this.mData[this.mTail + pId & this.mData.length - 1] = pObj;
	}

	public void push(Object pObj) {
		this.resize();
		this.mData[this.mHead] = pObj;
		this.mHead = this.mHead + 1 & this.mData.length - 1;
	}

	public Object pop() {
		Object obj = this.mData[this.mHead];
		this.mHead = this.mHead - 1 & this.mData.length - 1;
		return obj;
	}

	public void unshift(Object pObj) {
		this.resize();
		this.mTail = this.mTail - 1 & this.mData.length - 1;
		this.mData[this.mTail] = pObj;
	}

	public Object shift() {
		Object obj = this.mData[this.mTail];
		this.mTail = this.mTail + 1 & this.mData.length - 1;
		return obj;
	}

	public void clear() {
		this.mTail = this.mHead;
	}

	public void clear(int pCapacity) {
		this.mTail = this.mHead = 0;
		if (mData.length != pCapacity) {
			BinaryObjectArrayPool.recycle(mData);
			mData = BinaryObjectArrayPool.obtain(pCapacity);
		}
	}

	private void resize() {
		if (this.size() < this.mData.length - 1)
			return;
		Object[] a =
				BinaryObjectArrayPool.obtain(this.mData.length == 0 ? 2 : this.mData.length * 2);
		if (this.mTail <= this.mHead) {
			System.arraycopy(this.mData, this.mTail, a, this.mTail, this.mHead - this.mTail);
		} else {
			System.arraycopy(this.mData, 0, a, 0, this.mHead);
			System.arraycopy(this.mData, this.mTail, a, this.mData.length + this.mTail,
					this.mData.length - this.mTail);
			this.mTail += this.mData.length;
		}
		BinaryObjectArrayPool.recycle(this.mData);
		this.mData = a;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
