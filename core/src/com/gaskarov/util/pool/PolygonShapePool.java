package com.gaskarov.util.pool;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class PolygonShapePool {

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

	private PolygonShapePool() {
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

	private static PolygonShape obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (PolygonShapePool.class) {
				return sPool.size() == 0 ? new PolygonShape() : (PolygonShape) sPool.pop();
			}
		return new PolygonShape();
	}

	private static void recyclePure(PolygonShape pObj) {
		if (GlobalConstants.POOL)
			synchronized (PolygonShapePool.class) {
				sPool.push(pObj);
			}
	}

	public static PolygonShape obtain(float[] pVertices, int pOffset, int pSize) {

		PolygonShape obj = obtainPure();

		obj.set(pVertices, pOffset, pSize);
		obj.setRadius(0.01f);

		return obj;
	}

	public static void recycle(PolygonShape pObj) {
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
