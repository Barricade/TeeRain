package com.gaskarov.teerain.game;

import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.Cellularity.CellData;
import com.gaskarov.teerain.game.game.ControlOrganoid;
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
	private final int[] mItems = new int[MAX_ITEMS];
	private final CellData[] mItemsData = new CellData[MAX_ITEMS];
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
		for (int i = 0; i < MAX_ITEMS; ++i)
			obj.mItems[i] = Cells.CELL_TYPE_VACUUM;
		return obj;
	}

	public static void recycle(Player pObj) {
		while (pObj.mItemsKeys.size() > 0) {
			int id = pObj.mItemsKeys.pop();
			if (pObj.mItemsData[id] != null) {
				pObj.mItemsData[id].recycle();
				pObj.mItemsData[id] = null;
			}
		}
		recyclePure(pObj);
	}

	public void control(Cellularity pCellularity, int pX, int pY, int pZ,
			ControlOrganoid pControlOrganoid) {
		pControlOrganoid.setSpecialItem(mItems[SPECIAL_ITEM_ID], mItemsData[SPECIAL_ITEM_ID]);
		pControlOrganoid.setUseItem(mItems[mUseItem], mItemsData[mUseItem]);
	}

	public int getItem(int pId) {
		return mItems[pId];
	}

	public CellData getItemData(int pId) {
		return mItemsData[pId];
	}

	public void setItem(int pId, int pItem, CellData pItemData) {
		mItems[pId] = pItem;
		mItemsData[pId] = pItemData;
		if (pItem != Cells.CELL_TYPE_VACUUM)
			mItemsKeys.set(pId);
		else
			mItemsKeys.remove(pId);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
