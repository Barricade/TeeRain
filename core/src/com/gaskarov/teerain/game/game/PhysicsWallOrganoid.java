package com.gaskarov.teerain.game.game;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.gaskarov.teerain.core.Cell;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.util.Collidable;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.pool.BinaryFloatArrayPool;
import com.gaskarov.util.pool.FixtureDefPool;
import com.gaskarov.util.pool.PolygonShapePool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class PhysicsWallOrganoid implements Collidable {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final float FLOAT_EPS = 1e-6f;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private FixtureDef mMain;
	private FixtureDef mHorizontal;
	private FixtureDef mVertical;

	private byte mNear;

	// ===========================================================
	// Constructors
	// ===========================================================

	private PhysicsWallOrganoid() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void beginContact(Contact pContact, Fixture pFixture, Fixture pThisFixture, boolean pSwap) {
	}

	@Override
	public void endContact(Contact pContact, Fixture pFixture, Fixture pThisFixture, boolean pSwap) {
	}

	@Override
	public void preSolve(Contact pContact, Manifold pOldManifold, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap) {
		if (pThisFixture.getBody().getType() == BodyType.StaticBody
				&& pFixture.getBody().getType() == BodyType.DynamicBody && mNear != 31) {
			WorldManifold manifold = pContact.getWorldManifold();
			float[] separations = manifold.getSeparations();
			for (int i = manifold.getNumberOfContactPoints() - 1; i >= 0; --i)
				if (separations[i] < -0.01f)
					return;
			float normalX = manifold.getNormal().x;
			float normalY = manifold.getNormal().y;
			if (pSwap) {
				normalX *= -1;
				normalY *= -1;
			}
			if ((mNear & 1) != 0 && normalX >= FLOAT_EPS) {
				pContact.setEnabled(false);
			} else if ((mNear & 2) != 0 && normalY >= FLOAT_EPS) {
				pContact.setEnabled(false);
			} else if ((mNear & 4) != 0 && normalX <= -FLOAT_EPS) {
				pContact.setEnabled(false);
			} else if ((mNear & 8) != 0 && normalY <= -FLOAT_EPS) {
				pContact.setEnabled(false);
			}
		}
	}

	@Override
	public void postSolve(Contact pContact, ContactImpulse pContactImpulse, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap) {
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static PhysicsWallOrganoid obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (PhysicsWallOrganoid.class) {
				return sPool.size() == 0 ? new PhysicsWallOrganoid() : (PhysicsWallOrganoid) sPool
						.pop();
			}
		return new PhysicsWallOrganoid();
	}

	private static void recyclePure(PhysicsWallOrganoid pObj) {
		if (GlobalConstants.POOL)
			synchronized (PhysicsWallOrganoid.class) {
				sPool.push(pObj);
			}
	}

	public static PhysicsWallOrganoid obtain() {
		PhysicsWallOrganoid obj = obtainPure();
		obj.mMain = null;
		obj.mHorizontal = null;
		obj.mVertical = null;
		obj.mNear = -1;
		return obj;
	}

	public static void recycle(PhysicsWallOrganoid pObj) {
		recyclePure(pObj);
	}

	public void attach(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
	}

	public void detach(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		destroyPhysics(pCellularity, pX, pY, pZ);
	}

	public void refresh(Cellularity pCellularity, int pX, int pY, int pZ, Cell pCell) {
		byte flags = near(pCellularity, pX, pY, pZ);
		if (flags != mNear) {
			mNear = flags;
			destroyPhysics(pCellularity, pX, pY, pZ);
			createPhysics(pCellularity, pX, pY, pZ, pCell.getBorderSize(), pCell.getCornerValue(),
					pCell.getCornerSize(), pCell.getFriction(), pCell.getDensity(), pCell
							.getRestitution(), pCell.isSolid(), pCell.isSquare());
		}
	}

	private static byte near(Cellularity pCellularity, int pX, int pY, int pZ) {
		byte flags = 0;
		for (int i = 0; i < ArrayConstants.DIRECTIONS_2D_X.length; ++i) {
			Cell cell =
					pCellularity.getCell(pX + ArrayConstants.DIRECTIONS_2D_X[i], pY
							+ ArrayConstants.DIRECTIONS_2D_Y[i], pZ);
			flags |= (cell.isSolid() ? 1 << i : 0);
		}
		if (flags == 15) {
			boolean flag = true;
			for (int i = 0; i < ArrayConstants.SQUARE_CORNERS_X.length; ++i) {
				Cell cell =
						pCellularity.getCell(pX + ArrayConstants.DIRECTIONS_2D_X[i], pY
								+ ArrayConstants.DIRECTIONS_2D_Y[i], pZ);
				if (!cell.isSolid())
					flag = false;
			}
			if (flag)
				flags |= 16;
		}
		return flags;
	}

	private void createPhysics(Cellularity pCellularity, int pX, int pY, int pZ, float pBorderSize,
			float pCornerValue, float pCornerSize, float pFriction, float pDensity,
			float pRestitution, boolean pSolid, boolean pSquare) {
		boolean isChunk = pCellularity.isChunk();
		float offsetX = pX - (isChunk ? 0 : Settings.CHUNK_HSIZE);
		float offsetY = pY - (isChunk ? 0 : Settings.CHUNK_HSIZE);
		MetaBody body = pCellularity.getBody();
		float minX = offsetX + ((mNear & 4) != 0 ? 0 : pBorderSize);
		float minY = offsetY + ((mNear & 8) != 0 ? 0 : pBorderSize);
		float maxX = offsetX + 1.0f - ((mNear & 1) != 0 ? 0 : pBorderSize);
		float maxY = offsetY + 1.0f - ((mNear & 2) != 0 ? 0 : pBorderSize);
		float[] vert = BinaryFloatArrayPool.obtain(16);
		if (mNear != 31 || !pSolid || !pSquare || !isChunk) {
			vert[0] = maxX;
			vert[1] = maxY;
			vert[2] = minX;
			vert[3] = maxY;
			vert[4] = minX;
			vert[5] = minY;
			vert[6] = maxX;
			vert[7] = minY;
			mMain =
					FixtureDefPool.obtain(pDensity, pFriction, false, pRestitution, (short) 0x0000,
							(pSquare || mNear == 31) && pSolid ? (short) (pZ
									- Settings.CHUNK_MIN_DEPTH + 1) : (short) 0, (short) -1,
							PolygonShapePool.obtain(vert, 0, 8));
			body.createFixture(mMain, this);
		}
		if (!pSquare && mNear != 31 && pSolid) {

			// Horizontal
			vert[0] = maxX - pCornerValue;
			vert[1] = maxY - pCornerValue;
			vert[2] = maxX - pCornerSize;
			vert[3] = maxY;
			vert[4] = minX + pCornerSize;
			vert[5] = maxY;
			vert[6] = minX + pCornerValue;
			vert[7] = maxY - pCornerValue;
			vert[8] = minX + pCornerValue;
			vert[9] = minY + pCornerValue;
			vert[10] = minX + pCornerSize;
			vert[11] = minY;
			vert[12] = maxX - pCornerSize;
			vert[13] = minY;
			vert[14] = maxX - pCornerValue;
			vert[15] = minY + pCornerValue;
			mHorizontal =
					FixtureDefPool.obtain(0.0f, pFriction, false, pRestitution, (short) 0x0000,
							(short) (pZ - Settings.CHUNK_MIN_DEPTH + 1), (short) -1,
							PolygonShapePool.obtain(vert, 0, 16));
			body.createFixture(mHorizontal, this);

			// Vertical
			vert[0] = maxX;
			vert[1] = maxY - pCornerSize;
			vert[2] = maxX - pCornerValue;
			vert[3] = maxY - pCornerValue;
			vert[4] = minX + pCornerValue;
			vert[5] = maxY - pCornerValue;
			vert[6] = minX;
			vert[7] = maxY - pCornerSize;
			vert[8] = minX;
			vert[9] = minY + pCornerSize;
			vert[10] = minX + pCornerValue;
			vert[11] = minY + pCornerValue;
			vert[12] = maxX - pCornerValue;
			vert[13] = minY + pCornerValue;
			vert[14] = maxX;
			vert[15] = minY + pCornerSize;
			mVertical =
					FixtureDefPool.obtain(0.0f, pFriction, false, pRestitution, (short) 0x0000,
							(short) (pZ - Settings.CHUNK_MIN_DEPTH + 1), (short) -1,
							PolygonShapePool.obtain(vert, 0, 16));
			body.createFixture(mVertical, this);
		}
		BinaryFloatArrayPool.recycle(vert);
	}

	private void destroyPhysics(Cellularity pCellularity, int pX, int pY, int pZ) {
		MetaBody body = pCellularity.getBody();
		if (mMain != null) {
			body.destroyFixture(mMain);
			PolygonShapePool.recycle((PolygonShape) mMain.shape);
			FixtureDefPool.recycle(mMain);
			mMain = null;
		}
		if (mHorizontal != null) {
			body.destroyFixture(mHorizontal);
			PolygonShapePool.recycle((PolygonShape) mHorizontal.shape);
			FixtureDefPool.recycle(mHorizontal);
			mHorizontal = null;
		}
		if (mVertical != null) {
			body.destroyFixture(mVertical);
			PolygonShapePool.recycle((PolygonShape) mVertical.shape);
			FixtureDefPool.recycle(mVertical);
			mVertical = null;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
