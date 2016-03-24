package com.gaskarov.teerain.game.game.cell;

import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.GraphicsUtils;
import com.gaskarov.teerain.game.Player;
import com.gaskarov.teerain.game.game.ControlOrganoid;
import com.gaskarov.util.common.MathUtils;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class AirCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final AirCell sInstance = new AirCell();

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, Settings.AIR_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
	}

	@Override
	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, Settings.NO_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
	}

	@Override
	public void render(Cellularity pCellularity, int pX, int pY, int pZ, float pCos, float pSin) {
		for (int i = 0; i < Settings.LAYERS_PER_DEPTH; ++i)
			switch (i) {
			case 0: {
				int count = 0;
				count += GraphicsUtils.debugRender(this, pCellularity, pX, pY, pZ, pCos, pSin);
				GraphicsUtils.count(pCellularity, count);
				break;
			}
			default:
				GraphicsUtils.count(pCellularity, 0);
				break;
			}
	}

	@Override
	public void recycle() {
		recycle(this);
	}

	@Override
	public AirCell cpy() {
		return AirCell.obtain();
	}

	@Override
	public void touchDown(Cellularity pCellularity, int pX, int pY, int pZ, float pClickX,
			float pClickY, ControlOrganoid pControlOrganoid, Player pPlayer, int pUseItem) {
		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		int posX = tissularity.getOffsetX() + MathUtils.floor(pClickX);
		int posY = tissularity.getOffsetY() + MathUtils.floor(pClickY);
		Cellularity clickChunk =
				tissularity.getChunk(posX >> Settings.CHUNK_SIZE_LOG,
						posY >> Settings.CHUNK_SIZE_LOG);
		if (clickChunk != null)
			clickChunk.setCell(posX & Settings.CHUNK_SIZE_MASK, posY & Settings.CHUNK_SIZE_MASK, 0,
					cpy());
		MetaBody body = pCellularity.getBody();
		float c = (float) Math.cos(body.getAngle());
		float s = (float) Math.sin(body.getAngle());
		float offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
		float x =
				body.getOffsetX() + body.getPositionX() + (pX - offset + 0.5f) * c
						- (pY - offset + 0.5f) * s;
		float y =
				body.getOffsetY() + body.getPositionY() + (pX - offset + 0.5f) * s
						+ (pY - offset + 0.5f) * c;
		pControlOrganoid.fastLookTo(pClickX - x, pClickY - y);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static AirCell obtain() {
		return sInstance;
	}

	public static void recycle(AirCell pObj) {
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
