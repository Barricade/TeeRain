package com.gaskarov.teerain.organoid;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cell.Cell;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.tissularity.Tissularity;
import com.gaskarov.teerain.util.Collidable;
import com.gaskarov.teerain.util.MetaBody;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.List;
import com.gaskarov.util.pool.BinaryFloatArrayPool;
import com.gaskarov.util.pool.FixtureDefPool;
import com.gaskarov.util.pool.PolygonShapePool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class ControlOrganoid implements Collidable {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int FAST_LOOK_DELAY = 250;
	private static final float FLOAT_EPS = 1e-6f;
	private static final float POSITION_CHANGE_EPS = 1e-2f;
	private static final float EYES_ANGULAR_VELOCITY = (float) Math.PI * 5;

	public static final float GROUND_SENSOR_SIZE = 0.05f;
	public static final float GROUND_SENSOR_WIDTH_BORDER = 0.3f;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mControlMove;
	private boolean mJump;
	private float mJumpTimeDelayLeft;

	private float mSteps;
	private float mLastPositionX;
	private int mLastChunkX;
	private boolean mLastFlagX;

	private boolean mActiveLook;
	private float mLookToX;
	private float mLookToY;
	private float mEyesX;
	private float mEyesY;
	private boolean mLookTo;
	private long mLastFastLookTime;

	private FixtureDef mGroundSensor;
	private LinkedHashTable mDynamics;
	private LinkedHashTable mStatics;

	// ===========================================================
	// Constructors
	// ===========================================================

	private ControlOrganoid() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setControlMove(int pControlMove) {
		mControlMove = pControlMove;
	}

	public void setJump(boolean pJump) {
		mJump = pJump;
	}

	public float getSteps() {
		return mSteps;
	}

	public float getEyesX() {
		return mEyesX;
	}

	public float getEyesY() {
		return mEyesY;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void beginContact(Contact pContact, Fixture pFixture, Fixture pThisFixture, boolean pSwap) {
		if (pFixture.getBody().getType() == BodyType.DynamicBody) {
			mDynamics.set(pFixture.getBody());
		} else {
			mStatics.set(pFixture.getBody());
		}
	}

	@Override
	public void endContact(Contact pContact, Fixture pFixture, Fixture pThisFixture, boolean pSwap) {
		if (pFixture.getBody().getType() == BodyType.DynamicBody) {
			mDynamics.remove(pFixture.getBody());
		} else {
			mStatics.remove(pFixture.getBody());
		}
	}

	@Override
	public void preSolve(Contact pContact, Manifold pOldManifold, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap) {
	}

	@Override
	public void postSolve(Contact pContact, ContactImpulse pContactImpulse, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap) {
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static ControlOrganoid obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (ControlOrganoid.class) {
				return sPool.size() == 0 ? new ControlOrganoid() : (ControlOrganoid) sPool.pop();
			}
		return new ControlOrganoid();
	}

	private static void recyclePure(ControlOrganoid pObj) {
		if (GlobalConstants.POOL)
			synchronized (ControlOrganoid.class) {
				sPool.push(pObj);
			}
	}

	public static ControlOrganoid obtain() {
		ControlOrganoid obj = obtainPure();
		obj.mControlMove = 0;
		obj.mJump = false;
		obj.mJumpTimeDelayLeft = 0;

		obj.mSteps = (float) (Math.PI * 1.5f);
		obj.mLastFlagX = false;

		obj.mActiveLook = false;
		obj.mLookToX = 0f;
		obj.mLookToY = 0f;
		obj.mEyesX = 1f;
		obj.mEyesY = 0f;
		obj.mLookTo = true;
		obj.mLastFastLookTime = -1;

		obj.mGroundSensor = null;
		obj.mDynamics = LinkedHashTable.obtain();
		obj.mStatics = LinkedHashTable.obtain();
		return obj;
	}

	public static void recycle(ControlOrganoid pObj) {
		LinkedHashTable.recycle(pObj.mDynamics);
		pObj.mDynamics = null;
		LinkedHashTable.recycle(pObj.mStatics);
		pObj.mStatics = null;
		recyclePure(pObj);
	}

	public void attach(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		createPhysics(pCellularity, pX, pY, pZ);
		pCellularity.tickEnable(pX, pY, pZ);
	}

	public void detach(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		pCellularity.tickDisable(pX, pY, pZ);
		destroyPhysics(pCellularity, pX, pY, pZ);
	}

	public void tick(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		Cellularity chunk = pCellularity.isChunk() ? pCellularity : pCellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();

		tissularity.control(pCellularity, pX, pY, pZ, this);

		Cellularity cellularity = pCellularity;

		if (mJumpTimeDelayLeft > 0)
			mJumpTimeDelayLeft -= Settings.TIME_STEP;

		{
			boolean grounded = isGrounded();

			float maxSpeed =
					(grounded ? Settings.GROUND_CONTROL_SPEED : Settings.AIR_CONTROL_SPEED);
			float accel = (grounded ? Settings.GROUND_CONTROL_ACCEL : Settings.AIR_CONTROL_ACCEL);
			float friction = (grounded ? Settings.GROUND_FRICTION : Settings.AIR_FRICTION);

			MetaBody body = cellularity.getBody();
			if (mControlMove != 0) {
				body.setVelocity(saturatedAdd(-maxSpeed, maxSpeed, body.getVelocityX(),
						(mControlMove < 0 ? -1 : 1) * accel * Settings.TIME_STEP), body
						.getVelocityY());
			} else {
				body.setVelocity(frictionAdd(body.getVelocityX(), friction * Settings.TIME_STEP),
						body.getVelocityY());
			}

			if (mJump && (grounded || true) && mJumpTimeDelayLeft <= 0) {
				mJumpTimeDelayLeft = Settings.JUMP_DELAY;
				float jumpImpulse = Settings.GROUND_JUMP_VELOCITY * body.getMass();
				if (!isStaticGrounded()) {
					float impulse = jumpImpulse / mDynamics.size();
					for (List.Node i = mDynamics.begin(); i != mDynamics.end(); i =
							mDynamics.next(i)) {
						((Body) mDynamics.val(i)).applyLinearImpulse(0, -impulse, body
								.getMassCenterX(), body.getMassCenterY(), true);
					}
				}
				body.applyLinearImpulse(0, jumpImpulse, body.getMassCenterX(), body
						.getMassCenterY());
			}

			body.setFixed(true);

			if (grounded) {
				float positionX = body.getPositionX();
				int chunkX = chunk.getX() << Settings.CHUNK_SIZE_LOG;
				float change;
				if (mLastFlagX
						&& Math.abs(change = chunkX - mLastChunkX + positionX - mLastPositionX) > POSITION_CHANGE_EPS) {
					mSteps += change * Math.PI / 3;
					mSteps = MathUtils.mod(mSteps, (float) (Math.PI * 2));
				} else {
					float p1 = (float) (Math.PI * 0.5f);
					float p2 = (float) (Math.PI * 1.5f);
					mSteps =
							frictionAdd(mSteps, 10.0f * Settings.TIME_STEP, mSteps < Math.PI ? p1
									: p2);
				}
				mLastFlagX = true;
				mLastPositionX = positionX;
				mLastChunkX = chunkX;
			} else {
				float p1 = (float) (Math.PI * 0.5f);
				float p2 = (float) (Math.PI * 1.5f);
				if (mSteps != p1 && mSteps != p2)
					mSteps = pCellularity.getBody().getVelocityX() < 0 ? p1 : p2;
				mLastFlagX = false;
			}

			if (mLookTo
					&& (mLastFastLookTime == -1 || System.currentTimeMillis() - mLastFastLookTime > FAST_LOOK_DELAY)) {

				mLastFastLookTime = -1;

				float lookToX;
				float lookToY;

				if (mActiveLook) {
					lookToX = mLookToX;
					lookToY = mLookToY;
				} else {
					lookToX = body.getVelocityX();
					lookToY = body.getVelocityY();
					if (grounded) {
						lookToY = 0;
					}
				}

				if (Math.abs(lookToX) > FLOAT_EPS || Math.abs(lookToY) > FLOAT_EPS) {

					float lookToAngle = (float) Math.atan2(lookToY, lookToX);
					float eyesAngle = (float) Math.atan2(mEyesY, mEyesX);
					float angleChange = Settings.TIME_STEP * EYES_ANGULAR_VELOCITY;

					if (Math.abs(eyesAngle - lookToAngle) < angleChange
							|| Math.abs(eyesAngle - lookToAngle) > Math.PI * 2 - angleChange) {
						float len = (float) Math.sqrt(lookToX * lookToX + lookToY * lookToY);
						mEyesX = lookToX / len;
						mEyesY = lookToY / len;
					} else {
						if ((lookToAngle < eyesAngle && eyesAngle - lookToAngle < Math.PI)
								|| (lookToAngle >= eyesAngle && lookToAngle - eyesAngle > Math.PI))
							angleChange = -angleChange;

						float c = (float) Math.cos(angleChange);
						float s = (float) Math.sin(angleChange);
						float x = c * mEyesX - s * mEyesY;
						float y = s * mEyesX + c * mEyesY;
						mEyesX = x;
						mEyesY = y;
					}
				}
			}
		}
	}

	public boolean isStaticGrounded() {
		return mStatics.size() > 0;
	}

	public boolean isGrounded() {
		return mStatics.size() > 0 || mDynamics.size() > 0;
	}

	public static float saturatedAdd(float pMin, float pMax, float pCurrent, float pModifier) {
		if (pModifier < 0) {
			if (pCurrent < pMin)
				return pCurrent;
			pCurrent += pModifier;
			if (pCurrent < pMin)
				pCurrent = pMin;
			return pCurrent;
		} else {
			if (pCurrent > pMax)
				return pCurrent;
			pCurrent += pModifier;
			if (pCurrent > pMax)
				pCurrent = pMax;
			return pCurrent;
		}
	}

	public static float frictionAdd(float pCurrent, float pValue) {
		if (pCurrent < 0) {
			pCurrent += pValue;
			if (pCurrent > 0)
				return 0;
		} else {
			pCurrent -= pValue;
			if (pCurrent < 0)
				return 0;
		}
		return pCurrent;
	}

	public static float frictionAdd(float pCurrent, float pValue, float pDefault) {
		if (pCurrent < pDefault) {
			pCurrent += pValue;
			if (pCurrent > pDefault)
				return pDefault;
		} else {
			pCurrent -= pValue;
			if (pCurrent < pDefault)
				return pDefault;
		}
		return pCurrent;
	}

	private void createPhysics(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mGroundSensor == null) {
			boolean isChunk = pCellularity.isChunk();
			MetaBody body = pCellularity.getBody();
			float offsetX = pX - (isChunk ? 0 : Settings.CHUNK_HSIZE);
			float offsetY = pY - (isChunk ? 0 : Settings.CHUNK_HSIZE);
			float minX = offsetX;
			float minY = offsetY;
			float maxX = offsetX + 1.0f;
			float[] vertices = BinaryFloatArrayPool.obtain(8);
			vertices[0] = maxX - GROUND_SENSOR_WIDTH_BORDER;
			vertices[1] = minY;
			vertices[2] = minX + GROUND_SENSOR_WIDTH_BORDER;
			vertices[3] = minY;
			vertices[4] = minX + GROUND_SENSOR_WIDTH_BORDER;
			vertices[5] = minY - GROUND_SENSOR_SIZE;
			vertices[6] = maxX - GROUND_SENSOR_WIDTH_BORDER;
			vertices[7] = minY - GROUND_SENSOR_SIZE;
			mGroundSensor =
					FixtureDefPool.obtain(0.0f, 0.0f, true, 0.0f, (short) 0x0000, (short) (pZ
							- Settings.CHUNK_MIN_DEPTH + 1), (short) -1, PolygonShapePool.obtain(
							vertices, 0, 8));
			body.createFixture(mGroundSensor, this);
			BinaryFloatArrayPool.recycle(vertices);
		}
	}

	private void destroyPhysics(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mGroundSensor != null) {
			MetaBody body = pCellularity.getBody();
			body.destroyFixture(mGroundSensor);
			PolygonShapePool.recycle((PolygonShape) mGroundSensor.shape);
			FixtureDefPool.recycle(mGroundSensor);
			mGroundSensor = null;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
