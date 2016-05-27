package com.gaskarov.teerain.core.util;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.gaskarov.util.common.KeyValuePair;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.common.Pair;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.List;
import com.gaskarov.util.pool.BodyDefPool;
import com.gaskarov.util.pool.FixtureDefPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class MetaBody {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private BodyDef mBodyDef;
	private float mCos;
	private float mSin;
	private Body mBody;
	private LinkedHashTable mFixtures;
	private float mOffsetX;
	private float mOffsetY;
	private short mGroupIndexOffset;
	private boolean mFreezed;

	// ===========================================================
	// Constructors
	// ===========================================================

	private MetaBody() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public float getCos() {
		return mCos;
	}

	public float getSin() {
		return mSin;
	}

	public float getOffsetX() {
		return mOffsetX;
	}

	public float getOffsetY() {
		return mOffsetY;
	}

	public short getGroupIndexOffset() {
		return mGroupIndexOffset;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	private static MetaBody obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (MetaBody.class) {
				return sPool.size() == 0 ? new MetaBody() : (MetaBody) sPool.pop();
			}
		return new MetaBody();
	}

	private static void recyclePure(MetaBody pObj) {
		if (GlobalConstants.POOL)
			synchronized (MetaBody.class) {
				sPool.push(pObj);
			}
	}

	public static MetaBody obtain(boolean pActive, boolean pAllowSleep, float pAngle,
			float pAngularDamping, float pAngularVelocity, boolean pAwake, boolean pBullet,
			boolean pFixedRotation, float pGravityScale, float pLinearDamping,
			float pLinearVelocityX, float pLinearVelocityY, float pPositionX, float pPositionY,
			BodyType pType) {
		MetaBody obj = obtainPure();

		obj.mBodyDef =
				BodyDefPool.obtain(pActive, pAllowSleep, pAngle, pAngularDamping, pAngularVelocity,
						pAwake, pBullet, pFixedRotation, pGravityScale, pLinearDamping,
						pLinearVelocityX, pLinearVelocityY, pPositionX, pPositionY, pType);
		obj.mCos = (float) Math.cos(pAngle);
		obj.mSin = (float) Math.sin(pAngle);
		obj.mBody = null;
		obj.mFixtures = LinkedHashTable.obtain();
		obj.mOffsetX = 0;
		obj.mOffsetY = 0;
		obj.mGroupIndexOffset = 0;
		obj.mFreezed = false;

		return obj;
	}

	public static MetaBody obtain(BodyDef pBodyDef, float pPositionX, float pPositionY, float pAngle) {
		return obtain(pBodyDef.active, pBodyDef.allowSleep, pAngle, pBodyDef.angularDamping,
				pBodyDef.angularVelocity, pBodyDef.awake, pBodyDef.bullet, pBodyDef.fixedRotation,
				pBodyDef.gravityScale, pBodyDef.linearDamping, pBodyDef.linearVelocity.x,
				pBodyDef.linearVelocity.y, pPositionX, pPositionY, pBodyDef.type);
	}

	public static void recycle(MetaBody pObj) {

		pObj.setWorld(null);
		BodyDefPool.recycle(pObj.mBodyDef);
		pObj.mBodyDef = null;
		while (pObj.mFixtures.size() > 0) {
			KeyValuePair p = (KeyValuePair) pObj.mFixtures.remove(pObj.mFixtures.front());
			FixtureDefPool.recycle((FixtureDef) p.mA);
			Pair.recycle((Pair) p.mB);
			KeyValuePair.recycle(p);
		}
		LinkedHashTable.recycle(pObj.mFixtures);
		pObj.mFixtures = null;

		recyclePure(pObj);
	}

	public void freeze() {
		if (!mFreezed) {
			mFreezed = true;
			World world = mBody.getWorld();
			clear();
			mBodyDef.type = BodyType.StaticBody;
			create(world);
			mBodyDef.type = BodyType.DynamicBody;
		}
	}

	public void unfreeze() {
		if (mFreezed) {
			mFreezed = false;
			World world = mBody.getWorld();
			clear();
			create(world);
			mBody.setLinearVelocity(mBodyDef.linearVelocity);
			mBody.setAngularVelocity(mBodyDef.angularVelocity);
		}
	}

	public void setOffset(float pX, float pY) {
		if (mOffsetX == pX && mOffsetY == pY)
			return;
		if (mBody != null)
			mBody.setTransform(mBody.getPosition().x + pX - mOffsetX, mBody.getPosition().y + pY
					- mOffsetY, mBody.getAngle());
		mOffsetX = pX;
		mOffsetY = pY;
	}

	public void setOffsetMove(float pX, float pY) {
		if (mOffsetX == pX && mOffsetY == pY)
			return;
		mBodyDef.position.x -= pX - mOffsetX;
		mBodyDef.position.y -= pY - mOffsetY;
		mOffsetX = pX;
		mOffsetY = pY;
	}

	public void setGroupIndexOffset(short pGroupIndexOffset) {
		if (mGroupIndexOffset == pGroupIndexOffset)
			return;
		if (mBody != null) {
			for (List.Node i = mFixtures.begin(); i != mFixtures.end(); i = mFixtures.next(i)) {
				Pair p = (Pair) ((KeyValuePair) mFixtures.val(i)).mB;
				Fixture fixture = (Fixture) p.mA;
				Filter filter = fixture.getFilterData();
				filter.groupIndex +=
						(pGroupIndexOffset - mGroupIndexOffset) * MathUtils.sign(filter.groupIndex);
				fixture.setFilterData(filter);
			}
		}
		mGroupIndexOffset = pGroupIndexOffset;
	}

	public World getWorld() {
		return mBody == null ? null : mBody.getWorld();
	}

	public void setWorld(World pWorld) {
		if (pWorld == null)
			clear();
		else
			create(pWorld);
	}

	public void refresh() {
		if (mBody != null) {
			mBodyDef.angle = mBody.getAngle();
			mCos = (float) Math.cos(mBodyDef.angle);
			mSin = (float) Math.sin(mBodyDef.angle);
			if (!mFreezed) {
				mBodyDef.angularVelocity = mBody.getAngularVelocity();
				mBodyDef.linearVelocity.x = mBody.getLinearVelocity().x;
				mBodyDef.linearVelocity.y = mBody.getLinearVelocity().y;
			}
			mBodyDef.position.x = mBody.getPosition().x - mOffsetX;
			mBodyDef.position.y = mBody.getPosition().y - mOffsetY;
		}
	}

	public BodyType getBodyType() {
		return mBodyDef.type;
	}

	public float getAngle() {
		return mBodyDef.angle;
	}

	public float getPositionX() {
		return mBodyDef.position.x;
	}

	public float getPositionY() {
		return mBodyDef.position.y;
	}

	public void setPosition(float pX, float pY) {
		mBodyDef.position.x = pX;
		mBodyDef.position.y = pY;
		if (mBody != null)
			mBody.setTransform(mBodyDef.position.x + mOffsetX, mBodyDef.position.y + mOffsetY,
					mBody.getAngle());
	}

	public float getWorldPositionX() {
		return mBody == null ? 0 : mBody.getPosition().x;
	}

	public float getWorldPositionY() {
		return mBody == null ? 0 : mBody.getPosition().y;
	}

	public float getMassCenterX() {
		return mBody == null ? 0 : mBody.getWorldCenter().x;
	}

	public float getMassCenterY() {
		return mBody == null ? 0 : mBody.getWorldCenter().y;
	}

	public float getMass() {
		return mBody == null ? 0 : mBody.getMass();
	}

	public float getInertia() {
		return mBody == null ? 0 : mBody.getInertia();
	}

	public float getAngularVelocity() {
		return mBodyDef.angularVelocity;
	}

	public float getVelocityX() {
		return mBodyDef.linearVelocity.x;
	}

	public float getVelocityY() {
		return mBodyDef.linearVelocity.y;
	}

	public void setAngularVelocity(float pAngularVelocity) {
		mBodyDef.angularVelocity = pAngularVelocity;
		if (mBody != null)
			mBody.setAngularVelocity(pAngularVelocity);
	}

	public void setVelocity(float pVelocityX, float pVelocityY) {
		mBodyDef.linearVelocity.x = pVelocityX;
		if (mBody != null)
			mBody.setLinearVelocity(pVelocityX, pVelocityY);
	}

	public void applyLinearImpulse(float pImpulseX, float pImpulseY, float pPointX, float pPointY) {
		mBody.applyLinearImpulse(pImpulseX, pImpulseY, pPointX, pPointY, true);
	}

	public void applyAngularImpulse(float pTorque) {
		mBody.applyAngularImpulse(pTorque, true);
	}

	public void setFixed(boolean pIsFixed) {
		mBodyDef.fixedRotation = pIsFixed;
		if (mBody != null)
			mBody.setFixedRotation(pIsFixed);
	}

	public void setActive(boolean pIsActive) {
		mBodyDef.active = pIsActive;
		if (mBody != null)
			mBody.setActive(pIsActive);
	}

	public void setGravityScale(float pGravityScale) {
		mBodyDef.gravityScale = pGravityScale;
		if (mBody != null)
			mBody.setGravityScale(pGravityScale);
	}

	public void createFixture(FixtureDef pFixtureDef, Object pUserData) {
		Fixture fixture = null;
		if (mBody != null) {
			short tmp = pFixtureDef.filter.groupIndex;
			pFixtureDef.filter.groupIndex +=
					mGroupIndexOffset * MathUtils.sign(pFixtureDef.filter.groupIndex);
			fixture = mBody.createFixture(pFixtureDef);
			fixture.setUserData(pUserData);
			pFixtureDef.filter.groupIndex = tmp;
		}
		mFixtures.set(KeyValuePair.obtain(pFixtureDef, Pair.obtain(fixture, pUserData)));
	}

	public void destroyFixture(FixtureDef pFixtureDef) {
		KeyValuePair tmp = (KeyValuePair) mFixtures.remove(pFixtureDef);
		Pair p = (Pair) tmp.mB;
		if (p.mA != null)
			mBody.destroyFixture((Fixture) p.mA);
		Pair.recycle(p);
		KeyValuePair.recycle(tmp);
	}

	private void clear() {
		if (mBody != null) {
			for (List.Node i = mFixtures.begin(); i != mFixtures.end(); i = mFixtures.next(i))
				((Pair) ((KeyValuePair) mFixtures.val(i)).mB).mA = null;
			mBody.getWorld().destroyBody(mBody);
			mBody = null;
		}
	}

	private void create(World pWorld) {
		if (mBody != null && mBody.getWorld() == pWorld)
			return;
		clear();
		mBodyDef.position.x += mOffsetX;
		mBodyDef.position.y += mOffsetY;
		mBody = pWorld.createBody(mBodyDef);
		refresh();
		for (List.Node i = mFixtures.begin(); i != mFixtures.end(); i = mFixtures.next(i)) {
			FixtureDef fixtureDef = (FixtureDef) ((KeyValuePair) mFixtures.val(i)).mA;
			short tmp = fixtureDef.filter.groupIndex;
			fixtureDef.filter.groupIndex +=
					mGroupIndexOffset * MathUtils.sign(fixtureDef.filter.groupIndex);
			Pair p = (Pair) ((KeyValuePair) mFixtures.val(i)).mB;
			Fixture fixture = mBody.createFixture(fixtureDef);
			p.mA = fixture;
			fixture.setUserData(p.mB);
			fixtureDef.filter.groupIndex = tmp;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
