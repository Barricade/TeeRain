package com.gaskarov.teerain.tissularity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gaskarov.teerain.Organularity;
import com.gaskarov.teerain.Resources;
import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.organoid.ControlOrganoid;
import com.gaskarov.teerain.organoid.VisitorOrganoid;
import com.gaskarov.teerain.util.GraphicsModule;
import com.gaskarov.teerain.util.TimeMeasure;
import com.gaskarov.util.common.IntVector1;
import com.gaskarov.util.common.IntVector2;
import com.gaskarov.util.common.IntVector3;
import com.gaskarov.util.common.KeyValuePair;
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

	// ===========================================================
	// Fields
	// ===========================================================

	private final IntVector2 mVec2 = IntVector2.obtain(0, 0);

	protected Organularity mOrganularity;

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

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

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

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public abstract void control(Cellularity pCellularity, int pX, int pY, int pZ,
			ControlOrganoid pControlOrganoid);

	protected abstract Runnable chunkLoader(int pX, int pY, ChunkHolder pChunkHolder);

	protected abstract Runnable chunkUnloader(int pX, int pY, ChunkHolder pChunkHolder);

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

	public void resize(int pWidth, int pHeight) {
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
	}

	protected void dispose() {
		if (mOrganularity != null)
			mOrganularity.removeTissularity(this);

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
		if (pOldChunkX + pOldWidth <= pChunkX || pChunkX + pWidth <= pOldChunkX
				|| pOldChunkY + pOldHeight <= pChunkY || pChunkY + pHeight <= pOldChunkY) {
			addVisitor(pChunkX, pChunkY, pWidth, pHeight);
			removeVisitor(pOldChunkX, pOldChunkY, pOldWidth, pOldHeight);
			return;
		}

		pOldChunkX -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pOldChunkY -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pOldWidth += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;
		pOldHeight += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;
		pChunkX -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pChunkY -= Settings.VISITOR_EXTRA_BORDER_SIZE;
		pWidth += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;
		pHeight += Settings.VISITOR_EXTRA_BORDER_SIZE * 2;
		int chunkToX = pChunkX + pWidth;
		int chunkToY = pChunkY + pHeight;
		int oldChunkToX = pOldChunkX + pOldWidth;
		int oldChunkToY = pOldChunkY + pOldHeight;
		int totalChunkX = Math.min(pChunkX, pOldChunkX);
		int totalChunkY = Math.min(pChunkY, pOldChunkY);
		int totalChunkToX = Math.max(chunkToX, oldChunkToX);
		int totalChunkToY = Math.max(chunkToY, oldChunkToY);
		int softChunkX = pChunkX - Settings.VISITOR_SOFT_BORDER_SIZE;
		int softChunkY = pChunkY - Settings.VISITOR_SOFT_BORDER_SIZE;
		int softChunkToX = chunkToX + Settings.VISITOR_SOFT_BORDER_SIZE;
		int softChunkToY = chunkToY + Settings.VISITOR_SOFT_BORDER_SIZE;
		int softOldChunkX = pOldChunkX - Settings.VISITOR_SOFT_BORDER_SIZE;
		int softOldChunkY = pOldChunkY - Settings.VISITOR_SOFT_BORDER_SIZE;
		int softOldChunkToX = oldChunkToX + Settings.VISITOR_SOFT_BORDER_SIZE;
		int softOldChunkToY = oldChunkToY + Settings.VISITOR_SOFT_BORDER_SIZE;
		int softTotalChunkX = totalChunkX - Settings.VISITOR_SOFT_BORDER_SIZE;
		int softTotalChunkY = totalChunkY - Settings.VISITOR_SOFT_BORDER_SIZE;
		int softTotalChunkToX = totalChunkToX + Settings.VISITOR_SOFT_BORDER_SIZE;
		int softTotalChunkToY = totalChunkToY + Settings.VISITOR_SOFT_BORDER_SIZE;
		for (int i = softTotalChunkY; i < softTotalChunkToY; ++i)
			for (int j = softTotalChunkX; j < softTotalChunkToX; ++j) {
				boolean inRect = pChunkX <= j && j < chunkToX && pChunkY <= i && i < chunkToY;
				boolean inOldRect =
						pOldChunkX <= j && j < oldChunkToX && pOldChunkY <= i && i < oldChunkToY;
				boolean inSoftRect =
						softChunkX <= j && j < softChunkToX && softChunkY <= i && i < softChunkToY;
				boolean inSoftOldRect =
						softOldChunkX <= j && j < softOldChunkToX && softOldChunkY <= i
								&& i < softOldChunkToY;
				if (inSoftRect && !inSoftOldRect) {
					KeyValuePair val = (KeyValuePair) mChunkVisitors.get(mVec2.set(j, i));
					if (val == null) {
						mChunkVisitors.set(KeyValuePair.obtain(mVec2.cpy(), IntVector1.obtain(1)));
					} else {
						++((IntVector1) val.mB).x;
					}
				}
				if (inRect && !inOldRect) {
					loadChunk(j, i);
				}
				if (inSoftOldRect && !inSoftRect) {
					KeyValuePair val = (KeyValuePair) mChunkVisitors.get(mVec2.set(j, i));
					if (--((IntVector1) val.mB).x == 0) {
						mChunkVisitors.remove(mVec2);
						IntVector2.recycle((IntVector2) val.mA);
						IntVector1.recycle((IntVector1) val.mB);
						KeyValuePair.recycle(val);

						unloadChunk(j, i);
					}
				}
			}
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

	protected Cellularity getChunk(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mChunks.get(mVec2.set(pX, pY));
		return pair != null ? ((ChunkHolder) pair.mB).chunk() : null;
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

}
