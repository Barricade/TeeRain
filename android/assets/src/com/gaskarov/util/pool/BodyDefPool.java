package com.gaskarov.util.pool;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class BodyDefPool {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	// ===========================================================
	// Constructors
	// ===========================================================

	private BodyDefPool() {
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

	private static BodyDef obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (BodyDefPool.class) {
				return sPool.size() == 0 ? new BodyDef() : (BodyDef) sPool.pop();
			}
		return new BodyDef();
	}

	private static void recyclePure(BodyDef pObj) {
		if (GlobalConstants.POOL)
			synchronized (BodyDefPool.class) {
				sPool.push(pObj);
			}
	}

	public static BodyDef obtain(boolean pActive, boolean pAllowSleep, float pAngle,
			float pAngularDamping, float pAngularVelocity, boolean pAwake, boolean pBullet,
			boolean pFixedRotation, float pGravityScale, float pLinearDamping,
			float pLinearVelocityX, float pLinearVelocityY, float pPositionX, float pPositionY,
			BodyType pType) {

		BodyDef obj = obtainPure();

		obj.active = pActive;
		obj.allowSleep = pAllowSleep;
		obj.angle = pAngle;
		obj.angularDamping = pAngularDamping;
		obj.angularVelocity = pAngularVelocity;
		obj.awake = pAwake;
		obj.bullet = pBullet;
		obj.fixedRotation = pFixedRotation;
		obj.gravityScale = pGravityScale;
		obj.linearDamping = pLinearDamping;
		obj.linearVelocity.x = pLinearVelocityX;
		obj.linearVelocity.y = pLinearVelocityY;
		obj.position.x = pPositionX;
		obj.position.y = pPositionY;
		obj.type = pType;

		return obj;
	}

	public static void recycle(BodyDef pObj) {
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
