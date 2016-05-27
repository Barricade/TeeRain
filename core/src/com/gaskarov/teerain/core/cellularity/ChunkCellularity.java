package com.gaskarov.teerain.core.cellularity;

import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_SIZE;
import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_X;
import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_Y;
import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_Z;
import static com.gaskarov.teerain.resource.Settings.CHUNK_BOTTOM;
import static com.gaskarov.teerain.resource.Settings.CHUNK_DEPTH;
import static com.gaskarov.teerain.resource.Settings.CHUNK_DEPTH_LOG;
import static com.gaskarov.teerain.resource.Settings.CHUNK_DEPTH_SKY;
import static com.gaskarov.teerain.resource.Settings.CHUNK_DEPTH_VACUUM;
import static com.gaskarov.teerain.resource.Settings.CHUNK_LEFT;
import static com.gaskarov.teerain.resource.Settings.CHUNK_MAX_DEPTH;
import static com.gaskarov.teerain.resource.Settings.CHUNK_MIN_DEPTH;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGIONS_BOTTOM;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGIONS_LEFT;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGIONS_RIGHT;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGIONS_SIZE_LOG;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGIONS_SIZE_MASK;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGIONS_SQUARE;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGIONS_TOP;
import static com.gaskarov.teerain.resource.Settings.CHUNK_REGION_SIZE_LOG;
import static com.gaskarov.teerain.resource.Settings.CHUNK_RIGHT;
import static com.gaskarov.teerain.resource.Settings.CHUNK_SIZE_LOG;
import static com.gaskarov.teerain.resource.Settings.CHUNK_SIZE_MASK;
import static com.gaskarov.teerain.resource.Settings.CHUNK_SQUARE_LOG;
import static com.gaskarov.teerain.resource.Settings.CHUNK_TOP;
import static com.gaskarov.teerain.resource.Settings.CHUNK_VOLUME;
import static com.gaskarov.teerain.resource.Settings.CHUNK_VOLUME_COLORS;
import static com.gaskarov.teerain.resource.Settings.COLORS;
import static com.gaskarov.teerain.resource.Settings.COLORS_LOG;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_COUNT;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_HSIZE;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_SIZE;
import static com.gaskarov.teerain.resource.Settings.STATIC_BODY;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gaskarov.teerain.core.PhysicsWall;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.resource.Cells;
import com.gaskarov.teerain.resource.CellsAction;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.LinkedIntTable;
import com.gaskarov.util.container.List;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class ChunkCellularity extends Cellularity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private final Vector2 mTmpVec2 = new Vector2();

	private final boolean[] mTmpAISolid = new boolean[ArrayConstants.MOVE_AROUND_SIZE];
	private final int[] mTmpAIAround = new int[ArrayConstants.MOVE_AROUND_SIZE];

	private final int[] mTmpLight = new int[COLORS];
	private final int[] mTmpLightCorners = new int[4 << COLORS_LOG];

	private final short[] mAI = new short[CHUNK_VOLUME];
	private final short[] mAIModified = new short[CHUNK_VOLUME];
	private final LinkedIntTable mAIModifiedKeys = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mAIUpdate = LinkedIntTable
			.obtain(CHUNK_VOLUME);

	private final short[] mLight = new short[CHUNK_VOLUME_COLORS];
	private final short[] mLightModified = new short[CHUNK_VOLUME_COLORS];
	private final LinkedIntTable mLightModifiedKeys = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mLightUpdate = LinkedIntTable
			.obtain(CHUNK_VOLUME);

	private final int[] mCells = new int[CHUNK_VOLUME];
	private final int[] mCellsModified = new int[CHUNK_VOLUME];
	private final LinkedIntTable mCellsModifiedKeys = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mCellsUpdate = LinkedIntTable
			.obtain(CHUNK_VOLUME);

	private final CellData[] mCellsData = new CellData[CHUNK_VOLUME];
	private final PhysicsWall[] mCellsPhysics = new PhysicsWall[CHUNK_VOLUME];

	private final LinkedIntTable mCellsRefresh = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mCellsTick = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mCellsTissulared = LinkedIntTable
			.obtain(CHUNK_VOLUME);

	private final LinkedIntTable mCellsDestroy = LinkedIntTable
			.obtain(CHUNK_VOLUME);

	private final LinkedIntTable mDropUpdate = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mDropUsed = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mDropCurrent = LinkedIntTable
			.obtain(CHUNK_VOLUME);
	private int mDropMinX;
	private int mDropMinY;
	private int mDropMaxX;
	private int mDropMaxY;
	private int mDropCount;

	private final ChunkCellularity[][] mChunks = new ChunkCellularity[3][3];

	private Tissularity mTissularity;
	private int mX;
	private int mY;

	private final MetaBody[] mBodies = new MetaBody[CHUNK_REGIONS_SQUARE];
	private final LinkedHashTable[] mDynamics = new LinkedHashTable[CHUNK_REGIONS_SQUARE];

	private int mSkyR;
	private int mSkyG;
	private int mSkyB;

	private long mRandomSeed;
	private long mRandomNumber;

	// ===========================================================
	// Constructors
	// ===========================================================

	private ChunkCellularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public ChunkCellularity[][] getChunks() {
		return mChunks;
	}

	public LinkedHashTable[] getDynamics() {
		return mDynamics;
	}

	public int getSkyR() {
		return mSkyR;
	}

	public int getSkyG() {
		return mSkyG;
	}

	public int getSkyB() {
		return mSkyB;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean isChunk() {
		return true;
	}

	@Override
	public ChunkCellularity getChunk() {
		return this;
	}

	@Override
	public int getChunkX() {
		return mX;
	}

	@Override
	public int getChunkY() {
		return mY;
	}

	@Override
	public Tissularity getTissularity() {
		return mTissularity;
	}

	@Override
	public Vector2 localToChunk(float pX, float pY) {
		return mTmpVec2.set(pX, pY);
	}

	@Override
	public int localToGlobalX(int pX) {
		return pX + (mX << CHUNK_SIZE_LOG);
	}

	@Override
	public int localToGlobalY(int pY) {
		return pY + (mY << CHUNK_SIZE_LOG);
	}

	@Override
	public MetaBody getBody() {
		return mBodies[0];
	}

	@Override
	public MetaBody getBody(int pX, int pY) {
		int x = pX >> CHUNK_REGION_SIZE_LOG;
		int y = pY >> CHUNK_REGION_SIZE_LOG;
		return mBodies[x | (y << CHUNK_REGIONS_SIZE_LOG)];
	}

	@Override
	public void tickEnable(int pX, int pY, int pZ) {
		mCellsTick.set(getVal(pX, pY, pZ));
	}

	@Override
	public void tickDisable(int pX, int pY, int pZ) {
		mCellsTick.remove(getVal(pX, pY, pZ));
	}

	@Override
	public void tissularedEnable(int pX, int pY, int pZ) {
		mCellsTissulared.set(getVal(pX, pY, pZ));
	}

	@Override
	public void tissularedDisable(int pX, int pY, int pZ) {
		mCellsTissulared.remove(getVal(pX, pY, pZ));
	}

	@Override
	public void destroyCell(int pX, int pY, int pZ) {
		mCellsDestroy.set(getVal(pX, pY, pZ));
	}

	@Override
	public PhysicsWall getPhysicsWall(int pX, int pY, int pZ) {
		return mCellsPhysics[getVal(pX, pY, pZ)];
	}

	@Override
	public void setPhysicsWall(int pX, int pY, int pZ, PhysicsWall pPhysicsWall) {
		int val = getVal(pX, pY, pZ);
		if (mCellsPhysics[val] != null)
			mCellsPhysics[val].recycle();
		mCellsPhysics[val] = pPhysicsWall;
	}

	@Override
	public int getCell(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (chunk != null) {
			if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH)
				return chunk.getCellHelper(pX & CHUNK_SIZE_MASK, pY
						& CHUNK_SIZE_MASK, pZ);
			else
				return chunk.getDefaultCell(pX & CHUNK_SIZE_MASK, pY
						& CHUNK_SIZE_MASK, pZ);
		}
		return Cells.CELL_TYPE_VOID;
	}

	@Override
	public void setCell(int pX, int pY, int pZ, int pCell, CellData pCellData) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (chunk != null && CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH)
			chunk.setCellHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ,
					pCell, pCellData);
		else if (pCellData != null)
			pCellData.recycle();
	}

	@Override
	public CellData getCellData(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (chunk != null && CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH)
			return chunk.getCellDataHelper(pX & CHUNK_SIZE_MASK, pY
					& CHUNK_SIZE_MASK, pZ);
		return null;
	}

	@Override
	public boolean isShellable(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (chunk != null) {
			if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH) {
				int localX = pX & CHUNK_SIZE_MASK;
				int localY = pY & CHUNK_SIZE_MASK;
				return CellsAction.isShellable(chunk,
						chunk.getCellHelper(localX, localY, pZ), localX,
						localY, pZ);
			} else
				return false;
		}
		return true;
	}

	@Override
	public boolean isSolid(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (chunk != null) {
			if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH) {
				int localX = pX & CHUNK_SIZE_MASK;
				int localY = pY & CHUNK_SIZE_MASK;
				return CellsAction.isSolid(chunk,
						chunk.getCellHelper(localX, localY, pZ), localX,
						localY, pZ);
			} else
				return false;
		}
		return true;
	}

	@Override
	public int getDefaultCell(int pX, int pY, int pZ) {
		return Cells.CELL_TYPE_VOID;
	}

	@Override
	public int getDropDefaultCell(int pX, int pY, int pZ) {
		return Cells.CELL_TYPE_AIR;
	}

	@Override
	public long random() {
		return NoiseMath.combine(mRandomSeed, mRandomNumber++);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static ChunkCellularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (ChunkCellularity.class) {
				return sPool.size() == 0 ? new ChunkCellularity()
						: (ChunkCellularity) sPool.pop();
			}
		return new ChunkCellularity();
	}

	private static void recyclePure(ChunkCellularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (ChunkCellularity.class) {
				sPool.push(pObj);
			}
	}

	public static ChunkCellularity obtain(long pRandomSeed) {
		ChunkCellularity obj = obtainPure();

		obj.mChunks[1][1] = obj;
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i)
			obj.mBodies[i] = MetaBody.obtain(STATIC_BODY, 0, 0, 0);
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i)
			obj.mDynamics[i] = LinkedHashTable.obtain();
		obj.mSkyR = 0;
		obj.mSkyG = 0;
		obj.mSkyB = 0;
		obj.mRandomSeed = pRandomSeed;
		obj.mRandomNumber = 0;

		return obj;
	}

	public static void recycle(ChunkCellularity pObj) {

		for (int i = 0; i < CHUNK_VOLUME; ++i)
			pObj.mAI[i] = 0;
		for (int i = 0; i < CHUNK_VOLUME; ++i)
			pObj.mAIModified[i] = 0;
		pObj.mAIModifiedKeys.clear();
		pObj.mAIUpdate.clear();

		for (int i = 0; i < CHUNK_VOLUME_COLORS; ++i)
			pObj.mLight[i] = 0;
		for (int i = 0; i < CHUNK_VOLUME_COLORS; ++i)
			pObj.mLightModified[i] = 0;
		pObj.mLightModifiedKeys.clear();
		pObj.mLightUpdate.clear();

		for (int i = 0; i < CHUNK_VOLUME; ++i)
			pObj.mCells[i] = 0;
		for (int i = 0; i < CHUNK_VOLUME; ++i)
			if (pObj.mCellsData[i] != null) {
				pObj.mCellsData[i].recycle();
				pObj.mCellsData[i] = null;
			}
		for (int i = 0; i < CHUNK_VOLUME; ++i)
			pObj.mCellsModified[i] = 0;
		pObj.mCellsModifiedKeys.clear();
		pObj.mCellsUpdate.clear();

		for (int i = 0; i < CHUNK_VOLUME; ++i)
			if (pObj.mCellsPhysics[i] != null) {
				pObj.mCellsPhysics[i].recycle();
				pObj.mCellsPhysics[i] = null;
			}

		pObj.mCellsRefresh.clear();
		pObj.mCellsTick.clear();
		pObj.mCellsTissulared.clear();

		pObj.mCellsDestroy.clear();

		pObj.mDropUpdate.clear();
		pObj.mDropUsed.clear();
		pObj.mDropCurrent.clear();

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				pObj.mChunks[i][j] = null;

		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			MetaBody.recycle(pObj.mBodies[i]);
			pObj.mBodies[i] = null;
		}
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = pObj.mDynamics[i];
			pObj.mDynamics[i] = null;
			while (dynamics.size() > 0)
				DynamicCellularity.recycle((DynamicCellularity) dynamics
						.remove(dynamics.front()));
			LinkedHashTable.recycle(dynamics);
		}

		recyclePure(pObj);
	}

	private ChunkCellularity getChunk(int pX, int pY) {
		final int x = (pX >> CHUNK_SIZE_LOG) + 1;
		final int y = (pY >> CHUNK_SIZE_LOG) + 1;
		return 0 <= x && x < 3 && 0 <= y && y < 3 ? mChunks[y][x] : null;
	}

	public boolean isTissulared() {
		return mTissularity != null;
	}

	private static int getVal(int pX, int pY, int pZ) {
		return pX | (pY << CHUNK_SIZE_LOG) | (pZ << CHUNK_SQUARE_LOG);
	}

	private static int getValX(int pVal) {
		return pVal & CHUNK_SIZE_MASK;
	}

	private static int getValY(int pVal) {
		return (pVal >>> CHUNK_SIZE_LOG) & CHUNK_SIZE_MASK;
	}

	private static int getValZ(int pVal) {
		return pVal >>> CHUNK_SQUARE_LOG;
	}

	private int skyR(int pX, int pY) {
		return mSkyR;
	}

	private int skyG(int pX, int pY) {
		return mSkyG;
	}

	private int skyB(int pX, int pY) {
		return mSkyB;
	}

	public float offsetX() {
		return mBodies[0].getOffsetX();
	}

	public float offsetY() {
		return mBodies[0].getOffsetY();
	}

	public World getWorld() {
		return mBodies[0].getWorld();
	}

	public short getGroupIndexOffset() {
		return mTissularity == null ? 0 : (short) (mTissularity
				.getGroupIndexPackOffset() << CHUNK_DEPTH_LOG);
	}

	private int getCellHelper(int pX, int pY, int pZ) {
		final int cell = mCells[getVal(pX, pY, pZ)];
		return cell != 0 ? cell : getDefaultCell(pX, pY, pZ);
	}

	private void setCellHelper(int pX, int pY, int pZ, int pCell,
			CellData pCellData) {
		setCellPure(pX, pY, pZ, pCell, pCellData);
		for (int i = 0; i < CELL_UPDATE_SIZE; ++i)
			invalidateCell(pX + CELL_UPDATE_X[i], pY + CELL_UPDATE_Y[i], pZ
					+ CELL_UPDATE_Z[i]);
	}

	private void setCellPure(int pX, int pY, int pZ, int pCell,
			CellData pCellData) {
		final int val = getVal(pX, pY, pZ);
		final int defaultCell = getDefaultCell(pX, pY, pZ);
		final boolean tissulared = isTissulared();
		int oldCell = mCells[val];
		if (oldCell == 0)
			oldCell = defaultCell;
		if (tissulared)
			CellsAction.tissularedDetach(this, oldCell, pX, pY, pZ);
		CellsAction.detach(this, oldCell, pX, pY, pZ);

		if (mCellsData[val] != null)
			mCellsData[val].recycle();
		mCellsData[val] = pCellData;

		mCells[val] = 0;
		if (pCell != defaultCell)
			mCells[val] = pCell;
		CellsAction.attach(this, pCell, pX, pY, pZ);
		if (tissulared)
			CellsAction.tissularedAttach(this, pCell, pX, pY, pZ);
	}

	public CellData getCellDataHelper(int pX, int pY, int pZ) {
		return mCellsData[getVal(pX, pY, pZ)];
	}

	public void invalidateCell(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateCellHelper(pX & CHUNK_SIZE_MASK, pY
					& CHUNK_SIZE_MASK, pZ);
	}

	private void invalidateCellHelper(int pX, int pY, int pZ) {
		final int val = getVal(pX, pY, pZ);
		mCellsUpdate.set(val);
		mCellsRefresh.set(val);
		mDropUpdate.set(val);
		mLightUpdate.set(val);
		mAIUpdate.set(val);
		mCellsModifiedKeys.remove(val);
	}

	public int getAI(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (chunk != null)
			return chunk.getAIHelper(pX & CHUNK_SIZE_MASK,
					pY & CHUNK_SIZE_MASK, pZ);
		return 0;
	}

	private int getAIHelper(int pX, int pY, int pZ) {
		return mAI[getVal(pX, pY, pZ)];
	}

	public void setAI(int pX, int pY, int pZ, int pAI) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.setAIHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ,
					pAI);
	}

	private void setAIHelper(int pX, int pY, int pZ, int pAI) {
		final int val = getVal(pX, pY, pZ);
		mAI[val] = (short) pAI;
		for (int j = 0; j < ArrayConstants.SQUARE_3_SIZE; ++j)
			invalidateAI(pX + ArrayConstants.SQUARE_3_X[j], pY
					+ ArrayConstants.SQUARE_3_Y[j], pZ);
	}

	private void invalidateAI(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateAIHelper(pX & CHUNK_SIZE_MASK,
					pY & CHUNK_SIZE_MASK, pZ);
	}

	private void invalidateAIHelper(int pX, int pY, int pZ) {
		final int val = getVal(pX, pY, pZ);
		mAIUpdate.set(val);
		mAIModifiedKeys.remove(val);
	}

	public int[] getLight(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (pZ != CHUNK_DEPTH_VACUUM && chunk != null)
			return chunk.getLightHelper(pX & CHUNK_SIZE_MASK, pY
					& CHUNK_SIZE_MASK, pZ);
		mTmpLight[0] = 0;
		mTmpLight[1] = 0;
		mTmpLight[2] = 0;
		return mTmpLight;
	}

	private int[] getLightHelper(int pX, int pY, int pZ) {
		if (pZ != CHUNK_DEPTH_SKY) {
			final int colorId = getVal(pX, pY, pZ) << COLORS_LOG;
			mTmpLight[0] = mLight[colorId];
			mTmpLight[1] = mLight[colorId + 1];
			mTmpLight[2] = mLight[colorId + 2];
		} else {
			mTmpLight[0] = skyR(pX, pY);
			mTmpLight[1] = skyG(pX, pY);
			mTmpLight[2] = skyB(pX, pY);
		}
		return mTmpLight;
	}

	public void setLight(int pX, int pY, int pZ, int pR, int pG, int pB) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.setLightHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK,
					pZ, pR, pG, pB);
	}

	private void setLightHelper(int pX, int pY, int pZ, int pR, int pG, int pB) {
		final int colorId = getVal(pX, pY, pZ) << COLORS_LOG;
		mLight[colorId] = (short) pR;
		mLight[colorId + 1] = (short) pG;
		mLight[colorId + 2] = (short) pB;
		for (int j = 0; j < CELL_UPDATE_SIZE; ++j)
			invalidateLight(pX + CELL_UPDATE_X[j], pY + CELL_UPDATE_Y[j], pZ
					+ CELL_UPDATE_Z[j]);
	}

	private void invalidateLight(int pX, int pY, int pZ) {
		final ChunkCellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateLightHelper(pX & CHUNK_SIZE_MASK, pY
					& CHUNK_SIZE_MASK, pZ);
	}

	private void invalidateLightHelper(int pX, int pY, int pZ) {
		final int val = getVal(pX, pY, pZ);
		mLightUpdate.set(val);
		mLightModifiedKeys.remove(val);
	}

	public int[] getLightCorners(int pX, int pY, int pZ) {
		int[] light = getLight(pX - 1, pY - 1, pZ);
		int r00 = light[0];
		int g00 = light[1];
		int b00 = light[2];
		light = getLight(pX, pY - 1, pZ);
		int r10 = light[0];
		int g10 = light[1];
		int b10 = light[2];
		light = getLight(pX + 1, pY - 1, pZ);
		int r20 = light[0];
		int g20 = light[1];
		int b20 = light[2];
		light = getLight(pX - 1, pY, pZ);
		int r01 = light[0];
		int g01 = light[1];
		int b01 = light[2];
		light = getLight(pX, pY, pZ);
		int r11 = light[0];
		int g11 = light[1];
		int b11 = light[2];
		light = getLight(pX + 1, pY, pZ);
		int r21 = light[0];
		int g21 = light[1];
		int b21 = light[2];
		light = getLight(pX - 1, pY + 1, pZ);
		int r02 = light[0];
		int g02 = light[1];
		int b02 = light[2];
		light = getLight(pX, pY + 1, pZ);
		int r12 = light[0];
		int g12 = light[1];
		int b12 = light[2];
		light = getLight(pX + 1, pY + 1, pZ);
		int r22 = light[0];
		int g22 = light[1];
		int b22 = light[2];
		mTmpLightCorners[0] = Math.min(Math.min(r00, r10), Math.min(r01, r11));
		mTmpLightCorners[1] = Math.min(Math.min(g00, g10), Math.min(g01, g11));
		mTmpLightCorners[2] = Math.min(Math.min(b00, b10), Math.min(b01, b11));
		mTmpLightCorners[4] = Math.min(Math.min(r10, r20), Math.min(r11, r21));
		mTmpLightCorners[5] = Math.min(Math.min(g10, g20), Math.min(g11, g21));
		mTmpLightCorners[6] = Math.min(Math.min(b10, b20), Math.min(b11, b21));
		mTmpLightCorners[8] = Math.min(Math.min(r01, r11), Math.min(r02, r12));
		mTmpLightCorners[9] = Math.min(Math.min(g01, g11), Math.min(g02, g12));
		mTmpLightCorners[10] = Math.min(Math.min(b01, b11), Math.min(b02, b12));
		mTmpLightCorners[12] = Math.min(Math.min(r11, r21), Math.min(r12, r22));
		mTmpLightCorners[13] = Math.min(Math.min(g11, g21), Math.min(g12, g22));
		mTmpLightCorners[14] = Math.min(Math.min(b11, b21), Math.min(b12, b22));
		return mTmpLightCorners;
	}

	public int precalcCells(int pN) {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j))
				pN = ((DynamicCellularity) dynamics.val(j)).precalcCells(pN);
		}

		int num = Math.min(mCellsUpdate.size(), pN);
		for (int i = num; i > 0; --i) {
			final int val0 = mCellsUpdate.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			final int cell = CellsAction.update(this,
					getCellHelper(x0, y0, z0), x0, y0, z0);
			if (cell != 0) {
				mCellsModified[val0] = cell;
				mCellsModifiedKeys.set(val0);
			}
		}
		return pN - num;
	}

	public int precalcAI(int pN) {
		int num = Math.min(mAIUpdate.size(), pN);
		for (int i = num; i > 0; --i) {
			final int val0 = mAIUpdate.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			int aiField0 = 0;
			int aiVertical0 = 0;
			int aiHorizontal0 = 0;
			for (int j = 0; j < ArrayConstants.MOVE_AROUND_SIZE; ++j) {
				final int vx = ArrayConstants.MOVE_AROUND_X[j];
				final int vy = ArrayConstants.MOVE_AROUND_Y[j];
				final int x = x0 + vx;
				final int y = y0 + vy;
				final ChunkCellularity chunk = getChunk(x, y);
				if (chunk != null) {
					final int localX = x & CHUNK_SIZE_MASK;
					final int localY = y & CHUNK_SIZE_MASK;
					final int val = getVal(localX, localY, z0);
					final int cell = chunk.getCellHelper(localX, localY, z0);
					final int aiResistance = (vx == 0 || vy == 0) ? CellsAction
							.aiResistance(chunk, cell, localX, localY, z0)
							: CellsAction.aiDiagonalResistance(chunk, cell,
									localX, localY, z0);
					mTmpAISolid[j] = CellsAction.isSolid(chunk, cell, localX,
							localY, z0);
					int ai = chunk.mAI[val];
					int aiField = Math.max((ai & 255) - aiResistance, 0);
					int aiVertical = (vy == -1 ? Math.max(0,
							((ai >>> 8) & 15) - 1) : 0);
					int aiHorizontal = (vy == 1 ? Math.max(0, (ai >>> 12) - 1)
							: 0);
					mTmpAIAround[j] = aiField | (aiVertical << 8)
							| (aiHorizontal << 12);
				} else {
					mTmpAISolid[j] = false;
					mTmpAIAround[j] = 0;
				}
			}
			if (!CellsAction.isSolid(this, getCellHelper(x0, y0, z0), x0, y0,
					z0)) {
				if (mTmpAISolid[6]) {
					aiVertical0 = 6;
					aiHorizontal0 = 15;
				} else {
					for (int j = 0; j < ArrayConstants.MOVE_AROUND_SIZE; ++j) {
						if ((j & 1) == 0 || !mTmpAISolid[j - 1 & 7]
								&& !mTmpAISolid[j + 1 & 7]) {
							int ai = mTmpAIAround[j];
							int aiVertical = (ai >>> 8) & 15;
							int aiHorizontal = ai >>> 12;
							aiVertical0 = Math.max(aiVertical0, aiVertical);
							aiHorizontal0 = Math.max(aiHorizontal0,
									aiHorizontal);
						}
					}
					if (aiVertical0 != 0)
						aiHorizontal0 = 15;
				}
			}
			for (int j = 0; j < ArrayConstants.MOVE_AROUND_SIZE; ++j) {
				if ((j & 1) == 0 || !mTmpAISolid[j - 1 & 7]
						&& !mTmpAISolid[j + 1 & 7]) {
					final int vy = ArrayConstants.MOVE_AROUND_Y[j];
					int ai = mTmpAIAround[j];
					int aiField = ai & 255;
					if (vy == 0) {
						if (aiVertical0 == 0 || aiHorizontal0 == 0)
							aiField = 0;
					} else if (vy == -1) {
						if (aiHorizontal0 == 0)
							aiField = 0;
					} else {
						if (aiVertical0 == 0)
							aiField = 0;
					}
					aiField0 = Math.max(aiField0, aiField);
				}
			}
			final int ai0 = aiField0 | (aiVertical0 << 8)
					| (aiHorizontal0 << 12);
			if (mAI[val0] != ai0) {
				mAIModified[val0] = (short) ai0;
				mAIModifiedKeys.set(val0);
			}
		}
		return pN - num;
	}

	public int precalcLight(int pN) {
		int num = Math.min(pN, mLightUpdate.size());
		for (int i = num; i > 0; --i) {
			final int val0 = mLightUpdate.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			final int cell0 = getCellHelper(x0, y0, z0);
			int r0 = CellsAction.lightSourceR(this, cell0, z0, y0, z0);
			int g0 = CellsAction.lightSourceG(this, cell0, z0, y0, z0);
			int b0 = CellsAction.lightSourceB(this, cell0, z0, y0, z0);
			int k = 0;
			for (int j = 0; j < ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_SIZE; ++j) {
				final int x = x0
						- ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_X[j];
				final int y = y0
						- ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_Y[j];
				final int vz = ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_Z[j];
				final int z = z0 - vz;
				final ChunkCellularity chunk = getChunk(x, y);
				if (z != CHUNK_DEPTH_VACUUM && chunk != null) {
					final int localX = x & CHUNK_SIZE_MASK;
					final int localY = y & CHUNK_SIZE_MASK;
					int resistanceR, resistanceG, resistanceB;
					if (vz == -1) {
						resistanceR = CellsAction.lightResistance(this, cell0,
								x0, y0, z0, k++);
						resistanceG = CellsAction.lightResistance(this, cell0,
								x0, y0, z0, k++);
						resistanceB = CellsAction.lightResistance(this, cell0,
								x0, y0, z0, k++);
					} else {
						final int cell = chunk.getCellHelper(localX, localY, z);
						resistanceR = CellsAction.lightResistance(chunk, cell,
								localX, localY, z, k++);
						resistanceG = CellsAction.lightResistance(chunk, cell,
								localX, localY, z, k++);
						resistanceB = CellsAction.lightResistance(chunk, cell,
								localX, localY, z, k++);
					}
					if (z != CHUNK_DEPTH_SKY) {
						final int val = getVal(localX, localY, z);
						final int colorId = val << COLORS_LOG;
						final int r = chunk.mLight[colorId];
						final int g = chunk.mLight[colorId + 1];
						final int b = chunk.mLight[colorId + 2];
						r0 = Math.max(r0, r - resistanceR);
						g0 = Math.max(g0, g - resistanceG);
						b0 = Math.max(b0, b - resistanceB);
					} else {
						r0 = Math.max(r0, chunk.skyR(localX, localY)
								- resistanceR);
						g0 = Math.max(g0, chunk.skyG(localX, localY)
								- resistanceG);
						b0 = Math.max(b0, chunk.skyB(localX, localY)
								- resistanceB);
					}
				} else
					k += 3;
			}
			final int colorRId = val0 << COLORS_LOG;
			final int colorGId = colorRId + 1;
			final int colorBId = colorGId + 1;
			if (mLight[colorRId] != r0 || mLight[colorGId] != g0
					|| mLight[colorBId] != b0) {
				mLightModified[colorRId] = (short) r0;
				mLightModified[colorGId] = (short) g0;
				mLightModified[colorBId] = (short) b0;
				mLightModifiedKeys.set(val0);
			}
		}
		return pN - num;
	}

	public void updateCells() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j))
				((DynamicCellularity) dynamics.val(j)).updateCells();
		}

		while (mCellsModifiedKeys.size() > 0) {
			final int val0 = mCellsModifiedKeys.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			setCellPure(x0, y0, z0, mCellsModified[val0], null);
			for (int i = 0; i < CELL_UPDATE_SIZE; ++i) {
				final int x = x0 + CELL_UPDATE_X[i];
				final int y = y0 + CELL_UPDATE_Y[i];
				final int z = z0 + CELL_UPDATE_Z[i];
				final ChunkCellularity chunk = getChunk(x, y);
				if (CHUNK_MIN_DEPTH <= z && z <= CHUNK_MAX_DEPTH
						&& chunk != null) {
					final int val = getVal(x & CHUNK_SIZE_MASK, y
							& CHUNK_SIZE_MASK, z);
					chunk.mCellsUpdate.set(val);
					chunk.mCellsRefresh.set(val);
					chunk.mDropUpdate.set(val);
					chunk.mLightUpdate.set(val);
					chunk.mAIUpdate.set(val);
				}
			}
		}
	}

	public void updateAI() {
		while (mAIModifiedKeys.size() > 0) {
			final int val0 = mAIModifiedKeys.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			mAI[val0] = mAIModified[val0];
			for (int j = 0; j < ArrayConstants.SQUARE_3_SIZE; ++j) {
				final int x = x0 + ArrayConstants.SQUARE_3_X[j];
				final int y = y0 + ArrayConstants.SQUARE_3_Y[j];
				final int z = z0;
				final ChunkCellularity chunk = getChunk(x, y);
				if (chunk != null) {
					final int val = getVal(x & CHUNK_SIZE_MASK, y
							& CHUNK_SIZE_MASK, z);
					chunk.mAIUpdate.set(val);
				}
			}
		}
	}

	public void updateLight() {
		while (mLightModifiedKeys.size() > 0) {
			final int val0 = mLightModifiedKeys.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			final int colorRId = val0 << COLORS_LOG;
			final int colorGId = colorRId + 1;
			final int colorBId = colorGId + 1;
			mLight[colorRId] = mLightModified[colorRId];
			mLight[colorGId] = mLightModified[colorGId];
			mLight[colorBId] = mLightModified[colorBId];
			for (int j = 0; j < CELL_UPDATE_SIZE; ++j) {
				final int x = x0 + CELL_UPDATE_X[j];
				final int y = y0 + CELL_UPDATE_Y[j];
				final int z = z0 + CELL_UPDATE_Z[j];
				final ChunkCellularity chunk = getChunk(x, y);
				if (CHUNK_MIN_DEPTH <= z && z <= CHUNK_MAX_DEPTH
						&& chunk != null) {
					final int val = getVal(x & CHUNK_SIZE_MASK, y
							& CHUNK_SIZE_MASK, z);
					chunk.mLightUpdate.set(val);
				}
			}
		}
	}

	public void refreshCells() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j))
				((DynamicCellularity) dynamics.val(j)).refreshCells();
		}

		while (mCellsRefresh.size() > 0) {
			final int val = mCellsRefresh.pop();
			final int x = getValX(val);
			final int y = getValY(val);
			final int z = getValZ(val);
			CellsAction.refresh(this, getCellHelper(x, y, z), x, y, z);
		}
	}

	public void tickCells() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j))
				((DynamicCellularity) dynamics.val(j)).tickCells();
		}

		for (int i = mCellsTick.size() - 1; i >= 0; --i) {
			final int val = mCellsTick.key(i);
			final int x = getValX(val);
			final int y = getValY(val);
			final int z = getValZ(val);
			CellsAction.tick(this, getCellHelper(x, y, z), x, y, z);
		}
	}

	public void drop() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j))
				((DynamicCellularity) dynamics.val(j)).drop();
		}

		while (mDropUpdate.size() > 0) {
			int val0 = mDropUpdate.pop();
			int x0 = getValX(val0);
			int y0 = getValY(val0);
			int z0 = getValZ(val0);
			mDropMinX = mDropMaxX = x0;
			mDropMinY = mDropMaxY = y0;
			mDropCount = 0;
			if (dropDfs(x0, y0, z0)) {
				DynamicCellularity cellularity = DynamicCellularity.obtain(
						random(), mDropMinX + MAX_DROP_HSIZE, mDropMinY
								+ MAX_DROP_HSIZE, 0);
				pushCellularity(cellularity);
				for (int i = 0; i < 3; ++i)
					for (int j = 0; j < 3; ++j) {
						ChunkCellularity curCellularity = mChunks[i][j];
						if (curCellularity != null) {
							while (curCellularity.mDropCurrent.size() > 0) {
								int val = curCellularity.mDropCurrent.pop();
								int x = getValX(val);
								int y = getValY(val);
								int z = getValZ(val);
								CellData cellData = curCellularity
										.getCellDataHelper(x, y, z);
								cellularity.setCell(
										x + (j - 1 << CHUNK_SIZE_LOG)
												- mDropMinX, y
												+ (i - 1 << CHUNK_SIZE_LOG)
												- mDropMinY, z, curCellularity
												.getCellHelper(x, y, z),
										cellData != null ? cellData.cpy()
												: null);
								curCellularity.setCell(x, y, z, curCellularity
										.getDropDefaultCell(x, y, z), null);
							}
						}
					}
			} else {
				for (int i = 0; i < 3; ++i)
					for (int j = 0; j < 3; ++j) {
						ChunkCellularity curCellularity = mChunks[i][j];
						if (curCellularity != null)
							curCellularity.mDropCurrent.clear();
					}
			}
		}
	}

	private boolean dropDfs(int pX, int pY, int pZ) {
		final ChunkCellularity chunk0 = getChunk(pX, pY);
		final int x0 = pX & CHUNK_SIZE_MASK;
		final int y0 = pY & CHUNK_SIZE_MASK;
		final int val0 = getVal(x0, y0, pZ);
		final int cell0 = chunk0.getCellHelper(x0, y0, pZ);
		if (chunk0.mDropCurrent.get(val0))
			return true;
		if (chunk0.mDropUsed.get(val0))
			return false;
		mDropMinX = Math.min(mDropMinX, pX);
		mDropMinY = Math.min(mDropMinY, pY);
		mDropMaxX = Math.max(mDropMaxX, pX);
		mDropMaxY = Math.max(mDropMaxY, pY);
		++mDropCount;
		if (mDropMaxX - mDropMinX + 1 > MAX_DROP_SIZE
				|| mDropMaxY - mDropMinY + 1 > MAX_DROP_SIZE
				|| mDropCount > MAX_DROP_COUNT)
			return false;
		chunk0.mDropUsed.set(val0);
		chunk0.mDropCurrent.set(val0);
		chunk0.mDropUpdate.remove(val0);
		if (!CellsAction.isDroppable(chunk0, cell0, x0, y0, pZ))
			return false;
		for (int i = 0; i < ArrayConstants.DIRECTIONS_3D_SIZE; ++i) {
			int vX = ArrayConstants.DIRECTIONS_3D_X[i];
			int vY = ArrayConstants.DIRECTIONS_3D_Y[i];
			int vZ = ArrayConstants.DIRECTIONS_3D_Z[i];
			int posX = pX + vX;
			int posY = pY + vY;
			int z = pZ + vZ;
			ChunkCellularity chunk = getChunk(posX, posY);
			if (chunk == null)
				return false;
			if (CHUNK_MIN_DEPTH <= z && z <= CHUNK_MAX_DEPTH) {
				int x = posX & CHUNK_SIZE_MASK;
				int y = posY & CHUNK_SIZE_MASK;
				int cell = chunk.getCellHelper(x, y, z);
				boolean flag = true;
				if (CellsAction.isConnected(chunk0, cell0, x0, y0, pZ, cell,
						vX, vY, vZ)
						&& CellsAction.isConnected(chunk, cell, x, y, z, cell0,
								-vX, -vY, -vZ))
					flag = dropDfs(posX, posY, z);
				if (!flag)
					return false;
			}
		}
		return true;
	}

	public void postDrop() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j))
				((DynamicCellularity) dynamics.val(j)).postDrop();
		}

		mDropUsed.clear();
	}

	private void invalidateBorder() {
		final int left = CHUNK_LEFT + 1;
		final int right = CHUNK_RIGHT - 1;
		final int bottom = CHUNK_BOTTOM + 1;
		final int top = CHUNK_TOP - 1;
		boolean leftTop = false, rightTop = false, leftBottom = false, rightBottom = false;
		ChunkCellularity chunk;
		chunk = mChunks[1][2];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_BOTTOM; i <= CHUNK_TOP; ++i) {
					chunk.invalidateCellHelper(CHUNK_LEFT, i, z);
				}
				for (int i = bottom; i <= top; ++i) {
					invalidateCellHelper(CHUNK_RIGHT, i, z);
				}
			}
			rightBottom = true;
			rightTop = true;
		}
		chunk = mChunks[2][2];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateCellHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
			}
			rightTop = true;
		}
		chunk = mChunks[2][1];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_LEFT; i <= CHUNK_RIGHT; ++i) {
					chunk.invalidateCellHelper(i, CHUNK_BOTTOM, z);
				}
				for (int i = left; i <= right; ++i) {
					invalidateCellHelper(i, CHUNK_TOP, z);
				}
			}
			leftTop = true;
			rightTop = true;
		}
		chunk = mChunks[2][0];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateCellHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
			}
			leftTop = true;
		}
		chunk = mChunks[1][0];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_BOTTOM; i <= CHUNK_TOP; ++i) {
					chunk.invalidateCellHelper(CHUNK_RIGHT, i, z);
				}
				for (int i = bottom; i <= top; ++i) {
					invalidateCellHelper(CHUNK_LEFT, i, z);
				}
			}
			leftTop = true;
			leftBottom = true;
		}
		chunk = mChunks[0][0];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateCellHelper(CHUNK_RIGHT, CHUNK_TOP, z);
			}
			leftBottom = true;
		}
		chunk = mChunks[0][1];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_LEFT; i <= CHUNK_RIGHT; ++i) {
					chunk.invalidateCellHelper(i, CHUNK_TOP, z);
				}
				for (int i = left; i <= right; ++i) {
					invalidateCellHelper(i, CHUNK_BOTTOM, z);
				}
			}
			leftBottom = true;
			rightBottom = true;
		}
		chunk = mChunks[0][2];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateCellHelper(CHUNK_LEFT, CHUNK_TOP, z);
			}
			rightBottom = true;
		}

		if (leftBottom)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateCellHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
			}
		if (rightBottom)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateCellHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
			}
		if (leftTop)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateCellHelper(CHUNK_LEFT, CHUNK_TOP, z);
			}
		if (rightTop)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateCellHelper(CHUNK_RIGHT, CHUNK_TOP, z);
			}
	}

	public void cleanCellularities() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end();) {
				DynamicCellularity dynamic = (DynamicCellularity) dynamics
						.val(j);
				j = dynamics.next(j);
				if (dynamic.size() == 0) {
					removeCellularity(dynamic);
					DynamicCellularity.recycle(dynamic);
				}
			}
		}
	}

	public void refreshBodies() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j)) {
				DynamicCellularity dynamic = (DynamicCellularity) dynamics
						.val(j);
				dynamic.getBody().refresh();
			}
		}
	}

	public void refreshCellularitiesChunks() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end();) {
				DynamicCellularity dynamic = (DynamicCellularity) dynamics
						.val(j);
				MetaBody body = dynamic.getBody();
				int bodyX = MathUtils.floor(body.getPositionX());
				int bodyY = MathUtils.floor(body.getPositionY());
				int x = bodyX >> Settings.CHUNK_SIZE_LOG;
				int y = bodyY >> Settings.CHUNK_SIZE_LOG;
				int regionX = (bodyX & Settings.CHUNK_SIZE_MASK) >> CHUNK_REGION_SIZE_LOG;
				int regionY = (bodyY & Settings.CHUNK_SIZE_MASK) >> CHUNK_REGION_SIZE_LOG;
				j = dynamics.next(j);
				if (x != 0 || y != 0) {
					ChunkCellularity nextChunk = mChunks[1 + MathUtils.sign(y)][1 + MathUtils
							.sign(x)];
					if (nextChunk != null) {
						moveCellularity(dynamic, nextChunk);
						if (nextChunk.freeze(regionX, regionY))
							body.freeze();
						else
							body.unfreeze();
					}
				} else {
					if ((regionX | (regionY << CHUNK_REGIONS_SIZE_LOG)) != i)
						moveCellularity(dynamic, this);
					if (freeze(regionX, regionY))
						body.freeze();
					else
						body.unfreeze();
				}
			}
		}
	}

	private boolean freeze(int pRegionX, int pRegionY) {
		if (pRegionX == CHUNK_REGIONS_LEFT && mChunks[1][0] == null)
			return true;
		if (pRegionX == CHUNK_REGIONS_RIGHT && mChunks[1][2] == null)
			return true;
		if (pRegionY == CHUNK_REGIONS_BOTTOM && mChunks[0][1] == null)
			return true;
		if (pRegionY == CHUNK_REGIONS_TOP && mChunks[2][1] == null)
			return true;
		if (pRegionX == CHUNK_REGIONS_LEFT && pRegionY == CHUNK_REGIONS_BOTTOM
				&& mChunks[0][0] == null)
			return true;
		if (pRegionX == CHUNK_REGIONS_RIGHT && pRegionY == CHUNK_REGIONS_BOTTOM
				&& mChunks[0][2] == null)
			return true;
		if (pRegionX == CHUNK_REGIONS_LEFT && pRegionY == CHUNK_REGIONS_TOP
				&& mChunks[2][0] == null)
			return true;
		if (pRegionX == CHUNK_REGIONS_RIGHT && pRegionY == CHUNK_REGIONS_TOP
				&& mChunks[2][2] == null)
			return true;
		return false;
	}

	public int cellUpdate() {
		int n = mCellsUpdate.size();
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i)
			for (List.Node j = mDynamics[i].begin(); j != mDynamics[i].end(); j = mDynamics[i]
					.next(j))
				n += ((DynamicCellularity) mDynamics[i].val(j)).cellUpdate();
		return n;
	}

	public int aiUpdate() {
		return mAIUpdate.size();
	}

	public int lightUpdate() {
		return mLightUpdate.size();
	}

	public void refreshOffset() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			MetaBody body = mBodies[i];
			if (mTissularity != null) {
				short groupIndexOffset;
				float offsetX;
				float offsetY;
				World world;
				groupIndexOffset = getGroupIndexOffset();
				offsetX = (mX << CHUNK_SIZE_LOG) - mTissularity.getOffsetX();
				offsetY = (mY << CHUNK_SIZE_LOG) - mTissularity.getOffsetY();
				world = mTissularity.getOrganularity().getWorld();
				if (body.getGroupIndexOffset() != groupIndexOffset
						|| body.getOffsetX() != offsetX
						|| body.getOffsetY() != offsetY
						|| body.getWorld() != world) {
					body.setGroupIndexOffset(groupIndexOffset);
					body.setOffset(offsetX, offsetY);
					body.setWorld(world);
					for (List.Node j = mDynamics[i].begin(); j != mDynamics[i]
							.end(); j = mDynamics[i].next(j))
						((DynamicCellularity) mDynamics[i].val(j))
								.refreshOffset();
				}
			} else {
				if (body.getWorld() != null) {
					body.setWorld(null);
					body.setOffset(0, 0);
					body.setGroupIndexOffset((short) 0);
					for (List.Node j = mDynamics[i].begin(); j != mDynamics[i]
							.end(); j = mDynamics[i].next(j))
						((DynamicCellularity) mDynamics[i].val(j))
								.refreshOffset();
				}
			}
		}
	}

	private void tissularedAttachHelper() {
		for (int i = mCellsTissulared.size() - 1; i >= 0; --i) {
			int val = mCellsTissulared.key(i);
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			CellsAction.tissularedAttach(this, getCellHelper(x, y, z), x, y, z);
		}
	}

	private void tissularedDetachHelper() {
		for (int i = mCellsTissulared.size() - 1; i >= 0; --i) {
			int val = mCellsTissulared.key(i);
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			CellsAction.tissularedDetach(this, getCellHelper(x, y, z), x, y, z);
		}
	}

	public void attach(Tissularity pTissularity, int pX, int pY) {
		for (int j = 0; j < CHUNK_REGIONS_SQUARE; ++j)
			for (List.Node i = mDynamics[j].begin(); i != mDynamics[j].end(); i = mDynamics[j]
					.next(i))
				((DynamicCellularity) mDynamics[j].val(i)).detachChunk();
		mTissularity = pTissularity;
		mX = pX;
		mY = pY;
		refreshOffset();
		invalidateBorder();
		for (int j = 0; j < CHUNK_REGIONS_SQUARE; ++j)
			for (List.Node i = mDynamics[j].begin(); i != mDynamics[j].end(); i = mDynamics[j]
					.next(i))
				((DynamicCellularity) mDynamics[j].val(i))
						.attachChunk(this, j & CHUNK_REGIONS_SIZE_MASK,
								j >>> CHUNK_REGIONS_SIZE_LOG);
		tissularedAttachHelper();
	}

	public void detach() {
		if (mTissularity != null) {
			for (int j = 0; j < CHUNK_REGIONS_SQUARE; ++j)
				for (List.Node i = mDynamics[j].begin(); i != mDynamics[j]
						.end(); i = mDynamics[j].next(i))
					((DynamicCellularity) mDynamics[j].val(i)).detachChunk();
			tissularedDetachHelper();
			mTissularity = null;
			mX = 0;
			mY = 0;
			refreshOffset();
			invalidateBorder();
			for (int j = 0; j < CHUNK_REGIONS_SQUARE; ++j)
				for (List.Node i = mDynamics[j].begin(); i != mDynamics[j]
						.end(); i = mDynamics[j].next(i))
					((DynamicCellularity) mDynamics[j].val(i)).attachChunk(
							this, j & CHUNK_REGIONS_SIZE_MASK,
							j >>> CHUNK_REGIONS_SIZE_LOG);
		}
	}

	private void moveCellularity(DynamicCellularity pDynamic,
			ChunkCellularity pChunk) {
		mDynamics[pDynamic.getX() | (pDynamic.getY() << CHUNK_REGIONS_SIZE_LOG)]
				.remove(pDynamic);
		MetaBody body = pDynamic.getBody();
		int x = MathUtils.clamp(MathUtils.floor(body.getPositionX())
				+ (mX - pChunk.mX << CHUNK_SIZE_LOG) >> CHUNK_REGION_SIZE_LOG,
				CHUNK_REGIONS_LEFT, CHUNK_REGIONS_RIGHT);
		int y = MathUtils.clamp(MathUtils.floor(body.getPositionY())
				+ (mY - pChunk.mY << CHUNK_SIZE_LOG) >> CHUNK_REGION_SIZE_LOG,
				CHUNK_REGIONS_BOTTOM, CHUNK_REGIONS_TOP);
		pChunk.mDynamics[x | (y << CHUNK_REGIONS_SIZE_LOG)].set(pDynamic);
		pDynamic.replaceChunk(pChunk, x, y);
	}

	public void pushCellularity(DynamicCellularity pDynamic) {
		MetaBody body = pDynamic.getBody();
		int x = MathUtils.clamp(
				MathUtils.floor(body.getPositionX()) >> CHUNK_REGION_SIZE_LOG,
				CHUNK_REGIONS_LEFT, CHUNK_REGIONS_RIGHT);
		int y = MathUtils.clamp(
				MathUtils.floor(body.getPositionY()) >> CHUNK_REGION_SIZE_LOG,
				CHUNK_REGIONS_BOTTOM, CHUNK_REGIONS_TOP);
		mDynamics[x | (y << CHUNK_REGIONS_SIZE_LOG)].set(pDynamic);
		pDynamic.attachChunk(this, x, y);
	}

	public void removeCellularity(DynamicCellularity pDynamic) {
		int x = pDynamic.getX();
		int y = pDynamic.getY();
		pDynamic.detachChunk();
		mDynamics[x | (y << CHUNK_REGIONS_SIZE_LOG)].remove(pDynamic);
	}

	public void render(FloatArray[] pRenderBuffers, int pOffsetX, int pOffsetY,
			float pCameraX, float pCameraY, float pCellSize, int pChunkX,
			int pChunkY, float pWidth, float pHeight) {
		for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
			int chunkX = pChunkX << Settings.CHUNK_SIZE_LOG;
			int chunkY = pChunkY << Settings.CHUNK_SIZE_LOG;
			int tmpX = chunkX - pOffsetX;
			int tmpY = chunkY - pOffsetY;
			float depthFactor = Settings.DEPTH_FACTORS[z];
			float depthWidth = pWidth * depthFactor;
			float depthHeight = pHeight * depthFactor;

			int col1 = Math.max(CHUNK_LEFT,
					MathUtils.floor(pCameraX - depthWidth / 2) - tmpX);
			int col2 = Math.min(CHUNK_RIGHT,
					MathUtils.floor(pCameraX + depthWidth / 2) - tmpX);
			int row1 = Math.max(CHUNK_BOTTOM,
					MathUtils.floor(pCameraY - depthHeight / 2) - tmpY);
			int row2 = Math.min(CHUNK_TOP,
					MathUtils.floor(pCameraY + depthHeight / 2) - tmpY);

			float cellSize = pCellSize / depthFactor;

			float offsetX = -pCameraX * cellSize;
			float offsetY = -pCameraY * cellSize;

			for (int y = row1; y <= row2; ++y)
				for (int x = col1; x <= col2; ++x)
					CellsAction.render(this, getCellHelper(x, y, z), x, y, z,
							offsetX, offsetY, x + tmpX, y + tmpY, cellSize,
							1.0f, 0.0f, pRenderBuffers);
		}
	}

	public void destroyCells() {
		for (int i = 0; i < CHUNK_REGIONS_SQUARE; ++i) {
			LinkedHashTable dynamics = mDynamics[i];
			for (List.Node j = dynamics.begin(); j != dynamics.end(); j = dynamics
					.next(j))
				((DynamicCellularity) dynamics.val(j)).destroyCells();
		}

		while (mCellsDestroy.size() > 0) {
			int val = mCellsDestroy.pop();
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			CellsAction.destroyCell(this, getCellHelper(x, y, z), x, y, z);
		}
	}

	public void setSky(int pR, int pG, int pB) {
		if (mSkyR == pR && mSkyG == pG && mSkyB == pB)
			return;
		mSkyR = pR;
		mSkyG = pG;
		mSkyB = pB;
		for (int y = CHUNK_BOTTOM; y <= CHUNK_TOP; ++y)
			for (int x = CHUNK_LEFT; x <= CHUNK_RIGHT; ++x)
				invalidateLightHelper(x, y, Settings.CHUNK_MAX_DEPTH);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
