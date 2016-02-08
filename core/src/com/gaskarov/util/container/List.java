package com.gaskarov.util.container;

import com.gaskarov.util.constants.GlobalConstants;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class List {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mSize;
	private final Node mHead = Node.obtain();

	// ===========================================================
	// Constructors
	// ===========================================================

	private List() {
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

	private static List obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (List.class) {
				return sPool.size() == 0 ? new List() : (List) sPool.pop();
			}
		return new List();
	}

	private static void recyclePure(List pObj) {
		if (GlobalConstants.POOL)
			synchronized (List.class) {
				sPool.push(pObj);
			}
	}

	public static List obtain() {

		List obj = obtainPure();

		obj.mSize = 0;
		obj.mHead.mNext = obj.mHead.mPrev = obj.mHead;

		return obj;
	}

	public static void recycle(List pObj) {
		pObj.clear();
		recyclePure(pObj);
	}

	public int size() {
		return mSize;
	}

	public Node insert(Node pPrev, Object pVal) {
		Node node = Node.obtain(pVal, pPrev, pPrev.mNext);
		pPrev.mNext = node;
		node.mNext.mPrev = node;
		++mSize;
		return node;
	}

	public Node insertBefore(Node pNext, Object pVal) {
		Node node = Node.obtain(pVal, pNext.mPrev, pNext);
		pNext.mPrev = node;
		node.mPrev.mNext = node;
		++mSize;
		return node;
	}

	public Object erase(Node pNode) {
		pNode.mPrev.mNext = pNode.mNext;
		pNode.mNext.mPrev = pNode.mPrev;
		--mSize;
		Object val = pNode.mVal;
		Node.recycle(pNode);
		return val;
	}

	public Node push(Object pObj) {
		return insertBefore(mHead, pObj);
	}

	public Object pop() {
		return erase(mHead.mPrev);
	}

	public Node unshift(Object pObj) {
		return insert(mHead, pObj);
	}

	public Object shift() {
		return erase(mHead.mNext);
	}

	public Object back() {
		return mHead.mPrev.mVal;
	}

	public Object front() {
		return mHead.mNext.mVal;
	}

	public Node begin() {
		return mHead.mNext;
	}

	public Node end() {
		return mHead;
	}

	public Node next(Node pNode) {
		return pNode.mNext;
	}

	public Object val(Node pNode) {
		return pNode.mVal;
	}

	public void val(Node pNode, Object pVal) {
		pNode.mVal = pVal;
	}

	public void clear() {
		while (mSize > 0)
			pop();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static final class Node {

		private static final Array sPool = Array.obtain();

		private Object mVal;
		private Node mPrev;
		private Node mNext;

		private Node() {
		}

		private static Node obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (Node.class) {
					return sPool.size() == 0 ? new Node() : (Node) sPool.pop();
				}
			return new Node();
		}

		private static void recyclePure(Node pObj) {
			if (GlobalConstants.POOL)
				synchronized (Node.class) {
					sPool.push(pObj);
				}
		}

		public static Node obtain() {
			return obtain(null, null, null);
		}

		public static Node obtain(Object pVal, Node pPrev, Node pNext) {
			Node obj = obtainPure();

			obj.mVal = pVal;
			obj.mPrev = pPrev;
			obj.mNext = pNext;

			return obj;
		}

		public static void recycle(Node pObj) {
			pObj.mVal = null;
			pObj.mPrev = null;
			pObj.mNext = null;
			recyclePure(pObj);
		}

		@Override
		public boolean equals(Object pObj) {
			return mVal.equals(pObj);
		}

		@Override
		public int hashCode() {
			return mVal.hashCode();
		}
	}

}
