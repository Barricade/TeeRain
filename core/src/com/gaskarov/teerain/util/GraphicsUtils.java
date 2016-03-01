package com.gaskarov.teerain.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cell.Cell;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.util.container.FloatArray;

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

	public static void render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pOffsetX, float pOffsetY, int pTileX, int pTileY, float pSize, float pCos,
			float pSin, FloatArray pRenderBuffer, float pTileTX, float pTileTY, float pTileWidth,
			float pTileHeight, float pPositionX, float pPositionY, float pLocalWidth,
			float pLocalHeight, float pLocalCos, float pLocalSin) {
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
		Cellularity chunk;
		float offsetX = 0f;
		float offsetY = 0f;
		if (pCellularity.isChunk()) {
			chunk = pCellularity;
		} else {
			Cellularity dynamic = pCellularity;
			chunk = dynamic.getChunk();
			offsetX = dynamic.getBody().getPositionX();
			offsetY = dynamic.getBody().getPositionY();
		}
		int offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
		{
			float tmpX =
					pX - offset + pPositionX + (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight)
							/ 2;
			float tmpY =
					pY - offset + pPositionY + (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight)
							/ 2;
			float rtX = offsetX + pCos * tmpX - pSin * tmpY;
			float rtY = offsetY + pSin * tmpX + pCos * tmpY;
			int posX = MathUtils.floor(rtX);
			int posY = MathUtils.floor(rtY);
			float x = rtX - posX;
			float y = rtY - posY;
			rtR = chunk.getLightR(x, y, posX, posY, pZ);
			rtG = chunk.getLightG(x, y, posX, posY, pZ);
			rtB = chunk.getLightB(x, y, posX, posY, pZ);
		}
		{
			float tmpX =
					pX - offset + pPositionX
							+ (-pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) / 2;
			float tmpY =
					pY - offset + pPositionY
							+ (-pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) / 2;
			float ltX = offsetX + pCos * tmpX - pSin * tmpY;
			float ltY = offsetY + pSin * tmpX + pCos * tmpY;
			int posX = MathUtils.floor(ltX);
			int posY = MathUtils.floor(ltY);
			float x = ltX - posX;
			float y = ltY - posY;
			ltR = chunk.getLightR(x, y, posX, posY, pZ);
			ltG = chunk.getLightG(x, y, posX, posY, pZ);
			ltB = chunk.getLightB(x, y, posX, posY, pZ);
		}
		{
			float tmpX =
					pX - offset + pPositionX - (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight)
							/ 2;
			float tmpY =
					pY - offset + pPositionY - (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight)
							/ 2;
			float lbX = offsetX + pCos * tmpX - pSin * tmpY;
			float lbY = offsetY + pSin * tmpX + pCos * tmpY;
			int posX = MathUtils.floor(lbX);
			int posY = MathUtils.floor(lbY);
			float x = lbX - posX;
			float y = lbY - posY;
			lbR = chunk.getLightR(x, y, posX, posY, pZ);
			lbG = chunk.getLightG(x, y, posX, posY, pZ);
			lbB = chunk.getLightB(x, y, posX, posY, pZ);
		}
		{
			float tmpX =
					pX - offset + pPositionX + (pLocalCos * pLocalWidth + pLocalSin * pLocalHeight)
							/ 2;
			float tmpY =
					pY - offset + pPositionY + (pLocalSin * pLocalWidth - pLocalCos * pLocalHeight)
							/ 2;
			float rbX = offsetX + pCos * tmpX - pSin * tmpY;
			float rbY = offsetY + pSin * tmpX + pCos * tmpY;
			int posX = MathUtils.floor(rbX);
			int posY = MathUtils.floor(rbY);
			float x = rbX - posX;
			float y = rbY - posY;
			rbR = chunk.getLightR(x, y, posX, posY, pZ);
			rbG = chunk.getLightG(x, y, posX, posY, pZ);
			rbB = chunk.getLightB(x, y, posX, posY, pZ);
		}
		float colorRT =
				Color.toFloatBits(Math.min(rtR, 255), Math.min(rtG, 255), Math.min(rtB, 255), 255);
		float colorLT =
				Color.toFloatBits(Math.min(ltR, 255), Math.min(ltG, 255), Math.min(ltB, 255), 255);
		float colorLB =
				Color.toFloatBits(Math.min(lbR, 255), Math.min(lbG, 255), Math.min(lbB, 255), 255);
		float colorRB =
				Color.toFloatBits(Math.min(rbR, 255), Math.min(rbG, 255), Math.min(rbB, 255), 255);
		int bufferOffset = pRenderBuffer.size();
		pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
		drawTexture(pOffsetX, pOffsetY, pTileX + pPositionX, pTileY + pPositionY, pLocalWidth,
				pLocalHeight, pLocalCos, pLocalSin, pSize, pCos, pSin, pTileTX, pTileTY, pTileTX
						+ pTileWidth, pTileTY + pTileHeight, pRenderBuffer.data(), bufferOffset,
				colorRT, colorLT, colorLB, colorRB);
	}

	public static void render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pOffsetX, float pOffsetY, int pTileX, int pTileY, float pSize, float pCos,
			float pSin, FloatArray pRenderBuffer, float pTileTX, float pTileTY) {
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
			Cellularity dynamic = pCellularity;
			Cellularity chunk = dynamic.getChunk();
			float offsetX = dynamic.getBody().getPositionX();
			float offsetY = dynamic.getBody().getPositionY();
			int offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
			{
				float rtX = offsetX + pCos * (pX - offset + 1) - pSin * (pY - offset + 1);
				float rtY = offsetY + pSin * (pX - offset + 1) + pCos * (pY - offset + 1);
				int posX = MathUtils.floor(rtX);
				int posY = MathUtils.floor(rtY);
				float x = rtX - posX;
				float y = rtY - posY;
				rtR = chunk.getLightR(x, y, posX, posY, pZ);
				rtG = chunk.getLightG(x, y, posX, posY, pZ);
				rtB = chunk.getLightB(x, y, posX, posY, pZ);
			}
			{
				float ltX = offsetX + pCos * (pX - offset) - pSin * (pY - offset + 1);
				float ltY = offsetY + pSin * (pX - offset) + pCos * (pY - offset + 1);
				int posX = MathUtils.floor(ltX);
				int posY = MathUtils.floor(ltY);
				float x = ltX - posX;
				float y = ltY - posY;
				ltR = chunk.getLightR(x, y, posX, posY, pZ);
				ltG = chunk.getLightG(x, y, posX, posY, pZ);
				ltB = chunk.getLightB(x, y, posX, posY, pZ);
			}
			{
				float lbX = offsetX + pCos * (pX - offset) - pSin * (pY - offset);
				float lbY = offsetY + pSin * (pX - offset) + pCos * (pY - offset);
				int posX = MathUtils.floor(lbX);
				int posY = MathUtils.floor(lbY);
				float x = lbX - posX;
				float y = lbY - posY;
				lbR = chunk.getLightR(x, y, posX, posY, pZ);
				lbG = chunk.getLightG(x, y, posX, posY, pZ);
				lbB = chunk.getLightB(x, y, posX, posY, pZ);
			}
			{
				float rbX = offsetX + pCos * (pX - offset + 1) - pSin * (pY - offset);
				float rbY = offsetY + pSin * (pX - offset + 1) + pCos * (pY - offset);
				int posX = MathUtils.floor(rbX);
				int posY = MathUtils.floor(rbY);
				float x = rbX - posX;
				float y = rbY - posY;
				rbR = chunk.getLightR(x, y, posX, posY, pZ);
				rbG = chunk.getLightG(x, y, posX, posY, pZ);
				rbB = chunk.getLightB(x, y, posX, posY, pZ);
			}
		}
		float colorRT =
				Color.toFloatBits(Math.min(rtR, 255), Math.min(rtG, 255), Math.min(rtB, 255), 255);
		float colorLT =
				Color.toFloatBits(Math.min(ltR, 255), Math.min(ltG, 255), Math.min(ltB, 255), 255);
		float colorLB =
				Color.toFloatBits(Math.min(lbR, 255), Math.min(lbG, 255), Math.min(lbB, 255), 255);
		float colorRB =
				Color.toFloatBits(Math.min(rbR, 255), Math.min(rbG, 255), Math.min(rbB, 255), 255);
		int offset = pRenderBuffer.size();
		pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
		drawTexture(pOffsetX, pOffsetY, pTileX * 2, pTileY * 2, 2, 2, pSize / 2, pCos, pSin,
				pTileTX, pTileTY, pTileTX + Settings.TILE_W, pTileTY + Settings.TILE_H,
				pRenderBuffer.data(), offset, colorRT, colorLT, colorLB, colorRB);
	}

	public static boolean render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pOffsetX, float pOffsetY, int pTileX, int pTileY, float pSize, float pCos,
			float pSin, FloatArray pRenderBuffer, float pTileNX, float pTileNY, float pTileSX,
			float pTileSY, float pTileHX, float pTileHY, float pTileVX, float pTileVY,
			float pTileBX, float pTileBY, boolean pTileNTr, boolean pTileSTr, boolean pTileHTr,
			boolean pTileVTr, boolean pTileBTr) {
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
			Cellularity dynamic = pCellularity;
			Cellularity chunk = dynamic.getChunk();
			float offsetX = dynamic.getBody().getPositionX();
			float offsetY = dynamic.getBody().getPositionY();
			int offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
			{
				float rtX = offsetX + pCos * (pX - offset + 1) - pSin * (pY - offset + 1);
				float rtY = offsetY + pSin * (pX - offset + 1) + pCos * (pY - offset + 1);
				int posX = MathUtils.floor(rtX);
				int posY = MathUtils.floor(rtY);
				float x = rtX - posX;
				float y = rtY - posY;
				rtR = chunk.getLightR(x, y, posX, posY, pZ);
				rtG = chunk.getLightG(x, y, posX, posY, pZ);
				rtB = chunk.getLightB(x, y, posX, posY, pZ);
			}
			{
				float ltX = offsetX + pCos * (pX - offset) - pSin * (pY - offset + 1);
				float ltY = offsetY + pSin * (pX - offset) + pCos * (pY - offset + 1);
				int posX = MathUtils.floor(ltX);
				int posY = MathUtils.floor(ltY);
				float x = ltX - posX;
				float y = ltY - posY;
				ltR = chunk.getLightR(x, y, posX, posY, pZ);
				ltG = chunk.getLightG(x, y, posX, posY, pZ);
				ltB = chunk.getLightB(x, y, posX, posY, pZ);
			}
			{
				float lbX = offsetX + pCos * (pX - offset) - pSin * (pY - offset);
				float lbY = offsetY + pSin * (pX - offset) + pCos * (pY - offset);
				int posX = MathUtils.floor(lbX);
				int posY = MathUtils.floor(lbY);
				float x = lbX - posX;
				float y = lbY - posY;
				lbR = chunk.getLightR(x, y, posX, posY, pZ);
				lbG = chunk.getLightG(x, y, posX, posY, pZ);
				lbB = chunk.getLightB(x, y, posX, posY, pZ);
			}
			{
				float rbX = offsetX + pCos * (pX - offset + 1) - pSin * (pY - offset);
				float rbY = offsetY + pSin * (pX - offset + 1) + pCos * (pY - offset);
				int posX = MathUtils.floor(rbX);
				int posY = MathUtils.floor(rbY);
				float x = rbX - posX;
				float y = rbY - posY;
				rbR = chunk.getLightR(x, y, posX, posY, pZ);
				rbG = chunk.getLightG(x, y, posX, posY, pZ);
				rbB = chunk.getLightB(x, y, posX, posY, pZ);
			}
		}
		float colorRT =
				Color.toFloatBits(Math.min(rtR, 255), Math.min(rtG, 255), Math.min(rtB, 255), 255);
		float colorLT =
				Color.toFloatBits(Math.min(ltR, 255), Math.min(ltG, 255), Math.min(ltB, 255), 255);
		float colorLB =
				Color.toFloatBits(Math.min(lbR, 255), Math.min(lbG, 255), Math.min(lbB, 255), 255);
		float colorRB =
				Color.toFloatBits(Math.min(rbR, 255), Math.min(rbG, 255), Math.min(rbB, 255), 255);

		int cellHV = pCell.tilesConnectedMask();

		if (cellHV == 255) {
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, pTileX * 2, pTileY * 2, 2, 2, pSize / 2, pCos, pSin,
					pTileNX, pTileNY, pTileNX + Settings.TILE_W, pTileNY + Settings.TILE_H,
					pRenderBuffer.data(), offset, colorRT, colorLT, colorLB, colorRB);
			return pTileNTr;
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
		float colorC =
				Color.toFloatBits(Math.min(cR, 255), Math.min(cG, 255), Math.min(cB, 255), 255);
		float colorR =
				Color.toFloatBits(Math.min(rR, 255), Math.min(rG, 255), Math.min(rB, 255), 255);
		float colorT =
				Color.toFloatBits(Math.min(tR, 255), Math.min(tG, 255), Math.min(tB, 255), 255);
		float colorL =
				Color.toFloatBits(Math.min(lR, 255), Math.min(lG, 255), Math.min(lB, 255), 255);
		float colorB =
				Color.toFloatBits(Math.min(bR, 255), Math.min(bG, 255), Math.min(bB, 255), 255);
		return render(pCell, pCellularity, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY, pSize,
				pCos, pSin, pRenderBuffer, pTileNX, pTileNY, pTileSX, pTileSY, pTileHX, pTileHY,
				pTileVX, pTileVY, pTileBX, pTileBY, pTileNTr, pTileSTr, pTileHTr, pTileVTr,
				pTileBTr, colorRT, colorLT, colorLB, colorRB, colorC, colorR, colorT, colorL,
				colorB, cellHV);
	}

	public static boolean render(Cell pCell, Cellularity pCellularity, int pX, int pY, int pZ,
			float pOffsetX, float pOffsetY, int pTileX, int pTileY, float pSize, float pCos,
			float pSin, FloatArray pRenderBuffer, float pTileNX, float pTileNY, float pTileSX,
			float pTileSY, float pTileHX, float pTileHY, float pTileVX, float pTileVY,
			float pTileBX, float pTileBY, boolean pTileNTr, boolean pTileSTr, boolean pTileHTr,
			boolean pTileVTr, boolean pTileBTr, float pColorRT, float pColorLT, float pColorLB,
			float pColorRB, float pColorC, float pColorR, float pColorT, float pColorL,
			float pColorB, int pCellHV) {

		int smallTileX = pTileX * 2;
		int smallTileY = pTileY * 2;
		float halfSize = pSize / 2;

		boolean flag = false;
		{
			boolean flagH = (pCellHV & 1) != 0;
			boolean flagV = (pCellHV & 4) != 0;
			float u1 = Settings.TILE_HALF_W, v1 = 0;
			if (flagH && flagV) {
				if ((pCellHV & 2) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
					flag |= pTileNTr;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
					flag |= pTileSTr;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
				flag |= pTileHTr;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
				flag |= pTileVTr;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
				flag |= pTileBTr;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX + 1, smallTileY + 1, 1, 1, halfSize, pCos,
					pSin, u1, v1, u2, v2, pRenderBuffer.data(), offset, pColorRT, pColorT, pColorC,
					pColorR);
		}

		{
			boolean flagH = (pCellHV & 16) != 0;
			boolean flagV = (pCellHV & 4) != 0;
			float u1 = 0, v1 = 0;
			if (flagH && flagV) {
				if ((pCellHV & 8) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
					flag |= pTileNTr;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
					flag |= pTileSTr;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
				flag |= pTileHTr;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
				flag |= pTileVTr;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
				flag |= pTileBTr;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX, smallTileY + 1, 1, 1, halfSize, pCos, pSin,
					u1, v1, u2, v2, pRenderBuffer.data(), offset, pColorT, pColorLT, pColorL,
					pColorC);
		}

		{
			boolean flagH = (pCellHV & 16) != 0;
			boolean flagV = (pCellHV & 64) != 0;
			float u1 = 0, v1 = Settings.TILE_HALF_H;
			if (flagH && flagV) {
				if ((pCellHV & 32) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
					flag |= pTileNTr;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
					flag |= pTileSTr;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
				flag |= pTileHTr;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
				flag |= pTileVTr;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
				flag |= pTileBTr;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX, smallTileY, 1, 1, halfSize, pCos, pSin, u1,
					v1, u2, v2, pRenderBuffer.data(), offset, pColorC, pColorL, pColorLB, pColorB);
		}

		{
			boolean flagH = (pCellHV & 1) != 0;
			boolean flagV = (pCellHV & 64) != 0;
			float u1 = Settings.TILE_HALF_W, v1 = Settings.TILE_HALF_H;
			if (flagH && flagV) {
				if ((pCellHV & 128) != 0) {
					u1 += pTileNX;
					v1 += pTileNY;
					flag |= pTileNTr;
				} else {
					u1 += pTileSX;
					v1 += pTileSY;
					flag |= pTileSTr;
				}
			} else if (flagH) {
				u1 += pTileHX;
				v1 += pTileHY;
				flag |= pTileHTr;
			} else if (flagV) {
				u1 += pTileVX;
				v1 += pTileVY;
				flag |= pTileVTr;
			} else {
				u1 += pTileBX;
				v1 += pTileBY;
				flag |= pTileBTr;
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX + 1, smallTileY, 1, 1, halfSize, pCos, pSin,
					u1, v1, u2, v2, pRenderBuffer.data(), offset, pColorR, pColorC, pColorB,
					pColorRB);
		}

		return flag;
	}

	public static void drawTexture(float pOffsetX, float pOffsetY, int pTileX, int pTileY,
			int pTilesWidth, int pTilesHeight, float pSize, float pCos, float pSin, float pU1,
			float pV1, float pU2, float pV2, float[] pRenderBuffer, int pRenderBufferOffset,
			float pColorRT, float pColorLT, float pColorLB, float pColorRB) {

		float tmpRX = (pTileX + pTilesWidth) * pSize;
		float tmpRY = (pTileY + pTilesHeight) * pSize;
		float tmpLX = pTileX * pSize;
		float tmpLY = pTileY * pSize;

		float xrt, yrt, xlt, ylt, xlb, ylb, xrb, yrb;

		if (pSin == 0f) {
			xrt = tmpRX;
			yrt = tmpRY;
			xlt = tmpLX;
			ylt = tmpRY;
			xlb = tmpLX;
			ylb = tmpLY;
			xrb = tmpRX;
			yrb = tmpLY;
		} else {
			xrt = pCos * tmpRX - pSin * tmpRY;
			yrt = pSin * tmpRX + pCos * tmpRY;
			xlt = pCos * tmpLX - pSin * tmpRY;
			ylt = pSin * tmpLX + pCos * tmpRY;
			xlb = pCos * tmpLX - pSin * tmpLY;
			ylb = pSin * tmpLX + pCos * tmpLY;
			xrb = pCos * tmpRX - pSin * tmpLY;
			yrb = pSin * tmpRX + pCos * tmpLY;
		}

		pRenderBuffer[pRenderBufferOffset] = pOffsetX + xrt;
		pRenderBuffer[pRenderBufferOffset + 1] = pOffsetY + yrt;
		pRenderBuffer[pRenderBufferOffset + 2] = pColorRT;
		pRenderBuffer[pRenderBufferOffset + 3] = pU2;
		pRenderBuffer[pRenderBufferOffset + 4] = pV1;

		pRenderBuffer[pRenderBufferOffset + 5] = pOffsetX + xlt;
		pRenderBuffer[pRenderBufferOffset + 6] = pOffsetY + ylt;
		pRenderBuffer[pRenderBufferOffset + 7] = pColorLT;
		pRenderBuffer[pRenderBufferOffset + 8] = pU1;
		pRenderBuffer[pRenderBufferOffset + 9] = pV1;

		pRenderBuffer[pRenderBufferOffset + 10] = pOffsetX + xlb;
		pRenderBuffer[pRenderBufferOffset + 11] = pOffsetY + ylb;
		pRenderBuffer[pRenderBufferOffset + 12] = pColorLB;
		pRenderBuffer[pRenderBufferOffset + 13] = pU1;
		pRenderBuffer[pRenderBufferOffset + 14] = pV2;

		pRenderBuffer[pRenderBufferOffset + 15] = pOffsetX + xrb;
		pRenderBuffer[pRenderBufferOffset + 16] = pOffsetY + yrb;
		pRenderBuffer[pRenderBufferOffset + 17] = pColorRB;
		pRenderBuffer[pRenderBufferOffset + 18] = pU2;
		pRenderBuffer[pRenderBufferOffset + 19] = pV2;
	}

	public static void drawTexture(float pOffsetX, float pOffsetY, float pPositionX,
			float pPositionY, float pLocalWidth, float pLocalHeight, float pLocalCos,
			float pLocalSin, float pSize, float pCos, float pSin, float pU1, float pV1, float pU2,
			float pV2, float[] pRenderBuffer, int pRenderBufferOffset, float pColorRT,
			float pColorLT, float pColorLB, float pColorRB) {

		float vrtx = (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) / 2;
		float vrty = (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) / 2;
		float vltx = (-pLocalCos * pLocalWidth - pLocalSin * pLocalHeight) / 2;
		float vlty = (-pLocalSin * pLocalWidth + pLocalCos * pLocalHeight) / 2;
		float vlbx = -vrtx;
		float vlby = -vrty;
		float vrbx = -vltx;
		float vrby = -vlty;
		float tmpRTX = (pPositionX + vrtx) * pSize;
		float tmpRTY = (pPositionY + vrty) * pSize;
		float tmpLTX = (pPositionX + vltx) * pSize;
		float tmpLTY = (pPositionY + vlty) * pSize;
		float tmpRBX = (pPositionX + vrbx) * pSize;
		float tmpRBY = (pPositionY + vrby) * pSize;
		float tmpLBX = (pPositionX + vlbx) * pSize;
		float tmpLBY = (pPositionY + vlby) * pSize;
		float xrt = pCos * tmpRTX - pSin * tmpRTY;
		float yrt = pSin * tmpRTX + pCos * tmpRTY;
		float xlt = pCos * tmpLTX - pSin * tmpLTY;
		float ylt = pSin * tmpLTX + pCos * tmpLTY;
		float xlb = pCos * tmpLBX - pSin * tmpLBY;
		float ylb = pSin * tmpLBX + pCos * tmpLBY;
		float xrb = pCos * tmpRBX - pSin * tmpRBY;
		float yrb = pSin * tmpRBX + pCos * tmpRBY;

		pRenderBuffer[pRenderBufferOffset] = pOffsetX + xrt;
		pRenderBuffer[pRenderBufferOffset + 1] = pOffsetY + yrt;
		pRenderBuffer[pRenderBufferOffset + 2] = pColorRT;
		pRenderBuffer[pRenderBufferOffset + 3] = pU2;
		pRenderBuffer[pRenderBufferOffset + 4] = pV1;

		pRenderBuffer[pRenderBufferOffset + 5] = pOffsetX + xlt;
		pRenderBuffer[pRenderBufferOffset + 6] = pOffsetY + ylt;
		pRenderBuffer[pRenderBufferOffset + 7] = pColorLT;
		pRenderBuffer[pRenderBufferOffset + 8] = pU1;
		pRenderBuffer[pRenderBufferOffset + 9] = pV1;

		pRenderBuffer[pRenderBufferOffset + 10] = pOffsetX + xlb;
		pRenderBuffer[pRenderBufferOffset + 11] = pOffsetY + ylb;
		pRenderBuffer[pRenderBufferOffset + 12] = pColorLB;
		pRenderBuffer[pRenderBufferOffset + 13] = pU1;
		pRenderBuffer[pRenderBufferOffset + 14] = pV2;

		pRenderBuffer[pRenderBufferOffset + 15] = pOffsetX + xrb;
		pRenderBuffer[pRenderBufferOffset + 16] = pOffsetY + yrb;
		pRenderBuffer[pRenderBufferOffset + 17] = pColorRB;
		pRenderBuffer[pRenderBufferOffset + 18] = pU2;
		pRenderBuffer[pRenderBufferOffset + 19] = pV2;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
