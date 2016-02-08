package com.gaskarov.util.common;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class KeyValuePair {

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

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean equals(Object pObj) {
		return pObj.equals(mA);
	}

	@Override
	public int hashCode() {
		return mA.hashCode();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static KeyValuePair obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (KeyValuePair.class) {
				return sPool.size() == 0 ? new KeyValuePair() : (KeyValuePair) sPool.pop();
			}
		return new KeyValuePair();
	}

	private static void recyclePure(KeyValuePair pObj) {
		if (GlobalConstants.POOL)
			synchronized (KeyValuePair.class) {
				sPool.push(pObj);
			}
	}

	public static KeyValuePair obtain(Object pA, Object pB) {

		KeyValuePair obj = obtainPure();

		obj.set(pA, pB);

		return obj;
	}

	public static void recycle(KeyValuePair pObj) {
		pObj.mA = null;
		pObj.mB = null;
		recyclePure(pObj);
	}

	public KeyValuePair set(Object pA, Object pB) {
		mA = pA;
		mB = pB;
		return this;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
