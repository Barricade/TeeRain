package com.gaskarov.teerain.core.cellularity;

import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_SIZE;
import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_X;
import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_Y;
import static com.gaskarov.teerain.resource.Settings.CELL_UPDATE_Z;
import static com.gaskarov.teerain.resource.Settings.DYNAMIC_BODY;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_BOTTOM;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_LEFT;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_MAX_DEPTH;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_MIN_DEPTH;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_RIGHT;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_SIZE_LOG;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_SIZE_MASK;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_SQUARE_LOG;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_TOP;
import static com.gaskarov.teerain.resource.Settings.MAX_DROP_VOLUME;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gaskarov.teerain.core.PhysicsWall;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.resource.Cells;
import com.gaskarov.teerain.resource.CellsAction;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.LinkedIntTable;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class DynamicCellularity extends Cellularity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private final Vector2 mTmpVec2 = new Vector2();

	private final int[] mCells = new int[MAX_DROP_VOLUME];
	private final LinkedIntTable mCellsKeys = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);
	private final int[] mCellsModified = new int[MAX_DROP_VOLUME];
	private final LinkedIntTable mCellsModifiedKeys = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);
	private final LinkedIntTable mCellsUpdate = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);

	private final CellData[] mCellsData = new CellData[MAX_DROP_VOLUME];
	private final PhysicsWall[] mCellsPhysics = new PhysicsWall[MAX_DROP_VOLUME];

	private final LinkedIntTable mCellsRefresh = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);
	private final LinkedIntTable mCellsTick = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);
	private final LinkedIntTable mCellsTissulared = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);

	private final LinkedIntTable mCellsDestroy = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);

	private final LinkedIntTable mDropUpdate = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);
	private final LinkedIntTable mDropUsed = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);
	private final LinkedIntTable mDropCurrent = LinkedIntTable
			.obtain(MAX_DROP_VOLUME);

	private ChunkCellularity mChunk;
	private int mX;
	private int mY;

	private MetaBody mBody;

	private long mRandomSeed;
	private long mRandomNumber;

	// ===========================================================
	// Constructors
	// ===========================================================

	private DynamicCellularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean isChunk() {
		return false;
	}

	@Override
	public ChunkCellularity getChunk() {
		return mChunk;
	}

	@Override
	public int getChunkX() {
		return mChunk.getChunkX();
	}

	@Override
	public int getChunkY() {
		return mChunk.getChunkY();
	}

	@Override
	public Tissularity getTissularity() {
		return mChunk != null ? mChunk.getTissularity() : null;
	}

	@Override
	public Vector2 localToChunk(float pX, float pY) {
		float c = mBody.getCos();
		float s = mBody.getSin();
		float offset = Settings.MAX_DROP_HSIZE;
		float x = mBody.getPositionX() + (pX - offset) * c - (pY - offset) * s;
		float y = mBody.getPositionY() + (pX - offset) * s + (pY - offset) * c;
		return mTmpVec2.set(x, y);
	}

	@Override
	public int localToGlobalX(int pX) {
		return mChunk.localToGlobalX(pX);
	}

	@Override
	public int localToGlobalY(int pY) {
		return mChunk.localToGlobalY(pY);
	}

	@Override
	public MetaBody getBody() {
		return mBody;
	}

	@Override
	public MetaBody getBody(int pX, int pY) {
		return mBody;
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
		if (MAX_DROP_LEFT <= pX && pX <= MAX_DROP_RIGHT
				&& MAX_DROP_BOTTOM <= pY && pY <= MAX_DROP_TOP) {
			if (MAX_DROP_MIN_DEPTH <= pZ && pZ <= MAX_DROP_MAX_DEPTH)
				return getCellHelper(pX, pY, pZ);
			else
				return getDefaultCell(pX, pY, pZ);
		}
		return Cells.CELL_TYPE_VACUUM;
	}

	@Override
	public void setCell(int pX, int pY, int pZ, int pCell, CellData pCellData) {
		if (MAX_DROP_LEFT <= pX && pX <= MAX_DROP_RIGHT
				&& MAX_DROP_BOTTOM <= pY && pY <= MAX_DROP_TOP
				&& MAX_DROP_MIN_DEPTH <= pZ && pZ <= MAX_DROP_MAX_DEPTH)
			setCellHelper(pX, pY, pZ, pCell, pCellData);
		else if (pCellData != null)
			pCellData.recycle();
	}

	@Override
	public CellData getCellData(int pX, int pY, int pZ) {
		if (MAX_DROP_LEFT <= pX && pX <= MAX_DROP_RIGHT
				&& MAX_DROP_BOTTOM <= pY && pY <= MAX_DROP_TOP
				&& MAX_DROP_MIN_DEPTH <= pZ && pZ <= MAX_DROP_MAX_DEPTH)
			return getCellDataHelper(pX, pY, pZ);
		return null;
	}

	@Override
	public boolean isShellable(int pX, int pY, int pZ) {
		if (MAX_DROP_LEFT <= pX && pX <= MAX_DROP_RIGHT
				&& MAX_DROP_BOTTOM <= pY && pY <= MAX_DROP_TOP) {
			if (MAX_DROP_MIN_DEPTH <= pZ && pZ <= MAX_DROP_MAX_DEPTH)
				return CellsAction.isShellable(this, getCellHelper(pX, pY, pZ),
						pX, pY, pZ);
			else
				return false;
		}
		return false;
	}

	@Override
	public boolean isSolid(int pX, int pY, int pZ) {
		if (MAX_DROP_LEFT <= pX && pX <= MAX_DROP_RIGHT
				&& MAX_DROP_BOTTOM <= pY && pY <= MAX_DROP_TOP) {
			if (MAX_DROP_MIN_DEPTH <= pZ && pZ <= MAX_DROP_MAX_DEPTH)
				return CellsAction.isSolid(this, getCellHelper(pX, pY, pZ), pX,
						pY, pZ);
			else
				return false;
		}
		return false;
	}

	@Override
	public int getDefaultCell(int pX, int pY, int pZ) {
		return Cells.CELL_TYPE_VACUUM;
	}

	@Override
	public int getDropDefaultCell(int pX, int pY, int pZ) {
		return Cells.CELL_TYPE_VACUUM;
	}

	@Override
	public long random() {
		return NoiseMath.combine(mRandomSeed, mRandomNumber++);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static DynamicCellularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (DynamicCellularity.class) {
				return sPool.size() == 0 ? new DynamicCellularity()
						: (DynamicCellularity) sPool.pop();
			}
		return new DynamicCellularity();
	}

	private static void recyclePure(DynamicCellularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (DynamicCellularity.class) {
				sPool.push(pObj);
			}
	}

	public static DynamicCellularity obtain(long pRandomSeed, float pPositionX,
			float pPositionY, float pAngle) {
		DynamicCellularity obj = obtainPure();
		obj.mBody = MetaBody.obtain(DYNAMIC_BODY, pPositionX, pPositionY,
				pAngle);
		obj.mRandomSeed = pRandomSeed;
		obj.mRandomNumber = 0;
		return obj;
	}

	public static void recycle(DynamicCellularity pObj) {
		while (pObj.mCellsKeys.size() > 0) {
			int val = pObj.mCellsKeys.pop();
			pObj.mCells[val] = 0;
			if (pObj.mCellsData[val] != null) {
				pObj.mCellsData[val].recycle();
				pObj.mCellsData[val] = null;
			}
			if (pObj.mCellsPhysics[val] != null) {
				pObj.mCellsPhysics[val].recycle();
				pObj.mCellsPhysics[val] = null;
			}
		}

		while (pObj.mCellsModifiedKeys.size() > 0) {
			int val = pObj.mCellsModifiedKeys.pop();
			pObj.mCellsModified[val] = 0;
		}

		pObj.mCellsUpdate.clear();

		pObj.mCellsRefresh.clear();
		pObj.mCellsTick.clear();
		pObj.mCellsTissulared.clear();

		pObj.mCellsDestroy.clear();

		pObj.mDropUpdate.clear();
		pObj.mDropUsed.clear();
		pObj.mDropCurrent.clear();

		MetaBody.recycle(pObj.mBody);
		pObj.mBody = null;

		recyclePure(pObj);
	}

	private boolean isTissulared() {
		return mChunk != null && mChunk.isTissulared();
	}

	private static int getVal(int pX, int pY, int pZ) {
		return pX | (pY << MAX_DROP_SIZE_LOG) | (pZ << MAX_DROP_SQUARE_LOG);
	}

	private static int getValX(int pVal) {
		return pVal & MAX_DROP_SIZE_MASK;
	}

	private static int getValY(int pVal) {
		return (pVal >>> MAX_DROP_SIZE_LOG) & MAX_DROP_SIZE_MASK;
	}

	private static int getValZ(int pVal) {
		return pVal >>> MAX_DROP_SQUARE_LOG;
	}

	public int size() {
		return mCellsKeys.size();
	}

	public int getCellKeyX(int pId) {
		return getValX(mCellsKeys.key(pId));
	}

	public int getCellKeyY(int pId) {
		return getValY(mCellsKeys.key(pId));
	}

	public int getCellKeyZ(int pId) {
		return getValZ(mCellsKeys.key(pId));
	}

	public int cellUpdate() {
		return mCellsUpdate.size();
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
		if (pCell != defaultCell) {
			mCells[val] = pCell;
			mCellsKeys.set(val);
		} else {
			mCellsKeys.remove(val);
		}
		CellsAction.attach(this, pCell, pX, pY, pZ);
		if (tissulared)
			CellsAction.tissularedAttach(this, pCell, pX, pY, pZ);
	}

	public CellData getCellDataHelper(int pX, int pY, int pZ) {
		return mCellsData[getVal(pX, pY, pZ)];
	}

	public void invalidateCell(int pX, int pY, int pZ) {
		if (MAX_DROP_LEFT <= pX && pX <= MAX_DROP_RIGHT
				&& MAX_DROP_BOTTOM <= pY && pY <= MAX_DROP_TOP
				&& MAX_DROP_MIN_DEPTH <= pZ && pZ <= MAX_DROP_MAX_DEPTH)
			invalidateCellHelper(pX, pY, pZ);
	}

	private void invalidateCellHelper(int pX, int pY, int pZ) {
		final int val = getVal(pX, pY, pZ);
		mCellsUpdate.set(val);
		mCellsRefresh.set(val);
		mDropUpdate.set(val);
		mCellsModifiedKeys.remove(val);
	}

	public int precalcCells(int pN) {
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

	public void updateCells() {
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
				if (MAX_DROP_LEFT <= x && x <= MAX_DROP_RIGHT
						&& MAX_DROP_BOTTOM <= y && y <= MAX_DROP_TOP
						&& MAX_DROP_MIN_DEPTH <= z && z <= MAX_DROP_MAX_DEPTH) {
					final int val = getVal(x, y, z);
					mCellsUpdate.set(val);
					mCellsRefresh.set(val);
					mDropUpdate.set(val);
				}
			}
		}
	}

	public void refreshCells() {
		while (mCellsRefresh.size() > 0) {
			final int val = mCellsRefresh.pop();
			final int x = getValX(val);
			final int y = getValY(val);
			final int z = getValZ(val);
			CellsAction.refresh(this, getCellHelper(x, y, z), x, y, z);
		}
	}

	public void tickCells() {
		for (int i = mCellsTick.size() - 1; i >= 0; --i) {
			final int val = mCellsTick.key(i);
			final int x = getValX(val);
			final int y = getValY(val);
			final int z = getValZ(val);
			CellsAction.tick(this, getCellHelper(x, y, z), x, y, z);
		}
	}

	public void drop() {
		while (mDropUpdate.size() > 0) {
			int val0 = mDropUpdate.pop();
			int x0 = getValX(val0);
			int y0 = getValY(val0);
			int z0 = getValZ(val0);
			if (dropDfs(x0, y0, z0) && mDropCurrent.size() != mCellsKeys.size()) {
				DynamicCellularity cellularity = DynamicCellularity.obtain(
						random(), mBody.getPositionX(), mBody.getPositionY(),
						mBody.getAngle());
				float massCenterX = mBody.getMassCenterX();
				float massCenterY = mBody.getMassCenterY();
				float angularVelocity = mBody.getAngularVelocity();
				float velocityX = mBody.getVelocityX();
				float velocityY = mBody.getVelocityY();
				mChunk.pushCellularity(cellularity);
				while (mDropCurrent.size() > 0) {
					int val = mDropCurrent.pop();
					int x = getValX(val);
					int y = getValY(val);
					int z = getValZ(val);
					CellData cellData = getCellDataHelper(x, y, z);
					cellularity.setCell(x, y, z, getCellHelper(x, y, z),
							cellData != null ? cellData.cpy() : null);
					setCell(x, y, z, getDropDefaultCell(x, y, z), null);
				}
				MetaBody cellularityBody = cellularity.getBody();
				cellularityBody.setAngularVelocity(angularVelocity);
				float vX = (massCenterY - cellularityBody.getMassCenterY())
						* angularVelocity;
				float vY = (cellularityBody.getMassCenterX() - massCenterX)
						* angularVelocity;
				cellularityBody.setVelocity(velocityX + vX, velocityY + vY);

				float vX2 = (massCenterY - mBody.getMassCenterY())
						* angularVelocity;
				float vY2 = (mBody.getMassCenterX() - massCenterX)
						* angularVelocity;
				mBody.setVelocity(velocityX + vX2, velocityY + vY2);
			} else
				mDropCurrent.clear();
		}
	}

	private boolean dropDfs(int pX, int pY, int pZ) {
		final int x0 = pX;
		final int y0 = pY;
		final int val0 = getVal(x0, y0, pZ);
		final int cell0 = getCellHelper(x0, y0, pZ);
		if (mDropCurrent.get(val0))
			return true;
		if (mDropUsed.get(val0))
			return false;
		mDropUsed.set(val0);
		mDropCurrent.set(val0);
		mDropUpdate.remove(val0);
		if (!CellsAction.isDroppable(this, cell0, x0, y0, pZ))
			return false;
		for (int i = 0; i < ArrayConstants.DIRECTIONS_3D_SIZE; ++i) {
			int vX = ArrayConstants.DIRECTIONS_3D_X[i];
			int vY = ArrayConstants.DIRECTIONS_3D_Y[i];
			int vZ = ArrayConstants.DIRECTIONS_3D_Z[i];
			int posX = pX + vX;
			int posY = pY + vY;
			int z = pZ + vZ;
			if (!(MAX_DROP_LEFT <= posX && posX <= MAX_DROP_RIGHT
					&& MAX_DROP_BOTTOM <= posY && posY <= MAX_DROP_TOP))
				continue;
			if (MAX_DROP_MIN_DEPTH <= z && z <= MAX_DROP_MAX_DEPTH) {
				int x = posX;
				int y = posY;
				int cell = getCellHelper(x, y, z);
				boolean flag = true;
				if (CellsAction.isConnected(this, cell0, x0, y0, pZ, cell, vX,
						vY, vZ)
						&& CellsAction.isConnected(this, cell, x, y, z, cell0,
								-vX, -vY, -vZ))
					flag = dropDfs(posX, posY, z);
				if (!flag)
					return false;
			}
		}
		return true;
	}

	public void postDrop() {
		mDropUsed.clear();
	}

	public void refreshOffset() {
		if (mChunk != null) {
			short groupIndexOffset;
			float offsetX;
			float offsetY;
			World world;
			groupIndexOffset = mChunk.getGroupIndexOffset();
			offsetX = mChunk.offsetX();
			offsetY = mChunk.offsetY();
			world = mChunk.getWorld();
			if (mBody.getGroupIndexOffset() != groupIndexOffset
					|| mBody.getOffsetX() != offsetX
					|| mBody.getOffsetY() != offsetY
					|| mBody.getWorld() != world) {
				mBody.setGroupIndexOffset(groupIndexOffset);
				mBody.setOffset(offsetX, offsetY);
				mBody.setWorld(world);
			}
		} else {
			if (mBody.getWorld() != null) {
				mBody.setWorld(null);
				mBody.setOffset(0, 0);
				mBody.setGroupIndexOffset((short) 0);
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

	public void replaceChunk(ChunkCellularity pChunk, int pX, int pY) {
		mChunk = pChunk;
		mX = pX;
		mY = pY;
		mBody.setOffsetMove(mChunk.offsetX(), mChunk.offsetY());
	}

	public void attachChunk(ChunkCellularity pChunk, int pX, int pY) {
		mChunk = pChunk;
		mX = pX;
		mY = pY;
		refreshOffset();
		if (mChunk.isTissulared())
			tissularedAttachHelper();
	}

	public void detachChunk() {
		if (mChunk != null) {
			if (mChunk.isTissulared())
				tissularedDetachHelper();
			mChunk = null;
			refreshOffset();
		}
	}

	public void render(FloatArray[] pRenderBuffers, int pOffsetX, int pOffsetY,
			float pCameraX, float pCameraY, float pCellSize, int pChunkX,
			int pChunkY, float pWidth, float pHeight) {
		float c = mBody.getCos();
		float s = mBody.getSin();
		float posX = (pChunkX << Settings.CHUNK_SIZE_LOG) - pOffsetX
				+ mBody.getPositionX() - pCameraX;
		float posY = (pChunkY << Settings.CHUNK_SIZE_LOG) - pOffsetY
				+ mBody.getPositionY() - pCameraY;
		posX *= pCellSize;
		posY *= pCellSize;
		for (int i = mCellsKeys.size() - 1; i >= 0; --i) {
			int val = mCellsKeys.key(i);
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			float depthFactor = Settings.DEPTH_FACTORS[z];
			CellsAction.render(this, getCellHelper(x, y, z), x, y, z, posX
					/ depthFactor, posY / depthFactor, x
					- Settings.MAX_DROP_HSIZE, y - Settings.MAX_DROP_HSIZE,
					pCellSize / depthFactor, c, s, pRenderBuffers);
		}
	}

	public void destroyCells() {
		while (mCellsDestroy.size() > 0) {
			int val = mCellsDestroy.pop();
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			CellsAction.destroyCell(this, getCellHelper(x, y, z), x, y, z);
		}
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
