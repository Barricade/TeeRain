package com.gaskarov.teerain.game.hud;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.Player;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

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

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private Player mPlayer;

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
		addVisitor(0, 0, 2, 2);
		waitChunks();
	}

	@Override
	public void detach() {
		removeVisitor(0, 0, 2, 2);
		super.detach();
	}

	@Override
	public void tick() {
		if (mOrganularity == null)
			return;
		super.tick();
		OrthographicCamera camera = mOrganularity.getCamera();
		mCameraX = 5.0f - mOffsetX;
		mCameraY = 0.0f - mOffsetY + camera.viewportHeight / Settings.TILE_RENDER_HUD / 2;
	}

	@Override
	public boolean keyDown(int pKeycode) {
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
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		return true;
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		return true;
	}

	@Override
	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		return true;
	}

	@Override
	public boolean mouseMoved(int pScreenX, int pScreenY) {
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
				return sPool.size() == 0 ? new HUDTissularity() : (HUDTissularity) sPool.pop();
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

		public static ChunkLoader obtain(HUDTissularity pTissularity, int pX, int pY,
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

			long seed = 0;
			ChunkCellularity chunk = ChunkCellularity.obtain();
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

		private int genWorld(long pSeed, int pX, int pY, int pZ) {

			int ans = Cells.CELL_TYPE_VACUUM;

//			if (pY == 0 && 0 <= pX && pX < 10 && pZ == 0)
//				ans = Cells.CELL_TYPE_UI;

			return ans;
		}

	}

}
