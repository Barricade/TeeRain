package com.gaskarov.teerain.game.hud;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.RegionData;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.VacuumCell;
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
	public Runnable regionLoader(int pX, int pY, RegionDataHolder pRegionDataHolder) {
		return RegionLoader.obtain(this, pX, pY, pRegionDataHolder, true);
	}

	@Override
	public Runnable regionUnloader(int pX, int pY, RegionDataHolder pRegionDataHolder) {
		return RegionLoader.obtain(this, pX, pY, pRegionDataHolder, false);
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
	public void render(long pTime) {
		if (mOrganularity == null)
			return;
		OrthographicCamera camera = mOrganularity.getCamera();
		render(Settings.TILE_RENDER_HUD, camera.viewportWidth / Settings.TILE_RENDER_HUD,
				camera.viewportHeight / Settings.TILE_RENDER_HUD, pTime);
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
			Cellularity chunk = Cellularity.obtain(BodyType.StaticBody, 0, 0, 0);
			int offsetX = mX << Settings.CHUNK_SIZE_LOG;
			int offsetY = mY << Settings.CHUNK_SIZE_LOG;
			for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
				for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
					for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
						chunk.setCell(x, y, z, genWorld(seed, offsetX + x, offsetY + y, z));
						chunk.setLight(x, y, z, 0, 0, 0);
					}
			for (int i = 0; i < 16; ++i) {
				chunk.precalc(1);
				chunk.update();
			}
			chunk.drop();
			chunk.postDrop();
			chunk.refresh();
			chunk.precalc(1);

			mChunkHolder.setChunk(chunk);

			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

		private void unload() {
			Cellularity chunk = mChunkHolder.getChunk();
			mChunkHolder.setChunk(null);
			Cellularity.recycle(chunk);
			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

		private Cell genWorld(long pSeed, int pX, int pY, int pZ) {

			final int AIR_CELL_ID = 0;
			final int UI_CELL_ID = 1;
			int ans = AIR_CELL_ID;

			if (pY == 0 && 0 <= pX && pX < 10 && pZ == 0)
				ans = UI_CELL_ID;

			switch (ans) {
			case AIR_CELL_ID:
				return VacuumCell.obtain();
			case UI_CELL_ID:
				return UICell.obtain();
			default:
				return null;
			}
		}

	}

	public static class RegionLoader implements Runnable {

		private static final Array sPool = Array.obtain();

		private HUDTissularity mTissularity;
		private int mX;
		private int mY;
		private RegionDataHolder mRegionDataHolder;
		private boolean mLoad;

		@Override
		public void run() {
			if (mLoad)
				load();
			else
				unload();
			recycle(this);
		}

		private static RegionLoader obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (RegionLoader.class) {
					return sPool.size() == 0 ? new RegionLoader() : (RegionLoader) sPool.pop();
				}
			return new RegionLoader();
		}

		private static void recyclePure(RegionLoader pObj) {
			if (GlobalConstants.POOL)
				synchronized (RegionLoader.class) {
					sPool.push(pObj);
				}
		}

		public static RegionLoader obtain(HUDTissularity pTissularity, int pX, int pY,
				RegionDataHolder pRegionDataHolder, boolean pLoad) {
			RegionLoader obj = obtainPure();
			obj.mTissularity = pTissularity;
			obj.mX = pX;
			obj.mY = pY;
			obj.mRegionDataHolder = pRegionDataHolder;
			obj.mLoad = pLoad;
			return obj;
		}

		public static void recycle(RegionLoader pObj) {
			pObj.mTissularity = null;
			pObj.mRegionDataHolder = null;
			recyclePure(pObj);
		}

		private void load() {

			RegionData region = RegionData.obtain();

			mRegionDataHolder.setRegionData(region);

			synchronized (mTissularity.getOrganularity()) {
				mRegionDataHolder.finish();
			}
		}

		private void unload() {
			RegionData region = mRegionDataHolder.getRegionData();
			mRegionDataHolder.setRegionData(null);
			RegionData.recycle(region);
			synchronized (mTissularity.getOrganularity()) {
				mRegionDataHolder.finish();
			}
		}
	}

}
