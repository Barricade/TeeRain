package com.gaskarov.teerain.game.game.cell;

import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.VacuumCell;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.GraphicsUtils;
import com.gaskarov.teerain.game.Player;
import com.gaskarov.teerain.game.game.ControlOrganoid;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class HammerCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TILE_X = Settings.TILE_W * 5;
	public static final float TILE_Y = Settings.TILE_H;
	public static final float TILE_W = Settings.TILE_W * 2;
	public static final float TILE_H = Settings.TILE_H;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	// ===========================================================
	// Constructors
	// ===========================================================

	private HammerCell() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void recycle() {
		recycle(this);
	}

	@Override
	public Cell cpy() {
		return obtain();
	}

	@Override
	public int renderWeapon(Cellularity pCellularity, int pX, int pY, int pZ, float pCos,
			float pSin, ControlOrganoid pControlOrganoid) {
		float eyesW = 2.0f;
		float eyesH = pControlOrganoid.getEyesX() < 0 ? -1.0f : 1.0f;
		float eyesX = 0.5f + pControlOrganoid.getEyesX() / 1.4f;
		float eyesY = 0.5f + pControlOrganoid.getEyesY() / 1.4f;
		return GraphicsUtils.renderTexture(this, pCellularity, pX, pY, pZ, pCos, pSin, TILE_X,
				TILE_Y, TILE_W, TILE_H, eyesX, eyesY, eyesW, eyesH, pControlOrganoid.getEyesX(),
				pControlOrganoid.getEyesY());
	}

	@Override
	public void touchDown(Cellularity pCellularity, int pX, int pY, int pZ, float pClickX,
			float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer, int pUseItem) {
		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		// int posX = tissularity.getOffsetX() + MathUtils.floor(pClickX);
		// int posY = tissularity.getOffsetY() + MathUtils.floor(pClickY);
		// Cellularity clickChunk =
		// tissularity.getChunk(posX >> Settings.CHUNK_SIZE_LOG,
		// posY >> Settings.CHUNK_SIZE_LOG);
		// if (clickChunk != null)
		// clickChunk.setCell(posX & Settings.CHUNK_SIZE_MASK, posY &
		// Settings.CHUNK_SIZE_MASK, 0,
		// AirCell.obtain());
		MetaBody body = pCellularity.getBody();
		float c = (float) Math.cos(body.getAngle());
		float s = (float) Math.sin(body.getAngle());
		float offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
		float localX = body.getPositionX() + (pX - offset + 0.5f) * c - (pY - offset + 0.5f) * s;
		float localY = body.getPositionY() + (pX - offset + 0.5f) * s + (pY - offset + 0.5f) * c;
		float x = body.getOffsetX() + localX;
		float y = body.getOffsetY() + localY;
		tissularity.raycastCell(pZ, x, y, pClickX, pClickY, pCellularity, null);
		Cellularity dynamic = tissularity.getRayCastBlockCellularity();
		if (dynamic == null)
			tissularity.setCell(tissularity.getRayCastBlockCellX(), tissularity
					.getRayCastBlockCellY(), pZ, AirCell.obtain());
		else
			dynamic.setCell(tissularity.getRayCastBlockCellX(), tissularity.getRayCastBlockCellY(),
					pZ, VacuumCell.obtain());
		float vx = pClickX - x;
		float vy = pClickY - y;
		pControlOrganoid.fastLookTo(vx, vy);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static HammerCell obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (HammerCell.class) {
				return sPool.size() == 0 ? new HammerCell() : (HammerCell) sPool.pop();
			}
		return new HammerCell();
	}

	private static void recyclePure(HammerCell pObj) {
		if (GlobalConstants.POOL)
			synchronized (HammerCell.class) {
				sPool.push(pObj);
			}
	}

	public static HammerCell obtain() {
		HammerCell obj = obtainPure();
		return obj;
	}

	public static void recycle(HammerCell pObj) {
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
