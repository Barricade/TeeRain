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
public final class BinaryByteArrayPool {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final byte[] ZERO_SIZE_ARRAY = new byte[0];

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

	private BinaryByteArrayPool() {
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

	public static byte[] obtainPOT(int pSizePOT) {
		if (GlobalConstants.POOL)
			synchronized (BinaryByteArrayPool.class) {
				if (sPools[pSizePOT].size() == 0)
					return new byte[1 << pSizePOT];
				return (byte[]) sPools[pSizePOT].pop();
			}
		return new byte[1 << pSizePOT];
	}

	public static byte[] obtain(int pSize) {
		return pSize == 0 ? ZERO_SIZE_ARRAY : obtainPOT(pSize == 1 ? 0
				: MathUtils.log2(pSize - 1) + 1);
	}

	public static void recyclePOT(byte[] pArr, int pSizePOT) {
		if (GlobalConstants.POOL)
			synchronized (BinaryByteArrayPool.class) {
				sPools[pSizePOT].push(pArr);
			}
	}

	public static void recycle(byte[] pArr) {
		if (pArr.length != 0)
			recyclePOT(pArr, MathUtils.log2(pArr.length));
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
