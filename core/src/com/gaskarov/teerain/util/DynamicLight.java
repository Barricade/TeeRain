package com.gaskarov.teerain.util;

import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cell.Cell;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.tissularity.Tissularity;
import com.gaskarov.util.common.MathUtils;

/**
 * Copyright (c) 2014 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class DynamicLight {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private DynamicLight() {
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

	public static void attach(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		if (!pCellularity.isChunk() && pCell.isDynamicLightSource())
			pCellularity.tickEnable(pX, pY, pZ);
	}

	public static void detach(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		if (!pCellularity.isChunk() && pCell.isDynamicLightSource())
			pCellularity.tickDisable(pX, pY, pZ);
	}

	public static void tick(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		if (!pCellularity.isChunk() && pCell.isDynamicLightSource()) {
			Cellularity chunk = pCellularity.getChunk();
			if (chunk == null)
				return;
			Tissularity tissularity = chunk.getTissularity();
			if (tissularity == null)
				return;

			Cellularity cellularity = pCellularity;

			MetaBody body = cellularity.getBody();
			float c = (float) Math.cos(body.getAngle());
			float s = (float) Math.sin(body.getAngle());
			int offset = pCellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
			int x =
					MathUtils.floor(body.getPositionX() + (pX - offset + 0.5f) * c
							- (pY - offset + 0.5f) * s);
			int y =
					MathUtils.floor(body.getPositionY() + (pX - offset + 0.5f) * s
							+ (pY - offset + 0.5f) * c);
			int sourceId = pCellularity.getLightSource(pX, pY, pZ) << Settings.COLORS_LOG;
			int r = Settings.LIGHT_SOURCE[sourceId];
			int g = Settings.LIGHT_SOURCE[sourceId + 1];
			int b = Settings.LIGHT_SOURCE[sourceId + 2];
			int[] color = chunk.getLight(x, y, pZ);
			if (color[0] < r || color[1] < g || color[2] < b)
				chunk.setLight(x, y, pZ, r, g, b);
		}
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
