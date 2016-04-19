package com.gaskarov.util.pool;

import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.DataConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class BinaryIntArrayPool {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int[] ZERO_SIZE_ARRAY = new int[0];

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array[] sPools = new Array[DataConstants.BITS_PER_INT];

	// ===========================================================
	// Constructors
	// ===========================================================

	static {
		for (int i = 0; i < sPools.length; ++i)
			sPools[i] = Array.obtain();
	}

	private BinaryIntArrayPool() {
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

	public static int[] obtainPOT(int pSizePOT) {
		if (GlobalConstants.POOL)
			synchronized (BinaryIntArrayPool.class) {
				if (sPools[pSizePOT].size() == 0)
					return new int[1 << pSizePOT];
				return (int[]) sPools[pSizePOT].pop();
			}
		return new int[1 << pSizePOT];
	}

	public static int[] obtain(int pSize) {
		return pSize == 0 ? ZERO_SIZE_ARRAY : obtainPOT(pSize == 1 ? 0
				: MathUtils.log2(pSize - 1) + 1);
	}

	public static void recyclePOT(int[] pArr, int pSizePOT) {
		if (GlobalConstants.POOL)
			synchronized (BinaryIntArrayPool.class) {
				sPools[pSizePOT].push(pArr);
			}
	}

	public static void recycle(int[] pArr) {
		if (pArr.length != 0)
			recyclePOT(pArr, MathUtils.log2(pArr.length));
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
