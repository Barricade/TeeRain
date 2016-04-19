package com.gaskarov.util.common;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class IntVector1 {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	public int x;

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
		if (pObj == null)
			return false;
		if (!(pObj instanceof IntVector1))
			return pObj.equals(this);
		IntVector1 intVector1 = (IntVector1) pObj;
		return x == intVector1.x;
	}

	@Override
	public int hashCode() {
		return x + 373;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static IntVector1 obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (IntVector1.class) {
				return sPool.size() == 0 ? new IntVector1() : (IntVector1) sPool.pop();
			}
		return new IntVector1();
	}

	private static void recyclePure(IntVector1 pObj) {
		if (GlobalConstants.POOL)
			synchronized (IntVector1.class) {
				sPool.push(pObj);
			}
	}

	public static IntVector1 obtain(int pX) {
		IntVector1 obj = obtainPure();

		obj.set(pX);

		return obj;
	}

	public static void recycle(IntVector1 pObj) {
		recyclePure(pObj);
	}

	public IntVector1 cpy() {
		return obtain(x);
	}

	public IntVector1 set(int pX) {
		x = pX;
		return this;
	}

	public IntVector1 set(IntVector1 pObj) {
		return set(pObj.x);
	}

	public IntVector1 neg() {
		x = -x;
		return this;
	}

	public IntVector1 add(int pX) {
		x += pX;
		return this;
	}

	public IntVector1 add(IntVector1 pObj) {
		return add(pObj.x);
	}

	public IntVector1 sub(int pX) {
		x -= pX;
		return this;
	}

	public IntVector1 sub(IntVector1 pObj) {
		return sub(pObj.x);
	}

	public IntVector1 mul(int pK) {
		x *= pK;
		return this;
	}

	public IntVector1 div(int pK) {
		x /= pK;
		return this;
	}

	public IntVector1 mul(IntVector1 pObj) {
		return mul(pObj.x);
	}

	public IntVector1 div(IntVector1 pObj) {
		return div(pObj.x);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
