package com.gaskarov.teerain.debug;

import com.badlogic.gdx.Gdx;

/**
 * Copyright (c) 2014 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class TimeMeasure {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	public static long sMainThreadId;

	public static long sLast = 0;
	public static Measure sM1 = new Measure("M1");
	public static Measure sM2 = new Measure("M2");
	public static Measure sM3 = new Measure("M3");
	public static Measure sM4 = new Measure("M4");
	public static Measure sM5 = new Measure("M5");
	public static Measure sM6 = new Measure("M6");
	public static Measure sM7 = new Measure("M7");
	public static Measure sM8 = new Measure("M8");
	public static Measure sM9 = new Measure("M9");
	public static Measure sM10 = new Measure("M10");
	public static Measure sM11 = new Measure("M11");
	public static Measure sM12 = new Measure("M12");
	public static Measure sM13 = new Measure("M13");
	public static Measure sM14 = new Measure("M14");
	public static Measure sM15 = new Measure("M15");
	public static Measure sM16 = new Measure("M16");

	// ===========================================================
	// Constructors
	// ===========================================================

	private TimeMeasure() {
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

	public static synchronized void log() {
		long cur = System.currentTimeMillis();
		if (cur - sLast >= 10000) {
			sLast = cur;
			sM1.log();
			sM2.log();
			sM3.log();
			sM4.log();
			sM5.log();
			sM6.log();
			sM7.log();
			sM8.log();
			sM9.log();
			sM10.log();
			sM11.log();
			sM12.log();
			sM13.log();
			sM14.log();
			sM15.log();
			sM16.log();
			Gdx.app.log("END", "");
		}
	}

	public static synchronized void start() {
		sM1.globalStart();
		sM2.globalStart();
		sM3.globalStart();
		sM4.globalStart();
		sM5.globalStart();
		sM6.globalStart();
		sM7.globalStart();
		sM8.globalStart();
		sM9.globalStart();
		sM10.globalStart();
		sM11.globalStart();
		sM12.globalStart();
	}

	public static synchronized void end() {
		sM1.globalEnd();
		sM2.globalEnd();
		sM3.globalEnd();
		sM4.globalEnd();
		sM5.globalEnd();
		sM6.globalEnd();
		sM7.globalEnd();
		sM8.globalEnd();
		sM9.globalEnd();
		sM10.globalEnd();
		sM11.globalEnd();
		sM12.globalEnd();
	}

	public static synchronized void start2() {
		sM13.globalStart();
		sM14.globalStart();
		sM15.globalStart();
		sM16.globalStart();
	}

	public static synchronized void end2() {
		sM13.globalEnd();
		sM14.globalEnd();
		sM15.globalEnd();
		sM16.globalEnd();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class Measure {

		public long mMax;
		public long mMin;
		public long mNum;
		public long mTime;
		public long mStart;
		public String mTag;

		public long mBigNum;
		public long mTotalNum;
		public long mMaxNum;
		public long mMinNum;
		public long mTotalTime;
		public long mMaxTime;
		public long mMinTime;

		public Measure(String pString) {
			mTag = pString;
		}

		public void start() {
			// if (Thread.currentThread().getId() == TimeMeasure.sMainThreadId)
			synchronized (TimeMeasure.class) {
				mStart = System.nanoTime();
			}
		}

		public void end() {
			// if (Thread.currentThread().getId() == TimeMeasure.sMainThreadId)
			// {
			synchronized (TimeMeasure.class) {
				long cur = System.nanoTime() - mStart;
				mMax = Math.max(mMax, cur);
				mMin = Math.min(mMin, cur);
				++mNum;
				mTime += cur;
			}
			// }
		}

		public void globalStart() {
			mTime = 0;
			mMin = Long.MAX_VALUE;
			mMax = Long.MIN_VALUE;
			mNum = 0;
		}

		public void globalEnd() {
			mTotalNum += mNum;
			mMaxNum = Math.max(mMaxNum, mNum);
			mMinNum = Math.min(mMinNum, mNum);
			mNum = 0;
			mTotalTime += mTime;
			mMaxTime = Math.max(mMaxTime, mTime);
			mMinTime = Math.min(mMinTime, mTime);
			mTime = 0;
			++mBigNum;
		}

		public void log() {
			Gdx.app.log("", mTag + ":[A:" + (mBigNum == 0 ? 0 : mTotalTime / mBigNum / 1000000f)
					+ ",Mx:" + mMaxTime / 1000000 + ",Mn:" + mMinTime / 1000000 + ",NA:"
					+ (mBigNum == 0 ? 0 : mTotalNum / mBigNum) + ",NMx:" + mMaxNum + ",NMn:"
					+ mMinNum + "]");
			mTotalNum = 0;
			mMaxNum = Long.MIN_VALUE;
			mMinNum = Long.MAX_VALUE;
			mTotalTime = 0;
			mMaxTime = Long.MIN_VALUE;
			mMinTime = Long.MAX_VALUE;
			mBigNum = 0;
		}
	}

}
