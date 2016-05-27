package com.gaskarov.teerain.core.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.resource.CellsAction;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.container.FloatArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class GraphicsUtils {

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

	public static void renderDebug(int pCell, Cellularity pCellularity, int pX,
			int pY, int pZ, float pOffsetX, float pOffsetY, int pTileX,
			int pTileY, float pSize, float pCos, float pSin,
			FloatArray pRenderBuffer, float pTileTX, float pTileTY, int pR,
			int pG, int pB, int pA) {
		float color = Color.toFloatBits(pR, pG, pB, pA);
		int offset = pRenderBuffer.size();
		pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
		drawTexture(pOffsetX, pOffsetY, pTileX * 2, pTileY * 2, 2, 2,
				pSize / 2, pCos, pSin, pTileTX, pTileTY, pTileTX
						+ Settings.TILE_W, pTileTY + Settings.TILE_H,
				pRenderBuffer.data(), offset, color, color, color, color);
	}

	public static void renderTexture(Cellularity pCellularity, int pX, int pY,
			int pZ, float pOffsetX, float pOffsetY, int pTileX, int pTileY,
			float pSize, float pCos, float pSin, FloatArray pRenderBuffer,
			float pTileTX, float pTileTY, float pTileWidth, float pTileHeight,
			float pPositionX, float pPositionY, float pLocalWidth,
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
		ChunkCellularity chunk = pCellularity.getChunk();
		if (pCellularity.isChunk()) {
			final int[] lightCorners = chunk.getLightCorners(pX, pY, pZ);
			lbR = lightCorners[0];
			lbG = lightCorners[1];
			lbB = lightCorners[2];
			rbR = lightCorners[4];
			rbG = lightCorners[5];
			rbB = lightCorners[6];
			ltR = lightCorners[8];
			ltG = lightCorners[9];
			ltB = lightCorners[10];
			rtR = lightCorners[12];
			rtG = lightCorners[13];
			rtB = lightCorners[14];
		} else {
			{
				float localX = pPositionX
						- (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight)
						/ 2;
				float localY = pPositionY
						- (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight)
						/ 2;
				Vector2 p = pCellularity.localToChunk(pX + localX, pY + localY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				lbR = getLightR(lightCorners, x, y);
				lbG = getLightG(lightCorners, x, y);
				lbB = getLightB(lightCorners, x, y);
			}
			{
				float localX = pPositionX
						+ (pLocalCos * pLocalWidth + pLocalSin * pLocalHeight)
						/ 2;
				float localY = pPositionY
						+ (pLocalSin * pLocalWidth - pLocalCos * pLocalHeight)
						/ 2;
				Vector2 p = pCellularity.localToChunk(pX + localX, pY + localY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				rbR = getLightR(lightCorners, x, y);
				rbG = getLightG(lightCorners, x, y);
				rbB = getLightB(lightCorners, x, y);
			}
			{
				float localX = pPositionX
						+ (-pLocalCos * pLocalWidth - pLocalSin * pLocalHeight)
						/ 2;
				float localY = pPositionY
						+ (-pLocalSin * pLocalWidth + pLocalCos * pLocalHeight)
						/ 2;
				Vector2 p = pCellularity.localToChunk(pX + localX, pY + localY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				ltR = getLightR(lightCorners, x, y);
				ltG = getLightG(lightCorners, x, y);
				ltB = getLightB(lightCorners, x, y);
			}
			{
				float localX = pPositionX
						+ (pLocalCos * pLocalWidth - pLocalSin * pLocalHeight)
						/ 2;
				float localY = pPositionY
						+ (pLocalSin * pLocalWidth + pLocalCos * pLocalHeight)
						/ 2;
				Vector2 p = pCellularity.localToChunk(pX + localX, pY + localY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				rtR = getLightR(lightCorners, x, y);
				rtG = getLightG(lightCorners, x, y);
				rtB = getLightB(lightCorners, x, y);
			}
		}
		float colorRT = Color.toFloatBits(Math.min(rtR, 255),
				Math.min(rtG, 255), Math.min(rtB, 255), 255);
		float colorLT = Color.toFloatBits(Math.min(ltR, 255),
				Math.min(ltG, 255), Math.min(ltB, 255), 255);
		float colorLB = Color.toFloatBits(Math.min(lbR, 255),
				Math.min(lbG, 255), Math.min(lbB, 255), 255);
		float colorRB = Color.toFloatBits(Math.min(rbR, 255),
				Math.min(rbG, 255), Math.min(rbB, 255), 255);
		int bufferOffset = pRenderBuffer.size();
		pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
		drawTexture(pOffsetX, pOffsetY, pTileX + pPositionX, pTileY
				+ pPositionY, pLocalWidth, pLocalHeight, pLocalCos, pLocalSin,
				pSize, pCos, pSin, pTileTX, pTileTY, pTileTX + pTileWidth,
				pTileTY + pTileHeight, pRenderBuffer.data(), bufferOffset,
				colorRT, colorLT, colorLB, colorRB);
	}

	public static void render(int pCell, Cellularity pCellularity, int pX,
			int pY, int pZ, float pOffsetX, float pOffsetY, int pTileX,
			int pTileY, float pSize, float pCos, float pSin,
			FloatArray pRenderBuffer, float pTileTX, float pTileTY) {
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
		ChunkCellularity chunk = pCellularity.getChunk();
		if (pCellularity.isChunk()) {
			final int[] lightCorners = chunk.getLightCorners(pX, pY, pZ);
			lbR = lightCorners[0];
			lbG = lightCorners[1];
			lbB = lightCorners[2];
			rbR = lightCorners[4];
			rbG = lightCorners[5];
			rbB = lightCorners[6];
			ltR = lightCorners[8];
			ltG = lightCorners[9];
			ltB = lightCorners[10];
			rtR = lightCorners[12];
			rtG = lightCorners[13];
			rtB = lightCorners[14];
		} else {
			{
				Vector2 p = pCellularity.localToChunk(pX, pY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				lbR = getLightR(lightCorners, x, y);
				lbG = getLightG(lightCorners, x, y);
				lbB = getLightB(lightCorners, x, y);
			}
			{
				Vector2 p = pCellularity.localToChunk(pX + 1, pY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				rbR = getLightR(lightCorners, x, y);
				rbG = getLightG(lightCorners, x, y);
				rbB = getLightB(lightCorners, x, y);
			}
			{
				Vector2 p = pCellularity.localToChunk(pX, pY + 1);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				ltR = getLightR(lightCorners, x, y);
				ltG = getLightG(lightCorners, x, y);
				ltB = getLightB(lightCorners, x, y);
			}
			{
				Vector2 p = pCellularity.localToChunk(pX + 1, pY + 1);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				rtR = getLightR(lightCorners, x, y);
				rtG = getLightG(lightCorners, x, y);
				rtB = getLightB(lightCorners, x, y);
			}
		}
		float colorRT = Color.toFloatBits(Math.min(rtR, 255),
				Math.min(rtG, 255), Math.min(rtB, 255), 255);
		float colorLT = Color.toFloatBits(Math.min(ltR, 255),
				Math.min(ltG, 255), Math.min(ltB, 255), 255);
		float colorLB = Color.toFloatBits(Math.min(lbR, 255),
				Math.min(lbG, 255), Math.min(lbB, 255), 255);
		float colorRB = Color.toFloatBits(Math.min(rbR, 255),
				Math.min(rbG, 255), Math.min(rbB, 255), 255);
		int offset = pRenderBuffer.size();
		pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
		drawTexture(pOffsetX, pOffsetY, pTileX * 2, pTileY * 2, 2, 2,
				pSize / 2, pCos, pSin, pTileTX, pTileTY, pTileTX
						+ Settings.TILE_W, pTileTY + Settings.TILE_H,
				pRenderBuffer.data(), offset, colorRT, colorLT, colorLB,
				colorRB);
	}

	public static void render(int pCell, Cellularity pCellularity, int pX,
			int pY, int pZ, float pOffsetX, float pOffsetY, int pTileX,
			int pTileY, float pSize, float pCos, float pSin,
			FloatArray pRenderBuffer, float pTileNX, float pTileNY,
			float pTileSX, float pTileSY, float pTileHX, float pTileHY,
			float pTileVX, float pTileVY, float pTileBX, float pTileBY,
			float pTileCornerFixX, float pTileCornerFixY) {
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
		ChunkCellularity chunk = pCellularity.getChunk();
		if (pCellularity.isChunk()) {
			final int[] lightCorners = chunk.getLightCorners(pX, pY, pZ);
			lbR = lightCorners[0];
			lbG = lightCorners[1];
			lbB = lightCorners[2];
			rbR = lightCorners[4];
			rbG = lightCorners[5];
			rbB = lightCorners[6];
			ltR = lightCorners[8];
			ltG = lightCorners[9];
			ltB = lightCorners[10];
			rtR = lightCorners[12];
			rtG = lightCorners[13];
			rtB = lightCorners[14];
		} else {
			{
				Vector2 p = pCellularity.localToChunk(pX, pY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				lbR = getLightR(lightCorners, x, y);
				lbG = getLightG(lightCorners, x, y);
				lbB = getLightB(lightCorners, x, y);
			}
			{
				Vector2 p = pCellularity.localToChunk(pX + 1, pY);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				rbR = getLightR(lightCorners, x, y);
				rbG = getLightG(lightCorners, x, y);
				rbB = getLightB(lightCorners, x, y);
			}
			{
				Vector2 p = pCellularity.localToChunk(pX, pY + 1);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				ltR = getLightR(lightCorners, x, y);
				ltG = getLightG(lightCorners, x, y);
				ltB = getLightB(lightCorners, x, y);
			}
			{
				Vector2 p = pCellularity.localToChunk(pX + 1, pY + 1);
				int posX = MathUtils.floor(p.x);
				int posY = MathUtils.floor(p.y);
				float x = p.x - posX;
				float y = p.y - posY;
				int[] lightCorners = chunk.getLightCorners(posX, posY, pZ);
				rtR = getLightR(lightCorners, x, y);
				rtG = getLightG(lightCorners, x, y);
				rtB = getLightB(lightCorners, x, y);
			}
		}
		float colorRT = Color.toFloatBits(Math.min(rtR, 255),
				Math.min(rtG, 255), Math.min(rtB, 255), 255);
		float colorLT = Color.toFloatBits(Math.min(ltR, 255),
				Math.min(ltG, 255), Math.min(ltB, 255), 255);
		float colorLB = Color.toFloatBits(Math.min(lbR, 255),
				Math.min(lbG, 255), Math.min(lbB, 255), 255);
		float colorRB = Color.toFloatBits(Math.min(rbR, 255),
				Math.min(rbG, 255), Math.min(rbB, 255), 255);

		int cellHV = 0;
		for (int i = 0; i < ArrayConstants.MOVE_AROUND_X.length; ++i) {
			int vx = ArrayConstants.MOVE_AROUND_X[i];
			int vy = ArrayConstants.MOVE_AROUND_Y[i];
			if (CellsAction.isTileConnected(pCellularity, pCell, pX, pY, pZ,
					pCellularity.getCell(pX + vx, pY + vy, pZ), vx, vy))
				cellHV |= 1 << i;
		}

		if (cellHV == 255) {
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, pTileX * 2, pTileY * 2, 2, 2,
					pSize / 2, pCos, pSin, pTileNX, pTileNY, pTileNX
							+ Settings.TILE_W, pTileNY + Settings.TILE_H,
					pRenderBuffer.data(), offset, colorRT, colorLT, colorLB,
					colorRB);
			return;
		}

		int cellCornerFix = 0;
		if (CellsAction.isTileCornerFix(pCellularity, pCell, pX, pY, pZ))
			for (int i = 0; i < ArrayConstants.MOVE_AROUND_X.length; ++i) {
				int x = pX + ArrayConstants.MOVE_AROUND_X[i];
				int y = pY + ArrayConstants.MOVE_AROUND_Y[i];
				if (CellsAction.isTileCornerFix(pCellularity,
						pCellularity.getCell(x, y, pZ), x, y, pZ))
					cellCornerFix |= 1 << i;
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
		float colorC = Color.toFloatBits(Math.min(cR, 255), Math.min(cG, 255),
				Math.min(cB, 255), 255);
		float colorR = Color.toFloatBits(Math.min(rR, 255), Math.min(rG, 255),
				Math.min(rB, 255), 255);
		float colorT = Color.toFloatBits(Math.min(tR, 255), Math.min(tG, 255),
				Math.min(tB, 255), 255);
		float colorL = Color.toFloatBits(Math.min(lR, 255), Math.min(lG, 255),
				Math.min(lB, 255), 255);
		float colorB = Color.toFloatBits(Math.min(bR, 255), Math.min(bG, 255),
				Math.min(bB, 255), 255);
		render(pOffsetX, pOffsetY, pTileX, pTileY, pSize, pCos, pSin,
				pRenderBuffer, pTileNX, pTileNY, pTileSX, pTileSY, pTileHX,
				pTileHY, pTileVX, pTileVY, pTileBX, pTileBY, pTileCornerFixX,
				pTileCornerFixY, colorRT, colorLT, colorLB, colorRB, colorC,
				colorR, colorT, colorL, colorB, cellHV, cellCornerFix);
	}

	public static void render(float pOffsetX, float pOffsetY, int pTileX,
			int pTileY, float pSize, float pCos, float pSin,
			FloatArray pRenderBuffer, float pTileNX, float pTileNY,
			float pTileSX, float pTileSY, float pTileHX, float pTileHY,
			float pTileVX, float pTileVY, float pTileBX, float pTileBY,
			float pTileCornerFixX, float pTileCornerFixY, float pColorRT,
			float pColorLT, float pColorLB, float pColorRB, float pColorC,
			float pColorR, float pColorT, float pColorL, float pColorB,
			int pCellHV, int pCellCornerFix) {

		int smallTileX = pTileX * 2;
		int smallTileY = pTileY * 2;
		float halfSize = pSize / 2;

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
				if ((pCellCornerFix & 1) != 0 && (pCellCornerFix & 2) != 0
						&& (pCellCornerFix & 4) != 0) {
					u1 += pTileCornerFixX;
					v1 += pTileCornerFixY;
				} else {
					u1 += pTileBX;
					v1 += pTileBY;
				}
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX + 1, smallTileY + 1, 1,
					1, halfSize, pCos, pSin, u1, v1, u2, v2,
					pRenderBuffer.data(), offset, pColorRT, pColorT, pColorC,
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
				if ((pCellCornerFix & 4) != 0 && (pCellCornerFix & 8) != 0
						&& (pCellCornerFix & 16) != 0) {
					u1 += pTileCornerFixX;
					v1 += pTileCornerFixY;
				} else {
					u1 += pTileBX;
					v1 += pTileBY;
				}
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX, smallTileY + 1, 1, 1,
					halfSize, pCos, pSin, u1, v1, u2, v2, pRenderBuffer.data(),
					offset, pColorT, pColorLT, pColorL, pColorC);
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
				if ((pCellCornerFix & 16) != 0 && (pCellCornerFix & 32) != 0
						&& (pCellCornerFix & 64) != 0) {
					u1 += pTileCornerFixX;
					v1 += pTileCornerFixY;
				} else {
					u1 += pTileBX;
					v1 += pTileBY;
				}
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX, smallTileY, 1, 1,
					halfSize, pCos, pSin, u1, v1, u2, v2, pRenderBuffer.data(),
					offset, pColorC, pColorL, pColorLB, pColorB);
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
				if ((pCellCornerFix & 1) != 0 && (pCellCornerFix & 64) != 0
						&& (pCellCornerFix & 128) != 0) {
					u1 += pTileCornerFixX;
					v1 += pTileCornerFixY;
				} else {
					u1 += pTileBX;
					v1 += pTileBY;
				}
			}
			float u2 = u1 + Settings.TILE_HALF_W;
			float v2 = v1 + Settings.TILE_HALF_H;
			int offset = pRenderBuffer.size();
			pRenderBuffer.pushArray(Settings.ELEMENTS_PER_TEXTURE);
			drawTexture(pOffsetX, pOffsetY, smallTileX + 1, smallTileY, 1, 1,
					halfSize, pCos, pSin, u1, v1, u2, v2, pRenderBuffer.data(),
					offset, pColorR, pColorC, pColorB, pColorRB);
		}
	}

	public static void drawTexture(float pOffsetX, float pOffsetY,
			float pHalfWidth, float pHalfHeight, float pU1, float pV1,
			float pU2, float pV2, float[] pRenderBuffer,
			int pRenderBufferOffset, float pColorRT, float pColorLT,
			float pColorLB, float pColorRB) {

		pRenderBuffer[pRenderBufferOffset] = pOffsetX + pHalfWidth;
		pRenderBuffer[pRenderBufferOffset + 1] = pOffsetY + pHalfHeight;
		pRenderBuffer[pRenderBufferOffset + 2] = pColorRT;
		pRenderBuffer[pRenderBufferOffset + 3] = pU2;
		pRenderBuffer[pRenderBufferOffset + 4] = pV1;

		pRenderBuffer[pRenderBufferOffset + 5] = pOffsetX - pHalfWidth;
		pRenderBuffer[pRenderBufferOffset + 6] = pOffsetY + pHalfHeight;
		pRenderBuffer[pRenderBufferOffset + 7] = pColorLT;
		pRenderBuffer[pRenderBufferOffset + 8] = pU1;
		pRenderBuffer[pRenderBufferOffset + 9] = pV1;

		pRenderBuffer[pRenderBufferOffset + 10] = pOffsetX - pHalfWidth;
		pRenderBuffer[pRenderBufferOffset + 11] = pOffsetY - pHalfHeight;
		pRenderBuffer[pRenderBufferOffset + 12] = pColorLB;
		pRenderBuffer[pRenderBufferOffset + 13] = pU1;
		pRenderBuffer[pRenderBufferOffset + 14] = pV2;

		pRenderBuffer[pRenderBufferOffset + 15] = pOffsetX + pHalfWidth;
		pRenderBuffer[pRenderBufferOffset + 16] = pOffsetY - pHalfHeight;
		pRenderBuffer[pRenderBufferOffset + 17] = pColorRB;
		pRenderBuffer[pRenderBufferOffset + 18] = pU2;
		pRenderBuffer[pRenderBufferOffset + 19] = pV2;
	}

	public static void drawTexture(float pOffsetX, float pOffsetY, int pTileX,
			int pTileY, int pTilesWidth, int pTilesHeight, float pSize,
			float pCos, float pSin, float pU1, float pV1, float pU2, float pV2,
			float[] pRenderBuffer, int pRenderBufferOffset, float pColorRT,
			float pColorLT, float pColorLB, float pColorRB) {

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

	public static void drawTexture(float pOffsetX, float pOffsetY,
			float pPositionX, float pPositionY, float pLocalWidth,
			float pLocalHeight, float pLocalCos, float pLocalSin, float pSize,
			float pCos, float pSin, float pU1, float pV1, float pU2, float pV2,
			float[] pRenderBuffer, int pRenderBufferOffset, float pColorRT,
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

	private static int getLightR(int[] pLightCorners, float pFX, float pFY) {
		float b = (1f - pFX) * pLightCorners[0] + pFX * pLightCorners[4];
		float t = (1f - pFX) * pLightCorners[8] + pFX * pLightCorners[12];
		float m = (1f - pFY) * b + pFY * t;
		return (int) m;
	}

	private static int getLightG(int[] pLightCorners, float pFX, float pFY) {
		float b = (1f - pFX) * pLightCorners[1] + pFX * pLightCorners[5];
		float t = (1f - pFX) * pLightCorners[9] + pFX * pLightCorners[13];
		float m = (1f - pFY) * b + pFY * t;
		return (int) m;
	}

	private static int getLightB(int[] pLightCorners, float pFX, float pFY) {
		float b = (1f - pFX) * pLightCorners[2] + pFX * pLightCorners[6];
		float t = (1f - pFX) * pLightCorners[10] + pFX * pLightCorners[14];
		float m = (1f - pFY) * b + pFY * t;
		return (int) m;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
