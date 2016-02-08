package com.gaskarov.util.common;

import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class IntVector3 {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	public int x;
	public int y;
	public int z;

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
		if (!(pObj instanceof IntVector3))
			return pObj.equals(this);
		IntVector3 intVector3 = (IntVector3) pObj;
		return x == intVector3.x && y == intVector3.y && z == intVector3.z;
	}

	@Override
	public int hashCode() {
		return ((x + 373) * 71 + y) * 71 + z;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static IntVector3 obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (IntVector3.class) {
				return sPool.size() == 0 ? new IntVector3() : (IntVector3) sPool.pop();
			}
		return new IntVector3();
	}

	private static void recyclePure(IntVector3 pObj) {
		if (GlobalConstants.POOL)
			synchronized (IntVector3.class) {
				sPool.push(pObj);
			}
	}

	public static IntVector3 obtain(int pX, int pY, int pZ) {

		IntVector3 obj = obtainPure();

		obj.set(pX, pY, pZ);

		return obj;
	}

	public static void recycle(IntVector3 pObj) {
		recyclePure(pObj);
	}

	public IntVector3 cpy() {
		return obtain(x, y, z);
	}

	public IntVector3 set(int pX, int pY, int pZ) {
		x = pX;
		y = pY;
		z = pZ;
		return this;
	}

	public IntVector3 set(IntVector3 pIntVector3) {
		return set(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public IntVector3 neg() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public IntVector3 add(int pX, int pY, int pZ) {
		x += pX;
		y += pY;
		z += pZ;
		return this;
	}

	public IntVector3 add(IntVector3 pIntVector3) {
		return add(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public IntVector3 sub(int pX, int pY, int pZ) {
		x -= pX;
		y -= pY;
		z -= pZ;
		return this;
	}

	public IntVector3 sub(IntVector3 pIntVector3) {
		return sub(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public IntVector3 mul(int pK) {
		x *= pK;
		y *= pK;
		z *= pK;
		return this;
	}

	public IntVector3 div(int pK) {
		x /= pK;
		y /= pK;
		z /= pK;
		return this;
	}

	public IntVector3 mul(int pX, int pY, int pZ) {
		x *= pX;
		y *= pY;
		z *= pZ;
		return this;
	}

	public IntVector3 mul(IntVector3 pIntVector3) {
		return mul(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public IntVector3 div(int pX, int pY, int pZ) {
		x /= pX;
		y /= pY;
		z /= pZ;
		return this;
	}

	public IntVector3 div(IntVector3 pIntVector3) {
		return div(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public long dot(long pX, long pY, long pZ) {
		return x * pX + y * pY + z * pZ;
	}

	public long dot(IntVector3 pIntVector3) {
		return dot(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public IntVector3 crs(int pX, int pY, int pZ) {
		return set(y * pZ - z * pY, z * pX - x * pZ, x * pY - y * pX);
	}

	public IntVector3 crs(IntVector3 pIntVector3) {
		return crs(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public static long len2(long pX, long pY, long pZ) {
		return pX * pX + pY * pY + pZ * pZ;
	}

	public long len2() {
		return len2(x, y, z);
	}

	public long dst2(long pX, long pY, long pZ) {
		return len2(x - pX, y - pY, z - pZ);
	}

	public long dst2(IntVector3 pIntVector3) {
		return dst2(pIntVector3.x, pIntVector3.y, pIntVector3.z);
	}

	public IntVector3 rot90Z() {
		return set(-y, x, z);
	}

	public IntVector3 rot270Z() {
		return set(y, -x, z);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
