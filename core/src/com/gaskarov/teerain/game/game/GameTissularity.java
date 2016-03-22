package com.gaskarov.teerain.game.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.debug.TimeMeasure;
import com.gaskarov.teerain.game.Controller;
import com.gaskarov.teerain.game.game.cell.AirCell;
import com.gaskarov.teerain.game.game.cell.GroundCell;
import com.gaskarov.teerain.game.game.cell.RockCell;
import com.gaskarov.teerain.game.game.cell.TeeCell;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class GameTissularity extends Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private Controller mController;

	// ===========================================================
	// Constructors
	// ===========================================================

	private GameTissularity() {
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
	protected void tickUpdates() {
		mController.tick(this);
	}

	@Override
	public void render(long pTime) {
		if (mOrganularity == null)
			return;
		OrthographicCamera camera = mOrganularity.getCamera();
		TimeMeasure.sM11.start();
		render(Settings.TILE_RENDER, camera.viewportWidth / Settings.TILE_RENDER,
				camera.viewportHeight / Settings.TILE_RENDER, pTime);
		TimeMeasure.sM11.end();
	}

	@Override
	public boolean keyDown(int pKeycode) {
		mController.keyDown(this, pKeycode);
		return true;
	}

	@Override
	public boolean keyUp(int pKeycode) {
		mController.keyUp(this, pKeycode);
		return true;
	}

	@Override
	public boolean keyTyped(char pCharacter) {
		mController.keyTyped(this, pCharacter);
		return true;
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		mController.touchDown(this, pScreenX, pScreenY, pPointer, pButton);
		return true;
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		mController.touchUp(this, pScreenX, pScreenY, pPointer, pButton);
		return true;
	}

	@Override
	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		mController.touchDragged(this, pScreenX, pScreenY, pPointer);
		return true;
	}

	@Override
	public boolean mouseMoved(int pScreenX, int pScreenY) {
		mController.mouseMoved(this, pScreenX, pScreenY);
		return true;
	}

	@Override
	public boolean scrolled(int pAmount) {
		mController.scrolled(this, pAmount);
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static GameTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (GameTissularity.class) {
				return sPool.size() == 0 ? new GameTissularity() : (GameTissularity) sPool.pop();
			}
		return new GameTissularity();
	}

	private static void recyclePure(GameTissularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (GameTissularity.class) {
				sPool.push(pObj);
			}
	}

	public static GameTissularity obtain(Controller pController) {
		GameTissularity obj = obtainPure();
		obj.init();
		obj.mController = pController;
		return obj;
	}

	public static void recycle(GameTissularity pObj) {
		pObj.mController = null;
		pObj.dispose();
		recyclePure(pObj);
	}

	public void pushPlayer() {
		int w = 0;
		int h = ChunkLoader.getY(0);
		int chunkX = w >> Settings.CHUNK_SIZE_LOG;
		int chunkY = h >> Settings.CHUNK_SIZE_LOG;
		addVisitor(chunkX, chunkY, 1, 1);
		waitChunks();
		Cellularity chunk = getChunk(chunkX, chunkY);
		chunk.setCell(w & Settings.CHUNK_SIZE_MASK, h & Settings.CHUNK_SIZE_MASK, 0, TeeCell
				.obtain(0));
		waitChunks();
		removeVisitor(chunkX, chunkY, 1, 1);
	}

	public void renderDebug() {
		if (Settings.BOX2D_DEBUG_DRAW) {
			synchronized (mOrganularity) {
				OrthographicCamera camera = mOrganularity.getCamera();
				Matrix4 box2DDebugCameraMatrix = mOrganularity.getBox2DDebugCameraMatrix();
				box2DDebugCameraMatrix.set(camera.combined);
				box2DDebugCameraMatrix.scale(Settings.TILE_RENDER, Settings.TILE_RENDER, 1f);
				box2DDebugCameraMatrix.translate(-mCameraX, -mCameraY, 0);
				mOrganularity.getBox2DDebugRenderer().render(mOrganularity.getWorld(),
						box2DDebugCameraMatrix);
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ChunkLoader implements Runnable {

		private static final Array sPool = Array.obtain();

		private GameTissularity mTissularity;
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

		public static ChunkLoader obtain(GameTissularity pTissularity, int pX, int pY,
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

		public static int getY(int pX) {
			double val = 1024.0 * 0;
			return (int) (1024 * NoiseMath.perlinOctaveNoise(NoiseMath.combine(0, 0), pX / 1024.0,
					0, 2.0, 0.5, 4) + val);
		}

		private Cell genWorld(long pSeed, int pX, int pY, int pZ) {

			final int AIR_CELL_ID = 0;
			final int GROUND_CELL_ID = 1;
			final int ROCK_CELL_ID = 2;
			int ans = AIR_CELL_ID;

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
					ans = GROUND_CELL_ID;
				else
					ans = ROCK_CELL_ID;
			}

			// Caves
			if (Settings.CHUNK_MAX_DEPTH != pZ) {
				for (int i = 0; i < 3; ++i) {
					double val =
							NoiseMath.perlinOctaveNoise(NoiseMath.combine(pSeed, i + 3),
									pX / 512.0, pY / 256.0, pZ / 64.0, 2.0, 0.5, 6);
					if (0.5 - 0.005 < val && val < 0.5 + 0.005) {
						ans = AIR_CELL_ID;
						break;
					}
				}
			}

			switch (ans) {
			case AIR_CELL_ID:
				return AirCell.obtain();
			case GROUND_CELL_ID:
				return GroundCell.obtain();
			case ROCK_CELL_ID:
				return RockCell.obtain();
			default:
				return null;
			}
		}
	}

}
