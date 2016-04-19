package com.gaskarov.teerain.game.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.cellularity.DynamicCellularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.Controller;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
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
		addVisitor(w, h, 1, 1);
		waitChunks();
		ChunkCellularity chunk = getChunk(chunkX, chunkY);
		chunk.setCell(w & Settings.CHUNK_SIZE_MASK, h & Settings.CHUNK_SIZE_MASK, 0,
				Cells.CELL_TYPE_TEE, TeeCellData.obtain(0));
		waitChunks();
		removeVisitor(w, h, 1, 1);
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

		private static final int BUFFER_SIZE = 4096;

		private static final Array sPool = Array.obtain();

		private int mTmpN;
		private int mTmpId;

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

		private int read(RandomAccessFile pFile, byte[] pBuffer) {
			if (mTmpId == mTmpN) {
				try {
					mTmpN = pFile.read(pBuffer, 0, BUFFER_SIZE);
					if (mTmpN == -1)
						return -1;
				} catch (IOException e) {
					e.printStackTrace();
				}
				mTmpId = 0;
			}
			return pBuffer[mTmpId++] + 128;
		}

		private void write(RandomAccessFile pFile, byte[] pBuffer, int pVal) {
			if (mTmpId == BUFFER_SIZE) {
				try {
					pFile.write(pBuffer, 0, BUFFER_SIZE);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mTmpId = 0;
			}
			pBuffer[mTmpId++] = (byte) (pVal - 128);
		}

		private void write(RandomAccessFile pFile, byte[] pBuffer) {
			try {
				pFile.write(pBuffer, 0, mTmpId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private int readInt(RandomAccessFile pFile, byte[] pBuffer) {
			return read(pFile, pBuffer) | (read(pFile, pBuffer) << 8)
					| (read(pFile, pBuffer) << 16) | (read(pFile, pBuffer) << 24);
		}

		private float readFloat(RandomAccessFile pFile, byte[] pBuffer) {
			return Float.intBitsToFloat(readInt(pFile, pBuffer));
		}

		private void writeInt(RandomAccessFile pFile, byte[] pBuffer, int pVal) {
			write(pFile, pBuffer, pVal & 255);
			write(pFile, pBuffer, (pVal >>> 8) & 255);
			write(pFile, pBuffer, (pVal >>> 16) & 255);
			write(pFile, pBuffer, pVal >>> 24);
		}

		private void writeFloat(RandomAccessFile pFile, byte[] pBuffer, float pVal) {
			writeInt(pFile, pBuffer, Float.floatToRawIntBits(pVal));
		}

		private void load() {

			ChunkCellularity chunk = ChunkCellularity.obtain();

			File file = Gdx.files.local("chunk" + mX + "_" + mY).file();
			try {
				RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
				try {
					byte[] buffer = BinaryByteArrayPool.obtain(BUFFER_SIZE);
					mTmpN = 0;
					mTmpId = 0;
					for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
						for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
							for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
								int cell = readInt(randomAccessFile, buffer);
								chunk.setCell(x, y, z, cell, null);
								chunk.setLight(x, y, z, 0, 0, 0);
								chunk.setAI(x, y, z, 0);
							}
					int dynamicsN = readInt(randomAccessFile, buffer);
					for (int i = 0; i < dynamicsN; ++i) {
						float posX = readFloat(randomAccessFile, buffer);
						float posY = readFloat(randomAccessFile, buffer);
						float angle = readFloat(randomAccessFile, buffer);
						float vx = readFloat(randomAccessFile, buffer);
						float vy = readFloat(randomAccessFile, buffer);
						float angularVelocity = readFloat(randomAccessFile, buffer);
						DynamicCellularity cellularity =
								DynamicCellularity.obtain(posX, posY, angle);
						cellularity.getBody().setVelocity(vx, vy);
						cellularity.getBody().setAngularVelocity(angularVelocity);
						int num = readInt(randomAccessFile, buffer);
						for (int j = 0; j < num; ++j) {
							int x = read(randomAccessFile, buffer);
							int y = read(randomAccessFile, buffer);
							int z = read(randomAccessFile, buffer);
							int cell = readInt(randomAccessFile, buffer);
							cellularity.setCell(x, y, z, cell, null);
						}
						chunk.pushCellularity(cellularity);
					}

					BinaryByteArrayPool.recycle(buffer);
					randomAccessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				long seed = 0;
				int offsetX = mX << Settings.CHUNK_SIZE_LOG;
				int offsetY = mY << Settings.CHUNK_SIZE_LOG;
				for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
					for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
						for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
							chunk.setCell(x, y, z, genWorld(seed, offsetX + x, offsetY + y, z),
									null);
							chunk.setLight(x, y, z, 0, 0, 0);
							chunk.setAI(x, y, z, 0);
						}
				for (int i = 0; i < 16; ++i) {
					chunk.precalcCells(chunk.cellUpdate());
					chunk.updateCells();
				}
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

			File file = Gdx.files.local("chunk" + mX + "_" + mY).file();
			try {
				RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
				try {
					randomAccessFile.setLength(0);
					byte[] buffer = BinaryByteArrayPool.obtain(BUFFER_SIZE);
					mTmpN = 0;
					mTmpId = 0;
					for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
						for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
							for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
								int cell = chunk.getCell(x, y, z);
								writeInt(randomAccessFile, buffer, cell);
							}

					int dynamicsN = 0;
					LinkedHashTable[] dynamics = chunk.getDynamics();
					for (int i = 0; i < Settings.CHUNK_REGIONS_SQUARE; ++i)
						dynamicsN += dynamics[i].size();
					writeInt(randomAccessFile, buffer, dynamicsN);
					for (int i = 0; i < Settings.CHUNK_REGIONS_SQUARE; ++i) {
						for (List.Node k = dynamics[i].begin(); k != dynamics[i].end(); k =
								dynamics[i].next(k)) {
							DynamicCellularity cellularity =
									(DynamicCellularity) dynamics[i].val(k);
							MetaBody body = cellularity.getBody();
							writeFloat(randomAccessFile, buffer, body.getPositionX());
							writeFloat(randomAccessFile, buffer, body.getPositionY());
							writeFloat(randomAccessFile, buffer, body.getAngle());
							writeFloat(randomAccessFile, buffer, body.getVelocityX());
							writeFloat(randomAccessFile, buffer, body.getVelocityY());
							writeFloat(randomAccessFile, buffer, body.getAngularVelocity());
							int num = cellularity.size();
							writeInt(randomAccessFile, buffer, num);
							for (int j = 0; j < num; ++j) {
								int x = cellularity.getCellKeyX(j);
								int y = cellularity.getCellKeyY(j);
								int z = cellularity.getCellKeyZ(j);
								write(randomAccessFile, buffer, x);
								write(randomAccessFile, buffer, y);
								write(randomAccessFile, buffer, z);
								writeInt(randomAccessFile, buffer, cellularity.getCell(x, y, z));
							}
						}
					}

					write(randomAccessFile, buffer);

					BinaryByteArrayPool.recycle(buffer);
					randomAccessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

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
