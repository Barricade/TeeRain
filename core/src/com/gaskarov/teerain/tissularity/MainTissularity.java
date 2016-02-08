package com.gaskarov.teerain.tissularity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gaskarov.teerain.Controller;
import com.gaskarov.teerain.Organularity;
import com.gaskarov.teerain.Resources;
import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cell.AirCell;
import com.gaskarov.teerain.cell.Cell;
import com.gaskarov.teerain.cell.GroundCell;
import com.gaskarov.teerain.cell.RockCell;
import com.gaskarov.teerain.cell.TeeCell;
import com.gaskarov.teerain.cell.TreeCell;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.organoid.ControlOrganoid;
import com.gaskarov.teerain.util.TimeMeasure;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.List;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class MainTissularity extends Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int HIDDEN_CELLS_SIZE = (int) (2 / Settings.TILE_RENDER) + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private final boolean[][] mHiddenCells = new boolean[HIDDEN_CELLS_SIZE][HIDDEN_CELLS_SIZE];

	private Controller mController;

	// ===========================================================
	// Constructors
	// ===========================================================

	private MainTissularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void control(Cellularity pCellularity, int pX, int pY, int pZ,
			ControlOrganoid pControlOrganoid) {
		mController.control(pCellularity, pX, pY, pZ, pControlOrganoid);
	}

	@Override
	public Runnable chunkLoader(int pX, int pY, ChunkHolder pChunkHolder, boolean pLoad) {
		return ChunkLoader.obtain(this, pX, pY, pChunkHolder, pLoad);
	}

	@Override
	public Runnable chunkUnloader(int pX, int pY, ChunkHolder pChunkHolder, boolean pLoad) {
		return ChunkLoader.obtain(this, pX, pY, pChunkHolder, pLoad);
	}

	@Override
	public void attach(Organularity pOrganularity) {
		super.attach(pOrganularity);
		int w = 0;
		int h = ChunkLoader.getY(0);
		int chunkX = w >> Settings.CHUNK_SIZE_LOG;
		int chunkY = h >> Settings.CHUNK_SIZE_LOG;
		addVisitor(chunkX, chunkY, 1, 1);
		mCameraX = w - mOffsetX;
		mCameraY = h - mOffsetY;
		waitChunks();
		Cellularity chunk = getChunk(chunkX, chunkY);
		chunk.setCell(5, 5, 0, RockCell.obtain());
		chunk.setCell(2, 8, 0, TeeCell.obtain());
		for (int i = 0; i < 8; ++i)
			chunk.setCell(w, 1 + i, 0, RockCell.obtain());
		waitChunks();
		removeVisitor(chunkX, chunkY, 1, 1);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		FloatArray[] renderBuffers = mOrganularity.getRenderBuffers();
		OrthographicCamera camera = mOrganularity.getCamera();
		camera.position.x = mCameraX;
		camera.position.y = mCameraY;
		TimeMeasure.sM10.start();
		render(renderBuffers, mOffsetX, mOffsetY, mCameraX, mCameraY, 1, 1, camera.viewportWidth
				/ Settings.TILE_RENDER, camera.viewportHeight / Settings.TILE_RENDER);
		TimeMeasure.sM10.end();
		SpriteBatch spriteBatch = mOrganularity.getSpriteBatch();
		camera.zoom = 1f / Settings.TILE_RENDER;
		camera.update();
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
		for (int i = Settings.LAYERS - 1; i >= 0; --i) {
			FloatArray renderBuffer = renderBuffers[i];
			spriteBatch.draw(Resources.MAIN_TEXTURE, renderBuffer.data(), 0, renderBuffer.size());
			renderBuffer.clear();
		}
		spriteBatch.end();

		if (Settings.BOX2D_DEBUG_DRAW) {
			Matrix4 box2DDebugCameraMatrix = mOrganularity.getBox2DDebugCameraMatrix();
			box2DDebugCameraMatrix.set(camera.combined);
			mOrganularity.getBox2DDebugRenderer().render(mOrganularity.getWorld(),
					box2DDebugCameraMatrix);
		}
	}

	@Override
	public void resize(int pWidth, int pHeight) {
		float w = pWidth;
		float h = pHeight;
		float aspectRatio = w / h;

		OrthographicCamera camera = mOrganularity.getCamera();
		camera.viewportWidth = 2f * Math.min(aspectRatio, 1f);
		camera.viewportHeight = 2f / Math.max(aspectRatio, 1f);
		camera.update();
	}

	@Override
	public void keyDown(int pKeycode) {
		mController.keyDown(pKeycode);
	}

	@Override
	public void keyUp(int pKeycode) {
		mController.keyUp(pKeycode);
	}

	@Override
	public void keyTyped(char pCharacter) {
		mController.keyTyped(pCharacter);
	}

	@Override
	public void touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		mController.touchDown(pScreenX, pScreenY, pPointer, pButton);
	}

	@Override
	public void touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		mController.touchUp(pScreenX, pScreenY, pPointer, pButton);
	}

	@Override
	public void touchDragged(int pScreenX, int pScreenY, int pPointer) {
		mController.touchDragged(pScreenX, pScreenY, pPointer);
	}

	@Override
	public void mouseMoved(int pScreenX, int pScreenY) {
		mController.mouseMoved(pScreenX, pScreenY);
	}

	@Override
	public void scrolled(int pAmount) {
		mController.scrolled(pAmount);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static MainTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (MainTissularity.class) {
				return sPool.size() == 0 ? new MainTissularity() : (MainTissularity) sPool.pop();
			}
		return new MainTissularity();
	}

	private static void recyclePure(MainTissularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (MainTissularity.class) {
				sPool.push(pObj);
			}
	}

	public static MainTissularity obtain() {
		MainTissularity obj = obtainPure();
		obj.init();
		obj.mController = Controller.obtain(obj);
		return obj;
	}

	public static void recycle(MainTissularity pObj) {
		Controller.recycle(pObj.mController);
		pObj.mController = null;
		pObj.dispose();
		recyclePure(pObj);
	}

	private void render(FloatArray[] pRenderBuffers, int pOffsetX, int pOffsetY, float pCameraX,
			float pCameraY, float pCellWidth, float pCellHeight, float pWidth, float pHeight) {

		for (int i = 0; i < HIDDEN_CELLS_SIZE; ++i)
			for (int j = 0; j < HIDDEN_CELLS_SIZE; ++j)
				mHiddenCells[i][j] = true;

		float depthFactor = Settings.DEPTH_FACTORS[Settings.CHUNK_MAX_DEPTH];
		float depthWidth = pWidth * depthFactor;
		float depthHeight = pHeight * depthFactor;
		float left = pCameraX - depthWidth / 2;
		float bottom = pCameraY - depthHeight / 2;

		int extra = Settings.MAX_DROP_SIZE;

		int col1 =
				MathUtils.divFloor(pOffsetX + MathUtils.floor(left) - extra, Settings.CHUNK_SIZE);
		int col2 =
				MathUtils.divCeil(pOffsetX + MathUtils.ceil(left + depthWidth) + extra,
						Settings.CHUNK_SIZE);

		int row1 =
				MathUtils.divFloor(pOffsetY + MathUtils.floor(bottom) - extra, Settings.CHUNK_SIZE);
		int row2 =
				MathUtils.divCeil(pOffsetY + MathUtils.ceil(bottom + depthHeight) + extra,
						Settings.CHUNK_SIZE);

		for (int row = row1; row <= row2; ++row)
			for (int col = col1; col <= col2; ++col) {
				Cellularity chunk = getChunk(col, row);
				if (chunk != null) {
					chunk.prerender();
				}
			}
		for (int row = row1; row <= row2; ++row)
			for (int col = col1; col <= col2; ++col) {
				Cellularity chunk = getChunk(col, row);
				if (chunk != null) {
					LinkedHashTable dynamics = chunk.getCellularities();
					for (List.Node i = dynamics.begin(); i != dynamics.end(); i = dynamics.next(i)) {
						Cellularity cellularity = (Cellularity) dynamics.val(i);
						cellularity.render(mHiddenCells, pRenderBuffers, pOffsetX, pOffsetY,
								pCameraX, pCameraY, pCellWidth, pCellHeight, col, row, pWidth,
								pHeight, 0);
					}
				}
			}
		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z) {
			for (int row = row1; row <= row2; ++row)
				for (int col = col1; col <= col2; ++col) {
					Cellularity chunk = getChunk(col, row);
					if (chunk != null) {
						chunk.render(mHiddenCells, pRenderBuffers, pOffsetX, pOffsetY, pCameraX,
								pCameraY, pCellWidth, pCellHeight, col, row, pWidth, pHeight, z);
					}
				}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ChunkLoader implements Runnable {

		private static final Array sPool = Array.obtain();

		private MainTissularity mTissularity;
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

		public static ChunkLoader obtain(MainTissularity pTissularity, int pX, int pY,
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
			for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
				for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
					for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
						Cell cell = chunk.getCell(x, y, z);
						if (cell instanceof AirCell) {
							int[] colors = chunk.getLight(x, y, z);
							if (colors[0] > 128
									&& colors[1] > 128
									&& colors[2] > 128
									&& chunk.getCell(x, y - 1, z) instanceof GroundCell
									&& NoiseMath.hash(NoiseMath.combine(seed, NoiseMath.combine(
											offsetX + x, NoiseMath.combine(offsetY + y, z)))) % 16 == 0) {
								chunk.setCell(x, y, z, TreeCell.obtain(1, 0));
							}
						}
					}
			chunk.drop();
			chunk.postDrop();
			chunk.refresh();
			chunk.precalc(1);
			chunk.prerender();

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
							pY / 256.0, pZ / 64.0, 2.0, 0.85, 7);

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
