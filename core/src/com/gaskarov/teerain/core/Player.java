package com.gaskarov.teerain.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.badlogic.gdx.math.Vector2;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.Cellularity.CellData;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.cellularity.DynamicCellularity;
import com.gaskarov.teerain.game.TerrainGenerator;
import com.gaskarov.teerain.resource.Cells;
import com.gaskarov.teerain.resource.CellsAction;
import com.gaskarov.teerain.resource.ControlOrganoid;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.teerain.resource.TeeCellData;
import com.gaskarov.util.common.IntVector1;
import com.gaskarov.util.common.KeyValuePair;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.IntArray;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.LinkedIntTable;
import com.gaskarov.util.container.List;
import com.gaskarov.util.pool.BinaryByteArrayPool;

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

	public static final int MAX_ITEMS = 38;
	public static final int SPECIAL_ITEM_ID = 0;
	public static final int USE_ITEM_MIN_ID = 1;
	public static final int USE_ITEM_MAX_ID = 9;
	public static final int ARMOR_ITEM_ID = 10;
	public static final int FOOT_ARMOR_ITEM_ID = 11;
	public static final int HAND_ARMOR_ITEM_ID = 12;
	public static final int CRAFT_ITEM_MIN_ID = 13;
	public static final int CRAFT_ITEM_MAX_ID = 16;
	public static final int CRAFT_OUT_ITEM_MIN_ID = 17;
	public static final int CRAFT_OUT_ITEM_MAX_ID = 18;
	public static final int SELECT_ITEM_ID = 19;
	public static final int INVENTORY_ITEM_MIN_ID = 20;
	public static final int INVENTORY_ITEM_MAX_ID = 37;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private final IntVector1 mVec1 = IntVector1.obtain(0);

	private boolean mMob;
	private int mId;

	private Tissularity mTissularity;
	private boolean mVisitor;
	private int mVisitorX;
	private int mVisitorY;
	private boolean mActive;
	private boolean mDead;
	private int mChunkX;
	private int mChunkY;
	private float mPositionX;
	private float mPositionY;
	private float mVelocityX;
	private float mVelocityY;

	private int mUseItem;
	private final int[] mItems = new int[MAX_ITEMS];
	private final CellData[] mItemsData = new CellData[MAX_ITEMS];
	private final int[] mItemsCount = new int[MAX_ITEMS];
	private final LinkedIntTable mItemsKeys = LinkedIntTable.obtain(MAX_ITEMS);
	private IntArray mDropItems;
	private Array mDropItemsData;
	private IntArray mDropItemsCount;

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

	public IntArray getDropItems() {
		return mDropItems;
	}

	public Array getDropItemsData() {
		return mDropItemsData;
	}

	public IntArray getDropItemsCount() {
		return mDropItemsCount;
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

	public static Player obtain(int pId, boolean pMob) {
		Player obj = obtainPure();

		obj.mId = pId;
		obj.mMob = pMob;

		obj.mTissularity = null;
		obj.mVisitor = false;
		obj.mActive = false;
		obj.mDead = true;

		obj.mUseItem = USE_ITEM_MIN_ID;
		for (int i = 0; i < MAX_ITEMS; ++i)
			obj.mItems[i] = Cells.CELL_TYPE_VACUUM;
		obj.mDropItems = IntArray.obtain();
		obj.mDropItemsData = Array.obtain();
		obj.mDropItemsCount = IntArray.obtain();
		return obj;
	}

	public static void recycle(Player pObj) {
		while (pObj.mItemsKeys.size() > 0) {
			int id = pObj.mItemsKeys.pop();
			pObj.mItemsCount[id] = 0;
			if (pObj.mItemsData[id] != null) {
				pObj.mItemsData[id].recycle();
				pObj.mItemsData[id] = null;
			}
		}
		IntArray.recycle(pObj.mDropItems);
		pObj.mDropItems = null;
		while (pObj.mDropItemsData.size() > 0) {
			CellData cellData = (CellData) pObj.mDropItemsData.pop();
			if (cellData != null)
				cellData.recycle();
		}
		Array.recycle(pObj.mDropItemsData);
		pObj.mDropItemsData = null;
		IntArray.recycle(pObj.mDropItemsCount);
		pObj.mDropItemsCount = null;
		recyclePure(pObj);
	}

	public void attach(Tissularity pTissularity) {
		mTissularity = pTissularity;
	}

	public void detach() {
		if (!mMob) {
			if (mVisitor) {
				mVisitor = false;
				mTissularity.removeVisitor(mVisitorX, mVisitorY, 1, 1);
			}
			if (mActive) {
				mActive = false;
				LinkedHashTable controllables = mTissularity.getControllables();
				KeyValuePair pair = (KeyValuePair) controllables.get(mVec1
						.set(mId));
				if (pair != null) {
					LinkedHashTable tmp = (LinkedHashTable) pair.mB;
					int num = tmp.size();
					while (num > 0) {
						ControlOrganoid controlOrganoid = (ControlOrganoid) tmp
								.back();
						Cellularity cellularity = controlOrganoid
								.getCellularity();
						int x = controlOrganoid.getX();
						int y = controlOrganoid.getY();
						int z = controlOrganoid.getZ();
						cellularity.setCell(x, y, z,
								cellularity.getDropDefaultCell(x, y, z), null);
						--num;
					}
				}
			}
		}
		mTissularity = null;
	}

	public void tick() {
		if (mMob)
			return;
		if (mDead) {
			mDead = false;
			int x = (int) mTissularity.getOrganularity().random();
			mChunkX = x >> Settings.CHUNK_SIZE_LOG;
			mPositionX = (x & Settings.CHUNK_SIZE_MASK) + 0.5f;
			int y = mTissularity.getSpawnY(x, 0);
			mChunkY = y >> Settings.CHUNK_SIZE_LOG;
			mPositionY = (y & Settings.CHUNK_SIZE_MASK) + 0.5f;
			mVelocityX = 0f;
			mVelocityY = 0f;
		}
		if (!mActive) {
			ChunkCellularity chunk = mTissularity.getChunk(mChunkX, mChunkY);
			if (chunk != null) {
				mActive = true;
				DynamicCellularity dynamic = DynamicCellularity.obtain(
						mTissularity.getOrganularity().random(), mPositionX
								+ Settings.MAX_DROP_HSIZE - 0.5f, mPositionY
								+ Settings.MAX_DROP_HSIZE - 0.5f, 0f);
				chunk.pushCellularity(dynamic);
				dynamic.setCell(0, 0, 0, Cells.CELL_TYPE_TEE,
						TeeCellData.obtain(mId, false));
				dynamic.getBody().setVelocity(mVelocityX, mVelocityY);
			} else if (!mVisitor) {
				mVisitor = true;
				mVisitorX = (mChunkX << Settings.CHUNK_SIZE_LOG)
						+ Settings.CHUNK_HSIZE;
				mVisitorY = (mChunkY << Settings.CHUNK_SIZE_LOG)
						+ Settings.CHUNK_HSIZE;
				mTissularity.addVisitor(mVisitorX, mVisitorY, 1, 1);
			}
		} else {
			if (mVisitor) {
				mVisitor = false;
				mTissularity.removeVisitor(mVisitorX, mVisitorY, 1, 1);
			}
			LinkedHashTable controllables = mTissularity.getControllables();
			KeyValuePair pair = (KeyValuePair) controllables
					.get(mVec1.set(mId));
			if (pair != null) {
				LinkedHashTable tmp = (LinkedHashTable) pair.mB;
				for (List.Node i = tmp.begin(); i != tmp.end(); i = tmp.next(i)) {
					ControlOrganoid controlOrganoid = (ControlOrganoid) tmp
							.val(i);
					Cellularity cellularity = controlOrganoid.getCellularity();
					mChunkX = cellularity.getChunkX();
					mChunkY = cellularity.getChunkY();
					Vector2 p = cellularity.localToChunk(
							controlOrganoid.getX() + 0.5f,
							controlOrganoid.getY() + 0.5f);
					mPositionX = p.x;
					mPositionY = p.y;
					mVelocityX = cellularity.getBody().getVelocityX();
					mVelocityY = cellularity.getBody().getVelocityY();
				}
			}
		}
	}

	public int getItem(int pId) {
		return mItems[pId];
	}

	public CellData getItemData(int pId) {
		return mItemsData[pId];
	}

	public int getItemCount(int pId) {
		return mItemsCount[pId];
	}

	public void setItemCount(int pId, int pCount) {
		mItemsCount[pId] = pCount;
	}

	public void setItem(int pId, int pItem, CellData pItemData, int pItemCount) {
		mItems[pId] = pItem;
		if (mItemsData[pId] != null)
			mItemsData[pId].recycle();
		mItemsData[pId] = pItemData;
		mItemsCount[pId] = pItemCount;
		if (pItem != Cells.CELL_TYPE_VACUUM)
			mItemsKeys.set(pId);
		else
			mItemsKeys.remove(pId);
	}

	public int pushItem(int pItem, CellData pItemData, int pItemCount) {
		int i = Player.USE_ITEM_MIN_ID;
		while (i <= Player.USE_ITEM_MAX_ID) {
			if (getItem(i) == pItem
					&& (getItemData(i) == null && pItemData == null || getItemData(i) != null
							&& pItemData != null
							&& getItemData(i).equals(pItemData))) {
				int count = Math.min(pItemCount,
						CellsAction.stuckSize(pItem, pItemData)
								- getItemCount(i));
				setItem(i, pItem, pItemData != null ? pItemData.cpy() : null,
						getItemCount(i) + count);
				pItemCount -= count;
			}
			++i;
		}
		i = Player.INVENTORY_ITEM_MIN_ID;
		while (i <= Player.INVENTORY_ITEM_MAX_ID) {
			if (getItem(i) == pItem
					&& (getItemData(i) == null && pItemData == null || getItemData(i) != null
							&& pItemData != null
							&& getItemData(i).equals(pItemData))) {
				int count = Math.min(pItemCount,
						CellsAction.stuckSize(pItem, pItemData)
								- getItemCount(i));
				setItem(i, pItem, pItemData != null ? pItemData.cpy() : null,
						getItemCount(i) + count);
				pItemCount -= count;
			}
			++i;
		}
		i = Player.SPECIAL_ITEM_ID;
		if (getItem(i) == pItem
				&& (getItemData(i) == null && pItemData == null || getItemData(i) != null
						&& pItemData != null
						&& getItemData(i).equals(pItemData))) {
			int count = Math.min(pItemCount,
					CellsAction.stuckSize(pItem, pItemData) - getItemCount(i));
			setItem(i, pItem, pItemData != null ? pItemData.cpy() : null,
					getItemCount(i) + count);
			pItemCount -= count;
		}

		i = Player.USE_ITEM_MIN_ID;
		while (i <= Player.USE_ITEM_MAX_ID) {
			if (getItem(i) == Cells.CELL_TYPE_VACUUM && pItemCount > 0) {
				int count = Math.min(pItemCount,
						CellsAction.stuckSize(pItem, pItemData));
				setItem(i, pItem, pItemData != null ? pItemData.cpy() : null,
						getItemCount(i) + count);
				pItemCount -= count;
			}
			++i;
		}
		i = Player.INVENTORY_ITEM_MIN_ID;
		while (i <= Player.INVENTORY_ITEM_MAX_ID) {
			if (getItem(i) == Cells.CELL_TYPE_VACUUM && pItemCount > 0) {
				int count = Math.min(pItemCount,
						CellsAction.stuckSize(pItem, pItemData));
				setItem(i, pItem, pItemData != null ? pItemData.cpy() : null,
						getItemCount(i) + count);
				pItemCount -= count;
			}
			++i;
		}
		i = Player.SPECIAL_ITEM_ID;
		if (getItem(i) == Cells.CELL_TYPE_VACUUM && pItemCount > 0) {
			int count = Math.min(pItemCount,
					CellsAction.stuckSize(pItem, pItemData));
			setItem(i, pItem, pItemData != null ? pItemData.cpy() : null,
					getItemCount(i) + count);
			pItemCount -= count;
		}

		return pItemCount;
	}

	public void loadPlayer(File pFile) {

		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(pFile, "r");
			byte[] buffer = BinaryByteArrayPool
					.obtain(TerrainGenerator.BUFFER_SIZE);
			IntVector1 tmpN = IntVector1.obtain(0);
			IntVector1 tmpId = IntVector1.obtain(0);

			mDead = TerrainGenerator
					.read(randomAccessFile, buffer, tmpN, tmpId) == 0;
			mChunkX = TerrainGenerator.readInt(randomAccessFile, buffer, tmpN,
					tmpId);
			mChunkY = TerrainGenerator.readInt(randomAccessFile, buffer, tmpN,
					tmpId);
			mPositionX = TerrainGenerator.readFloat(randomAccessFile, buffer,
					tmpN, tmpId);
			mPositionY = TerrainGenerator.readFloat(randomAccessFile, buffer,
					tmpN, tmpId);
			mVelocityX = TerrainGenerator.readFloat(randomAccessFile, buffer,
					tmpN, tmpId);
			mVelocityY = TerrainGenerator.readFloat(randomAccessFile, buffer,
					tmpN, tmpId);

			for (int i = 0; i < Player.MAX_ITEMS; ++i) {
				int item = TerrainGenerator.readInt(randomAccessFile, buffer,
						tmpN, tmpId);
				CellData itemData = TerrainGenerator.readCellData(
						randomAccessFile, buffer, tmpN, tmpId);
				int itemCount = TerrainGenerator.readInt(randomAccessFile,
						buffer, tmpN, tmpId);
				setItem(i, item, itemData, itemCount);
			}
			IntVector1.recycle(tmpN);
			IntVector1.recycle(tmpId);

			BinaryByteArrayPool.recycle(buffer);
			randomAccessFile.close();
		} catch (IOException e) {
		}
	}

	public void unloadPlayer(File pFile) {

		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(pFile,
					"rw");

			randomAccessFile.setLength(0);
			byte[] buffer = BinaryByteArrayPool
					.obtain(TerrainGenerator.BUFFER_SIZE);
			IntVector1 tmpN = IntVector1.obtain(0);
			IntVector1 tmpId = IntVector1.obtain(0);

			TerrainGenerator.write(randomAccessFile, buffer, tmpN, tmpId,
					mDead ? 0 : 1);
			TerrainGenerator.writeInt(randomAccessFile, buffer, tmpN, tmpId,
					mChunkX);
			TerrainGenerator.writeInt(randomAccessFile, buffer, tmpN, tmpId,
					mChunkY);
			TerrainGenerator.writeFloat(randomAccessFile, buffer, tmpN, tmpId,
					mPositionX);
			TerrainGenerator.writeFloat(randomAccessFile, buffer, tmpN, tmpId,
					mPositionY);
			TerrainGenerator.writeFloat(randomAccessFile, buffer, tmpN, tmpId,
					mVelocityX);
			TerrainGenerator.writeFloat(randomAccessFile, buffer, tmpN, tmpId,
					mVelocityY);

			for (int i = 0; i < Player.MAX_ITEMS; ++i) {
				int item = getItem(i);
				CellData itemData = getItemData(i);
				int itemCount = getItemCount(i);
				TerrainGenerator.writeInt(randomAccessFile, buffer, tmpN,
						tmpId, item);
				TerrainGenerator.writeCellData(randomAccessFile, buffer, tmpN,
						tmpId, itemData);
				TerrainGenerator.writeInt(randomAccessFile, buffer, tmpN,
						tmpId, itemCount);
			}

			TerrainGenerator.write(randomAccessFile, buffer, tmpN, tmpId);

			IntVector1.recycle(tmpN);
			IntVector1.recycle(tmpId);

			BinaryByteArrayPool.recycle(buffer);
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
