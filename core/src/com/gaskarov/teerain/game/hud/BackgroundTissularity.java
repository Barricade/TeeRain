package com.gaskarov.teerain.game.hud;

import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class BackgroundTissularity extends Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_CENTER_X = 64 + 128 * 5;
	private static final int CAMERA_CENTER_Y = 64 + 128 * 3;
	private static final int CAMERA_RADIUS = 32;
	private static final float CAMERA_ANGULAR_VELOCITY = 0.025f * 2 * (float) Math.PI;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private float mAngle;

	// ===========================================================
	// Constructors
	// ===========================================================

	private BackgroundTissularity() {
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
		mAngle = 0;
		addVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
	}

	@Override
	public void detach() {
		removeVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
		super.detach();
	}

	@Override
	public void tickUpdates() {
		mAngle += Settings.TIME_STEP * CAMERA_ANGULAR_VELOCITY;
		mCameraX = CAMERA_CENTER_X - mOffsetX + ((float) Math.cos(mAngle)) * CAMERA_RADIUS;
		mCameraY = CAMERA_CENTER_Y - mOffsetY + ((float) Math.sin(mAngle)) * CAMERA_RADIUS;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static BackgroundTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (BackgroundTissularity.class) {
				return sPool.size() == 0 ? new BackgroundTissularity()
						: (BackgroundTissularity) sPool.pop();
			}
		return new BackgroundTissularity();
	}

	private static void recyclePure(BackgroundTissularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (BackgroundTissularity.class) {
				sPool.push(pObj);
			}
	}

	public static BackgroundTissularity obtain() {
		BackgroundTissularity obj = obtainPure();
		obj.init();
		return obj;
	}

	public static void recycle(BackgroundTissularity pObj) {
		pObj.dispose();
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ChunkLoader implements Runnable {

		private static final Array sPool = Array.obtain();

		private BackgroundTissularity mTissularity;
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

		public static ChunkLoader obtain(BackgroundTissularity pTissularity, int pX, int pY,
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

		public static int getY(int pX) {
			double val = 1024.0 * 0;
			return (int) (1024 * NoiseMath.perlinOctaveNoise(NoiseMath.combine(0, 0), pX / 1024.0,
					0, 2.0, 0.5, 4) + val);
		}

		private strictfp int genWorld(long pSeed, int pX, int pY, int pZ) {

			int ans = Cells.CELL_TYPE_AIR;

			int height =
					(int) (1024 * NoiseMath.perlinOctaveNoise(NoiseMath.combine(pSeed, 0),
							pX / 1024.0, pZ / 64.0, 2.0, 0.5, 4));
			// double amp =
			// 1.0
			// + NoiseMath.perlinOctaveNoise(NoiseMath.combine(pSeed, 1), pX /
			// 256.0,
			// pZ / 64.0, 2.0, 0.5, 2) * (1024.0 - 1.0);
			double pressure = (height - pY) / 1024.0;
			pressure -= 0.5;
			pressure *= pressure * pressure;
			pressure += 0.5;
			double density =
					NoiseMath.perlinOctaveNoise(NoiseMath.combine(pSeed, 2), pX / 256.0,
							pY / 256.0, pZ / 64.0, 2.0, 0.99, 6);

			if (density + pressure >= 1.0) {
				if (density + pressure / 2 <= 0.85)
					ans = Cells.CELL_TYPE_GROUND;
				else
					ans = Cells.CELL_TYPE_ROCK;
			}

			// Caves
			if (Settings.CHUNK_MAX_DEPTH != pZ) {
				for (int i = 0; i < 3; ++i) {
					double val =
							NoiseMath.perlinOctaveNoise(NoiseMath.combine(pSeed, i + 3),
									pX / 512.0, pY / 256.0, pZ / 64.0, 2.0, 0.5, 6);
					if (0.5 - 0.005 < val && val < 0.5 + 0.005) {
						ans = Cells.CELL_TYPE_AIR;
						break;
					}
				}
			}

			return ans;
		}
	}

}
