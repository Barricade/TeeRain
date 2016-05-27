package com.gaskarov.teerain.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Player;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.util.GraphicsUtils;
import com.gaskarov.teerain.resource.Cells;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.teerain.resource.TeeCellData;
import com.gaskarov.util.common.IntVector1;
import com.gaskarov.util.common.KeyValuePair;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.List;
import com.gaskarov.util.pool.BinaryByteArrayPool;

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

	private static final int STEP = 180;
	private static final int DAY_DURATION = STEP * 24 * 10;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private IntVector1 mTmpVec1 = IntVector1.obtain(0);

	private String mMapName;

	private Controller mController;

	private long mSeed;

	private int mDay;
	private int mDayTime;

	private Player mZombie;

	// ===========================================================
	// Constructors
	// ===========================================================

	private GameTissularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getMapName() {
		return mMapName;
	}

	public void setMapName(String pMapName) {
		mMapName = pMapName;
	}

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
		loadMapData();
		setPlayer(Settings.ZOMBIE_ID, mZombie);
	}

	@Override
	public void detach() {
		removePlayer(Settings.ZOMBIE_ID, mZombie);
		unloadMapData();
		super.detach();
	}

	@Override
	public void setPlayer(int pId, Player pPlayer) {
		pPlayer.loadPlayer(Gdx.files.local(
				"maps/" + mMapName + "/players/player" + pId).file());
		super.setPlayer(pId, pPlayer);
	}

	@Override
	public void removePlayer(int pId, Player pPlayer) {
		pPlayer.unloadPlayer(Gdx.files.local(
				"maps/" + mMapName + "/players/player" + pId).file());
		super.removePlayer(pId, pPlayer);
	}

	@Override
	public int getSpawnY(int pX, int pZ) {
		return TerrainGenerator.getSpawnY(mSeed, pX, pZ);
	}

	@Override
	public void renderTick(int pOffsetX, int pOffsetY, float pCameraX,
			float pCameraY, float pCellSize, float pWidth, float pHeight) {
		FloatArray buffer = mOrganularity.getRenderBufferA();
		int n = buffer.size();
		buffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
		int r = Math.min(255, getSkyR(mDayTime));
		int g = Math.min(255, getSkyG(mDayTime));
		int b = Math.min(255, getSkyB(mDayTime));
		float color = Color.toFloatBits(r, g, b, 255);
		GraphicsUtils.drawTexture(0f, 0f, 1f, 1f, Settings.TILE_W * 11,
				Settings.TILE_H * 0, Settings.TILE_W * 12, Settings.TILE_H * 1,
				buffer.data(), n, color, color, color, color);
		super.renderTick(pOffsetX, pOffsetY, pCameraX, pCameraY, pCellSize,
				pWidth, pHeight);
	}

	@Override
	protected void tickUpdates() {
		if (++mDayTime == DAY_DURATION) {
			++mDay;
			mDayTime = 0;
		}
		updateSky();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks
				.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks
					.val(i)).mB).chunk();
			if (chunk != null) {
				for (int j = 0; j < 1; ++j) {
					KeyValuePair pair = (KeyValuePair) mControllables
							.get(mTmpVec1.set(Settings.ZOMBIE_ID));
					if (pair == null || ((LinkedHashTable) pair.mB).size() < 32) {
						int x = (int) (Math.random() * Settings.CHUNK_SIZE);
						int y = (int) (Math.random() * Settings.CHUNK_SIZE);
						int[] light = chunk.getLight(x, y, 0);
						if (light[0] == 0
								&& light[1] == 0
								&& light[2] == 0
								&& chunk.getCell(x, y, 0) == Cells.CELL_TYPE_AIR
								&& (chunk.getAI(x, y, 0) & 255) != 0)
							chunk.setCell(x, y, 0, Cells.CELL_TYPE_TEE_ZOMBIE,
									TeeCellData
											.obtain(Settings.ZOMBIE_ID, true));
					}
				}
			}
		}
		mController.tick(this);
	}

	@Override
	public boolean keyDown(int pKeycode) {
		if (pKeycode == Keys.ESCAPE) {
			Gdx.app.exit();
			return false;
		}
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
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer,
			int pButton) {
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
				return sPool.size() == 0 ? new GameTissularity()
						: (GameTissularity) sPool.pop();
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
		obj.mZombie = Player.obtain(Settings.ZOMBIE_ID, true);
		obj.mZombie.setItem(Player.USE_ITEM_MIN_ID, Cells.CELL_TYPE_HAMMER,
				null, 1);
		return obj;
	}

	public static void recycle(GameTissularity pObj) {
		Player.recycle(pObj.mZombie);
		pObj.mZombie = null;
		pObj.mController = null;
		pObj.dispose();
		recyclePure(pObj);
	}

	private int getSkyR(int pDayTime) {
		int step = pDayTime / STEP;
		if (210 <= step || step < 60)
			return 0;
		else if (60 <= step && step < 80) {
			return Math.min(256, (step - 60) * 16);
		} else if (190 <= step && step < 210) {
			return Math.max(0, 256 - (step - 190) * 16);
		} else
			return 256;
	}

	private int getSkyG(int pDayTime) {
		int step = pDayTime / STEP;
		if (210 <= step || step < 60)
			return 0;
		else if (60 <= step && step < 80) {
			return Math.min(256, (step - 60) * 16);
		} else if (190 <= step && step < 210) {
			return Math.max(0, 256 - (step - 190) * 16);
		} else
			return 256;
	}

	private int getSkyB(int pDayTime) {
		int step = pDayTime / STEP;
		if (210 <= step || step < 60)
			return 0;
		else if (60 <= step && step < 80) {
			return Math.min(256, (step - 60) * 16);
		} else if (190 <= step && step < 210) {
			return Math.max(0, 256 - (step - 190) * 16);
		} else
			return 256;
	}

	public void updateSky() {
		int r = getSkyR(mDayTime);
		int g = getSkyG(mDayTime);
		int b = getSkyB(mDayTime);
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks
				.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks
					.val(i)).mB).chunk();
			if (chunk != null) {
				chunk.setSky(r, g, b);
			}
		}
	}

	public void renderDebug() {
		if (Settings.BOX2D_DEBUG_DRAW) {
			synchronized (mOrganularity) {
				OrthographicCamera camera = mOrganularity.getCamera();
				Matrix4 box2DDebugCameraMatrix = mOrganularity
						.getBox2DDebugCameraMatrix();
				box2DDebugCameraMatrix.set(camera.combined);
				box2DDebugCameraMatrix.scale(Settings.TILE_RENDER,
						Settings.TILE_RENDER, 1f);
				box2DDebugCameraMatrix.translate(-mCameraX, -mCameraY, 0);
				mOrganularity.getBox2DDebugRenderer().render(
						mOrganularity.getWorld(), box2DDebugCameraMatrix);
			}
		}
	}

	private void loadMapData() {
		File file = Gdx.files.local("maps/" + mMapName + "/mapData").file();

		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			byte[] buffer = BinaryByteArrayPool
					.obtain(TerrainGenerator.BUFFER_SIZE);
			IntVector1 tmpN = IntVector1.obtain(0);
			IntVector1 tmpId = IntVector1.obtain(0);
			mSeed = TerrainGenerator.readLong(randomAccessFile, buffer, tmpN,
					tmpId);
			mDay = TerrainGenerator.readInt(randomAccessFile, buffer, tmpN,
					tmpId);
			mDayTime = TerrainGenerator.readInt(randomAccessFile, buffer, tmpN,
					tmpId);
			IntVector1.recycle(tmpN);
			IntVector1.recycle(tmpId);

			BinaryByteArrayPool.recycle(buffer);
			randomAccessFile.close();
		} catch (IOException e) {
			mSeed = mOrganularity.random();
			mDay = 0;
			mDayTime = 80 * STEP;
		}
	}

	private void unloadMapData() {
		File file = Gdx.files.local("maps/" + mMapName + "/mapData").file();

		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			randomAccessFile.setLength(0);
			byte[] buffer = BinaryByteArrayPool
					.obtain(TerrainGenerator.BUFFER_SIZE);
			IntVector1 tmpN = IntVector1.obtain(0);
			IntVector1 tmpId = IntVector1.obtain(0);

			TerrainGenerator.writeLong(randomAccessFile, buffer, tmpN, tmpId,
					mSeed);
			TerrainGenerator.writeInt(randomAccessFile, buffer, tmpN, tmpId,
					mDay);
			TerrainGenerator.writeInt(randomAccessFile, buffer, tmpN, tmpId,
					mDayTime);

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

		public static ChunkLoader obtain(GameTissularity pTissularity, int pX,
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

			try {
				chunk.setSky(mTissularity.getSkyR(mTissularity.mDayTime),
						mTissularity.getSkyG(mTissularity.mDayTime),
						mTissularity.getSkyB(mTissularity.mDayTime));
				TerrainGenerator.loadChunk(
						Gdx.files.local(
								"maps/" + mTissularity.mMapName + "/chunk" + mX
										+ "_" + mY).file(), chunk, mX, mY);
			} catch (FileNotFoundException e) {
				chunk.setSky(256, 256, 256);
				TerrainGenerator.genChunk(mTissularity.mSeed, chunk, mX, mY);
				for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
					for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
						for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
							chunk.setLight(x, y, z, 0, 0, 0);
						}
				chunk.setSky(mTissularity.getSkyR(mTissularity.mDayTime),
						mTissularity.getSkyG(mTissularity.mDayTime),
						mTissularity.getSkyB(mTissularity.mDayTime));
				for (int i = 0; i < 16; ++i) {
					chunk.precalcLight(chunk.lightUpdate());
					chunk.updateLight();
				}
				chunk.precalcLight(chunk.lightUpdate());
			} catch (IOException e) {
				e.printStackTrace();
			}

			mChunkHolder.setChunk(chunk);

			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

		private void unload() {
			ChunkCellularity chunk = mChunkHolder.getChunk();
			mChunkHolder.setChunk(null);

			try {
				TerrainGenerator.unloadChunk(
						Gdx.files.local(
								"maps/" + mTissularity.mMapName + "/chunk" + mX
										+ "_" + mY).file(), chunk, mX, mY);
			} catch (IOException e) {
				e.printStackTrace();
			}

			ChunkCellularity.recycle(chunk);

			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

	}

}
