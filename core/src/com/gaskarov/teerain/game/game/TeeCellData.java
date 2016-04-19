package com.gaskarov.teerain.game.game;

import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.VisitorOrganoid;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.Cellularity.CellData;
import com.gaskarov.teerain.core.util.GraphicsUtils;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class TeeCellData extends CellData {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private ControlOrganoid mControlOrganoid;
	private VisitorOrganoid mVisitorOrganoid;

	// ===========================================================
	// Constructors
	// ===========================================================

	private TeeCellData() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public TeeCellData cpy() {
		return obtain(mControlOrganoid.getId());
	}

	@Override
	public void recycle() {
		recycle(this);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static TeeCellData obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (TeeCellData.class) {
				return sPool.size() == 0 ? new TeeCellData() : (TeeCellData) sPool.pop();
			}
		return new TeeCellData();
	}

	private static void recyclePure(TeeCellData pObj) {
		if (GlobalConstants.POOL)
			synchronized (TeeCellData.class) {
				sPool.push(pObj);
			}
	}

	public static TeeCellData obtain(int pId) {
		TeeCellData obj = obtainPure();
		obj.mControlOrganoid = ControlOrganoid.obtain(pId);
		obj.mVisitorOrganoid = VisitorOrganoid.obtain(pId != 1);
		return obj;
	}

	public static void recycle(TeeCellData pObj) {
		VisitorOrganoid.recycle(pObj.mVisitorOrganoid);
		pObj.mVisitorOrganoid = null;
		ControlOrganoid.recycle(pObj.mControlOrganoid);
		pObj.mControlOrganoid = null;
		recyclePure(pObj);
	}

	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
		mControlOrganoid.attach(pCellularity, pX, pY, pZ);
		mVisitorOrganoid.attach(pCellularity, pX, pY, pZ);
		pCellularity.tissularedEnable(pX, pY, pZ);
	}

	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.tissularedDisable(pX, pY, pZ);
		mVisitorOrganoid.detach(pCellularity, pX, pY, pZ);
		mControlOrganoid.detach(pCellularity, pX, pY, pZ);
	}

	public void tissularedAttach(Cellularity pCellularity, int pX, int pY, int pZ) {
		mVisitorOrganoid.tissularedAttach(pCellularity, pX, pY, pZ);
		mControlOrganoid.tissularedAttach(pCellularity, pX, pY, pZ);
	}

	public void tissularedDetach(Cellularity pCellularity, int pX, int pY, int pZ) {
		mVisitorOrganoid.tissularedDetach(pCellularity, pX, pY, pZ);
		mControlOrganoid.tissularedDetach(pCellularity, pX, pY, pZ);
	}

	public void tick(Cellularity pCellularity, int pX, int pY, int pZ) {
		mControlOrganoid.tick(pCellularity, pX, pY, pZ);
		mVisitorOrganoid.tick(pCellularity, pX, pY, pZ);
	}

	public void render(int pCell, Cellularity pCellularity, int pX, int pY, int pZ, float pOffsetX,
			float pOffsetY, int pTileX, int pTileY, float pSize, float pCos, float pSin,
			FloatArray[] pRenderBuffers) {

		int cellType = pCell & Cells.CELL_TYPES_MAX_MASK;
		FloatArray renderBuffer =
				pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH + Cells.CELLS_RENDER_LAYER[cellType]];
		float[] renderData = Cells.CELLS_RENDER_TILE_DATA[cellType];

		float footBackX, footBackY, footBackR;
		float backSteps = mControlOrganoid.getSteps();
		if (mControlOrganoid.isGrounded()) {
			float tmp = MathUtils.mod(backSteps + (float) Math.PI, (float) Math.PI * 2);
			footBackX = getFootPositionX(tmp);
			footBackY = getFootPositionY(tmp);
			footBackR = getFootPositionR(tmp);
		} else {
			if (backSteps > Math.PI) {
				footBackX = 0.25f + 0.5f;
				footBackY = 0.125f;
				footBackR = (float) Math.PI / 10;
			} else {
				footBackX = -0.25f + 0.5f;
				footBackY = 0.125f;
				footBackR = (float) -Math.PI / 10;
			}
		}

		float footBackW = 0.5f;
		float footBackH = 0.25f;
		GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
				pSize, pCos, pSin, renderBuffer, renderData[2], renderData[3],
				Settings.TILE_HALF_W, Settings.TILE_QUARTER_H, footBackX, footBackY, footBackW,
				footBackH, (float) Math.cos(footBackR), (float) Math.sin(footBackR));

		pCellularity.renderWeapon(mControlOrganoid.getUseItem(), mControlOrganoid.getUseItemData(),
				pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY, pSize, pCos, pSin, renderBuffer,
				mControlOrganoid);

		GraphicsUtils.render(pCell, pCellularity, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
				pSize, pCos, pSin, renderBuffer, renderData[0], renderData[1]);

		float eyesW = 0.5f;
		float eyesH = 0.25f;
		float eyesX = 0.5f + mControlOrganoid.getEyesX() / 8;
		float eyesY = 0.5f + mControlOrganoid.getEyesY() / 8;
		GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
				pSize, pCos, pSin, renderBuffer, renderData[4], renderData[5],
				Settings.TILE_HALF_W, Settings.TILE_QUARTER_H, eyesX, eyesY, eyesW, eyesH, 1f, 0f);

		float footFrontX, footFrontY, footFrontR;
		float frontSteps = mControlOrganoid.getSteps();
		if (mControlOrganoid.isGrounded()) {
			footFrontX = getFootPositionX(frontSteps);
			footFrontY = getFootPositionY(frontSteps);
			footFrontR = getFootPositionR(frontSteps);
		} else {
			if (frontSteps > Math.PI) {
				footFrontX = -0.25f + 0.5f;
				footFrontY = 0.125f;
				footFrontR = (float) Math.PI / 10;
			} else {
				footFrontX = 0.25f + 0.5f;
				footFrontY = 0.125f;
				footFrontR = (float) -Math.PI / 10;
			}
		}

		float footFrontW = 0.5f;
		float footFrontH = 0.25f;
		GraphicsUtils.renderTexture(pCellularity, pX, pY, pZ, pOffsetX, pOffsetY, pTileX, pTileY,
				pSize, pCos, pSin, renderBuffer, renderData[2], renderData[3],
				Settings.TILE_HALF_W, Settings.TILE_QUARTER_H, footFrontX, footFrontY, footFrontW,
				footFrontH, (float) Math.cos(footFrontR), (float) Math.sin(footFrontR));
	}

	private static float getFootPositionX(float pDist) {
		return (float) Math.sin(pDist) * 0.25f + 0.5f;
	}

	private static float getFootPositionY(float pDist) {

		float footY;
		if (pDist < Math.PI / 2) {
			footY = -1 + (float) Math.sin(pDist);
		} else if (pDist < Math.PI) {
			footY = 0;
		} else if (pDist < Math.PI * 1.5) {
			footY = 0;
		} else {
			footY = -(float) Math.sin((float) (pDist - Math.PI * 1.5));
		}

		return -footY * 0.125f + 0.125f;
	}

	private static float getFootPositionR(float pDist) {

		float footR;
		if (pDist < Math.PI / 2) {
			if (pDist < Math.PI / 4) {
				footR = -(float) Math.sin(pDist) * 0.5f;
			} else {
				footR = -(float) Math.sin((float) (Math.PI / 2 - pDist)) * 0.5f;
			}
		} else if (pDist < Math.PI) {
			footR = 0;
		} else if (pDist < Math.PI * 1.5) {
			footR = 0;
		} else {
			if (pDist < Math.PI * 1.75) {
				footR = (float) Math.sin((float) (pDist - Math.PI * 1.5)) * 0.5f;
			} else {
				footR = (float) Math.sin((float) (Math.PI / 2 - (pDist - Math.PI * 1.5))) * 0.5f;
			}
		}

		return -footR;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
