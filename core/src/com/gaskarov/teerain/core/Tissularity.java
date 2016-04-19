package com.gaskarov.teerain.core;

import static com.gaskarov.teerain.core.util.Settings.AI_STEPS_PER_TICK;
import static com.gaskarov.teerain.core.util.Settings.CELL_STEPS_PER_TICK;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_MAX_DEPTH;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_REGIONS_BOTTOM;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_REGIONS_LEFT;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_REGIONS_RIGHT;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_REGIONS_SIZE_LOG;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_REGIONS_TOP;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_REGION_SIZE_LOG;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_SIZE_LOG;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_SIZE_MASK;
import static com.gaskarov.teerain.core.util.Settings.DEPTH_FACTORS;
import static com.gaskarov.teerain.core.util.Settings.LIGHT_STEPS_PER_TICK;
import static com.gaskarov.teerain.core.util.Settings.MAX_CENTER_OFFSET;
import static com.gaskarov.teerain.core.util.Settings.MAX_CHUNK_PUSH_PER_TICK;
import static com.gaskarov.teerain.core.util.Settings.MAX_DROP_HSIZE;
import static com.gaskarov.teerain.core.util.Settings.MAX_DROP_SIZE;
import static com.gaskarov.teerain.core.util.Settings.VISITOR_EXTRA_BORDER_SIZE;
import static com.gaskarov.teerain.core.util.Settings.VISITOR_SOFT_BORDER_SIZE;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.cellularity.DynamicCellularity;
import com.gaskarov.teerain.core.util.MetaBody;
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

	protected LinkedHashTable mChunkVisitors;
	protected LinkedHashTable mChunks;
	protected Queue mDelayedChunks;

	protected short mGroupIndexPackOffset;

	protected int mOffsetX;
	protected int mOffsetY;

	protected float mCameraX;
	protected float mCameraY;

	protected int mCellStepsLeft;
	protected int mAIStepsLeft;
	protected int mLightStepsLeft;

	protected LinkedHashTable mVisitors;

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

	public LinkedHashTable getControllables() {
		return mControllables;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	protected abstract Runnable chunkLoader(int pX, int pY, ChunkHolder pChunkHolder);

	protected abstract Runnable chunkUnloader(int pX, int pY, ChunkHolder pChunkHolder);

	// ===========================================================
	// Methods
	// ===========================================================

	public void attach(Organularity pOrganularity) {
		mOrganularity = pOrganularity;
		mCellStepsLeft = CELL_STEPS_PER_TICK;
		mAIStepsLeft = AI_STEPS_PER_TICK;
		mLightStepsLeft = LIGHT_STEPS_PER_TICK;
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
		TimeMeasure.sM4.start();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.cleanCellularities();
		}
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.refreshBodies();
		}
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.refreshCellularitiesChunks();
		}
		if (mChunks.size() != 0) {
			IntVector2 p = (IntVector2) ((KeyValuePair) mChunks.front()).mA;
			int x = p.x << CHUNK_SIZE_LOG;
			int y = p.y << CHUNK_SIZE_LOG;
			if (Math.abs(mOffsetX - x) > MAX_CENTER_OFFSET
					|| Math.abs(mOffsetY - y) > MAX_CENTER_OFFSET) {
				mCameraX += mOffsetX - x;
				mCameraY += mOffsetY - y;
				mOffsetX = x;
				mOffsetY = y;
				for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
					ChunkCellularity chunk =
							((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
					if (chunk != null)
						chunk.refreshOffset();
				}
			}
		}
		tickUpdates();
		TimeMeasure.sM4.end();

		TimeMeasure.sM5.start();
		int cellUpdate = 0;
		int aiUpdate = 0;
		int lightUpdate = 0;
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null) {
				cellUpdate += chunk.cellUpdate();
				aiUpdate += chunk.aiUpdate();
				lightUpdate += chunk.lightUpdate();
			}
		}
		cellUpdate = MathUtils.divCeil(cellUpdate, Math.max(mCellStepsLeft - 1, 1));
		aiUpdate = MathUtils.divCeil(aiUpdate, Math.max(mAIStepsLeft - 1, 1));
		lightUpdate = MathUtils.divCeil(lightUpdate, Math.max(mLightStepsLeft - 1, 1));

		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null) {
				cellUpdate = chunk.precalcCells(cellUpdate);
				aiUpdate = chunk.precalcAI(aiUpdate);
				lightUpdate = chunk.precalcLight(lightUpdate);
			}
		}
		TimeMeasure.sM5.end();

		TimeMeasure.sM6.start();
		if (--mAIStepsLeft == 0) {
			for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
				ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
				if (chunk != null)
					chunk.updateAI();
			}
			mAIStepsLeft = AI_STEPS_PER_TICK;
		}
		if (--mLightStepsLeft == 0) {
			for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
				ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
				if (chunk != null)
					chunk.updateLight();
			}
			mLightStepsLeft = LIGHT_STEPS_PER_TICK;
		}
		if (--mCellStepsLeft == 0) {
			for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
				ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
				if (chunk != null)
					chunk.updateCells();
			}
			mCellStepsLeft = CELL_STEPS_PER_TICK;
		}
		TimeMeasure.sM6.end();

		TimeMeasure.sM7.start();
		for (int i = 0; mDelayedChunks.size() > 0 && i < MAX_CHUNK_PUSH_PER_TICK; ++i)
			pushOneDelayedChunk();
		TimeMeasure.sM7.end();

		TimeMeasure.sM8.start();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.drop();
		}

		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.postDrop();
		}
		TimeMeasure.sM8.end();

		TimeMeasure.sM9.start();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.tickCells();
		}
		TimeMeasure.sM9.end();

		TimeMeasure.sM10.start();
		for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i)) {
			ChunkCellularity chunk = ((ChunkHolder) ((KeyValuePair) mChunks.val(i)).mB).chunk();
			if (chunk != null)
				chunk.refreshCells();
		}
		TimeMeasure.sM10.end();

		TimeMeasure.sM11.start();
		OrthographicCamera camera = mOrganularity.getCamera();
		renderTick(mOffsetX, mOffsetY, mCameraX, mCameraY, Settings.TILE_RENDER,
				camera.viewportWidth / Settings.TILE_RENDER, camera.viewportHeight
						/ Settings.TILE_RENDER);
		TimeMeasure.sM11.end();
	}

	protected void renderTick(int pOffsetX, int pOffsetY, float pCameraX, float pCameraY,
			float pCellSize, float pWidth, float pHeight) {

		FloatArray[] renderBuffers = mOrganularity.getRenderBuffers();

		float depthFactor = DEPTH_FACTORS[CHUNK_MAX_DEPTH];
		float depthWidth = pWidth * depthFactor;
		float depthHeight = pHeight * depthFactor;
		float left = pCameraX - depthWidth / 2;
		float bottom = pCameraY - depthHeight / 2;

		int extra = MAX_DROP_SIZE;

		int col1 = pOffsetX + MathUtils.floor(left) - extra >> CHUNK_REGION_SIZE_LOG;
		int col2 = pOffsetX + MathUtils.ceil(left + depthWidth) + extra >> CHUNK_REGION_SIZE_LOG;

		int row1 = pOffsetY + MathUtils.floor(bottom) - extra >> CHUNK_REGION_SIZE_LOG;
		int row2 = pOffsetY + MathUtils.ceil(bottom + depthHeight) + extra >> CHUNK_REGION_SIZE_LOG;

		int chunkCol1 = col1 >> CHUNK_REGIONS_SIZE_LOG;
		int chunkCol2 = col2 >> CHUNK_REGIONS_SIZE_LOG;
		int chunkRow1 = row1 >> CHUNK_REGIONS_SIZE_LOG;
		int chunkRow2 = row2 >> CHUNK_REGIONS_SIZE_LOG;

		for (int chunkRow = chunkRow1; chunkRow <= chunkRow2; ++chunkRow)
			for (int chunkCol = chunkCol1; chunkCol <= chunkCol2; ++chunkCol) {
				ChunkCellularity chunk = getChunk(chunkCol, chunkRow);
				if (chunk != null) {
					int localCol1 =
							Math.max(CHUNK_REGIONS_LEFT, col1
									- (chunkCol << CHUNK_REGIONS_SIZE_LOG));
					int localCol2 =
							Math.min(CHUNK_REGIONS_RIGHT, col2
									- (chunkCol << CHUNK_REGIONS_SIZE_LOG));
					int localRow1 =
							Math.max(CHUNK_REGIONS_BOTTOM, row1
									- (chunkRow << CHUNK_REGIONS_SIZE_LOG));
					int localRow2 =
							Math.min(CHUNK_REGIONS_TOP, row2 - (chunkRow << CHUNK_REGIONS_SIZE_LOG));
					for (int localRow = localRow1; localRow <= localRow2; ++localRow)
						for (int localCol = localCol1; localCol <= localCol2; ++localCol) {
							LinkedHashTable dynamics =
									chunk.getDynamics()[localCol
											| (localRow << CHUNK_REGIONS_SIZE_LOG)];
							for (List.Node i = dynamics.begin(); i != dynamics.end(); i =
									dynamics.next(i)) {
								DynamicCellularity cellularity =
										(DynamicCellularity) dynamics.val(i);
								cellularity.render(renderBuffers, pOffsetX, pOffsetY, pCameraX,
										pCameraY, pCellSize, chunkCol, chunkRow, pWidth, pHeight);
							}
						}
				}
			}
		for (int chunkRow = chunkRow1; chunkRow <= chunkRow2; ++chunkRow)
			for (int chunkCol = chunkCol1; chunkCol <= chunkCol2; ++chunkCol) {
				ChunkCellularity chunk = getChunk(chunkCol, chunkRow);
				if (chunk != null) {
					chunk.render(renderBuffers, pOffsetX, pOffsetY, pCameraX, pCameraY, pCellSize,
							chunkCol, chunkRow, pWidth, pHeight);
				}
			}

		FloatArray renderBufferA = mOrganularity.getRenderBufferA();
		for (int i = Settings.LAYERS - 1; i >= 0; --i) {
			FloatArray renderBuffer = renderBuffers[i];
			renderBufferA.pushArray(renderBuffer);
			renderBuffer.clear();
		}
	}

	public void render() {
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

		mCellStepsLeft = 0;
		mAIStepsLeft = 0;
		mLightStepsLeft = 0;

		mVisitors = LinkedHashTable.obtain();

		mControllables = LinkedHashTable.obtain();
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

		pChunkX -= VISITOR_EXTRA_BORDER_SIZE;
		pChunkY -= VISITOR_EXTRA_BORDER_SIZE;
		pWidth += VISITOR_EXTRA_BORDER_SIZE * 2;
		pHeight += VISITOR_EXTRA_BORDER_SIZE * 2;

		int chunkLeft = pChunkX >> CHUNK_SIZE_LOG;
		int chunkBottom = pChunkY >> CHUNK_SIZE_LOG;
		int chunkRight = (pChunkX + pWidth - 1) >> CHUNK_SIZE_LOG;
		int chunkTop = (pChunkY + pHeight - 1) >> CHUNK_SIZE_LOG;
		int softChunkLeft = (pChunkX - VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		int softChunkBottom = (pChunkY - VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		int softChunkRight = (pChunkX + pWidth - 1 + VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		int softChunkTop = (pChunkY + pHeight - 1 + VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		for (int i = softChunkBottom; i <= softChunkTop; ++i)
			for (int j = softChunkLeft; j <= softChunkRight; ++j) {
				mVec2.set(j, i);

				KeyValuePair val = (KeyValuePair) mChunkVisitors.get(mVec2);

				if (val == null) {
					mChunkVisitors.set(KeyValuePair.obtain(mVec2.cpy(), IntVector1.obtain(1)));
				} else {
					++((IntVector1) val.mB).x;
				}

				if (chunkBottom <= i && i <= chunkTop && chunkLeft <= j && j <= chunkRight) {
					loadChunk(mVec2.x, mVec2.y);
				}
			}
	}

	public void removeVisitor(int pChunkX, int pChunkY, int pWidth, int pHeight) {

		pChunkX -= VISITOR_EXTRA_BORDER_SIZE;
		pChunkY -= VISITOR_EXTRA_BORDER_SIZE;
		pWidth += VISITOR_EXTRA_BORDER_SIZE * 2;
		pHeight += VISITOR_EXTRA_BORDER_SIZE * 2;

		int softChunkLeft = (pChunkX - VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		int softChunkBottom = (pChunkY - VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		int softChunkRight = (pChunkX + pWidth - 1 + VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		int softChunkTop = (pChunkY + pHeight - 1 + VISITOR_SOFT_BORDER_SIZE) >> CHUNK_SIZE_LOG;
		for (int i = softChunkBottom; i <= softChunkTop; ++i)
			for (int j = softChunkLeft; j <= softChunkRight; ++j) {
				mVec2.set(j, i);

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

	public ChunkCellularity getChunk(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mChunks.get(mVec2.set(pX, pY));
		return pair != null ? ((ChunkHolder) pair.mB).chunk() : null;
	}

	private void raycastCellDynamic(int pZ, float pStartX, float pStartY, float pEndX, float pEndY,
			DynamicCellularity pCellularity) {

		int startX = MathUtils.floor(pStartX);
		int startY = MathUtils.floor(pStartY);

		if (pCellularity.isBlocking(pCellularity.getCell(startX, startY, pZ), startX, startY, pZ)) {
			mRayCastBlockCellX = startX;
			mRayCastBlockCellY = startY;
			mRayCastBlockDistance2 = 0;
			mRayCastType = RAYCAST_TYPE_START;
			mRayCastBlockCellularity = pCellularity;
			return;
		}

		int endX = MathUtils.floor(pEndX);
		int endY = MathUtils.floor(pEndY);
		float dx = pEndX - pStartX;
		float dy = pEndY - pStartY;

		int vx = pStartX < pEndX ? 1 : -1;
		int vy = pStartY < pEndY ? 1 : -1;

		for (int posX = startX; posX != endX; posX += vx) {
			int nextPosX = posX + vx;
			float x = (posX + nextPosX + 1) * 0.5f;
			float y = pStartY + (x - pStartX) / dx * dy;
			int nextPosY = MathUtils.floor(y);
			if (pCellularity.isBlocking(pCellularity.getCell(nextPosX, nextPosY, pZ), nextPosX,
					nextPosY, pZ)) {
				float x1 = x - pStartX;
				float y1 = y - pStartY;
				float distance = x1 * x1 + y1 * y1;
				if (distance < mRayCastBlockDistance2) {
					mRayCastBlockCellX = nextPosX;
					mRayCastBlockCellY = nextPosY;
					mRayCastPreBlockCellX = posX;
					mRayCastPreBlockCellY = nextPosY;
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
			if (pCellularity.isBlocking(pCellularity.getCell(nextPosX, nextPosY, pZ), nextPosX,
					nextPosY, pZ)) {
				float x1 = x - pStartX;
				float y1 = y - pStartY;
				float distance = x1 * x1 + y1 * y1;
				if (distance < mRayCastBlockDistance2) {
					mRayCastBlockCellX = nextPosX;
					mRayCastBlockCellY = nextPosY;
					mRayCastPreBlockCellX = nextPosX;
					mRayCastPreBlockCellY = posY;
					mRayCastBlockX = x;
					mRayCastBlockY = y;
					mRayCastBlockDistance2 = distance;
					mRayCastType = RAYCAST_TYPE_MIDDLE;
					mRayCastBlockCellularity = pCellularity;
				}
				break;
			}
		}
	}

	public int getAI(int pX, int pY, int pZ) {
		ChunkCellularity chunk = getChunk(pX >> CHUNK_SIZE_LOG, pY >> CHUNK_SIZE_LOG);
		return chunk == null ? 0 : chunk.getAI(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
	}

	public void setAI(int pX, int pY, int pZ, int pAI) {
		ChunkCellularity chunk = getChunk(pX >> CHUNK_SIZE_LOG, pY >> CHUNK_SIZE_LOG);
		if (chunk != null)
			chunk.setAI(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ, pAI);
	}

	public boolean isSolid(int pX, int pY, int pZ) {
		ChunkCellularity chunk = getChunk(pX >> CHUNK_SIZE_LOG, pY >> CHUNK_SIZE_LOG);
		final int localX = pX & CHUNK_SIZE_MASK;
		final int localY = pY & CHUNK_SIZE_MASK;
		return chunk == null
				|| chunk.isSolid(chunk.getCell(localX, localY, pZ), localX, localY, pZ);
	}

	public boolean isBlocking(int pX, int pY, int pZ) {
		ChunkCellularity chunk = getChunk(pX >> CHUNK_SIZE_LOG, pY >> CHUNK_SIZE_LOG);
		final int localX = pX & CHUNK_SIZE_MASK;
		final int localY = pY & CHUNK_SIZE_MASK;
		return chunk == null
				|| chunk.isBlocking(chunk.getCell(localX, localY, pZ), localX, localY, pZ);
	}

	public void raycastCell(int pZ, float pStartX, float pStartY, float pEndX, float pEndY,
			DynamicCellularity pIgnore) {

		int startX = MathUtils.floor(pStartX);
		int startY = MathUtils.floor(pStartY);

		if (isBlocking(startX + mOffsetX, startY + mOffsetY, pZ)) {
			mRayCastBlockCellX = startX + mOffsetX;
			mRayCastBlockCellY = startY + mOffsetY;
			mRayCastBlockDistance2 = 0;
			mRayCastType = RAYCAST_TYPE_START;
			mRayCastBlockCellularity = null;
			return;
		}

		int endX = MathUtils.floor(pEndX);
		int endY = MathUtils.floor(pEndY);
		float dx = pEndX - pStartX;
		float dy = pEndY - pStartY;

		mRayCastBlockCellX = endX + mOffsetX;
		mRayCastBlockCellY = endY + mOffsetY;
		mRayCastBlockDistance2 = dx * dx + dy * dy;
		mRayCastType = RAYCAST_TYPE_END;
		mRayCastBlockCellularity = null;

		int vx = pStartX < pEndX ? 1 : -1;
		int vy = pStartY < pEndY ? 1 : -1;

		for (int posX = startX; posX != endX; posX += vx) {
			int nextPosX = posX + vx;
			float x = (posX + nextPosX + 1) * 0.5f;
			float y = pStartY + (x - pStartX) / dx * dy;
			int nextPosY = MathUtils.floor(y);
			if (isBlocking(nextPosX + mOffsetX, nextPosY + mOffsetY, pZ)) {
				float x1 = x - pStartX;
				float y1 = y - pStartY;
				float distance = x1 * x1 + y1 * y1;
				if (distance < mRayCastBlockDistance2) {
					mRayCastBlockCellX = nextPosX + mOffsetX;
					mRayCastBlockCellY = nextPosY + mOffsetY;
					mRayCastPreBlockCellX = posX + mOffsetX;
					mRayCastPreBlockCellY = nextPosY + mOffsetY;
					mRayCastBlockX = x;
					mRayCastBlockY = y;
					mRayCastBlockDistance2 = x1 * x1 + y1 * y1;
					mRayCastType = RAYCAST_TYPE_MIDDLE;
					mRayCastBlockCellularity = null;
				}
				break;
			}
		}

		for (int posY = startY; posY != endY; posY += vy) {
			int nextPosY = posY + vy;
			float y = (posY + nextPosY + 1) * 0.5f;
			float x = pStartX + (y - pStartY) / dy * dx;
			int nextPosX = MathUtils.floor(x);
			if (isBlocking(nextPosX + mOffsetX, nextPosY + mOffsetY, pZ)) {
				float x1 = x - pStartX;
				float y1 = y - pStartY;
				float distance = x1 * x1 + y1 * y1;
				if (distance < mRayCastBlockDistance2) {
					mRayCastBlockCellX = nextPosX + mOffsetX;
					mRayCastBlockCellY = nextPosY + mOffsetY;
					mRayCastPreBlockCellX = nextPosX + mOffsetX;
					mRayCastPreBlockCellY = posY + mOffsetY;
					mRayCastBlockX = x;
					mRayCastBlockY = y;
					mRayCastBlockDistance2 = distance;
					mRayCastType = RAYCAST_TYPE_MIDDLE;
					mRayCastBlockCellularity = null;
				}
				break;
			}
		}

		int left = (Math.min(startX, endX) + mOffsetX >> CHUNK_REGION_SIZE_LOG) - 1;
		int bottom = (Math.min(startY, endY) + mOffsetY >> CHUNK_REGION_SIZE_LOG) - 1;
		int right = (Math.max(startX, endX) + mOffsetX >> CHUNK_REGION_SIZE_LOG) + 1;
		int top = (Math.max(startY, endY) + mOffsetY >> CHUNK_REGION_SIZE_LOG) + 1;
		int chunkLeft = left >> CHUNK_REGIONS_SIZE_LOG;
		int chunkBottom = bottom >> CHUNK_REGIONS_SIZE_LOG;
		int chunkRight = right >> CHUNK_REGIONS_SIZE_LOG;
		int chunkTop = top >> CHUNK_REGIONS_SIZE_LOG;
		for (int i = chunkBottom; i <= chunkTop; ++i)
			for (int j = chunkLeft; j <= chunkRight; ++j) {
				ChunkCellularity chunk = getChunk(j, i);
				if (chunk != null) {
					LinkedHashTable[] dynamics = chunk.getDynamics();
					int regionLeft =
							Math.max(CHUNK_REGIONS_LEFT, left - (j << CHUNK_REGIONS_SIZE_LOG));
					int regionBottom =
							Math.max(CHUNK_REGIONS_BOTTOM, bottom - (i << CHUNK_REGIONS_SIZE_LOG));
					int regionRight =
							Math.min(CHUNK_REGIONS_RIGHT, right - (j << CHUNK_REGIONS_SIZE_LOG));
					int regionTop =
							Math.min(CHUNK_REGIONS_TOP, top - (i << CHUNK_REGIONS_SIZE_LOG));
					for (int p = regionBottom; p <= regionTop; ++p)
						for (int q = regionLeft; q <= regionRight; ++q) {
							int m = q | (p << CHUNK_REGIONS_SIZE_LOG);
							for (List.Node k = dynamics[m].begin(); k != dynamics[m].end(); k =
									dynamics[m].next(k)) {
								DynamicCellularity dynamic =
										(DynamicCellularity) dynamics[m].val(k);
								if (dynamic != pIgnore) {
									MetaBody body = dynamic.getBody();
									float x1 = pStartX - body.getPositionX() - body.getOffsetX();
									float y1 = pStartY - body.getPositionY() - body.getOffsetY();
									float x2 = pEndX - body.getPositionX() - body.getOffsetX();
									float y2 = pEndY - body.getPositionY() - body.getOffsetY();
									float angle = -body.getAngle();
									float c = (float) Math.cos(angle);
									float s = (float) Math.sin(angle);
									float dynamicStartX = c * x1 - s * y1 + MAX_DROP_HSIZE;
									float dynamicStartY = s * x1 + c * y1 + MAX_DROP_HSIZE;
									float dynamicEndX = c * x2 - s * y2 + MAX_DROP_HSIZE;
									float dynamicEndY = s * x2 + c * y2 + MAX_DROP_HSIZE;
									raycastCellDynamic(pZ, dynamicStartX, dynamicStartY,
											dynamicEndX, dynamicEndY, dynamic);
								}
							}
						}
				}
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
		private ChunkCellularity mChunk;
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

		public ChunkCellularity getChunk() {
			return mChunk;
		}

		public void setChunk(ChunkCellularity pChunk) {
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

		public ChunkCellularity chunk() {
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
				ChunkCellularity[][] chunks = mChunk.getChunks();
				for (int i = 0; i < ArrayConstants.MOVE_AROUND_SIZE; ++i) {
					int vx = ArrayConstants.MOVE_AROUND_X[i];
					int vy = ArrayConstants.MOVE_AROUND_Y[i];
					ChunkCellularity chunk = mTissularity.getChunk(mX + vx, mY + vy);
					if (chunk != null) {
						chunks[vy + 1][vx + 1] = chunk;
						chunk.getChunks()[1 - vy][1 - vx] = mChunk;
					}
				}
				mChunk.attach(mTissularity, mX, mY);
				mState = STATE_ATTACHED;
			} else if (mState == STATE_DETACHING) {
				mChunk.detach();
				ChunkCellularity[][] chunks = mChunk.getChunks();
				for (int i = 0; i < ArrayConstants.MOVE_AROUND_SIZE; ++i) {
					int vx = ArrayConstants.MOVE_AROUND_X[i];
					int vy = ArrayConstants.MOVE_AROUND_Y[i];
					ChunkCellularity chunk = chunks[vy + 1][vx + 1];
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
