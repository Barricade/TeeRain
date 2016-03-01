package com.gaskarov.teerain;

import com.gaskarov.teerain.cell.Cell;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.LinkedIntTable;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Player {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int MAX_ITEMS = 9;
	public static final int SPECIAL_ITEM_ID = 0;
	public static final int USE_ITEM_MIN_ID = 1;
	public static final int USE_ITEM_MAX_ID = 8;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mUseItem;
	private final Cell[] mItems = new Cell[MAX_ITEMS];
	private final LinkedIntTable mItemsKeys = LinkedIntTable.obtain(MAX_ITEMS);

	// ===========================================================
	// Constructors
	// ===========================================================

	private Player() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getUseItem() {
		return mUseItem;
	}

	public void setUseItem(int pUseItem) {
		mUseItem = pUseItem;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	private static Player obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (Player.class) {
				return sPool.size() == 0 ? new Player() : (Player) sPool.pop();
			}
		return new Player();
	}

	private static void recyclePure(Player pObj) {
		if (GlobalConstants.POOL)
			synchronized (Player.class) {
				sPool.push(pObj);
			}
	}

	public static Player obtain() {
		Player obj = obtainPure();
		obj.mUseItem = USE_ITEM_MIN_ID;
		return obj;
	}

	public static void recycle(Player pObj) {
		while (pObj.mItemsKeys.size() > 0) {
			int id = pObj.mItemsKeys.pop();
			pObj.mItems[id].recycle();
			pObj.mItems[id] = null;
		}
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
