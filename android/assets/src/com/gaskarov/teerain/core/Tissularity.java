package com.gaskarov.teerain.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gaskarov.teerain.core.util.GraphicsModule;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Resources;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.debug.TimeMeasure;
import com.gaskarov.util.common.IntVector1;
import com.gaskarov.util.common.IntVector2;
import com.gaskarov.util.common.IntVector3;
import com.gaskarov.util.common.KeyValuePair;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.common.Pair;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.List;
import com.gaskarov.util.container.Queue;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public abstract class Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int RAYCAST_TYPE_START = 0;
	public static final int RAYCAST_TYPE_MIDDLE = 1;
	public static final int RAYCAST_TYPE_END = 2;

	// ===========================================================
	// Fields
	// ===========================================================

	private final IntVector2 mVec2 = IntVector2.obtain(0, 0);

	private int mRayCastBlockCellX;
	private int mRayCastBlockCellY;
	private int mRayCastPreBlockCellX;
	private int mRayCastPreBlockCellY;
	private float mRayCastBlockX;
	private float mRayCastBlockY;
	private int mRayCastType;
	private float mRayCastBlockDistance2;
	private Cellularity mRayCastBlockCellularity;

	protected Organularity mOrganularity;

	protected LinkedHashTable mRegionVisitors;
	protected LinkedHashTable mRegions;

	protected LinkedHashTable mChunkVisitors;
	protected LinkedHashTable mChunks;
	protected Queue mDelayedChunks;

	protected short mGroupIndexPackOffset;

	protected int mOffsetX;
	protected int mOffsetY;

	protected float mCameraX;
	protected float mCameraY;

	protected int mStepsLeft;

	protected LinkedHashTable mVisitors;

	protected GraphicsModule mGraphicsModule;

	protected LinkedHashTable mControllables;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getRayCastBlockCellX() {
		return mRayCastBlockCellX;
	}

	public int getRayCastBlockCellY() {
		return mRayCastBlockCellY;
	}

	public int getRayCastPreBlockCellX() {
		return mRayCastPreBlockCellX;
	}

	public int getRayCastPreBlockCellY() {
		return mRayCastPreBlockCellY;
	}

	public float getRayCastBlockX() {
		return mRayCastBlockX;
	}

	public float getRayCastBlockY() {
		return mRayCastBlockY;
	}

	public int getRayCastType() {
		return mRayCastType;
	}

	public Cellularity getRayCastBlockCellularity() {
		return mRayCastBlockCellularity;
	}

	public Organularity getOrganularity() {
		return mOrganularity;
	}

	public int getOffsetX() {
		return mOffsetX;
	}

	public int getOffsetY() {
		return mOffsetY;
	}

	public float getCameraX() {
		return mCameraX;
	}

	public float getCameraY() {
		return mCameraY;
	}

	public void setCameraX(float pCameraX) {
		mCameraX = pCameraX;
	}

	public void setCameraY(float pCameraY) {
		mCameraY = pCameraY;
	}

	public short getGroupIndexPackOffset() {
		return mGroupIndexPackOffset;
	}

	public GraphicsModule getGraphicsModule() {
		return mGraphicsModule;
	}

	public LinkedHashTable getControllables() {
		return mControllables;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	protected abstract Runnable chunkLoader(int pX, int pY, ChunkHolder pChunkHolder);

	protected abstract Runnable chunkUnloader(int pX, int pY, ChunkHolder pChunkHolder);

	protected abstract Runnable regionLoader(int pX, int pY, RegionDataHolder pRegionDataHolder);

	protected abstract Runnable regionUnloader(int pX, int pY, RegionDataHolder pRegionDataHolder);

	// ===========================================================
	// Methods
	// ===========================================================

	public void attach(Organularity pOrganularity) {
		mOrganularity = pOrganularity;
		mStepsLeft = Settings.STEPS_PER_TICK;
		mGroupIndexPackOffset = mOrganularity.addGroupIndexPack();
	}

	public void detach() {
		while (mVisitors.size() > 0) {
			KeyValuePair p = (KeyValuePair) mVisitors.front();
			Pair pair = (Pair) p.mB;
			IntVector3 tmp = (IntVector3) pair.mB;
			((VisitorOrganoid) p.mA)
					.setIsEnabled((Cellularity) pair.mA, tmp.x, tmp.y, tmp.z, false);
		}
		waitChunks();
		mOrganularity.removeGroupIndex(mGroupIndexPackOffset);
		mGroupIndexPackOffset = 0;
		mOrganularity = null;
	}

	public void tick() {
		if (mOrganularity == null)
			return;
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.refreshBodies();
		}
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.refreshCellularitiesChunks();
		}
		if (mChunks.size() != 0) {
			IntVector2 p = (IntVector2) ((KeyValuePair) mChunks.front()).mA;
			int x = p.x << Settings.CHUNK_SIZE_LOG;
			int y = p.y << Settings.CHUNK_SIZE_LOG;
			if (Math.abs(mOffsetX - x) > Settings.MAX_CENTER_OFFSET
					|| Math.abs(mOffsetY - y) > Settings.MAX_CENTER_OFFSET) {
				mCameraX += mOffsetX - x;
				mCameraY += mOffsetY - y;
				mOffsetX = x;
				mOffsetY = y;
				mGraphicsModule.commandOffset(mOffsetX, mOffsetY);
				for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
					Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
					if (chunk != null) {
						chunk.refreshOffset();
						chunk.refreshRender();
					}
				}
			}
		}
		tickUpdates();
		TimeMeasure.sM2.start();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.precalc(Math.max(mStepsLeft - 1, 1));
		}
		TimeMeasure.sM2.end();
		if (--mStepsLeft == 0) {
			TimeMeasure.sM3.start();
			for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
				Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
				if (chunk != null)
					chunk.update();
			}
			TimeMeasure.sM3.end();
			mStepsLeft = Settings.STEPS_PER_TICK;
		}
		TimeMeasure.sM4.start();
		for (int i = 0; mDelayedChunks.size() > 0 && i < Settings.MAX_CHUNK_PUSH_PER_TICK; ++i)
			pushOneDelayedChunk();
		TimeMeasure.sM4.end();
		TimeMeasure.sM5.start();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.drop();
		}
		TimeMeasure.sM5.end();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.postDrop();
		}
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.tick();
		}
		TimeMeasure.sM6.start();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.refresh();
		}
		TimeMeasure.sM6.end();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			Cellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.render();
		}
		mGraphicsModule.commandMoveCamera(0, 0, 0, mCameraX, mCameraY);
		mGraphicsModule.commandsFlush(mOrganularity.getUpdateLastTime());
	}

	public void render(long pTime) {
	}

	public boolean keyDown(int pKeycode) {
		return true;
	}

	public boolean keyUp(int pKeycode) {
		return true;
	}

	public boolean keyTyped(char pCharacter) {
		return true;
	}

	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		return true;
	}

	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		return true;
	}

	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		return true;
	}

	public boolean mouseMoved(int pScreenX, int pScreenY) {
		return true;
	}

	public boolean scrolled(int pAmount) {
		return true;
	}

	protected void init() {

		mOrganularity = null;

		mRegionVisitors = LinkedHashTable.obtain();
		mRegions = LinkedHashTable.obtain();

		mChunkVisitors = LinkedHashTable.obtain();
		mChunks = LinkedHashTable.obtain();
		mDelayedChunks = Queue.obtain();

		mOffsetX = 0;
		mOffsetY = 0;

		mGroupIndexPackOffset = 0;

		mCameraX = 0;
		mCameraY = 0;

		mStepsLeft = 0;

		mVisitors = LinkedHashTable.obtain();

		mGraphicsModule = GraphicsModule.obtain();

		mControllables = LinkedHashTable.obtain();
	}

	protected void dispose() {
		if (mOrganularity != null)
			mOrganularity.removeTissularity(this);

		LinkedHashTable.recycle(mRegionVisitors);
		mRegionVisitors = null;
		LinkedHashTable.recycle(mRegions);
		mRegions = null;
		LinkedHashTable.recycle(mChunkVisitors);
		mChunkVisitors = null;
		LinkedHashTable.recycle(mChunks);
		mChunks = null;
		Queue.recycle(mDelayedChunks);
		mDelayedChunks = null;
		LinkedHashTable.recycle(mVisitors);
		mVisitors = null;
		GraphicsModule.recycle(mGraphicsModule);
		mGraphicsModule = null;
		LinkedHashTable.recycle(mControllables);
		mControllables = null;
	}

	protected void tickUpdates() {
	}

	public void pushVisitor(VisitorOrganoid pVisitor, Cellularity pCellularity, int pX, int pY,
			int pZ) {
		mVisitors.set(KeyValuePair.obtain(pVisitor, Pair.obtain(pCellularity, IntVector3.obtain(pX,
				pY, pZ))));
	}

	public void removeVisitor(VisitorOrganoid pVisitor) {
		KeyValuePair p = (KeyValuePair) mVisitors.remove(pVisitor);
		if (p != null) {
			Pair pair = (Pair) p.mB;
			IntVector3.recycle((IntVector3) pair.mB);
			Pair.recycle(pair);
			KeyValuePair.recycle(p);
		}
	}

	public void moveVisitor(int pOldChunkX, int pOldChunkY, int pOldWidth, int pOldHeight,
			int pChunkX, int pChunkY, int pWidth, int pHeight) {
		if (pOldChunkX == pChunkX && pOldChunkY == pChunkY && pOldWidth == pWidth
				&& pOldHeight == pHeight)
			return;
		addVisitor(pChunkX, pChunkY, pWidth, pHeight);
		removeVisitor(pOldChunkX, pOldChunkY, pOldWidth, pOldHeight);
	}

	public void addVisitor(int pChunkX, int pChunkY, int pWidth, int pHeight) {

		pChunkX -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pChunkY -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pWidth += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;
		pHeight += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;

		int h = pHeight + Settings.VISITOR_SOFT_BORDER_SIZE;
		int w = pWidth + Settings.VISITOR_SOFT_BORDER_SIZE;
		for (int i = -Settings.VISITOR_SOFT_BORDER_SIZE; i < h; ++i)
			for (int j = -Settings.VISITOR_SOFT_BORDER_SIZE; j < w; ++j) {
				mVec2.set(j + pChunkX, i + pChunkY);

				KeyValuePair val = (KeyValuePair) mChunkVisitors.get(mVec2);

				if (val == null) {
					mChunkVisitors.set(KeyValuePair.obtain(mVec2.cpy(), IntVector1.obtain(1)));
				} else {
					++((IntVector1) val.mB).x;
				}

				if (0 <= i && i < pHeight && 0 <= j && j < pWidth) {
					loadChunk(mVec2.x, mVec2.y);
				}
			}

		int startX = pChunkX >> Settings.REGION_SIZE_LOG;
		int startY = pChunkY >> Settings.REGION_SIZE_LOG;
		int endX = pChunkX + pWidth >> Settings.REGION_SIZE_LOG;
		int endY = pChunkY + pHeight >> Settings.REGION_SIZE_LOG;
		int localStartX = pChunkX & Settings.REGION_SIZE_MASK;
		int localStartY = pChunkY & Settings.REGION_SIZE_MASK;
		int localEndX = pChunkX + pWidth & Settings.REGION_SIZE_MASK;
		int localEndY = pChunkY + pHeight & Settings.REGION_SIZE_MASK;

		int softStartX = startX - (localStartX < Settings.REGION_SOFT_SIZE ? 1 : 0);
		int softStartY = startY - (localStartY < Settings.REGION_SOFT_SIZE ? 1 : 0);
		int softEndX =
				endX + (localEndX >= Settings.REGION_SIZE - Settings.REGION_SOFT_SIZE ? 1 : 0);
		int softEndY =
				endY + (localEndY >= Settings.REGION_SIZE - Settings.REGION_SOFT_SIZE ? 1 : 0);
		int preStartX = startX - (localStartX < Settings.REGION_PRELOAD_SIZE ? 1 : 0);
		int preStartY = startY - (localStartY < Settings.REGION_PRELOAD_SIZE ? 1 : 0);
		int preEndX =
				endX + (localEndX >= Settings.REGION_SIZE - Settings.REGION_PRELOAD_SIZE ? 1 : 0);
		int preEndY =
				endY + (localEndY >= Settings.REGION_SIZE - Settings.REGION_PRELOAD_SIZE ? 1 : 0);
		for (int i = softStartY; i <= softEndY; ++i)
			for (int j = softStartX; j <= softEndX; ++j) {
				mVec2.set(j, i);

				KeyValuePair val = (KeyValuePair) mRegionVisitors.get(mVec2);

				if (val == null) {
					mRegionVisitors.set(KeyValuePair.obtain(mVec2.cpy(), IntVector1.obtain(1)));
				} else {
					++((IntVector1) val.mB).x;
				}

				if (preStartY <= i && i <= preEndY && preStartX <= j && j <= preEndX) {
					loadRegion(mVec2.x, mVec2.y);
				}
			}
	}

	public void removeVisitor(int pChunkX, int pChunkY, int pWidth, int pHeight) {

		pChunkX -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pChunkY -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pWidth += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;
		pHeight += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;

		int h = pHeight + Settings.VISITOR_SOFT_BORDER_SIZE;
		int w = pWidth + Settings.VISITOR_SOFT_BORDER_SIZE;
		for (int i = -Settings.VISITOR_SOFT_BORDER_SIZE; i < h; ++i)
			for (int j = -Settings.VISITOR_SOFT_BORDER_SIZE; j < w; ++j) {

				mVec2.set(j + pChunkX, i + pChunkY);

				KeyValuePair val = (KeyValuePair) mChunkVisitors.get(mVec2);
				if (--((IntVector1) val.mB).x == 0) {
					mChunkVisitors.remove(mVec2);
					IntVector2.recycle((IntVector2) val.mA);
					IntVector1.recycle((IntVector1) val.mB);
					KeyValuePair.recycle(val);

					unloadChunk(mVec2.x, mVec2.y);
				}
			}

		int startX = pChunkX >> Settings.REGION_SIZE_LOG;
		int startY = pChunkY >> Settings.REGION_SIZE_LOG;
		int endX = pChunkX + pWidth >> Settings.REGION_SIZE_LOG;
		int endY = pChunkY + pHeight >> Settings.REGION_SIZE_LOG;
		int localStartX = pChunkX & Settings.REGION_SIZE_MASK;
		int localStartY = pChunkY & Settings.REGION_SIZE_MASK;
		int localEndX = pChunkX + pWidth & Settings.REGION_SIZE_MASK;
		int localEndY = pChunkY + pHeight & Settings.REGION_SIZE_MASK;

		int softStartX = startX - (localStartX < Settings.REGION_SOFT_SIZE ? 1 : 0);
		int softStartY = startY - (localStartY < Settings.REGION_SOFT_SIZE ? 1 : 0);
		int softEndX =
				endX + (localEndX >= Settings.REGION_SIZE - Settings.REGION_SOFT_SIZE ? 1 : 0);
		int softEndY =
				endY + (localEndY >= Settings.REGION_SIZE - Settings.REGION_SOFT_SIZE ? 1 : 0);
		for (int i = softStartY; i <= softEndY; ++i)
			for (int j = softStartX; j <= softEndX; ++j) {
				mVec2.set(j, i);

				KeyValuePair val = (KeyValuePair) mRegionVisitors.get(mVec2);
				if (--((IntVector1) val.mB).x == 0) {
					mRegionVisitors.remove(mVec2);
					IntVector2.recycle((IntVector2) val.mA);
					IntVector1.recycle((IntVector1) val.mB);
					KeyValuePair.recycle(val);

					unloadRegion(mVec2.x, mVec2.y);
				}
			}
	}

	private void loadRegion(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mRegions.get(mVec2.set(pX, pY));
		if (pair == null)
			mRegions.set(pair =
					KeyValuePair.obtain(mVec2.cpy(), RegionDataHolder.obtain(this, pX, pY)));
		((RegionDataHolder) pair.mB).nextState(RegionDataHolder.NEXT_STATE_LOADED);
	}

	private void unloadRegion(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mRegions.get(mVec2.set(pX, pY));
		if (pair != null)
			((RegionDataHolder) pair.mB).nextState(RegionDataHolder.NEXT_STATE_EMPTY);
	}

	private void removeRegion(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mRegions.remove(mVec2.set(pX, pY));
		IntVector2.recycle((IntVector2) pair.mA);
		RegionDataHolder.recycle((RegionDataHolder) pair.mB);
		KeyValuePair.recycle(pair);
	}

	private void loadChunk(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mChunks.get(mVec2.set(pX, pY));
		if (pair == null)
			mChunks.set(pair = KeyValuePair.obtain(mVec2.cpy(), ChunkHolder.obtain(this, pX, pY)));
		((ChunkHolder) pair.mB).nextState(ChunkHolder.NEXT_STATE_ATTACHED);
	}

	private void unloadChunk(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mChunks.get(mVec2.set(pX, pY));
		if (pair != null)
			((ChunkHolder) pair.mB).nextState(ChunkHolder.NEXT_STATE_EMPTY);
	}

	private void removeChunk(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mChunks.remove(mVec2.set(pX, pY));
		IntVector2.recycle((IntVector2) pair.mA);
		ChunkHolder.recycle((ChunkHolder) pair.mB);
		KeyValuePair.recycle(pair);
	}

	private void attachChunkDelayed(ChunkHolder pChunkHolder) {
		mDelayedChunks.push(pChunkHolder);
	}

	private void detachChunkDelayed(ChunkHolder pChunkHolder) {
		mDelayedChunks.push(pChunkHolder);
	}

	private void pushDelayedChunks() {
		while (mDelayedChunks.size() > 0)
			pushOneDelayedChunk();
	}

	protected void waitChunks() {
		do {
			mOrganularity.waitOperations();
			pushDelayedChunks();
		} while (mDelayedChunks.size() > 0 || !mOrganularity.operationsEmpty());
	}

	private void pushOneDelayedChunk() {
		if (mDelayedChunks.size() > 0)
			((ChunkHolder) mDelayedChunks.shift()).finish();
	}

	public Cellularity getChunk(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mChunks.get(mVec2.set(pX, pY));
		return pair != null ? ((ChunkHolder) pair.mB).chunk() : null;
	}

	public Cell getCell(int pX, int pY, int pZ) {
		Cellularity chunk = getChunk(pX >> Settings.CHUNK_SIZE_LOG, pY >> Settings.CHUNK_SIZE_LOG);
		return chunk != null ? chunk.getCell(pX & Settings.CHUNK_SIZE_MASK, pY
				& Settings.CHUNK_SIZE_MASK, pZ) : VoidCell.obtain();
	}

	public void setCell(int pX, int pY, int pZ, Cell pCell) {
		Cellularity chunk = getChunk(pX >> Settings.CHUNK_SIZE_LOG, pY >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null)
			chunk.setCell(pX & Settings.CHUNK_SIZE_MASK, pY & Settings.CHUNK_SIZE_MASK, pZ, pCell);
		else
			pCell.recycle();
	}

	public int getAI(int pX, int pY, int pZ) {
		Cellularity chunk = getChunk(pX >> Settings.CHUNK_SIZE_LOG, pY >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null)
			return chunk.getAI(pX & Settings.CHUNK_SIZE_MASK, pY & Settings.CHUNK_SIZE_MASK, pZ);
		return 0;
	}

	public void setAI(int pX, int pY, int pZ, int pAIField, int pAIVertical, int pAIHorizontal) {
		Cellularity chunk = getChunk(pX >> Settings.CHUNK_SIZE_LOG, pY >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null)
			chunk.setAI(pX & Settings.CHUNK_SIZE_MASK, pY & Settings.CHUNK_SIZE_MASK, pZ, pAIField,
					pAIVertical, pAIHorizontal);
	}

	public int[] getLight(int pX, int pY, int pZ) {
		Cellularity chunk = getChunk(pX >> Settings.CHUNK_SIZE_LOG, pY >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null)
			return chunk.getLight(pX & Settings.CHUNK_SIZE_MASK, pY & Settings.CHUNK_SIZE_MASK, pZ);
		return Cellularity.COLOR_BLACK;
	}

	public void setLight(int pX, int pY, int pZ, int pR, int pG, int pB) {
		Cellularity chunk = getChunk(pX >> Settings.CHUNK_SIZE_LOG, pY >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null)
			chunk.setLight(pX & Settings.CHUNK_SIZE_MASK, pY & Settings.CHUNK_SIZE_MASK, pZ, pR,
					pG, pB);
	}

	public void raycastCell(int pZ, float pStartX, float pStartY, float pEndX, float pEndY,
			Cellularity pIgnore, Cellularity pCellularity) {

		int offsetX = pCellularity == null ? mOffsetX : 0;
		int offsetY = pCellularity == null ? mOffsetY : 0;

		int startX = MathUtils.floor(pStartX);
		int startY = MathUtils.floor(pStartY);

		{
			Cell cell = getCell(startX + offsetX, startY + offsetY, pZ);
			if (cell.isBlocking()) {
				mRayCastBlockCellX = startX + offsetX;
				mRayCastBlockCellY = startY + offsetY;
				mRayCastBlockDistance2 = 0;
				mRayCastType = RAYCAST_TYPE_START;
				mRayCastBlockCellularity = pCellularity;
				return;
			}
		}

		int endX = MathUtils.floor(pEndX);
		int endY = MathUtils.floor(pEndY);
		float dx = pEndX - pStartX;
		float dy = pEndY - pStartY;

		if (pCellularity == null) {
			mRayCastBlockCellX = endX + offsetX;
			mRayCastBlockCellY = endY + offsetY;
			mRayCastBlockDistance2 = dx * dx + dy * dy;
			mRayCastType = RAYCAST_TYPE_END;
			mRayCastBlockCellularity = pCellularity;
		}

		int vx = pStartX < pEndX ? 1 : -1;
		int vy = pStartY < pEndY ? 1 : -1;

		for (int posX = startX; posX != endX; posX += vx) {
			int nextPosX = posX + vx;
			float x = (posX + nextPosX + 1) * 0.5f;
			float y = pStartY + (x - pStartX) / dx * dy;
			int nextPosY = MathUtils.floor(y);
			Cell cell =
					pCellularity == null ? getCell(nextPosX + offsetX, nextPosY + offsetY, pZ)
							: pCellularity.getCell(nextPosX, nextPosY, pZ);
			if (cell.isBlocking()) {
				float x1 = x - pStartX;
				float y1 = y - pStartY;
				float distance = x1 * x1 + y1 * y1;
				if (distance < mRayCastBlockDistance2) {
					mRayCastBlockCellX = nextPosX + offsetX;
					mRayCastBlockCellY = nextPosY + offsetY;
					mRayCastPreBlockCellX = posX + offsetX;
					mRayCastPreBlockCellY = nextPosY + offsetY;
					mRayCastBlockX = x;
					mRayCastBlockY = y;
					mRayCastBlockDistance2 = x1 * x1 + y1 * y1;
					mRayCastType = RAYCAST_TYPE_MIDDLE;
					mRayCastBlockCellularity = pCellularity;
				}
				break;
			}
		}

		for (int posY = startY; posY != endY; posY += vy) {
			int nextPosY = posY + vy;
			float y = (posY + nextPosY + 1) * 0.5f;
			float x = pStartX + (y - pStartY) / dy * dx;
			int nextPosX = MathUtils.floor(x);
			Cell cell =
					pCellularity == null ? getCell(nextPosX + offsetX, nextPosY + offsetY, pZ)
							: pCellularity.getCell(nextPosX, nextPosY, pZ);
			if (cell.isBlocking()) {
				float x1 = x - pStartX;
				float y1 = y - pStartY;
				float distance = x1 * x1 + y1 * y1;
				if (distance < mRayCastBlockDistance2) {
					mRayCastBlockCellX = nextPosX + offsetX;
					mRayCastBlockCellY = nextPosY + offsetY;
					mRayCastPreBlockCellX = nextPosX + offsetX;
					mRayCastPreBlockCellY = posY + offsetY;
					mRayCastBlockX = x;
					mRayCastBlockY = y;
					mRayCastBlockDistance2 = distance;
					mRayCastType = RAYCAST_TYPE_MIDDLE;
					mRayCastBlockCellularity = pCellularity;
				}
				break;
			}
		}

		if (pCellularity == null) {
			int left = (Math.min(startX, endX) >> Settings.CHUNK_SIZE_LOG) - 1;
			int bottom = (Math.min(startY, endY) >> Settings.CHUNK_SIZE_LOG) - 1;
			int right = (Math.max(startX, endX) >> Settings.CHUNK_SIZE_LOG) + 1;
			int top = (Math.max(startY, endY) >> Settings.CHUNK_SIZE_LOG) + 1;
			for (int i = bottom; i <= top; ++i)
				for (int j = left; j <= right; ++j) {
					Cellularity chunk = getChunk(j, i);
					if (chunk != null) {
						LinkedHashTable dynamics = chunk.getCellularities();
						for (List.Node k = dynamics.begin(); k != dynamics.end(); k =
								dynamics.next(k)) {
							Cellularity dynamic = (Cellularity) dynamics.val(k);
							if (dynamic != pIgnore) {
								MetaBody body = dynamic.getBody();
								float x1 = pStartX - body.getPositionX() - body.getOffsetX();
								float y1 = pStartY - body.getPositionY() - body.getOffsetY();
								float x2 = pEndX - body.getPositionX() - body.getOffsetX();
								float y2 = pEndY - body.getPositionY() - body.getOffsetY();
								float angle = -body.getAngle();
								float c = (float) Math.cos(angle);
								float s = (float) Math.sin(angle);
								float dynamicStartX = c * x1 - s * y1 + Settings.CHUNK_HSIZE;
								float dynamicStartY = s * x1 + c * y1 + Settings.CHUNK_HSIZE;
								float dynamicEndX = c * x2 - s * y2 + Settings.CHUNK_HSIZE;
								float dynamicEndY = s * x2 + c * y2 + Settings.CHUNK_HSIZE;
								raycastCell(pZ, dynamicStartX, dynamicStartY, dynamicEndX,
										dynamicEndY, pIgnore, dynamic);
							}
						}
					}
				}
		}
	}

	protected void render(float pCellSize, float pWidth, float pHeight, long pTime) {

		FloatArray[] renderBuffers = mOrganularity.getRenderBuffers();
		mGraphicsModule.render(renderBuffers, pCellSize, pWidth, pHeight, pTime);

		SpriteBatch spriteBatch = mOrganularity.getSpriteBatch();
		for (int i = Settings.LAYERS - 1; i >= 0; --i) {
			FloatArray renderBuffer = renderBuffers[i];
			spriteBatch.draw(Resources.MAIN_TEXTURE, renderBuffer.data(), 0, renderBuffer.size());
			renderBuffer.clear();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	protected final static class ChunkHolder {

		public static final int STATE_EMPTY = 0;
		public static final int STATE_LOADING = 1;
		public static final int STATE_LOADED = 2;
		public static final int STATE_ATTACHING = 3;
		public static final int STATE_ATTACHED = 4;
		public static final int STATE_DETACHING = 5;
		public static final int STATE_UNLOADING = 6;

		public static final int NEXT_STATE_EMPTY = 0;
		public static final int NEXT_STATE_ATTACHED = 1;

		private static final Array sPool = Array.obtain();

		private Tissularity mTissularity;
		private int mX;
		private int mY;
		private Cellularity mChunk;
		private int mState;
		private int mNextState;

		private ChunkHolder() {
		}

		public int getX() {
			return mX;
		}

		public int getY() {
			return mY;
		}

		public Cellularity getChunk() {
			return mChunk;
		}

		public void setChunk(Cellularity pChunk) {
			mChunk = pChunk;
		}

		private static ChunkHolder obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (ChunkHolder.class) {
					return sPool.size() == 0 ? new ChunkHolder() : (ChunkHolder) sPool.pop();
				}
			return new ChunkHolder();
		}

		private static void recyclePure(ChunkHolder pObj) {
			if (GlobalConstants.POOL)
				synchronized (ChunkHolder.class) {
					sPool.push(pObj);
				}
		}

		public static ChunkHolder obtain(Tissularity pTissularity, int pX, int pY) {
			ChunkHolder obj = obtainPure();
			obj.mTissularity = pTissularity;
			obj.mX = pX;
			obj.mY = pY;
			obj.mState = STATE_EMPTY;
			obj.mNextState = NEXT_STATE_EMPTY;
			return obj;
		}

		public static void recycle(ChunkHolder pObj) {
			pObj.mTissularity = null;
			recyclePure(pObj);
		}

		public Cellularity chunk() {
			return mState == STATE_ATTACHED ? mChunk : null;
		}

		public void nextState(int pNextState) {
			mNextState = pNextState;
			next();
		}

		public void finish() {
			if (mState == STATE_LOADING) {
				mState = STATE_LOADED;
			} else if (mState == STATE_ATTACHING) {
				Cellularity[][] chunks = mChunk.getChunks();
				for (int i = 0; i < ArrayConstants.MOVE_AROUND_SIZE; ++i) {
					int vx = ArrayConstants.MOVE_AROUND_X[i];
					int vy = ArrayConstants.MOVE_AROUND_Y[i];
					Cellularity chunk = mTissularity.getChunk(mX + vx, mY + vy);
					if (chunk != null) {
						chunks[vy + 1][vx + 1] = chunk;
						chunk.getChunks()[1 - vy][1 - vx] = mChunk;
					}
				}
				mChunk.attach(mTissularity, mX, mY);
				mState = STATE_ATTACHED;
			} else if (mState == STATE_DETACHING) {
				mChunk.detach();
				Cellularity[][] chunks = mChunk.getChunks();
				for (int i = 0; i < ArrayConstants.MOVE_AROUND_SIZE; ++i) {
					int vx = ArrayConstants.MOVE_AROUND_X[i];
					int vy = ArrayConstants.MOVE_AROUND_Y[i];
					Cellularity chunk = chunks[vy + 1][vx + 1];
					if (chunk != null) {
						chunk.getChunks()[1 - vy][1 - vx] = null;
						chunks[vy + 1][vx + 1] = null;
					}
				}
				mState = STATE_LOADED;
			} else if (mState == STATE_UNLOADING) {
				mState = STATE_EMPTY;
			}
			next();
		}

		private void next() {
			if (mState == STATE_LOADED) {
				if (mNextState == NEXT_STATE_ATTACHED) {
					mState = STATE_ATTACHING;
					mTissularity.attachChunkDelayed(this);
				} else {
					mState = STATE_UNLOADING;
					mTissularity.getOrganularity().pushOperation(
							mTissularity.chunkUnloader(mX, mY, this));
				}
			} else if (mState == STATE_EMPTY) {
				if (mNextState == NEXT_STATE_ATTACHED) {
					mState = STATE_LOADING;
					mTissularity.getOrganularity().pushOperation(
							mTissularity.chunkLoader(mX, mY, this));
				} else {
					mTissularity.removeChunk(mX, mY);
				}
			} else if (mState == STATE_ATTACHED) {
				if (mNextState == NEXT_STATE_EMPTY) {
					mState = STATE_DETACHING;
					mTissularity.detachChunkDelayed(this);
				}
			}
		}

	}

	protected final static class RegionDataHolder {

		public static final int STATE_EMPTY = 0;
		public static final int STATE_LOADING = 1;
		public static final int STATE_LOADED = 2;
		public static final int STATE_UNLOADING = 3;

		public static final int NEXT_STATE_EMPTY = 0;
		public static final int NEXT_STATE_LOADED = 1;

		private static final Array sPool = Array.obtain();

		private Tissularity mTissularity;
		private int mX;
		private int mY;
		private RegionData mRegionData;
		private int mState;
		private int mNextState;

		private RegionDataHolder() {
		}

		public int getX() {
			return mX;
		}

		public int getY() {
			return mY;
		}

		public RegionData getRegionData() {
			return mRegionData;
		}

		public void setRegionData(RegionData pRegionData) {
			mRegionData = pRegionData;
		}

		private static RegionDataHolder obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (RegionDataHolder.class) {
					return sPool.size() == 0 ? new RegionDataHolder() : (RegionDataHolder) sPool
							.pop();
				}
			return new RegionDataHolder();
		}

		private static void recyclePure(RegionDataHolder pObj) {
			if (GlobalConstants.POOL)
				synchronized (RegionDataHolder.class) {
					sPool.push(pObj);
				}
		}

		public static RegionDataHolder obtain(Tissularity pTissularity, int pX, int pY) {
			RegionDataHolder obj = obtainPure();
			obj.mTissularity = pTissularity;
			obj.mX = pX;
			obj.mY = pY;
			obj.mState = STATE_EMPTY;
			obj.mNextState = NEXT_STATE_EMPTY;
			return obj;
		}

		public static void recycle(RegionDataHolder pObj) {
			pObj.mTissularity = null;
			recyclePure(pObj);
		}

		public RegionData regionData() {
			return mState == STATE_LOADED ? mRegionData : null;
		}

		public void nextState(int pNextState) {
			mNextState = pNextState;
			next();
		}

		public void finish() {
			if (mState == STATE_LOADING) {
				mState = STATE_LOADED;
			} else if (mState == STATE_UNLOADING) {
				mState = STATE_EMPTY;
			}
			next();
		}

		private void next() {
			if (mState == STATE_LOADED) {
				if (mNextState == NEXT_STATE_EMPTY) {
					mState = STATE_UNLOADING;
					mTissularity.getOrganularity().pushDataOperation(
							mTissularity.regionUnloader(mX, mY, this));
				}
			} else if (mState == STATE_EMPTY) {
				if (mNextState == NEXT_STATE_LOADED) {
					mState = STATE_LOADING;
					mTissularity.getOrganularity().pushDataOperation(
							mTissularity.regionLoader(mX, mY, this));
				} else {
					mTissularity.removeRegion(mX, mY);
				}
			}
		}

	}

}
