package com.gaskarov.util.common;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class MathUtils {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private MathUtils() {
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

	public static int ceilMaskPOT(int n) {
		n |= n >>> 1;
		n |= n >>> 2;
		n |= n >>> 4;
		n |= n >>> 8;
		n |= n >>> 16;
		return n;
	}

	public static int ceilPOT(int n) {
		return ceilMaskPOT(n - 1) + 1;
	}

	public static int floorPOT(int n) {
		return ceilMaskPOT(n) + 1 >>> 1;
	}

	public static int nextPOT(int n) {
		return ceilMaskPOT(n) + 1;
	}

	public static int prevPOT(int n) {
		return ceilMaskPOT(n - 1) + 1 >>> 1;
	}

	public static int log2(int n) {
		int m = 0;
		if (n >= 65536) {
			n >>>= 16;
			m = 16;
		}
		if (n >= 256) {
			n >>>= 8;
			m += 8;
		}
		if (n >= 16) {
			n >>>= 4;
			m += 4;
		}
		if (n >= 4) {
			n >>>= 2;
			m += 2;
		}
		return m + (n >>> 1);
	}

	public static strictfp int strictFloor(double pX) {
		return pX > 0 ? (int) pX : (int) pX - 1;
	}

	public static int floor(float pX) {
		return pX > 0 ? (int) pX : (int) pX - 1;
	}

	public static int ceil(float pX) {
		return pX > 0 ? (int) pX + 1 : (int) pX;
	}

	public static long floorToLong(float pX) {
		return pX > 0 ? (long) pX : (long) pX - 1;
	}

	public static long ceilToLong(float pX) {
		return pX > 0 ? (long) pX + 1 : (long) pX;
	}

	public static int round(float pX) {
		return floor(pX + 0.5f);
	}

	public static int divCeil(int pNum, int pDivisor) {
		boolean sign = pNum > 0 ^ pDivisor > 0;
		pNum = Math.abs(pNum);
		pDivisor = Math.abs(pDivisor);
		return sign ? -pNum / pDivisor : (pNum + pDivisor - 1) / pDivisor;
	}

	public static int divFloor(int pNum, int pDivisor) {
		boolean sign = pNum > 0 ^ pDivisor > 0;
		pNum = Math.abs(pNum);
		pDivisor = Math.abs(pDivisor);
		return sign ? (-pNum - pDivisor + 1) / pDivisor : pNum / pDivisor;
	}

	public static int mod(int pNum, int pDivisor) {
		int divisor = Math.abs(pDivisor);
		return (pNum % divisor + divisor) % divisor;
	}

	public static float mod(float pNum, float pDivisor) {
		return ((pNum % pDivisor) + pDivisor) % pDivisor;
	}

	public static int sign(int pN) {
		return pN > 0 ? 1 : pN < 0 ? -1 : 0;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
