package com.gaskarov.util.common;

import com.gaskarov.util.pool.BinaryByteArrayPool;
import com.gaskarov.util.pool.BinaryFloatArrayPool;
import com.gaskarov.util.pool.BinaryIntArrayPool;
import com.gaskarov.util.pool.BinaryObjectArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class ArrayUtils {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private ArrayUtils() {
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

	public static Object[] obtainArray2(int pX, int pY) {
		Object[] arr = BinaryObjectArrayPool.obtain(pY);
		for (int i = 0; i < pY; ++i)
			arr[i] = BinaryObjectArrayPool.obtain(pX);
		return arr;
	}

	public static void recycleArray2(Object[] pArr, int pX, int pY) {
		for (int i = 0; i < pY; ++i)
			BinaryObjectArrayPool.recycle((Object[]) pArr[i]);
		BinaryObjectArrayPool.recycle(pArr);
	}

	public static Object[] obtainArray3(int pX, int pY, int pZ) {
		Object[] arr = BinaryObjectArrayPool.obtain(pZ);
		for (int i = 0; i < pZ; ++i) {
			arr[i] = BinaryObjectArrayPool.obtain(pY);
			for (int j = 0; j < pY; ++j)
				((Object[]) arr[i])[j] = BinaryObjectArrayPool.obtain(pX);
		}
		return arr;
	}

	public static void recycleArray3(Object[] pArr, int pX, int pY, int pZ) {
		for (int i = 0; i < pZ; ++i) {
			for (int j = 0; j < pY; ++j)
				BinaryObjectArrayPool.recycle((Object[]) ((Object[]) pArr[i])[j]);
			BinaryObjectArrayPool.recycle((Object[]) pArr[i]);
		}
		BinaryObjectArrayPool.recycle(pArr);
	}

	public static Object[] copyOf(Object[] pOriginal, int pLength) {
		Object[] oCopy = BinaryObjectArrayPool.obtain(pLength);
		System.arraycopy(pOriginal, 0, oCopy, 0, pOriginal.length);
		return oCopy;
	}

	public static float[] copyOf(float[] pOriginal, int pLength) {
		float[] oCopy = BinaryFloatArrayPool.obtain(pLength);
		System.arraycopy(pOriginal, 0, oCopy, 0, pOriginal.length);
		return oCopy;
	}

	public static int[] copyOf(int[] pOriginal, int pLength) {
		int[] oCopy = BinaryIntArrayPool.obtain(pLength);
		System.arraycopy(pOriginal, 0, oCopy, 0, pOriginal.length);
		return oCopy;
	}

	public static byte[] copyOf(byte[] pOriginal, int pLength) {
		byte[] oCopy = BinaryByteArrayPool.obtain(pLength);
		System.arraycopy(pOriginal, 0, oCopy, 0, pOriginal.length);
		return oCopy;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
