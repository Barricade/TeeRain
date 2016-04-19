package com.gaskarov.teerain.game.game.cell;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
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
public final class GrenadeGunCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TILE_X = Settings.TILE_W * 5;
	public static final float TILE_Y = Settings.TILE_H * 2;
	public static final float TILE_W = Settings.TILE_W * 3;
	public static final float TILE_H = Settings.TILE_H;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	// ===========================================================
	// Constructors
	// ===========================================================

	private GrenadeGunCell() {
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
		float eyesW = 3.0f;
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
		MetaBody body = pCellularity.getBody();
		float c = (float) Math.cos(body.getAngle());
		float s = (float) Math.sin(body.getAngle());
		float offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
		float localX = body.getPositionX() + (pX - offset + 0.5f) * c - (pY - offset + 0.5f) * s;
		float localY = body.getPositionY() + (pX - offset + 0.5f) * s + (pY - offset + 0.5f) * c;
		float x = body.getOffsetX() + localX;
		float y = body.getOffsetY() + localY;
		float vx = pClickX - x;
		float vy = pClickY - y;
		pControlOrganoid.fastLookTo(vx, vy);
		float k = (float) Math.sqrt(vx * vx + vy * vy);
		vx /= k;
		vy /= k;
		Cellularity cellularity =
				Cellularity.obtain(BodyType.DynamicBody, localX + vx + Settings.CHUNK_HSIZE - 0.5f,
						localY + vy + Settings.CHUNK_HSIZE - 0.5f, 0);
		chunk.pushCellularity(cellularity);
		cellularity.setCell(0, 0, 0, LampCell.obtain());
		MetaBody bulletBody = cellularity.getBody();
		float velocity = 30f;
		bulletBody.setVelocity(vx * velocity, vy * velocity);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static GrenadeGunCell obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (GrenadeGunCell.class) {
				return sPool.size() == 0 ? new GrenadeGunCell() : (GrenadeGunCell) sPool.pop();
			}
		return new GrenadeGunCell();
	}

	private static void recyclePure(GrenadeGunCell pObj) {
		if (GlobalConstants.POOL)
			synchronized (GrenadeGunCell.class) {
				sPool.push(pObj);
			}
	}

	public static GrenadeGunCell obtain() {
		GrenadeGunCell obj = obtainPure();
		return obj;
	}

	public static void recycle(GrenadeGunCell pObj) {
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
