package com.gaskarov.teerain.resource;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.Cellularity.CellData;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.util.Collidable;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.IntArray;
import com.gaskarov.util.pool.BinaryFloatArrayPool;
import com.gaskarov.util.pool.FixtureDefPool;
import com.gaskarov.util.pool.PolygonShapePool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class LootCellData extends CellData implements Collidable {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MAX_ITEMS_RENDER = 4;
	private static final float ITEMS_RENDER_INTERVAL = 0.025f;
	private static final float ITEMS_DISPERTION = 0.25f;
	private static final float ITEMS_RENDER_STEP_INTERVAL = (float) Math.PI * 2
			/ MAX_ITEMS_RENDER;
	private static final float SPEED = 8f;
	private static final float STEP_ACCEL = (float) Math.PI * 1.5f;
	private static final float SENSOR_WIDTH_BORDER = 0.25f;
	private static final float SENSOR_HEIGHT_BORDER = 0.25f;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private int mX;
	private int mY;
	private int mZ;
	private Cellularity mCellularity;

	private FixtureDef mSensor;

	private float mPositionX;
	private float mStep;
	private IntArray mItems;
	private Array mItemsData;
	private IntArray mItemsCount;

	// ===========================================================
	// Constructors
	// ===========================================================

	private LootCellData() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public IntArray getItems() {
		return mItems;
	}

	public Array getItemsData() {
		return mItemsData;
	}

	public IntArray getItemsCount() {
		return mItemsCount;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public LootCellData cpy() {
		LootCellData loot = obtain();
		loot.mPositionX = mPositionX;
		loot.mStep = mStep;
		for (int i = 0; i < mItems.size(); ++i) {
			CellData cellData = (CellData) mItemsData.get(i);
			loot.push(mItems.get(i), cellData != null ? cellData.cpy() : null,
					mItemsCount.get(i));
		}
		return loot;
	}

	@Override
	public void recycle() {
		recycle(this);
	}

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
		if (pData instanceof LootCellData) {
			LootCellData loot = (LootCellData) pData;
			if (loot.mItems.size() > 0 && mItems.size() > 0) {
				for (int i = 0; i < loot.mItems.size(); ++i) {
					CellData itemData = (CellData) loot.mItemsData.get(i);
					push(loot.mItems.get(i), itemData, loot.mItemsCount.get(i));
				}
				loot.mItems.clear();
				loot.mItemsData.clear();
				loot.mItemsCount.clear();
				loot.mCellularity.destroyCell(loot.mX, loot.mY, loot.mZ);
			}
		}
	}

	@Override
	public void endContact(Contact pContact, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap, Collidable pData) {
	}

	@Override
	public void preSolve(Contact pContact, Manifold pOldManifold,
			Fixture pFixture, Fixture pThisFixture, boolean pSwap,
			Collidable pData) {
	}

	@Override
	public void postSolve(Contact pContact, ContactImpulse pContactImpulse,
			Fixture pFixture, Fixture pThisFixture, boolean pSwap,
			Collidable pData) {
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static LootCellData obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (LootCellData.class) {
				return sPool.size() == 0 ? new LootCellData()
						: (LootCellData) sPool.pop();
			}
		return new LootCellData();
	}

	private static void recyclePure(LootCellData pObj) {
		if (GlobalConstants.POOL)
			synchronized (LootCellData.class) {
				sPool.push(pObj);
			}
	}

	public static LootCellData obtain() {
		LootCellData obj = obtainPure();
		obj.mSensor = null;
		obj.mPositionX = ((float) Math.random() - 0.5f) * ITEMS_DISPERTION;
		obj.mStep = (float) (Math.random() * Math.PI * 2);
		obj.mItems = IntArray.obtain();
		obj.mItemsData = Array.obtain();
		obj.mItemsCount = IntArray.obtain();
		return obj;
	}

	public static void recycle(LootCellData pObj) {
		IntArray.recycle(pObj.mItems);
		pObj.mItems = null;
		while (pObj.mItemsData.size() > 0) {
			CellData cellData = (CellData) pObj.mItemsData.pop();
			if (cellData != null) {
				cellData.recycle();
			}
		}
		Array.recycle(pObj.mItemsData);
		pObj.mItemsData = null;
		IntArray.recycle(pObj.mItemsCount);
		pObj.mItemsCount = null;
		recyclePure(pObj);
	}

	public void push(int pItem, CellData pItemData, int pItemCount) {
		for (int i = 0; i < mItems.size(); ++i) {
			if (mItems.get(i) == pItem
					&& (mItemsData.get(i) == null && pItemData == null || mItemsData
							.get(i) != null
							&& pItemData != null
							&& mItemsData.get(i).equals(pItemData))) {
				mItemsCount.set(i, mItemsCount.get(i) + pItemCount);
				if (pItemData != null)
					pItemData.recycle();
				return;
			}
		}
		mItems.push(pItem);
		mItemsData.push(pItemData);
		mItemsCount.push(pItemCount);
	}

	public void attach(Cellularity pCellularity, int pX, int pY, int pZ) {
		mX = pX;
		mY = pY;
		mZ = pZ;
		mCellularity = pCellularity;
		createPhysics(pCellularity, pX, pY, pZ);
		pCellularity.tickEnable(pX, pY, pZ);
	}

	public void detach(Cellularity pCellularity, int pX, int pY, int pZ) {
		pCellularity.tickDisable(pX, pY, pZ);
		destroyPhysics(pCellularity, pX, pY, pZ);
		mCellularity = null;
	}

	public void tick(Cellularity pCellularity, int pX, int pY, int pZ) {
		MetaBody body = pCellularity.getBody();
		body.setGravityScale(0);
		Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
		float x = p.x;
		float y = p.y;
		int posX = MathUtils.floor(x);
		int posY = MathUtils.floor(y);
		ChunkCellularity chunk = pCellularity.getChunk();
		boolean flag10 = chunk.isSolid(posX, posY - 1, pZ);
		boolean flag01 = chunk.isSolid(posX - 1, posY, pZ);
		boolean flag11 = chunk.isSolid(posX, posY, pZ);
		boolean flag21 = chunk.isSolid(posX + 1, posY, pZ);
		boolean flag12 = chunk.isSolid(posX, posY + 1, pZ);
		float vx = 0;
		float vy = 0;
		if (flag11) {
			if (!flag10)
				vy = -SPEED;
			else if (!flag01 && !flag21)
				vx = x - posX < 0.5f ? -SPEED : SPEED;
			else if (!flag01)
				vx = -SPEED;
			else if (!flag21)
				vx = SPEED;
			else if (!flag12)
				vy = SPEED;
		} else if (!flag10) {
			vy = -SPEED;
		} else {
			float speed = SPEED;
			float d = Math.abs(y - posY - 0.5f);
			if (d < SPEED * Settings.TIME_STEP)
				speed = d / Settings.TIME_STEP;
			vy = y - posY < 0.5f ? speed : -speed;
		}

		body.setVelocity(vx, vy);
		mStep += STEP_ACCEL * Settings.TIME_STEP;
	}

	public void render(int pCell, Cellularity pCellularity, int pX, int pY,
			int pZ, float pOffsetX, float pOffsetY, int pTileX, int pTileY,
			float pSize, float pCos, float pSin, FloatArray[] pRenderBuffers) {
		int count = 0;
		for (int i = 0; i < mItems.size(); ++i)
			count += mItemsCount.get(i);
		int num = Math.min(MAX_ITEMS_RENDER, count);
		if (num == 0)
			return;
		float offset = -(num - 1) * ITEMS_RENDER_INTERVAL * 0.5f;
		int k = 0;
		for (int i = 0; i < mItems.size(); ++i) {
			for (int j = Math.min(mItemsCount.get(i), num
					- (mItems.size() - i - 1)); j > 0; --j) {
				CellsAction.renderItem(
						pCellularity,
						mItems.get(i),
						(CellData) mItemsData.get(i),
						pX,
						pY,
						pZ,
						pOffsetX,
						pOffsetY,
						pTileX,
						pTileY,
						pSize,
						pCos,
						pSin,
						pRenderBuffers[pZ * Settings.LAYERS_PER_DEPTH
								+ Cells.CELL_RENDER_LAYER_SPECIAL],
						0.5f + mPositionX + offset + k * ITEMS_RENDER_INTERVAL,
						0.5f + (float) Math.sin(mStep + k
								* ITEMS_RENDER_STEP_INTERVAL) * 0.25f);
				++k;
				if (--num == 0)
					return;
			}
		}
	}

	private void createPhysics(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mSensor == null) {
			boolean isChunk = pCellularity.isChunk();
			MetaBody body = pCellularity.getBody();
			float offsetX = pX - (isChunk ? 0 : Settings.MAX_DROP_HSIZE);
			float offsetY = pY - (isChunk ? 0 : Settings.MAX_DROP_HSIZE);
			float x = offsetX + 0.5f;
			float y = offsetY + 0.5f;
			float[] vertices = BinaryFloatArrayPool.obtain(8);
			vertices[0] = x + SENSOR_WIDTH_BORDER;
			vertices[1] = y + SENSOR_HEIGHT_BORDER;
			vertices[2] = x - SENSOR_WIDTH_BORDER;
			vertices[3] = y + SENSOR_HEIGHT_BORDER;
			vertices[4] = x - SENSOR_WIDTH_BORDER;
			vertices[5] = y - SENSOR_HEIGHT_BORDER;
			vertices[6] = x + SENSOR_WIDTH_BORDER;
			vertices[7] = y - SENSOR_HEIGHT_BORDER;
			mSensor = FixtureDefPool.obtain(0.0f, 0.0f, true, 0.0f,
					(short) 0x0000,
					(short) (pZ - Settings.CHUNK_MIN_DEPTH + 1), (short) -1,
					PolygonShapePool.obtain(vertices, 0, 8));
			body.createFixture(mSensor, this);
			BinaryFloatArrayPool.recycle(vertices);
		}
	}

	private void destroyPhysics(Cellularity pCellularity, int pX, int pY, int pZ) {
		if (mSensor != null) {
			MetaBody body = pCellularity.getBody();
			body.destroyFixture(mSensor);
			PolygonShapePool.recycle((PolygonShape) mSensor.shape);
			FixtureDefPool.recycle(mSensor);
			mSensor = null;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
