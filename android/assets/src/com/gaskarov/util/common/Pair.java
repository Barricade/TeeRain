package com.gaskarov.util.common;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Pair {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	public Object mA;
	public Object mB;

	// ===========================================================
	// Constructors
	// ===========================================================

	private Pair() {
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

	private static Pair obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (Pair.class) {
				return sPool.size() == 0 ? new Pair() : (Pair) sPool.pop();
			}
		return new Pair();
	}

	private static void recyclePure(Pair pObj) {
		if (GlobalConstants.POOL)
			synchronized (Pair.class) {
				sPool.push(pObj);
			}
	}

	public static Pair obtain(Object pA, Object pB) {

		Pair obj = obtainPure();

		obj.set(pA, pB);

		return obj;
	}

	public static void recycle(Pair pObj) {
		pObj.mA = null;
		pObj.mB = null;
		recyclePure(pObj);
	}

	public Pair set(Object pA, Object pB) {
		mA = pA;
		mB = pB;
		return this;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
