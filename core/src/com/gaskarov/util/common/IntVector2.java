package com.gaskarov.util.common;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class IntVector2 {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	public int x;
	public int y;

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
		if (!(pObj instanceof IntVector2))
			return pObj.equals(this);
		IntVector2 intVector2 = (IntVector2) pObj;
		return x == intVector2.x && y == intVector2.y;
	}

	@Override
	public int hashCode() {
		return (x + 373) * 71 + y;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static IntVector2 obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (IntVector2.class) {
				return sPool.size() == 0 ? new IntVector2() : (IntVector2) sPool.pop();
			}
		return new IntVector2();
	}

	private static void recyclePure(IntVector2 pObj) {
		if (GlobalConstants.POOL)
			synchronized (IntVector2.class) {
				sPool.push(pObj);
			}
	}

	public static IntVector2 obtain(int pX, int pY) {

		IntVector2 obj = obtainPure();

		obj.set(pX, pY);

		return obj;
	}

	public static void recycle(IntVector2 pObj) {
		recyclePure(pObj);
	}

	public IntVector2 cpy() {
		return obtain(x, y);
	}

	public IntVector2 set(int pX, int pY) {
		x = pX;
		y = pY;
		return this;
	}

	public IntVector2 set(IntVector2 pIntVector2) {
		return set(pIntVector2.x, pIntVector2.y);
	}

	public IntVector2 neg() {
		x = -x;
		y = -y;
		return this;
	}

	public IntVector2 add(int pX, int pY) {
		x += pX;
		y += pY;
		return this;
	}

	public IntVector2 add(IntVector2 pIntVector2) {
		return add(pIntVector2.x, pIntVector2.y);
	}

	public IntVector2 sub(int pX, int pY) {
		x -= pX;
		y -= pY;
		return this;
	}

	public IntVector2 sub(IntVector2 pIntVector2) {
		return sub(pIntVector2.x, pIntVector2.y);
	}

	public IntVector2 mul(int pK) {
		x *= pK;
		y *= pK;
		return this;
	}

	public IntVector2 div(int pK) {
		x /= pK;
		y /= pK;
		return this;
	}

	public IntVector2 mul(int pX, int pY) {
		x *= pX;
		y *= pY;
		return this;
	}

	public IntVector2 mul(IntVector2 pIntVector2) {
		return mul(pIntVector2.x, pIntVector2.y);
	}

	public IntVector2 div(int pX, int pY) {
		x /= pX;
		y /= pY;
		return this;
	}

	public IntVector2 div(IntVector2 pIntVector2) {
		return div(pIntVector2.x, pIntVector2.y);
	}

	public long dot(long pX, long pY) {
		return x * pX + y * pY;
	}

	public long dot(IntVector2 pIntVector2) {
		return dot(pIntVector2.x, pIntVector2.y);
	}

	public long crs(long pX, long pY) {
		return x * pY - y * pX;
	}

	public long crs(IntVector2 pIntVector2) {
		return crs(pIntVector2.x, pIntVector2.y);
	}

	public static long len2(long pX, long pY) {
		return pX * pX + pY * pY;
	}

	public long len2() {
		return len2(x, y);
	}

	public long dst2(long pX, long pY) {
		return len2(x - pX, y - pY);
	}

	public long dst2(IntVector2 pIntVector2) {
		return dst2(pIntVector2.x, pIntVector2.y);
	}

	public IntVector2 rot90() {
		return set(-y, x);
	}

	public IntVector2 rot270() {
		return set(y, -x);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
