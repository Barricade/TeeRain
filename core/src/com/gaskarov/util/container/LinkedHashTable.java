package com.gaskarov.util.container;

import com.gaskarov.util.constants.GlobalConstants;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class LinkedHashTable {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private HashTable mHashTable;
	private List mList;

	// ===========================================================
	// Constructors
	// ===========================================================

	private LinkedHashTable() {
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

	private static LinkedHashTable obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (LinkedHashTable.class) {
				return sPool.size() == 0 ? new LinkedHashTable() : (LinkedHashTable) sPool.pop();
			}
		return new LinkedHashTable();
	}

	private static void recyclePure(LinkedHashTable pObj) {
		if (GlobalConstants.POOL)
			synchronized (LinkedHashTable.class) {
				sPool.push(pObj);
			}
	}

	public static LinkedHashTable obtain() {

		LinkedHashTable obj = obtainPure();

		obj.mHashTable = HashTable.obtain();
		obj.mList = List.obtain();

		return obj;
	}

	public static void recycle(LinkedHashTable pObj) {
		HashTable.recycle(pObj.mHashTable);
		pObj.mHashTable = null;
		List.recycle(pObj.mList);
		pObj.mList = null;
		recyclePure(pObj);
	}

	public int size() {
		return this.mHashTable.size();
	}

	public Object get(Object pObj) {
		List.Node node = (List.Node) mHashTable.get(pObj);
		return node == null ? null : mList.val(node);
	}

	public Object set(Object pObj) {
		List.Node node = mList.push(pObj);
		node = (List.Node) mHashTable.set(node);
		return node != null ? mList.erase(node) : null;
	}

	public Object remove(Object pObj) {
		List.Node node = (List.Node) mHashTable.remove(pObj);
		return node != null ? mList.erase(node) : null;
	}

	public Object back() {
		return mList.back();
	}

	public Object front() {
		return mList.front();
	}

	public List.Node begin() {
		return mList.begin();
	}

	public List.Node end() {
		return mList.end();
	}

	public List.Node next(List.Node pNode) {
		return mList.next(pNode);
	}

	public Object val(List.Node pNode) {
		return mList.val(pNode);
	}

	public void clear() {
		while (mList.size() > 0) {
			mHashTable.remove(mList.begin());
			mList.shift();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
