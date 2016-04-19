package com.gaskarov.util.pool;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class FixtureDefPool {

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

	private FixtureDefPool() {
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

	private static FixtureDef obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (FixtureDefPool.class) {
				return sPool.size() == 0 ? new FixtureDef() : (FixtureDef) sPool.pop();
			}
		return new FixtureDef();
	}

	private static void recyclePure(FixtureDef pObj) {
		if (GlobalConstants.POOL)
			synchronized (FixtureDefPool.class) {
				sPool.push(pObj);
			}
	}

	public static FixtureDef obtain(float pDensity, float pFriction, boolean pIsSensor,
			float pRestitution, short pCategoryBits, short pGroupIndex, short pMaskBits,
			Shape pShape) {

		FixtureDef obj = obtainPure();

		obj.density = pDensity;
		obj.friction = pFriction;
		obj.isSensor = pIsSensor;
		obj.restitution = pRestitution;
		obj.filter.categoryBits = pCategoryBits;
		obj.filter.groupIndex = pGroupIndex;
		obj.filter.maskBits = pMaskBits;
		obj.shape = pShape;

		return obj;
	}

	public static void recycle(FixtureDef pObj) {
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
