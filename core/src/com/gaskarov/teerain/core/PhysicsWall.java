package com.gaskarov.teerain.core;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.util.Collidable;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.resource.CellsAction;
import com.gaskarov.teerain.resource.Settings;
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
public class PhysicsWall implements Collidable {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mX;
	private int mY;
	private int mZ;
	private Cellularity mCellularity;
	private FixtureDef mMain;
	private FixtureDef mHorizontal;
	private FixtureDef mVertical;

	private byte mNear;

	// ===========================================================
	// Constructors
	// ===========================================================

	private PhysicsWall() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getNear() {
		return mNear;
	}

	public void setNear(int pNear) {
		mNear = (byte) pNear;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public int getX() {
		return mX;
	}

	@Override
	public int getY() {
		return mY;
	}

	@Override
	public int getZ() {
		return mZ;
	}

	@Override
	public Cellularity getCellularity() {
		return mCellularity;
	}

	@Override
	public void beginContact(Contact pContact, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap, Collidable pData) {
		CellsAction.beginContact(mCellularity,
				mCellularity.getCell(mX, mY, mZ), mX, mY, mZ, pContact,
				pFixture, pThisFixture, pSwap, pData, this);
	}

	@Override
	public void endContact(Contact pContact, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap, Collidable pData) {
		CellsAction.endContact(mCellularity, mCellularity.getCell(mX, mY, mZ),
				mX, mY, mZ, pContact, pFixture, pThisFixture, pSwap, pData,
				this);
	}

	@Override
	public void preSolve(Contact pContact, Manifold pOldManifold,
			Fixture pFixture, Fixture pThisFixture, boolean pSwap,
			Collidable pData) {
		CellsAction.preSolve(mCellularity, mCellularity.getCell(mX, mY, mZ),
				mX, mY, mZ, pContact, pOldManifold, pFixture, pThisFixture,
				pSwap, pData, this);
	}

	@Override
	public void postSolve(Contact pContact, ContactImpulse pContactImpulse,
			Fixture pFixture, Fixture pThisFixture, boolean pSwap,
			Collidable pData) {
		CellsAction.postSolve(mCellularity, mCellularity.getCell(mX, mY, mZ),
				mX, mY, mZ, pContact, pContactImpulse, pFixture, pThisFixture,
				pSwap, pData, this);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static PhysicsWall obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (PhysicsWall.class) {
				return sPool.size() == 0 ? new PhysicsWall()
						: (PhysicsWall) sPool.pop();
			}
		return new PhysicsWall();
	}

	private static void recyclePure(PhysicsWall pObj) {
		if (GlobalConstants.POOL)
			synchronized (PhysicsWall.class) {
				sPool.push(pObj);
			}
	}

	public static PhysicsWall obtain(Cellularity pCellularity, int pX, int pY,
			int pZ) {
		PhysicsWall obj = obtainPure();
		obj.mX = pX;
		obj.mY = pY;
		obj.mZ = pZ;
		obj.mCellularity = pCellularity;
		obj.mMain = null;
		obj.mHorizontal = null;
		obj.mVertical = null;
		obj.mNear = -1;
		return obj;
	}

	public static void recycle(PhysicsWall pObj) {
		pObj.mCellularity = null;
		recyclePure(pObj);
	}

	public void recycle() {
		recycle(this);
	}

	public static byte near(Cellularity pCellularity, int pX, int pY, int pZ) {
		byte flags = 0;
		for (int i = 0; i < ArrayConstants.DIRECTIONS_2D_X.length; ++i) {
			boolean shellable = pCellularity.isShellable(pX
					+ ArrayConstants.DIRECTIONS_2D_X[i], pY
					+ ArrayConstants.DIRECTIONS_2D_Y[i], pZ);
			flags |= (shellable ? 1 << i : 0);
		}
		if (flags == 15)
			flags |= 16;
		return flags;
	}

	public void createPhysics(Cellularity pCellularity, int pX, int pY, int pZ,
			float pBorderSize, float pCornerValue, float pCornerSize,
			float pDensity, float pFriction, float pRestitution,
			boolean pSolid, boolean pSquare) {
		boolean isChunk = pCellularity.isChunk();
		float offsetX = pX - (isChunk ? 0 : Settings.MAX_DROP_HSIZE);
		float offsetY = pY - (isChunk ? 0 : Settings.MAX_DROP_HSIZE);
		MetaBody body = pCellularity.getBody(pX, pY);
		float minX = offsetX + ((mNear & 4) != 0 ? 0 : pBorderSize);
		float minY = offsetY + ((mNear & 8) != 0 ? 0 : pBorderSize);
		float maxX = offsetX + 1.0f - ((mNear & 1) != 0 ? 0 : pBorderSize);
		float maxY = offsetY + 1.0f - ((mNear & 2) != 0 ? 0 : pBorderSize);
		float[] vert = BinaryFloatArrayPool.obtain(16);
		vert[0] = maxX;
		vert[1] = maxY;
		vert[2] = minX;
		vert[3] = maxY;
		vert[4] = minX;
		vert[5] = minY;
		vert[6] = maxX;
		vert[7] = minY;
		mMain = FixtureDefPool.obtain(pDensity, pFriction, false, pRestitution,
				(short) 0x0000, pSquare && pSolid ? (short) (pZ
						- Settings.CHUNK_MIN_DEPTH + 1) : (short) 0,
				(short) -1, PolygonShapePool.obtain(vert, 0, 8));
		body.createFixture(mMain, this);

		if (!pSquare && pSolid) {

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
			mHorizontal = FixtureDefPool.obtain(0.0f, pFriction, false,
					pRestitution, (short) 0x0000, (short) (pZ
							- Settings.CHUNK_MIN_DEPTH + 1), (short) -1,
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
			mVertical = FixtureDefPool.obtain(0.0f, pFriction, false,
					pRestitution, (short) 0x0000, (short) (pZ
							- Settings.CHUNK_MIN_DEPTH + 1), (short) -1,
					PolygonShapePool.obtain(vert, 0, 16));
			body.createFixture(mVertical, this);
		}
		BinaryFloatArrayPool.recycle(vert);
	}

	public void destroyPhysics(Cellularity pCellularity, int pX, int pY, int pZ) {
		MetaBody body = pCellularity.getBody(pX, pY);
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
