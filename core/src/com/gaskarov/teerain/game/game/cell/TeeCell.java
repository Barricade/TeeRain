package com.gaskarov.teerain.game.game.cell;

import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.VisitorOrganoid;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.GraphicsUtils;
import com.gaskarov.teerain.game.game.ControlOrganoid;
import com.gaskarov.teerain.game.game.DynamicLight;
import com.gaskarov.teerain.game.game.PhysicsWallOrganoid;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class TeeCell extends Cell {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TILE_X = Settings.TILE_W * 6;
	public static final float TILE_Y = Settings.TILE_H * 0;
	public static final float TILE_EYES_X = Settings.TILE_W * 7;
	public static final float TILE_EYES_Y = Settings.TILE_H * 0.25f;
	public static final float TILE_FOOT_X = Settings.TILE_W * 7;
	public static final float TILE_FOOT_Y = Settings.TILE_H * 0f;

	public static final int LAYER = 2;

	public static final float BORDER_SIZE = 0.02f;
	public static final float CORNER_SIZE = 0.3f;
	public static final float CORNER_VALUE = 0.125f;
	public static final float FRICTION = 0.0f;
	public static final float DENSITY = 1.0f;
	public static final float RESTITUTION = 0.0f;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private PhysicsWallOrganoid mPhysicsWallOrganoid;
	private ControlOrganoid mControlOrganoid;
	private VisitorOrganoid mVisitorOrganoid;

	// ===========================================================
	// Constructors
	// ===========================================================

	private TeeCell() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, Settings.SOLID_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
		DynamicLight.attach(pCellularity, pX, pY, pZ, this);
		mPhysicsWallOrganoid.attach(pCellularity, pX, pY, pZ, this);
		mControlOrganoid.attach(pCellularity, pX, pY, pZ, this);
		mVisitorOrganoid.attach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.setCellLightData(pX, pY, pZ, Settings.NO_LIGHT_RESISTANCE_ID,
				Settings.NO_LIGHT_SOURCE_ID);
		mVisitorOrganoid.detach(pCellularity, pX, pY, pZ, this);
		mControlOrganoid.detach(pCellularity, pX, pY, pZ, this);
		mPhysicsWallOrganoid.detach(pCellularity, pX, pY, pZ, this);
		DynamicLight.detach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void tissularedAttach(Cellularity pCellularity, int pX, int pY, int pZ) {
		mVisitorOrganoid.tissularedAttach(pCellularity, pX, pY, pZ, this);
		mControlOrganoid.tissularedAttach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void tissularedDetach(Cellularity pCellularity, int pX, int pY, int pZ) {
		mVisitorOrganoid.tissularedDetach(pCellularity, pX, pY, pZ, this);
		mControlOrganoid.tissularedDetach(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void refresh(Cellularity pCellularity, int pX, int pY, int pZ) {
		mPhysicsWallOrganoid.refresh(pCellularity, pX, pY, pZ, this);
	}

	@Override
	public void tick(Cellularity pCellularity, int pX, int pY, int pZ) {
		DynamicLight.tick(pCellularity, pX, pY, pZ, this);
		mControlOrganoid.tick(pCellularity, pX, pY, pZ, this);
		mVisitorOrganoid.tick(pCellularity, pX, pY, pZ, this);
		pCellularity.invalidateRender(pX, pY, pZ);
	}

	@Override
	public void recycle() {
		recycle(this);
	}

	@Override
	public Cell cpy() {
		return obtain(mControlOrganoid.getId());
	}

	@Override
	public boolean isDynamicLightSource() {
		return true;
	}

	@Override
	public float getBorderSize() {
		return BORDER_SIZE;
	}

	@Override
	public float getCornerValue() {
		return CORNER_VALUE;
	}

	@Override
	public float getCornerSize() {
		return CORNER_SIZE;
	}

	@Override
	public float getFriction() {
		return FRICTION;
	}

	@Override
	public float getDensity() {
		return DENSITY;
	}

	@Override
	public float getRestitution() {
		return RESTITUTION;
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public boolean isSquare() {
		return false;
	}

	@Override
	public boolean isTileConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY) {
		return false;
	}

	@Override
	public boolean isDroppable(Cellularity pCellularity, int pX, int pY, int pZ) {
		return true;
	}

	@Override
	public boolean isConnected(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell,
			int pVX, int pVY, int pVZ) {
		return false;
	}

	@Override
	public void render(Cellularity pCellularity, int pX, int pY, int pZ, float pCos, float pSin) {
		for (int i = 0; i < Settings.LAYERS_PER_DEPTH; ++i)
			switch (i) {
			case LAYER: {
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
				int count =
						GraphicsUtils
								.renderTexture(this, pCellularity, pX, pY, pZ, pCos, pSin,
										TILE_FOOT_X, TILE_FOOT_Y, Settings.TILE_HALF_W,
										Settings.TILE_QUARTER_H, footBackX, footBackY, footBackW,
										footBackH, (float) Math.cos(footBackR), (float) Math
												.sin(footBackR));

				count +=
						mControlOrganoid.getUseItem().renderWeapon(pCellularity, pX, pY, pZ, pCos,
								pSin, mControlOrganoid);

				count +=
						GraphicsUtils.render(this, pCellularity, pX, pY, pZ, pCos, pSin, TILE_X,
								TILE_Y);

				float eyesW = 0.5f;
				float eyesH = 0.25f;
				float eyesX = 0.5f + mControlOrganoid.getEyesX() / 8;
				float eyesY = 0.5f + mControlOrganoid.getEyesY() / 8;
				count +=
						GraphicsUtils.renderTexture(this, pCellularity, pX, pY, pZ, pCos, pSin,
								TILE_EYES_X, TILE_EYES_Y, Settings.TILE_HALF_W,
								Settings.TILE_QUARTER_H, eyesX, eyesY, eyesW, eyesH, 1f, 0f);

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
				count +=
						GraphicsUtils.renderTexture(this, pCellularity, pX, pY, pZ, pCos, pSin,
								TILE_FOOT_X, TILE_FOOT_Y, Settings.TILE_HALF_W,
								Settings.TILE_QUARTER_H, footFrontX, footFrontY, footFrontW,
								footFrontH, (float) Math.cos(footFrontR), (float) Math
										.sin(footFrontR));

				GraphicsUtils.count(pCellularity, count);
				break;
			}
			default:
				GraphicsUtils.count(pCellularity, 0);
				break;
			}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static TeeCell obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (TeeCell.class) {
				return sPool.size() == 0 ? new TeeCell() : (TeeCell) sPool.pop();
			}
		return new TeeCell();
	}

	private static void recyclePure(TeeCell pObj) {
		if (GlobalConstants.POOL)
			synchronized (TeeCell.class) {
				sPool.push(pObj);
			}
	}

	public static TeeCell obtain(int pId) {
		TeeCell obj = obtainPure();
		obj.mPhysicsWallOrganoid = PhysicsWallOrganoid.obtain();
		obj.mControlOrganoid = ControlOrganoid.obtain(pId);
		obj.mVisitorOrganoid = VisitorOrganoid.obtain();
		return obj;
	}

	public static void recycle(TeeCell pObj) {
		VisitorOrganoid.recycle(pObj.mVisitorOrganoid);
		pObj.mVisitorOrganoid = null;
		ControlOrganoid.recycle(pObj.mControlOrganoid);
		pObj.mControlOrganoid = null;
		PhysicsWallOrganoid.recycle(pObj.mPhysicsWallOrganoid);
		pObj.mPhysicsWallOrganoid = null;
		recyclePure(pObj);
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
