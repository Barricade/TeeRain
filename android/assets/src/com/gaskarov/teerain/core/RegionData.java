package com.gaskarov.teerain.core;

import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.ByteArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class RegionData {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAPACITY_DATA = 2048;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private final ByteArray[] mData = new ByteArray[Settings.REGION_SQUARE];

	// ===========================================================
	// Constructors
	// ===========================================================

	private RegionData() {
		for (int i = 0; i < mData.length; ++i)
			mData[i] = ByteArray.obtain(CAPACITY_DATA);
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

	private static RegionData obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (RegionData.class) {
				return sPool.size() == 0 ? new RegionData() : (RegionData) sPool.pop();
			}
		return new RegionData();
	}

	private static void recyclePure(RegionData pObj) {
		if (GlobalConstants.POOL)
			synchronized (RegionData.class) {
				sPool.push(pObj);
			}
	}

	public static RegionData obtain() {
		RegionData obj = obtainPure();
		return obj;
	}

	public static void recycle(RegionData pObj) {
		for (int i = 0; i < pObj.mData.length; ++i)
			pObj.mData[i].clear(CAPACITY_DATA);
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
