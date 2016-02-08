package com.gaskarov.util.common;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class NoiseMath {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private NoiseMath() {
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

	public static strictfp double normalizeHash(final long pX) {
		return (double) (pX & Long.MAX_VALUE) / Long.MAX_VALUE;
	}

	public static strictfp double perlinNoise(final long pSeed, final double pX) {

		final int ix = MathUtils.strictFloor(pX);
		final double fx = pX - ix;

		final double v1 = normalizeHash(combine(pSeed, ix));
		final double v2 = normalizeHash(combine(pSeed, ix + 1));

		return interpolate(v1, v2, fx);
	}

	public static strictfp double perlinNoise(final long pSeed, final double pX, final double pY) {

		final int ix = MathUtils.strictFloor(pX);
		final double fx = pX - ix;
		final int iy = MathUtils.strictFloor(pY);
		final double fy = pY - iy;

		final double v1 = normalizeHash(combine(pSeed, combine(ix, iy)));
		final double v2 = normalizeHash(combine(pSeed, combine(ix + 1, iy)));
		final double v3 = normalizeHash(combine(pSeed, combine(ix, iy + 1)));
		final double v4 = normalizeHash(combine(pSeed, combine(ix + 1, iy + 1)));

		final double i1 = interpolate(v1, v2, fx);
		final double i2 = interpolate(v3, v4, fx);

		return interpolate(i1, i2, fy);
	}

	public static strictfp double perlinNoise(final long pSeed, final double pX, final double pY,
			final double pZ) {

		final int ix = MathUtils.strictFloor(pX);
		final double fx = pX - ix;
		final int iy = MathUtils.strictFloor(pY);
		final double fy = pY - iy;
		final int iz = MathUtils.strictFloor(pZ);
		final double fz = pZ - iz;

		final long yz1 = combine(iy, iz);
		final double v1 = normalizeHash(combine(pSeed, combine(ix, yz1)));
		final double v2 = normalizeHash(combine(pSeed, combine(ix + 1, yz1)));
		final long yz2 = combine(iy + 1, iz);
		final double v3 = normalizeHash(combine(pSeed, combine(ix, yz2)));
		final double v4 = normalizeHash(combine(pSeed, combine(ix + 1, yz2)));
		final long yz3 = combine(iy, iz + 1);
		final double v5 = normalizeHash(combine(pSeed, combine(ix, yz3)));
		final double v6 = normalizeHash(combine(pSeed, combine(ix + 1, yz3)));
		final long yz4 = combine(iy + 1, iz + 1);
		final double v7 = normalizeHash(combine(pSeed, combine(ix, yz4)));
		final double v8 = normalizeHash(combine(pSeed, combine(ix + 1, yz4)));

		final double i1 = interpolate(v1, v2, fx);
		final double i2 = interpolate(v3, v4, fx);
		final double i3 = interpolate(v5, v6, fx);
		final double i4 = interpolate(v7, v8, fx);

		final double j1 = interpolate(i1, i2, fy);
		final double j2 = interpolate(i3, i4, fy);

		return interpolate(j1, j2, fz);
	}

	public static strictfp double perlinOctaveNoise(final long pSeed, final double pX,
			final double pFrequency, final double pAmplitude, final int pOctaves) {

		double result = 0;
		double amp = 1.0;
		double freq = 1.0;
		double max = 0;

		for (int i = 0; i < pOctaves; ++i) {
			result += perlinNoise(combine(pSeed, i), pX * freq) * amp;
			max += amp;
			freq *= pFrequency;
			amp *= pAmplitude;
		}

		return result / max;
	}

	public static strictfp double perlinOctaveNoise(final long pSeed, final double pX,
			final double pY, final double pFrequency, final double pAmplitude, final int pOctaves) {

		double result = 0;
		double amp = 1.0;
		double freq = 1.0;
		double max = 0;

		for (int i = 0; i < pOctaves; ++i) {
			result += perlinNoise(combine(pSeed, i), pX * freq, pY * freq) * amp;
			max += amp;
			freq *= pFrequency;
			amp *= pAmplitude;
		}

		return result / max;
	}

	public static strictfp double perlinOctaveNoise(final long pSeed, final double pX,
			final double pY, final double pZ, final double pFrequency, final double pAmplitude,
			final int pOctaves) {

		double result = 0;
		double amp = 1.0;
		double freq = 1.0;
		double max = 0;

		for (int i = 0; i < pOctaves; ++i) {
			result += perlinNoise(combine(pSeed, i), pX * freq, pY * freq, pZ * freq) * amp;
			max += amp;
			freq *= pFrequency;
			amp *= pAmplitude;
		}

		return result / max;
	}

	public static long hash(long pKey) {
		pKey = (~pKey) + (pKey << 21); // key = (key << 21) - key - 1;
		pKey = pKey ^ (pKey >>> 24);
		pKey = (pKey + (pKey << 3)) + (pKey << 8); // key * 265
		pKey = pKey ^ (pKey >>> 14);
		pKey = (pKey + (pKey << 2)) + (pKey << 4); // key * 21
		pKey = pKey ^ (pKey >>> 28);
		pKey = pKey + (pKey << 31);
		return pKey;
	}

	public static long hashLCG(final long pX) {
		return pX * 6364136223846793005l + 1442695040888963407l;
	}

	public static long hashDown(final long pX) {
		final long x = pX ^ (pX >>> 20) ^ (pX >>> 12);
		return x ^ (x >>> 7) ^ (x >>> 4);
	}

	public static long hashUp(final long pX) {
		final long x = pX ^ (pX << 20) ^ (pX << 12);
		return x ^ (x << 7) ^ (x << 4);
	}

	public static long combine(final long pX, final long pY) {
		return hash(hash(pX) ^ pY);
	}

	public static strictfp double interpolate(final double pA, final double pB, final double pX) {
		final double x = pX * pX * pX * (pX * (pX * 6.0 - 15.0) + 10.0);
		return pA * (1.0 - x) + pB * x;
	}

	public static strictfp double interpolateLinear(final double pA, final double pB,
			final double pX) {
		return pA * (1.0 - pX) + pB * pX;
	}

	public static strictfp double interpolateCosine(final double pA, final double pB,
			final double pX) {
		final double x = (1.0 - StrictMath.cos(pX * Math.PI)) * 0.5;
		return pA * (1.0 - x) + pB * x;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
