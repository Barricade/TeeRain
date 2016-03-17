package com.gaskarov.teerain.util;

import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cell.Cell;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.tissularity.Tissularity;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.IntArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class GraphicsUtils {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private GraphicsUtils() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static int renderTexture(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pTileTX, float pTileTY, float pTileWidth, float pTileHeight, float pPositionX,
			float pPositionY, float pLocalWidth, float pLocalHeight, float pLocalCos,
			float pLocalSin) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int tileX = getTileX(pCellularity, pX);
		int tileY = getTileY(pCellularity, pY);

		drawTexture(tileX + pPositionX, tileY + pPositionY, pLocalWidth, pLocalHeight, pLocalCos,
				pLocalSin, pTileTX, pTileTY, pTileTX + pTileWidth, pTileTY + pTileHeight,
				tissularity.getGraphicsModule().getFloatBuffer());
		return 1;
	}

	public static int render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pTileTX, float pTileTY) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int smallTileX = getTileX(pCellularity, pX) * 2;
		int smallTileY = getTileY(pCellularity, pY) * 2;

		drawTexture(smallTileX, smallTileY, 2, 2, pTileTX, pTileTY, pTileTX + Settings.TILE_W,
				pTileTY + Settings.TILE_H, tissularity.getGraphicsModule().getFloatBuffer());
		return 1;
	}

	public static int render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pTileNX, float pTileNY, float pTileSX, float pTileSY, float pTileHX,
			float pTileHY, float pTileVX, float pTileVY, float pTileBX, float pTileBY) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int smallTileX = getTileX(pCellularity, pX) * 2;
		int smallTileY = getTileY(pCellularity, pY) * 2;

		int cellHV = pCell.tilesConnectedMask();

		if (cellHV == 255) {
			drawTexture(smallTileX, smallTileY, 2, 2, pTileNX, pTileNY, pTileNX + Settings.TILE_W,
					pTileNY + Settings.TILE_H, tissularity.getGraphicsModule().getFloatBuffer());
			return 1;
		}

		return render(pCell, pCellularity, pX, pY, pZ, pTileNX, pTileNY, pTileSX, pTileSY, pTileHX,
				pTileHY, pTileVX, pTileVY, pTileBX, pTileBY, cellHV);
	}

	public static int render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pTileNX, float pTileNY, float pTileSX, float pTileSY, float pTileHX,
			float pTileHY, float pTileVX, float pTileVY, float pTileBX, float pTileBY, int pCellHV) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int smallTileX = getTileX(pCellularity, pX) * 2;
		int smallTileY = getTileY(pCellularity, pY) * 2;
		FloatArray floatBuffer = tissularity.getGraphicsModule().getFloatBuffer();

		{
			boolean flagH = (pCellHV & 1) != 0;
			boolean flagV = (pCellHV & 4) != 0;
			float u1 = Settings.TILE_HALF_W, v1 = 0;
			if (flagH && flagV) {
				if ((pCellHV & 2) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			drawTexture(smallTileX + 1, smallTileY + 1, 1, 1, u1, v1, u2, v2, floatBuffer);
		}

		{
			boolean flagH = (pCellHV & 16) != 0;
			boolean flagV = (pCellHV & 4) != 0;
			float u1 = 0, v1 = 0;
			if (flagH && flagV) {
				if ((pCellHV & 8) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			drawTexture(smallTileX, smallTileY + 1, 1, 1, u1, v1, u2, v2, floatBuffer);
		}

		{
			boolean flagH = (pCellHV & 16) != 0;
			boolean flagV = (pCellHV & 64) != 0;
			float u1 = 0, v1 = Settings.TILE_HALF_H;
			if (flagH && flagV) {
				if ((pCellHV & 32) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			drawTexture(smallTileX, smallTileY, 1, 1, u1, v1, u2, v2, floatBuffer);
		}

		{
			boolean flagH = (pCellHV & 1) != 0;
			boolean flagV = (pCellHV & 64) != 0;
			float u1 = Settings.TILE_HALF_W, v1 = Settings.TILE_HALF_H;
			if (flagH && flagV) {
				if ((pCellHV & 128) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			drawTexture(smallTileX + 1, smallTileY, 1, 1, u1, v1, u2, v2, floatBuffer);
		}

		return 4;
	}

	public static void drawTexture(int pTileX, int pTileY, int pTilesWidth, int pTilesHeight,
			float pU1, float pV1, float pU2, float pV2, FloatArray pFloatBuffer) {

		float tmpRX = (pTileX + pTilesWidth) * 0.5f;
		float tmpRY = (pTileY + pTilesHeight) * 0.5f;
		float tmpLX = pTileX * 0.5f;
		float tmpLY = pTileY * 0.5f;

		pFloatBuffer.push(tmpRX);
		pFloatBuffer.push(tmpRY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU2);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLX);
		pFloatBuffer.push(tmpRY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLX);
		pFloatBuffer.push(tmpLY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV2);

		pFloatBuffer.push(tmpRX);
		pFloatBuffer.push(tmpLY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU2);
		pFloatBuffer.push(pV2);
	}

	public static void drawTexture(float pPositionX, float pPositionY, float pLocalWidth,
			float pLocalHeight, float pLocalCos, float pLocalSin, float pU1, float pV1, float pU2,
			float pV2, FloatArray pFloatBuffer) {

		float vrtx = (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) * 0.5f;
		float vrty = (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) * 0.5f;
		float vltx = (-pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) * 0.5f;
		float vlty = (-pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) * 0.5f;
		float vlbx = -vrtx;
		float vlby = -vrty;
		float vrbx = -vltx;
		float vrby = -vlty;
		float tmpRTX = pPositionX + vrtx;
		float tmpRTY = pPositionY + vrty;
		float tmpLTX = pPositionX + vltx;
		float tmpLTY = pPositionY + vlty;
		float tmpRBX = pPositionX + vrbx;
		float tmpRBY = pPositionY + vrby;
		float tmpLBX = pPositionX + vlbx;
		float tmpLBY = pPositionY + vlby;

		pFloatBuffer.push(tmpRTX);
		pFloatBuffer.push(tmpRTY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU2);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLTX);
		pFloatBuffer.push(tmpLTY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLBX);
		pFloatBuffer.push(tmpLBY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV2);

		pFloatBuffer.push(tmpRBX);
		pFloatBuffer.push(tmpRBY);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(1f);
		pFloatBuffer.push(pU2);
		pFloatBuffer.push(pV2);
	}

	public static void count(Cellularity pCellularity, int pCount) {
		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		IntArray intBuffer = tissularity.getGraphicsModule().getIntBuffer();
		intBuffer.push(pCount);
	}

	private static int getTileX(Cellularity pCellularity, int pX) {
		if (pCellularity.isChunk()) {
			Tissularity tissularity = pCellularity.getTissularity();
			return (pCellularity.getX() << Settings.CHUNK_SIZE_LOG) + pX - tissularity.getOffsetX();
		}
		return pX - Settings.CHUNK_HSIZE;
	}

	private static int getTileY(Cellularity pCellularity, int pY) {
		if (pCellularity.isChunk()) {
			Tissularity tissularity = pCellularity.getTissularity();
			return (pCellularity.getY() << Settings.CHUNK_SIZE_LOG) + pY - tissularity.getOffsetY();
		}
		return pY - Settings.CHUNK_HSIZE;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
