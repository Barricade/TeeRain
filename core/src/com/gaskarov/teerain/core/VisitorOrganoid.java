package com.gaskarov.teerain.core;

import com.badlogic.gdx.math.Vector2;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class VisitorOrganoid {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private boolean mIsEnabled;
	private boolean mIsVisitor;
	private int mVisitorX;
	private int mVisitorY;
	private int mVisitorWidth;
	private int mVisitorHeight;

	// ===========================================================
	// Constructors
	// ===========================================================

	private VisitorOrganoid() {
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

	private static VisitorOrganoid obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (VisitorOrganoid.class) {
				return sPool.size() == 0 ? new VisitorOrganoid() : (VisitorOrganoid) sPool.pop();
			}
		return new VisitorOrganoid();
	}

	private static void recyclePure(VisitorOrganoid pObj) {
		if (GlobalConstants.POOL)
			synchronized (VisitorOrganoid.class) {
				sPool.push(pObj);
			}
	}

	public static VisitorOrganoid obtain(boolean pIsEnabled) {
		VisitorOrganoid obj = obtainPure();
		obj.mIsEnabled = pIsEnabled;
		obj.mIsVisitor = false;
		obj.mVisitorX = 0;
		obj.mVisitorY = 0;
		obj.mVisitorWidth = 0;
		obj.mVisitorHeight = 0;
		return obj;
	}

	public static void recycle(VisitorOrganoid pObj) {
		recyclePure(pObj);
	}

	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mIsEnabled) {
			if (!pCellularity.isChunk())
				pCellularity.tickEnable(pX, pY, pZ);
		}
	}

	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mIsEnabled) {
			if (!pCellularity.isChunk())
				pCellularity.tickDisable(pX, pY, pZ);
		}
	}

	public void tissularedAttach(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mIsEnabled)
			pushVisitor(pCellularity, pX, pY, pZ);
	}

	public void tissularedDetach(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mIsEnabled)
			removeVisitor(pCellularity.getTissularity());
	}

	public void tick(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mIsEnabled && !pCellularity.isChunk())
			pushVisitor(pCellularity, pX, pY, pZ);
	}

	public void setIsEnabled(Cellularity pCellularity, int pX, int pY, int pZ, boolean pIsEnabled) {
		if (mIsEnabled == pIsEnabled)
			return;
		mIsEnabled = pIsEnabled;
		if (pCellularity == null)
			return;
		Tissularity tissularity = pCellularity.getTissularity();
		if (mIsEnabled) {
			if (tissularity != null)
				pushVisitor(pCellularity, pX, pY, pZ);
			if (!pCellularity.isChunk())
				pCellularity.tickEnable(pX, pY, pZ);
		} else {
			if (!pCellularity.isChunk())
				pCellularity.tickDisable(pX, pY, pZ);
			if (tissularity != null)
				removeVisitor(tissularity);
		}
	}

	private void pushVisitor(Cellularity pCellularity, int pX, int pY, int pZ) {
		Tissularity tissularity = pCellularity.getTissularity();

		Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
		float x = p.x;
		float y = p.y;

		int lx = pCellularity.localToGlobalX(MathUtils.floor(x) - (Settings.VISITOR_WIDTH + 1) / 2);
		int ly =
				pCellularity.localToGlobalY(MathUtils.floor(y) - (Settings.VISITOR_HEIGHT + 1) / 2);
		int rx = pCellularity.localToGlobalX(MathUtils.ceil(x) + (Settings.VISITOR_WIDTH - 1) / 2);
		int ry = pCellularity.localToGlobalY(MathUtils.ceil(y) + (Settings.VISITOR_HEIGHT - 1) / 2);
		int width = rx - lx + 1;
		int height = ry - ly + 1;
		if (mIsVisitor)
			tissularity.moveVisitor(mVisitorX, mVisitorY, mVisitorWidth, mVisitorHeight, lx, ly,
					width, height);
		else {
			tissularity.addVisitor(lx, ly, width, height);
			tissularity.pushVisitor(this, pCellularity, pX, pY, pZ);
		}
		mIsVisitor = true;
		mVisitorX = lx;
		mVisitorY = ly;
		mVisitorWidth = width;
		mVisitorHeight = height;
	}

	private void removeVisitor(Tissularity pTissularity) {
		if (mIsVisitor) {
			pTissularity.removeVisitor(this);
			pTissularity.removeVisitor(mVisitorX, mVisitorY, mVisitorWidth, mVisitorHeight);
			mIsVisitor = false;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
