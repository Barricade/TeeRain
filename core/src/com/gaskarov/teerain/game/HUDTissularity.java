package com.gaskarov.teerain.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Player;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.Cellularity.CellData;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.cellularity.DynamicCellularity;
import com.gaskarov.teerain.core.util.GraphicsUtils;
import com.gaskarov.teerain.resource.Cells;
import com.gaskarov.teerain.resource.CellsAction;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class HUDTissularity extends Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_CENTER_X = 64;
	private static final int CAMERA_CENTER_Y = 64;

	private static final int[][] INVENTORY_SHORT = new int[][] {
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 2 } };

	private static final int[][] INVENTORY_FULL = new int[][] {
			{ 6, 6, 3, 3, 3, 3, 3, 3, 4, 4 }, //
			{ 6, 6, 3, 3, 3, 3, 3, 3, 4, 4 }, //
			{ 6, 6, 3, 3, 3, 3, 3, 3, 5, 5 }, //
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 2 } };

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private Player mPlayer;
	private DynamicCellularity mSelect;
	private boolean mIsInventoryFull;

	// ===========================================================
	// Constructors
	// ===========================================================

	private HUDTissularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public Runnable chunkLoader(int pX, int pY, ChunkHolder pChunkHolder) {
		return ChunkLoader.obtain(this, pX, pY, pChunkHolder, true);
	}

	@Override
	public Runnable chunkUnloader(int pX, int pY, ChunkHolder pChunkHolder) {
		return ChunkLoader.obtain(this, pX, pY, pChunkHolder, false);
	}

	@Override
	public void attach(Organularity pOrganularity) {
		super.attach(pOrganularity);
		addVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
		waitChunks();
		mSelect = DynamicCellularity.obtain(mOrganularity.random(), 0, 0, 0);
		mSelect.setCell(0, 0, 0, Cells.CELL_TYPE_AIR, null);
		mSelect.getBody().setActive(false);
		ChunkCellularity chunk = getChunk(
				CAMERA_CENTER_X >> Settings.CHUNK_SIZE_LOG,
				CAMERA_CENTER_Y >> Settings.CHUNK_SIZE_LOG);
		chunk.pushCellularity(mSelect);
		mIsInventoryFull = false;
	}

	@Override
	public void detach() {
		mSelect = null;
		removeVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
		super.detach();
	}

	@Override
	public float getTileRender() {
		return Settings.TILE_RENDER_HUD;
	}

	@Override
	public void tick() {
		if (mOrganularity == null)
			return;
		super.tick();
		OrthographicCamera camera = mOrganularity.getCamera();
		mCameraX = CAMERA_CENTER_X - mOffsetX;
		mCameraY = CAMERA_CENTER_Y - mOffsetY + camera.viewportHeight
				/ getTileRender() / 2;
	}

	@Override
	public void renderInventory(int pCell, Cellularity pCellularity, int pX,
			int pY, int pZ, float pOffsetX, float pOffsetY, int pTileX,
			int pTileY, float pSize, float pCos, float pSin,
			FloatArray pRenderBuffer) {
		int cellParam = (pCell >>> Cells.CELL_TYPES_MAX_LOG) & 255;
		int useItem = commandToItem(cellParam);
		if (useItem != -1) {
			CellsAction.renderItem(pCellularity, mPlayer.getItem(useItem),
					mPlayer.getItemData(useItem), pX, pY, pZ, pOffsetX,
					pOffsetY, pTileX, pTileY, pSize, pCos, pSin, pRenderBuffer,
					0.5f, 0.5f);
			int count = mPlayer.getItemCount(useItem);
			int digit0 = Cells.digitToSymbol(count % 10);
			int digit1 = Cells.digitToSymbol(count / 10 % 10);
			float size = 0.35f;
			float halfSize = size * 0.5f;
			float offsetBottom = 0.0f;
			float offsetRight = 0.0f;
			if (count > 1)
				GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffer, Cells.SYMBOLS_TILE_X[digit0],
						Cells.SYMBOLS_TILE_Y[digit0], Settings.TILE_W,
						Settings.TILE_H, 1f - halfSize - offsetRight, halfSize
								+ offsetBottom, size, size, 1f, 0f);
			if (count >= 10)
				GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX,
						pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
						pRenderBuffer, Cells.SYMBOLS_TILE_X[digit1],
						Cells.SYMBOLS_TILE_Y[digit1], Settings.TILE_W,
						Settings.TILE_H, 1f - halfSize - size - offsetRight,
						halfSize + offsetBottom, size, size, 1f, 0f);
		}
	}

	@Override
	public boolean keyDown(int pKeycode) {
		switch (pKeycode) {
		case Keys.E:
			mIsInventoryFull = !mIsInventoryFull;
			if (mIsInventoryFull)
				setInventory(getChunk(0, 0), INVENTORY_FULL);
			else
				setInventory(getChunk(0, 0), INVENTORY_SHORT);
			break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int pKeycode) {
		return true;
	}

	@Override
	public boolean keyTyped(char pCharacter) {
		return true;
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer,
			int pButton) {
		float clickX = screenToWorldX(this, pScreenX);
		float clickY = screenToWorldY(this, pScreenY);
		int x = mOffsetX + MathUtils.floor(clickX);
		int y = mOffsetY + MathUtils.floor(clickY);
		ChunkCellularity chunk = getChunk(x >> Settings.CHUNK_SIZE_LOG,
				y >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null) {
			int cell = chunk.getCell(x & Settings.CHUNK_SIZE_MASK, y
					& Settings.CHUNK_SIZE_MASK, 0);
			int cellType = cell & Cells.CELL_TYPES_MAX_MASK;
			int cellParam = (cell >>> Cells.CELL_TYPES_MAX_LOG) & 255;
			if (cellType == Cells.CELL_TYPE_INVENTORY) {
				int useItem = commandToItem(cellParam);
				if (useItem != -1) {

					int item = mPlayer.getItem(Player.SELECT_ITEM_ID);
					CellData itemData = mPlayer
							.getItemData(Player.SELECT_ITEM_ID);
					if (itemData != null)
						itemData = itemData.cpy();
					int itemCount = mPlayer.getItemCount(Player.SELECT_ITEM_ID);

					int oldItem = mPlayer.getItem(useItem);
					CellData oldItemData = mPlayer.getItemData(useItem);
					if (oldItemData != null)
						oldItemData = oldItemData.cpy();
					int oldItemCount = mPlayer.getItemCount(useItem);

					mPlayer.setItem(Player.SELECT_ITEM_ID, oldItem,
							oldItemData, oldItemCount);
					mPlayer.setItem(useItem, item, itemData, itemCount);
					if (mPlayer.getItem(Player.SELECT_ITEM_ID) == Cells.CELL_TYPE_VACUUM)
						mSelect.setCell(0, 0, 0, Cells.CELL_TYPE_AIR, null);
					else
						mSelect.setCell(
								0,
								0,
								0,
								Cells.CELL_TYPE_INVENTORY
										| (Cells.INVENTORY_PARAM_SELECT_ITEM << Cells.CELL_TYPES_MAX_LOG),
								null);
					int chunkX = mSelect.getChunkX();
					int chunkY = mSelect.getChunkY();
					mSelect.getBody().setPosition(
							mOffsetX - (chunkX << Settings.CHUNK_SIZE_LOG)
									+ clickX + Settings.MAX_DROP_HSIZE - 0.5f,
							mOffsetY - (chunkY << Settings.CHUNK_SIZE_LOG)
									+ clickY + Settings.MAX_DROP_HSIZE - 0.5f);
				}
				return false;
			}
		}
		if (mPlayer.getItem(Player.SELECT_ITEM_ID) != Cells.CELL_TYPE_VACUUM) {
			int item = mPlayer.getItem(Player.SELECT_ITEM_ID);
			CellData itemData = mPlayer.getItemData(Player.SELECT_ITEM_ID);
			if (itemData != null)
				itemData = itemData.cpy();
			int itemCount = mPlayer.getItemCount(Player.SELECT_ITEM_ID);
			mPlayer.getDropItems().push(item);
			mPlayer.getDropItemsData().push(itemData);
			mPlayer.getDropItemsCount().push(itemCount);
			mPlayer.setItem(Player.SELECT_ITEM_ID, Cells.CELL_TYPE_VACUUM,
					null, 0);
			mSelect.setCell(0, 0, 0, Cells.CELL_TYPE_AIR, null);
			return false;
		}
		return true;
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		return true;
	}

	@Override
	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		return mouseMoved(pScreenX, pScreenY);
	}

	@Override
	public boolean mouseMoved(int pScreenX, int pScreenY) {
		float clickX = screenToWorldX(this, pScreenX);
		float clickY = screenToWorldY(this, pScreenY);
		int chunkX = mSelect.getChunkX();
		int chunkY = mSelect.getChunkY();
		mSelect.getBody().setPosition(
				mOffsetX - (chunkX << Settings.CHUNK_SIZE_LOG) + clickX
						+ Settings.MAX_DROP_HSIZE - 0.5f,
				mOffsetY - (chunkY << Settings.CHUNK_SIZE_LOG) + clickY
						+ Settings.MAX_DROP_HSIZE - 0.5f);
		return true;
	}

	@Override
	public boolean scrolled(int pAmount) {
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static HUDTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (HUDTissularity.class) {
				return sPool.size() == 0 ? new HUDTissularity()
						: (HUDTissularity) sPool.pop();
			}
		return new HUDTissularity();
	}

	private static void recyclePure(HUDTissularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (HUDTissularity.class) {
				sPool.push(pObj);
			}
	}

	public static HUDTissularity obtain(Player pPlayer) {
		HUDTissularity obj = obtainPure();
		obj.init();
		obj.mPlayer = pPlayer;
		return obj;
	}

	public static void recycle(HUDTissularity pObj) {
		pObj.mPlayer = null;
		pObj.dispose();
		recyclePure(pObj);
	}

	private static void setInventory(ChunkCellularity pChunk, int[][] pInventory) {
		int useItem = Cells.INVENTORY_PARAM_USE_ITEM_MIN;
		int inventoryItem = Cells.INVENTORY_PARAM_INVENTORY_ITEM_MIN;
		int craftItem = Cells.INVENTORY_PARAM_CRAFT_ITEM_MIN;
		int craftOutItem = Cells.INVENTORY_PARAM_CRAFT_OUT_ITEM_MIN;
		for (int i = 0; i < pInventory.length; ++i)
			for (int j = 0; j < pInventory[i].length; ++j) {
				int x = CAMERA_CENTER_X + j - 5;
				int y = CAMERA_CENTER_Y - (i - 3);
				int cell;
				switch (pInventory[i][j]) {
				case 0:
					cell = Cells.CELL_TYPE_VOID;
					break;
				case 1:
					cell = Cells.CELL_TYPE_INVENTORY
							| (useItem++ << Cells.CELL_TYPES_MAX_LOG);
					break;
				case 2:
					cell = Cells.CELL_TYPE_INVENTORY
							| (Cells.INVENTORY_PARAM_SPECIAL_ITEM << Cells.CELL_TYPES_MAX_LOG);
					break;
				case 3:
					cell = Cells.CELL_TYPE_INVENTORY
							| (inventoryItem++ << Cells.CELL_TYPES_MAX_LOG);
					break;
				case 4:
					cell = Cells.CELL_TYPE_INVENTORY
							| (craftItem++ << Cells.CELL_TYPES_MAX_LOG);
					break;
				case 5:
					cell = Cells.CELL_TYPE_INVENTORY
							| (craftOutItem++ << Cells.CELL_TYPES_MAX_LOG);
					break;
				case 6:
					cell = Cells.CELL_TYPE_INVENTORY
							| (Cells.INVENTORY_PARAM_NO << Cells.CELL_TYPES_MAX_LOG);
					break;
				default:
					cell = Cells.CELL_TYPE_VOID;
					break;
				}
				pChunk.setCell(x, y, 0, cell, null);
			}
	}

	private static float screenToWorldX(Tissularity pTissularity, int pScreenX) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraX()
				+ (pScreenX - Gdx.graphics.getWidth() * 0.5f)
				/ Gdx.graphics.getWidth() / pTissularity.getTileRender()
				* camera.viewportWidth;
	}

	private static float screenToWorldY(Tissularity pTissularity, int pScreenY) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraY()
				- (pScreenY - Gdx.graphics.getHeight() * 0.5f)
				/ Gdx.graphics.getHeight() / pTissularity.getTileRender()
				* camera.viewportHeight;
	}

	private static int commandToItem(int pParam) {
		if (Cells.INVENTORY_PARAM_USE_ITEM_MIN <= pParam
				&& pParam <= Cells.INVENTORY_PARAM_USE_ITEM_MAX)
			return Player.USE_ITEM_MIN_ID + pParam
					- Cells.INVENTORY_PARAM_USE_ITEM_MIN;
		else if (Cells.INVENTORY_PARAM_CRAFT_ITEM_MIN <= pParam
				&& pParam <= Cells.INVENTORY_PARAM_CRAFT_ITEM_MAX)
			return Player.CRAFT_ITEM_MIN_ID + pParam
					- Cells.INVENTORY_PARAM_CRAFT_ITEM_MIN;
		else if (Cells.INVENTORY_PARAM_CRAFT_OUT_ITEM_MIN <= pParam
				&& pParam <= Cells.INVENTORY_PARAM_CRAFT_OUT_ITEM_MAX)
			return Player.CRAFT_OUT_ITEM_MIN_ID + pParam
					- Cells.INVENTORY_PARAM_CRAFT_OUT_ITEM_MIN;
		else if (Cells.INVENTORY_PARAM_INVENTORY_ITEM_MIN <= pParam
				&& pParam <= Cells.INVENTORY_PARAM_INVENTORY_ITEM_MAX)
			return Player.INVENTORY_ITEM_MIN_ID + pParam
					- Cells.INVENTORY_PARAM_INVENTORY_ITEM_MIN;
		else
			switch (pParam) {
			case Cells.INVENTORY_PARAM_SPECIAL_ITEM:
				return Player.SPECIAL_ITEM_ID;
			case Cells.INVENTORY_PARAM_ARMOR_ITEM:
				return Player.ARMOR_ITEM_ID;
			case Cells.INVENTORY_PARAM_FOOT_ARMOR_ITEM:
				return Player.FOOT_ARMOR_ITEM_ID;
			case Cells.INVENTORY_PARAM_HAND_ARMOR_ITEM:
				return Player.HAND_ARMOR_ITEM_ID;
			case Cells.INVENTORY_PARAM_SELECT_ITEM:
				return Player.SELECT_ITEM_ID;
			default:
				break;
			}
		return -1;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ChunkLoader implements Runnable {

		private static final Array sPool = Array.obtain();

		private HUDTissularity mTissularity;
		private int mX;
		private int mY;
		private ChunkHolder mChunkHolder;
		private boolean mLoad;

		@Override
		public void run() {
			if (mLoad)
				load();
			else
				unload();
			recycle(this);
		}

		private static ChunkLoader obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (ChunkLoader.class) {
					return sPool.size() == 0 ? new ChunkLoader()
							: (ChunkLoader) sPool.pop();
				}
			return new ChunkLoader();
		}

		private static void recyclePure(ChunkLoader pObj) {
			if (GlobalConstants.POOL)
				synchronized (ChunkLoader.class) {
					sPool.push(pObj);
				}
		}

		public static ChunkLoader obtain(HUDTissularity pTissularity, int pX,
				int pY, ChunkHolder pChunkHolder, boolean pLoad) {
			ChunkLoader obj = obtainPure();
			obj.mTissularity = pTissularity;
			obj.mX = pX;
			obj.mY = pY;
			obj.mChunkHolder = pChunkHolder;
			obj.mLoad = pLoad;
			return obj;
		}

		public static void recycle(ChunkLoader pObj) {
			pObj.mTissularity = null;
			pObj.mChunkHolder = null;
			recyclePure(pObj);
		}

		private void load() {

			ChunkCellularity chunk = ChunkCellularity
					.obtain(mTissularity.mOrganularity.random());
			chunk.setSky(256, 256, 256);

			for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
				for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
					for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
						chunk.setCell(x, y, z, Cells.CELL_TYPE_VOID, null);
						chunk.setLight(x, y, z, 0, 0, 0);
						chunk.setAI(x, y, z, 0);
					}
			if (mX == 0 && mY == 0) {
				setInventory(chunk, INVENTORY_SHORT);
			}
			for (int i = 0; i < 16; ++i) {
				chunk.precalcCells(chunk.cellUpdate());
				chunk.updateCells();
			}
			for (int i = 0; i < 16; ++i) {
				chunk.precalcLight(chunk.lightUpdate());
				chunk.updateLight();
			}
			for (int i = 0; i < 16; ++i) {
				chunk.precalcAI(chunk.aiUpdate());
				chunk.updateAI();
			}
			chunk.drop();
			chunk.postDrop();
			chunk.refreshCells();
			chunk.precalcCells(chunk.cellUpdate());
			chunk.precalcLight(chunk.lightUpdate());
			chunk.precalcAI(chunk.aiUpdate());

			mChunkHolder.setChunk(chunk);

			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

		private void unload() {
			ChunkCellularity chunk = mChunkHolder.getChunk();
			mChunkHolder.setChunk(null);
			ChunkCellularity.recycle(chunk);
			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}
	}

}
