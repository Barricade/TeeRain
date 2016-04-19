package com.gaskarov.util.common;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class DataUtils {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private DataUtils() {
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

	public static void shortToByteArray(short pSrc, byte[] pDst, int pOffset) {
		pDst[pOffset] = (byte) (pSrc >>> 8);
		pDst[pOffset + 1] = (byte) pSrc;
	}

	public static void intToByteArray(int pSrc, byte[] pDst, int pOffset) {
		pDst[pOffset] = (byte) (pSrc >>> 24);
		pDst[pOffset + 1] = (byte) (pSrc >>> 16);
		pDst[pOffset + 2] = (byte) (pSrc >>> 8);
		pDst[pOffset + 3] = (byte) pSrc;
	}

	public static void longToByteArray(long pSrc, byte[] pDst, int pOffset) {
		pDst[pOffset] = (byte) (pSrc >>> 56);
		pDst[pOffset + 1] = (byte) (pSrc >>> 48);
		pDst[pOffset + 2] = (byte) (pSrc >>> 40);
		pDst[pOffset + 3] = (byte) (pSrc >>> 32);
		pDst[pOffset + 4] = (byte) (pSrc >>> 24);
		pDst[pOffset + 5] = (byte) (pSrc >>> 16);
		pDst[pOffset + 6] = (byte) (pSrc >>> 8);
		pDst[pOffset + 7] = (byte) pSrc;
	}

	public static short byteArrayToShort(byte[] pA, int pOffset) {
		return (short) ((short) pA[pOffset] << 8 | (short) pA[pOffset + 1]);
	}

	public static int byteArrayToInt(byte[] pA, int pOffset) {
		return (int) pA[pOffset] << 24 | (int) pA[pOffset + 1] << 16 | (int) pA[pOffset + 2] << 8
				| (int) pA[pOffset + 3];
	}

	public static long byteArrayToLong(byte[] pA, int pOffset) {
		return (long) pA[pOffset] << 56 | (long) pA[pOffset + 1] << 48
				| (long) pA[pOffset + 2] << 40 | (long) pA[pOffset + 3] << 32
				| (long) pA[pOffset + 4] << 24 | (long) pA[pOffset + 5] << 16
				| (long) pA[pOffset + 6] << 8 | (long) pA[pOffset + 7];
	}

	public static boolean equals(byte[] pL, int pLOffset, byte[] pR, int pROffset, int pSize) {
		for (int i = 0; i < pSize; ++i)
			if (pL[pLOffset + i] != pR[pROffset + i])
				return false;
		return true;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
