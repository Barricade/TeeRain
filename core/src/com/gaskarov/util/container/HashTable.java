package com.gaskarov.util.container;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.pool.BinaryObjectArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class HashTable {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private Object[] mData;
	private int mSize;

	// ===========================================================
	// Constructors
	// ===========================================================

	private HashTable() {
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

	private static HashTable obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (HashTable.class) {
				return sPool.size() == 0 ? new HashTable() : (HashTable) sPool.pop();
			}
		return new HashTable();
	}

	private static void recyclePure(HashTable pObj) {
		if (GlobalConstants.POOL)
			synchronized (HashTable.class) {
				sPool.push(pObj);
			}
	}

	public static HashTable obtain() {

		HashTable obj = obtainPure();

		obj.mData = BinaryObjectArrayPool.ZERO_SIZE_ARRAY;
		obj.mSize = 0;

		return obj;
	}

	public static void recycle(HashTable pObj) {
		for (int i = 0; i < pObj.mData.length; ++i) {
			List.recycle((List) pObj.mData[i]);
			pObj.mData[i] = null;
		}
		BinaryObjectArrayPool.recycle(pObj.mData);
		pObj.mData = null;
		recyclePure(pObj);
	}

	public int size() {
		return this.mSize;
	}

	public Object get(Object pObj) {
		if (mData.length == 0)
			return null;
		int h = pObj.hashCode();
		List d = (List) this.mData[h & this.mData.length - 1];
		for (List.Node i = d.begin(); i != d.end(); i = d.next(i))
			if (d.val(i).equals(pObj))
				return d.val(i);
		return null;
	}

	public Object set(Object pObj) {
		this.resize();
		int h = pObj.hashCode();
		List d = (List) this.mData[h & this.mData.length - 1];
		for (List.Node i = d.begin(); i != d.end(); i = d.next(i))
			if (d.val(i).equals(pObj)) {
				Object last = d.val(i);
				d.val(i, pObj);
				return last;
			}
		++mSize;
		((List) this.mData[h & this.mData.length - 1]).push(pObj);
		return null;
	}

	public Object remove(Object pObj) {
		if (mData.length == 0)
			return null;
		int h = pObj.hashCode();
		List d = (List) this.mData[h & this.mData.length - 1];
		for (List.Node i = d.begin(); i != d.end(); i = d.next(i))
			if (d.val(i).equals(pObj)) {
				Object last = d.val(i);
				d.erase(i);
				--this.mSize;
				return last;
			}
		return null;
	}

	public void clear() {
		for (int i = 0; i < this.mData.length; ++i)
			((List) this.mData[i]).clear();
		this.mSize = 0;
	}

	private void resize() {
		if (this.mSize < this.mData.length / 2)
			return;
		Object[] a =
				BinaryObjectArrayPool.obtain(this.mData.length == 0 ? 1 : this.mData.length * 2);
		for (int i = 0; i < a.length; ++i)
			a[i] = List.obtain();
		for (int i = 0; i < this.mData.length; ++i) {
			List d = (List) this.mData[i];
			this.mData[i] = null;
			for (List.Node j = d.begin(); j != d.end(); j = d.next(j))
				((List) a[d.val(j).hashCode() & a.length - 1]).push(d.val(j));
			List.recycle(d);
		}
		BinaryObjectArrayPool.recycle(this.mData);
		this.mData = a;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
