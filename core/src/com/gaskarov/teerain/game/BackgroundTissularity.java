package com.gaskarov.teerain.game;

import com.badlogic.gdx.graphics.Color;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.util.GraphicsUtils;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;

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

	private static final long SEED = 1350;
	private static final int CAMERA_CENTER_X = 64;
	private static final int CAMERA_CENTER_Y = 64;
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
		waitChunks();
	}

	@Override
	public void detach() {
		removeVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
		super.detach();
	}

	@Override
	public float getTileRender() {
		return Settings.TILE_RENDER_BACKGROUND;
	}

	@Override
	public void renderTick(int pOffsetX, int pOffsetY, float pCameraX,
			float pCameraY, float pCellSize, float pWidth, float pHeight) {
		FloatArray buffer = mOrganularity.getRenderBufferA();
		int n = buffer.size();
		buffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
		int r = 255;
		int g = 255;
		int b = 255;
		float color = Color.toFloatBits(r, g, b, 255);
		GraphicsUtils.drawTexture(0f, 0f, 1f, 1f, Settings.TILE_W * 11,
				Settings.TILE_H * 0, Settings.TILE_W * 12, Settings.TILE_H * 1,
				buffer.data(), n, color, color, color, color);
		super.renderTick(pOffsetX, pOffsetY, pCameraX, pCameraY, pCellSize,
				pWidth, pHeight);
	}

	@Override
	public void tickUpdates() {
		mAngle += Settings.TIME_STEP * CAMERA_ANGULAR_VELOCITY;
		mCameraX = CAMERA_CENTER_X - mOffsetX + ((float) Math.cos(mAngle))
				* CAMERA_RADIUS;
		mCameraY = CAMERA_CENTER_Y - mOffsetY + ((float) Math.sin(mAngle))
				* CAMERA_RADIUS;
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

		public static ChunkLoader obtain(BackgroundTissularity pTissularity,
				int pX, int pY, ChunkHolder pChunkHolder, boolean pLoad) {
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
			TerrainGenerator.genChunkBackground(SEED, chunk, mX, mY);

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
