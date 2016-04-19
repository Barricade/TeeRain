package com.gaskarov.teerain.game.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.MainTissularity;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class MenuTissularity extends Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_CENTER_X = 64;
	private static final int CAMERA_CENTER_Y = 64;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private MainTissularity mMainTissularity;

	// ===========================================================
	// Constructors
	// ===========================================================

	private MenuTissularity() {
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
		mCameraX = CAMERA_CENTER_X - mOffsetX;
		mCameraY = CAMERA_CENTER_Y - mOffsetY;
	}

	@Override
	public void detach() {
		removeVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
		super.detach();
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		float clickX = screenToWorldX(this, pScreenX);
		float clickY = screenToWorldY(this, pScreenY);
		int x = mOffsetX + MathUtils.floor(clickX);
		int y = mOffsetY + MathUtils.floor(clickY);
		ChunkCellularity chunk =
				getChunk(x >> Settings.CHUNK_SIZE_LOG, y >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null) {
			int cell = chunk.getCell(x & Settings.CHUNK_SIZE_MASK, y & Settings.CHUNK_SIZE_MASK, 0);
			int cellType = cell & Cells.CELL_TYPES_MAX_MASK;
			int cellParam = cell >>> Cells.CELL_TYPES_MAX_LOG;
			if (cellType == Cells.CELL_TYPE_UI) {
				int command = (cellParam >>> 8) & 255;
				switch (command) {
				case 1:
					mMainTissularity.inMenu(false);
					break;
				case 2:
					Gdx.app.exit();
					break;
				default:
					break;
				}
			}
		}
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static MenuTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (MenuTissularity.class) {
				return sPool.size() == 0 ? new MenuTissularity() : (MenuTissularity) sPool.pop();
			}
		return new MenuTissularity();
	}

	private static void recyclePure(MenuTissularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (MenuTissularity.class) {
				sPool.push(pObj);
			}
	}

	public static MenuTissularity obtain(MainTissularity pMainTissularity) {
		MenuTissularity obj = obtainPure();
		obj.init();
		obj.mMainTissularity = pMainTissularity;
		return obj;
	}

	public static void recycle(MenuTissularity pObj) {
		pObj.mMainTissularity = null;
		pObj.dispose();
		recyclePure(pObj);
	}

	private static float screenToWorldX(Tissularity pTissularity, int pScreenX) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraX() + (pScreenX - Gdx.graphics.getWidth() * 0.5f)
				/ Gdx.graphics.getWidth() / Settings.TILE_RENDER * camera.viewportWidth;
	}

	private static float screenToWorldY(Tissularity pTissularity, int pScreenY) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraY() - (pScreenY - Gdx.graphics.getHeight() * 0.5f)
				/ Gdx.graphics.getHeight() / Settings.TILE_RENDER * camera.viewportHeight;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ChunkLoader implements Runnable {

		private static final int LOGO_SIZE = 59;
		private static final int[] LOGO_POS_X = new int[] { -12, -11, -11, -11, -11, -11, -10, -8,
				-8, -8, -8, -8, -7, -7, -7, -5, -5, -5, -5, -5, -4, -4, -4, -2, -2, -2, -2, -2, -1,
				-1, 0, 0, 0, 2, 2, 2, 2, 3, 3, 4, 4, 4, 4, 6, 6, 6, 6, 8, 8, 8, 8, 8, 9, 10, 11,
				11, 11, 11, 11 };
		private static final int[] LOGO_POS_Y = new int[] { 0, 0, -1, -2, -3, -4, 0, 0, -1, -2, -3,
				-4, 0, -2, -4, 0, -1, -2, -3, -4, 0, -2, -4, 0, -1, -2, -3, -4, 0, -2, -1, -3, -4,
				-1, -2, -3, -4, 0, -2, -1, -2, -3, -4, 0, -2, -3, -4, 0, -1, -2, -3, -4, -1, -2, 0,
				-1, -2, -3, -4 };

		private static final Array sPool = Array.obtain();

		private MenuTissularity mTissularity;
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
					return sPool.size() == 0 ? new ChunkLoader() : (ChunkLoader) sPool.pop();
				}
			return new ChunkLoader();
		}

		private static void recyclePure(ChunkLoader pObj) {
			if (GlobalConstants.POOL)
				synchronized (ChunkLoader.class) {
					sPool.push(pObj);
				}
		}

		public static ChunkLoader obtain(MenuTissularity pTissularity, int pX, int pY,
				ChunkHolder pChunkHolder, boolean pLoad) {
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

			ChunkCellularity chunk = ChunkCellularity.obtain();

			long seed = 0;
			int offsetX = mX << Settings.CHUNK_SIZE_LOG;
			int offsetY = mY << Settings.CHUNK_SIZE_LOG;
			for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
				for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
					for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
						chunk.setCell(x, y, z, genWorld(seed, offsetX + x, offsetY + y, z), null);
						chunk.setLight(x, y, z, 0, 0, 0);
						chunk.setAI(x, y, z, 0);
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

		private strictfp int genWorld(long pSeed, int pX, int pY, int pZ) {

			int ans = Cells.CELL_TYPE_VOID;

			if (pZ == 0) {

				for (int i = 0; i < LOGO_SIZE; ++i)
					if (pX == CAMERA_CENTER_X + LOGO_POS_X[i]
							&& pY == CAMERA_CENTER_Y + 5 + LOGO_POS_Y[i])
						ans = Cells.CELL_TYPE_GROUND;

				if (CAMERA_CENTER_X - 3 <= pX && pX < CAMERA_CENTER_X + 3
						&& pY == CAMERA_CENTER_Y - 2)
					ans = Cells.CELL_TYPE_UI;
				if (CAMERA_CENTER_X - 3 <= pX && pX < CAMERA_CENTER_X + 3
						&& pY == CAMERA_CENTER_Y - 4)
					ans = Cells.CELL_TYPE_UI;

				if (pX == CAMERA_CENTER_X - 2 && pY == CAMERA_CENTER_Y - 2)
					ans |= ((26 | (1 << 8)) << Cells.CELL_TYPES_MAX_LOG);
				else if (pX == CAMERA_CENTER_X - 1 && pY == CAMERA_CENTER_Y - 2)
					ans |= ((22 | (1 << 8)) << Cells.CELL_TYPES_MAX_LOG);
				else if (pX == CAMERA_CENTER_X && pY == CAMERA_CENTER_Y - 2)
					ans |= ((11 | (1 << 8)) << Cells.CELL_TYPES_MAX_LOG);
				else if (pX == CAMERA_CENTER_X + 1 && pY == CAMERA_CENTER_Y - 2)
					ans |= ((35 | (1 << 8)) << Cells.CELL_TYPES_MAX_LOG);

				if (pX == CAMERA_CENTER_X - 2 && pY == CAMERA_CENTER_Y - 4)
					ans |= ((15 | (2 << 8)) << Cells.CELL_TYPES_MAX_LOG);
				else if (pX == CAMERA_CENTER_X - 1 && pY == CAMERA_CENTER_Y - 4)
					ans |= ((34 | (2 << 8)) << Cells.CELL_TYPES_MAX_LOG);
				else if (pX == CAMERA_CENTER_X && pY == CAMERA_CENTER_Y - 4)
					ans |= ((19 | (2 << 8)) << Cells.CELL_TYPES_MAX_LOG);
				else if (pX == CAMERA_CENTER_X + 1 && pY == CAMERA_CENTER_Y - 4)
					ans |= ((30 | (2 << 8)) << Cells.CELL_TYPES_MAX_LOG);
			}

			return ans;
		}
	}

}
