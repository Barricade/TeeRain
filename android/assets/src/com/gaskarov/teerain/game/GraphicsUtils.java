package com.gaskarov.teerain.game;

import static com.gaskarov.teerain.core.util.Settings.CHUNK_HSIZE;

import com.badlogic.gdx.graphics.Color;
import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.ArrayConstants;
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

	public static int debugRender(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pCos, float pSin) {
		int count = 0;
		if (pZ == 0) {
			count +=
					GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pCos, pSin,
							8 * Settings.TILE_W, 0 * Settings.TILE_H, 255, 255, 255, ((pCellularity
									.getAI(pX, pY, pZ) >> 8) & 15) * 42);
			count +=
					GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pCos, pSin,
							0 * Settings.TILE_W, 7 * Settings.TILE_H, 255, 255, 255, (pCellularity
									.getAI(pX, pY, pZ) >>> 12) * 16);
			count +=
					GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pCos, pSin,
							0 * Settings.TILE_W, 1 * Settings.TILE_H, 255, 0, 0, (pCellularity
									.getAI(pX, pY, pZ) & 255) / 2);
		}
		return count;
	}

	public static int render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pCos, float pSin, float pTileTX, float pTileTY, int pR, int pG, int pB, int pA) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int smallTileX = getTileX(pCellularity, pX) * 2;
		int smallTileY = getTileY(pCellularity, pY) * 2;

		float color = Color.toFloatBits(pR, pG, pB, pA);

		drawTexture(smallTileX, smallTileY, 2, 2, pTileTX, pTileTY, pTileTX + Settings.TILE_W,
				pTileTY + Settings.TILE_H, tissularity.getGraphicsModule().getFloatBuffer(), color,
				color, color, color);
		return 1;
	}

	public static int renderTexture(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pCos, float pSin, float pTileTX, float pTileTY, float pTileWidth,
			float pTileHeight, float pPositionX, float pPositionY, float pLocalWidth,
			float pLocalHeight, float pLocalCos, float pLocalSin) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int tileX = getTileX(pCellularity, pX);
		int tileY = getTileY(pCellularity, pY);

		int rtR = 0;
		int rtG = 0;
		int rtB = 0;
		int ltR = 0;
		int ltG = 0;
		int ltB = 0;
		int lbR = 0;
		int lbG = 0;
		int lbB = 0;
		int rbR = 0;
		int rbG = 0;
		int rbB = 0;
		float offsetX = 0f;
		float offsetY = 0f;
		int offset = 0;
		if (!pCellularity.isChunk()) {
			offsetX = pCellularity.getBody().getPositionX();
			offsetY = pCellularity.getBody().getPositionY();
			offset = CHUNK_HSIZE;
		}
		float x = pX - offset + pPositionX;
		float y = pY - offset + pPositionY;
		float rtTmpX = x + (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) / 2;
		float rtTmpY = y + (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) / 2;
		float ltTmpX = x + (-pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) / 2;
		float ltTmpY = y + (-pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) / 2;
		float lbTmpX = x - (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) / 2;
		float lbTmpY = y - (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) / 2;
		float rbTmpX = x + (pLocalCos * pLocalWidth + pLocalSin * pLocalHeight) / 2;
		float rbTmpY = y + (pLocalSin * pLocalWidth - pLocalCos * pLocalHeight) / 2;
		float rtX = offsetX + pCos * rtTmpX - pSin * rtTmpY;
		float rtY = offsetY + pSin * rtTmpX + pCos * rtTmpY;
		float ltX = offsetX + pCos * ltTmpX - pSin * ltTmpY;
		float ltY = offsetY + pSin * ltTmpX + pCos * ltTmpY;
		float lbX = offsetX + pCos * lbTmpX - pSin * lbTmpY;
		float lbY = offsetY + pSin * lbTmpX + pCos * lbTmpY;
		float rbX = offsetX + pCos * rbTmpX - pSin * rbTmpY;
		float rbY = offsetY + pSin * rbTmpX + pCos * rbTmpY;
		int rtPosX = MathUtils.floor(rtX);
		int rtPosY = MathUtils.floor(rtY);
		int ltPosX = MathUtils.floor(ltX);
		int ltPosY = MathUtils.floor(ltY);
		int lbPosX = MathUtils.floor(lbX);
		int lbPosY = MathUtils.floor(lbY);
		int rbPosX = MathUtils.floor(rbX);
		int rbPosY = MathUtils.floor(rbY);
		float rtLocalX = rtX - rtPosX;
		float rtLocalY = rtY - rtPosY;
		float ltLocalX = ltX - ltPosX;
		float ltLocalY = ltY - ltPosY;
		float lbLocalX = lbX - lbPosX;
		float lbLocalY = lbY - lbPosY;
		float rbLocalX = rbX - rbPosX;
		float rbLocalY = rbY - rbPosY;
		rtR = chunk.getLightR(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
		rtG = chunk.getLightG(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
		rtB = chunk.getLightB(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
		ltR = chunk.getLightR(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
		ltG = chunk.getLightG(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
		ltB = chunk.getLightB(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
		lbR = chunk.getLightR(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
		lbG = chunk.getLightG(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
		lbB = chunk.getLightB(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
		rbR = chunk.getLightR(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
		rbG = chunk.getLightG(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
		rbB = chunk.getLightB(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
		rtR = Math.min(rtR, 255);
		rtG = Math.min(rtG, 255);
		rtB = Math.min(rtB, 255);
		ltR = Math.min(ltR, 255);
		ltG = Math.min(ltG, 255);
		ltB = Math.min(ltB, 255);
		lbR = Math.min(lbR, 255);
		lbG = Math.min(lbG, 255);
		lbB = Math.min(lbB, 255);
		rbR = Math.min(rbR, 255);
		rbG = Math.min(rbG, 255);
		rbB = Math.min(rbB, 255);
		float colorRT = Color.toFloatBits(rtR, rtG, rtB, 255);
		float colorLT = Color.toFloatBits(ltR, ltG, ltB, 255);
		float colorLB = Color.toFloatBits(lbR, lbG, lbB, 255);
		float colorRB = Color.toFloatBits(rbR, rbG, rbB, 255);

		drawTexture(tileX + pPositionX, tileY + pPositionY, pLocalWidth, pLocalHeight, pLocalCos,
				pLocalSin, pTileTX, pTileTY, pTileTX + pTileWidth, pTileTY + pTileHeight,
				tissularity.getGraphicsModule().getFloatBuffer(), colorRT, colorLT, colorLB,
				colorRB);
		return 1;
	}

	public static int render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pCos, float pSin, float pTileTX, float pTileTY) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int smallTileX = getTileX(pCellularity, pX) * 2;
		int smallTileY = getTileY(pCellularity, pY) * 2;

		int rtR = 0;
		int rtG = 0;
		int rtB = 0;
		int ltR = 0;
		int ltG = 0;
		int ltB = 0;
		int lbR = 0;
		int lbG = 0;
		int lbB = 0;
		int rbR = 0;
		int rbG = 0;
		int rbB = 0;
		if (pCellularity.isChunk()) {
			final int[] lightCorners = pCellularity.getLightCorners();
			final int val = pCellularity.getLightCornersOffset(pX, pY, pZ);
			rtR = lightCorners[val];
			rtG = lightCorners[val + 1];
			rtB = lightCorners[val + 2];
			ltR = lightCorners[val + 4];
			ltG = lightCorners[val + 5];
			ltB = lightCorners[val + 6];
			lbR = lightCorners[val + 8];
			lbG = lightCorners[val + 9];
			lbB = lightCorners[val + 10];
			rbR = lightCorners[val + 12];
			rbG = lightCorners[val + 13];
			rbB = lightCorners[val + 14];
		} else {
			float offsetX = pCellularity.getBody().getPositionX();
			float offsetY = pCellularity.getBody().getPositionY();
			float rtX = offsetX + pCos * (pX - CHUNK_HSIZE + 1) - pSin * (pY - CHUNK_HSIZE + 1);
			float rtY = offsetY + pSin * (pX - CHUNK_HSIZE + 1) + pCos * (pY - CHUNK_HSIZE + 1);
			float ltX = offsetX + pCos * (pX - CHUNK_HSIZE) - pSin * (pY - CHUNK_HSIZE + 1);
			float ltY = offsetY + pSin * (pX - CHUNK_HSIZE) + pCos * (pY - CHUNK_HSIZE + 1);
			float lbX = offsetX + pCos * (pX - CHUNK_HSIZE) - pSin * (pY - CHUNK_HSIZE);
			float lbY = offsetY + pSin * (pX - CHUNK_HSIZE) + pCos * (pY - CHUNK_HSIZE);
			float rbX = offsetX + pCos * (pX - CHUNK_HSIZE + 1) - pSin * (pY - CHUNK_HSIZE);
			float rbY = offsetY + pSin * (pX - CHUNK_HSIZE + 1) + pCos * (pY - CHUNK_HSIZE);
			int rtPosX = MathUtils.floor(rtX);
			int rtPosY = MathUtils.floor(rtY);
			int ltPosX = MathUtils.floor(ltX);
			int ltPosY = MathUtils.floor(ltY);
			int lbPosX = MathUtils.floor(lbX);
			int lbPosY = MathUtils.floor(lbY);
			int rbPosX = MathUtils.floor(rbX);
			int rbPosY = MathUtils.floor(rbY);
			float rtLocalX = rtX - rtPosX;
			float rtLocalY = rtY - rtPosY;
			float ltLocalX = ltX - ltPosX;
			float ltLocalY = ltY - ltPosY;
			float lbLocalX = lbX - lbPosX;
			float lbLocalY = lbY - lbPosY;
			float rbLocalX = rbX - rbPosX;
			float rbLocalY = rbY - rbPosY;
			rtR = chunk.getLightR(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
			rtG = chunk.getLightG(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
			rtB = chunk.getLightB(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
			ltR = chunk.getLightR(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
			ltG = chunk.getLightG(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
			ltB = chunk.getLightB(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
			lbR = chunk.getLightR(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
			lbG = chunk.getLightG(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
			lbB = chunk.getLightB(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
			rbR = chunk.getLightR(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
			rbG = chunk.getLightG(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
			rbB = chunk.getLightB(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
		}
		rtR = Math.min(rtR, 255);
		rtG = Math.min(rtG, 255);
		rtB = Math.min(rtB, 255);
		ltR = Math.min(ltR, 255);
		ltG = Math.min(ltG, 255);
		ltB = Math.min(ltB, 255);
		lbR = Math.min(lbR, 255);
		lbG = Math.min(lbG, 255);
		lbB = Math.min(lbB, 255);
		rbR = Math.min(rbR, 255);
		rbG = Math.min(rbG, 255);
		rbB = Math.min(rbB, 255);
		float colorRT = Color.toFloatBits(rtR, rtG, rtB, 255);
		float colorLT = Color.toFloatBits(ltR, ltG, ltB, 255);
		float colorLB = Color.toFloatBits(lbR, lbG, lbB, 255);
		float colorRB = Color.toFloatBits(rbR, rbG, rbB, 255);

		drawTexture(smallTileX, smallTileY, 2, 2, pTileTX, pTileTY, pTileTX + Settings.TILE_W,
				pTileTY + Settings.TILE_H, tissularity.getGraphicsModule().getFloatBuffer(),
				colorRT, colorLT, colorLB, colorRB);
		return 1;
	}

	public static int render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pCos, float pSin, float pTileNX, float pTileNY, float pTileSX, float pTileSY,
			float pTileHX, float pTileHY, float pTileVX, float pTileVY, float pTileBX, float pTileBY) {

		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int smallTileX = getTileX(pCellularity, pX) * 2;
		int smallTileY = getTileY(pCellularity, pY) * 2;

		int rtR = 0;
		int rtG = 0;
		int rtB = 0;
		int ltR = 0;
		int ltG = 0;
		int ltB = 0;
		int lbR = 0;
		int lbG = 0;
		int lbB = 0;
		int rbR = 0;
		int rbG = 0;
		int rbB = 0;
		if (pCellularity.isChunk()) {
			final int[] lightCorners = pCellularity.getLightCorners();
			final int val = pCellularity.getLightCornersOffset(pX, pY, pZ);
			rtR = lightCorners[val];
			rtG = lightCorners[val + 1];
			rtB = lightCorners[val + 2];
			ltR = lightCorners[val + 4];
			ltG = lightCorners[val + 5];
			ltB = lightCorners[val + 6];
			lbR = lightCorners[val + 8];
			lbG = lightCorners[val + 9];
			lbB = lightCorners[val + 10];
			rbR = lightCorners[val + 12];
			rbG = lightCorners[val + 13];
			rbB = lightCorners[val + 14];
		} else {
			float offsetX = pCellularity.getBody().getPositionX();
			float offsetY = pCellularity.getBody().getPositionY();
			float rtX = offsetX + pCos * (pX - CHUNK_HSIZE + 1) - pSin * (pY - CHUNK_HSIZE + 1);
			float rtY = offsetY + pSin * (pX - CHUNK_HSIZE + 1) + pCos * (pY - CHUNK_HSIZE + 1);
			float ltX = offsetX + pCos * (pX - CHUNK_HSIZE) - pSin * (pY - CHUNK_HSIZE + 1);
			float ltY = offsetY + pSin * (pX - CHUNK_HSIZE) + pCos * (pY - CHUNK_HSIZE + 1);
			float lbX = offsetX + pCos * (pX - CHUNK_HSIZE) - pSin * (pY - CHUNK_HSIZE);
			float lbY = offsetY + pSin * (pX - CHUNK_HSIZE) + pCos * (pY - CHUNK_HSIZE);
			float rbX = offsetX + pCos * (pX - CHUNK_HSIZE + 1) - pSin * (pY - CHUNK_HSIZE);
			float rbY = offsetY + pSin * (pX - CHUNK_HSIZE + 1) + pCos * (pY - CHUNK_HSIZE);
			int rtPosX = MathUtils.floor(rtX);
			int rtPosY = MathUtils.floor(rtY);
			int ltPosX = MathUtils.floor(ltX);
			int ltPosY = MathUtils.floor(ltY);
			int lbPosX = MathUtils.floor(lbX);
			int lbPosY = MathUtils.floor(lbY);
			int rbPosX = MathUtils.floor(rbX);
			int rbPosY = MathUtils.floor(rbY);
			float rtLocalX = rtX - rtPosX;
			float rtLocalY = rtY - rtPosY;
			float ltLocalX = ltX - ltPosX;
			float ltLocalY = ltY - ltPosY;
			float lbLocalX = lbX - lbPosX;
			float lbLocalY = lbY - lbPosY;
			float rbLocalX = rbX - rbPosX;
			float rbLocalY = rbY - rbPosY;
			rtR = chunk.getLightR(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
			rtG = chunk.getLightG(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
			rtB = chunk.getLightB(rtLocalX, rtLocalY, rtPosX, rtPosY, pZ);
			ltR = chunk.getLightR(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
			ltG = chunk.getLightG(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
			ltB = chunk.getLightB(ltLocalX, ltLocalY, ltPosX, ltPosY, pZ);
			lbR = chunk.getLightR(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
			lbG = chunk.getLightG(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
			lbB = chunk.getLightB(lbLocalX, lbLocalY, lbPosX, lbPosY, pZ);
			rbR = chunk.getLightR(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
			rbG = chunk.getLightG(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
			rbB = chunk.getLightB(rbLocalX, rbLocalY, rbPosX, rbPosY, pZ);
		}
		rtR = Math.min(rtR, 255);
		rtG = Math.min(rtG, 255);
		rtB = Math.min(rtB, 255);
		ltR = Math.min(ltR, 255);
		ltG = Math.min(ltG, 255);
		ltB = Math.min(ltB, 255);
		lbR = Math.min(lbR, 255);
		lbG = Math.min(lbG, 255);
		lbB = Math.min(lbB, 255);
		rbR = Math.min(rbR, 255);
		rbG = Math.min(rbG, 255);
		rbB = Math.min(rbB, 255);
		float colorRT = Color.toFloatBits(rtR, rtG, rtB, 255);
		float colorLT = Color.toFloatBits(ltR, ltG, ltB, 255);
		float colorLB = Color.toFloatBits(lbR, lbG, lbB, 255);
		float colorRB = Color.toFloatBits(rbR, rbG, rbB, 255);

		int cellHV = 0;
		for (int i = 0; i < ArrayConstants.MOVE_AROUND_X.length; ++i) {
			int vx = ArrayConstants.MOVE_AROUND_X[i];
			int vy = ArrayConstants.MOVE_AROUND_Y[i];
			if (pCell.isTileConnected(pCellularity, pX, pY, pZ, pCellularity.getCell(pX + vx, pY
					+ vy, pZ), vx, vy))
				cellHV |= 1 << i;
		}

		if (cellHV == 255) {
			drawTexture(smallTileX, smallTileY, 2, 2, pTileNX, pTileNY, pTileNX + Settings.TILE_W,
					pTileNY + Settings.TILE_H, tissularity.getGraphicsModule().getFloatBuffer(),
					colorRT, colorLT, colorLB, colorRB);
			return 1;
		}

		int rtrbR = rtR + rbR;
		int ltlbR = ltR + lbR;
		int rtrbG = rtG + rbG;
		int ltlbG = ltG + lbG;
		int rtrbB = rtB + rbB;
		int ltlbB = ltB + lbB;
		int cR = (rtrbR + ltlbR) / 4;
		int cG = (rtrbG + ltlbG) / 4;
		int cB = (rtrbB + ltlbB) / 4;
		int rR = rtrbR / 2;
		int rG = rtrbG / 2;
		int rB = rtrbB / 2;
		int tR = (rtR + ltR) / 2;
		int tG = (rtG + ltG) / 2;
		int tB = (rtB + ltB) / 2;
		int lR = ltlbR / 2;
		int lG = ltlbG / 2;
		int lB = ltlbB / 2;
		int bR = (lbR + rbR) / 2;
		int bG = (lbG + rbG) / 2;
		int bB = (lbB + rbB) / 2;
		float colorC = Color.toFloatBits(cR, cG, cB, 255);
		float colorR = Color.toFloatBits(rR, rG, rB, 255);
		float colorT = Color.toFloatBits(tR, tG, tB, 255);
		float colorL = Color.toFloatBits(lR, lG, lB, 255);
		float colorB = Color.toFloatBits(bR, bG, bB, 255);

		return render(pCell, pCellularity, pX, pY, pZ, pTileNX, pTileNY, pTileSX, pTileSY, pTileHX,
				pTileHY, pTileVX, pTileVY, pTileBX, pTileBY, colorRT, colorLT, colorLB, colorRB,
				colorC, colorR, colorT, colorL, colorB, cellHV);
	}

	public static int render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pTileNX, float pTileNY, float pTileSX, float pTileSY, float pTileHX,
			float pTileHY, float pTileVX, float pTileVY, float pTileBX, float pTileBY,
			float pColorRT, float pColorLT, float pColorLB, float pColorRB, float pColorC,
			float pColorR, float pColorT, float pColorL, float pColorB, int pCellHV) {

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
			drawTexture(smallTileX + 1, smallTileY + 1, 1, 1, u1, v1, u2, v2, floatBuffer,
					pColorRT, pColorT, pColorC, pColorR);
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
			drawTexture(smallTileX, smallTileY + 1, 1, 1, u1, v1, u2, v2, floatBuffer, pColorT,
					pColorLT, pColorL, pColorC);
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
			drawTexture(smallTileX, smallTileY, 1, 1, u1, v1, u2, v2, floatBuffer, pColorC,
					pColorL, pColorLB, pColorB);
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
			drawTexture(smallTileX + 1, smallTileY, 1, 1, u1, v1, u2, v2, floatBuffer, pColorR,
					pColorC, pColorB, pColorRB);
		}

		return 4;
	}

	public static void drawTexture(int pTileX, int pTileY, int pTilesWidth, int pTilesHeight,
			float pU1, float pV1, float pU2, float pV2, FloatArray pFloatBuffer, float pColorRT,
			float pColorLT, float pColorLB, float pColorRB) {

		float tmpRX = (pTileX + pTilesWidth) * 0.5f;
		float tmpRY = (pTileY + pTilesHeight) * 0.5f;
		float tmpLX = pTileX * 0.5f;
		float tmpLY = pTileY * 0.5f;

		pFloatBuffer.push(tmpRX);
		pFloatBuffer.push(tmpRY);
		pFloatBuffer.push(pColorRT);
		pFloatBuffer.push(pU2);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLX);
		pFloatBuffer.push(tmpRY);
		pFloatBuffer.push(pColorLT);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLX);
		pFloatBuffer.push(tmpLY);
		pFloatBuffer.push(pColorLB);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV2);

		pFloatBuffer.push(tmpRX);
		pFloatBuffer.push(tmpLY);
		pFloatBuffer.push(pColorRB);
		pFloatBuffer.push(pU2);
		pFloatBuffer.push(pV2);
	}

	public static void drawTexture(float pPositionX, float pPositionY, float pLocalWidth,
			float pLocalHeight, float pLocalCos, float pLocalSin, float pU1, float pV1, float pU2,
			float pV2, FloatArray pFloatBuffer, float pColorRT, float pColorLT, float pColorLB,
			float pColorRB) {

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
		pFloatBuffer.push(pColorRT);
		pFloatBuffer.push(pU2);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLTX);
		pFloatBuffer.push(tmpLTY);
		pFloatBuffer.push(pColorLT);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV1);

		pFloatBuffer.push(tmpLBX);
		pFloatBuffer.push(tmpLBY);
		pFloatBuffer.push(pColorLB);
		pFloatBuffer.push(pU1);
		pFloatBuffer.push(pV2);

		pFloatBuffer.push(tmpRBX);
		pFloatBuffer.push(tmpRBY);
		pFloatBuffer.push(pColorRB);
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
