package com.gaskarov.teerain.core;

import static com.gaskarov.teerain.core.util.Settings.CELL_UPDATE_SIZE;
import static com.gaskarov.teerain.core.util.Settings.CELL_UPDATE_X;
import static com.gaskarov.teerain.core.util.Settings.CELL_UPDATE_Y;
import static com.gaskarov.teerain.core.util.Settings.CELL_UPDATE_Z;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_BOTTOM;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_DEPTH;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_DEPTH_LOG;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_DEPTH_SKY;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_DEPTH_VACUUM;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_HSIZE;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_LEFT;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_MAX_DEPTH;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_MIN_DEPTH;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_RIGHT;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_SIZE;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_SIZE_LOG;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_SIZE_MASK;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_SQUARE_LOG;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_TOP;
import static com.gaskarov.teerain.core.util.Settings.CHUNK_VOLUME;
import static com.gaskarov.teerain.core.util.Settings.COLORS;
import static com.gaskarov.teerain.core.util.Settings.COLORS_LOG;
import static com.gaskarov.teerain.core.util.Settings.LIGHT_CORNERS_SIZE_LOG;
import static com.gaskarov.teerain.core.util.Settings.LIGHT_RESISTANCE;
import static com.gaskarov.teerain.core.util.Settings.LIGHT_RESISTANCE_SIZE_LOG;
import static com.gaskarov.teerain.core.util.Settings.LIGHT_SOURCE;
import static com.gaskarov.teerain.core.util.Settings.LIGHT_SOURCE_SIZE_LOG;
import static com.gaskarov.teerain.core.util.Settings.MAX_DROP_COUNT;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_SIZE;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_X;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_Y;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_AND_SQUARE_CORNERS_Z;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_SIZE;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_X;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_Y;
import static com.gaskarov.util.constants.ArrayConstants.DIRECTIONS_3D_Z;
import static com.gaskarov.util.constants.ArrayConstants.MOVE_AROUND_SIZE;
import static com.gaskarov.util.constants.ArrayConstants.MOVE_AROUND_X;
import static com.gaskarov.util.constants.ArrayConstants.MOVE_AROUND_Y;
import static com.gaskarov.util.constants.ArrayConstants.SQUARE_3_SIZE;
import static com.gaskarov.util.constants.ArrayConstants.SQUARE_3_X;
import static com.gaskarov.util.constants.ArrayConstants.SQUARE_3_Y;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gaskarov.teerain.core.util.GraphicsModule;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.game.cell.AirCell;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.ArrayConstants;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.LinkedIntTable;
import com.gaskarov.util.container.List;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Cellularity {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int[] COLOR_BLACK = new int[COLORS];

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private final Vector2 mTmpVec2 = new Vector2();

	private final boolean[] mTmpAISolid = new boolean[MOVE_AROUND_SIZE];
	private final int[] mTmpAIAround = new int[MOVE_AROUND_SIZE];

	private final int[] mTmpLight = new int[COLORS];
	private final int[] mTmpLightAround = new int[MOVE_AROUND_SIZE << COLORS_LOG];

	private final short[] mAI = new short[CHUNK_VOLUME];
	private final short[] mAIModified = new short[CHUNK_VOLUME];
	private final LinkedIntTable mAIModifiedKeys = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mAIUpdate = LinkedIntTable.obtain(CHUNK_VOLUME);

	private final int[] mLight = new int[CHUNK_VOLUME << COLORS_LOG];
	private final int[] mLightModified = new int[CHUNK_VOLUME << COLORS_LOG];
	private final LinkedIntTable mLightModifiedKeys = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mLightUpdate = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mLightRefresh = LinkedIntTable.obtain(CHUNK_VOLUME);

	private final int[] mLightCorners = new int[CHUNK_VOLUME << LIGHT_CORNERS_SIZE_LOG];

	private final int[] mLightResistance = new int[CHUNK_VOLUME];
	private final int[] mLightSource = new int[CHUNK_VOLUME];

	private final Cell[] mCells = new Cell[CHUNK_VOLUME];
	private final LinkedIntTable mCellsKeys = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final Cell[] mCellsModified = new Cell[CHUNK_VOLUME];
	private final LinkedIntTable mCellsModifiedKeys = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mCellsUpdate = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mCellsRefresh = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mRender = LinkedIntTable.obtain(CHUNK_VOLUME);

	private final LinkedIntTable mCellsTick = LinkedIntTable.obtain(CHUNK_VOLUME);

	private final LinkedIntTable mDropUpdate = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mDropUsed = LinkedIntTable.obtain(CHUNK_VOLUME);
	private final LinkedIntTable mDropCurrent = LinkedIntTable.obtain(CHUNK_VOLUME);
	private int mDropMinX;
	private int mDropMinY;
	private int mDropMaxX;
	private int mDropMaxY;
	private int mDropCount;

	private final Cellularity[][] mChunks = new Cellularity[3][3];

	private MetaBody mMetaBody;

	private Tissularity mTissularity;
	private int mX;
	private int mY;
	private LinkedHashTable mCellularities;
	private Cellularity mChunk;

	private int mGraphicsModuleKey;

	// ===========================================================
	// Constructors
	// ===========================================================

	private Cellularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int[] getLightCorners() {
		return mLightCorners;
	}

	public Cellularity[][] getChunks() {
		return mChunks;
	}

	public MetaBody getBody() {
		return mMetaBody;
	}

	public Tissularity getTissularity() {
		return mTissularity;
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	public LinkedHashTable getCellularities() {
		return mCellularities;
	}

	public Cellularity getChunk() {
		return mChunk;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	private static Cellularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (Cellularity.class) {
				return sPool.size() == 0 ? new Cellularity() : (Cellularity) sPool.pop();
			}
		return new Cellularity();
	}

	private static void recyclePure(Cellularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (Cellularity.class) {
				sPool.push(pObj);
			}
	}

	public static Cellularity obtain(BodyType pBodyType, float pPositionX, float pPositionY,
			float pAngle) {
		Cellularity obj = obtainPure();
		obj.mChunks[1][1] = obj;
		obj.mMetaBody =
				MetaBody.obtain(pBodyType == BodyType.StaticBody ? Settings.STATIC_BODY
						: Settings.DYNAMIC_BODY, pPositionX, pPositionY, pAngle);
		obj.mCellularities = LinkedHashTable.obtain();
		return obj;
	}

	public static void recycle(Cellularity pObj) {
		while (pObj.mCellularities.size() > 0) {
			Cellularity cellularity = (Cellularity) pObj.mCellularities.front();
			pObj.removeCellularity(cellularity);
			Cellularity.recycle(cellularity);
		}

		while (pObj.mCellsKeys.size() > 0) {
			int val = pObj.mCellsKeys.pop();
			pObj.mCells[val].detach(pObj, getValX(val), getValY(val), getValZ(val));
			pObj.mCells[val].recycle();
			pObj.mCells[val] = null;
		}

		LinkedHashTable.recycle(pObj.mCellularities);
		pObj.mCellularities = null;

		pObj.mAIModifiedKeys.clear();
		pObj.mAIUpdate.clear();

		pObj.mLightModifiedKeys.clear();
		pObj.mLightUpdate.clear();
		pObj.mLightRefresh.clear();

		while (pObj.mCellsModifiedKeys.size() > 0) {
			int val = pObj.mCellsModifiedKeys.pop();
			pObj.mCellsModified[val].recycle();
			pObj.mCellsModified[val] = null;
		}
		pObj.mCellsUpdate.clear();
		pObj.mCellsRefresh.clear();
		pObj.mRender.clear();

		pObj.mCellsTick.clear();

		pObj.mDropUpdate.clear();
		pObj.mDropUsed.clear();
		pObj.mDropCurrent.clear();

		pObj.mChunks[1][1] = null;

		MetaBody.recycle(pObj.mMetaBody);
		pObj.mMetaBody = null;

		recyclePure(pObj);
	}

	public boolean isChunk() {
		return mMetaBody.getBodyType() == BodyType.StaticBody;
	}

	public Cell getCell(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		final int localX = pX & CHUNK_SIZE_MASK;
		final int localY = pY & CHUNK_SIZE_MASK;
		if (chunk != null) {
			if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH)
				return chunk.getCellHelper(localX, localY, pZ);
			else
				return chunk.getDefaultCell(localX, localY, pZ);
		}
		return isChunk() ? VoidCell.obtain() : VacuumCell.obtain();
	}

	public void setCell(int pX, int pY, int pZ, Cell pCell) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.setCellHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ, pCell);
		else
			pCell.recycle();
	}

	public void precalc(int pN) {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).precalc(pN);

		for (int i = (mAIUpdate.size() + pN - 1) / pN; i > 0; --i) {
			final int val0 = mAIUpdate.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			int aiField0 = 0;
			int aiVertical0 = 0;
			int aiHorizontal0 = 0;
			for (int j = 0; j < MOVE_AROUND_SIZE; ++j) {
				final int vx = MOVE_AROUND_X[j];
				final int vy = MOVE_AROUND_Y[j];
				final int x = x0 + vx;
				final int y = y0 + vy;
				final Cellularity chunk = getChunk(x, y);
				if (chunk != null) {
					final int localX = x & CHUNK_SIZE_MASK;
					final int localY = y & CHUNK_SIZE_MASK;
					final int val = getVal(localX, localY, z0);
					Cell cell = chunk.getCellHelper(localX, localY, z0);
					final int aiResistance =
							(vx == 0 || vy == 0) ? cell.aiResistance() : cell
									.aiDiagonalResistance();
					mTmpAISolid[j] = cell.isSolid();
					int ai = chunk.mAI[val];
					int aiField = Math.max((ai & 255) - aiResistance, 0);
					int aiVertical = (vy == -1 ? Math.max(0, ((ai >>> 8) & 15) - 1) : 0);
					int aiHorizontal = (vy == 1 ? Math.max(0, (ai >>> 12) - 1) : 0);
					mTmpAIAround[j] = aiField | (aiVertical << 8) | (aiHorizontal << 12);
				} else {
					mTmpAISolid[j] = false;
					mTmpAIAround[j] = 0;
				}
			}
			if (!getCellHelper(x0, y0, z0).isSolid()) {
				if (mTmpAISolid[6]) {
					aiVertical0 = 6;
					aiHorizontal0 = 15;
				} else {
					for (int j = 0; j < MOVE_AROUND_SIZE; ++j) {
						if ((j & 1) == 0 || !mTmpAISolid[j - 1 & 7] && !mTmpAISolid[j + 1 & 7]) {
							int ai = mTmpAIAround[j];
							int aiVertical = (ai >>> 8) & 15;
							int aiHorizontal = ai >>> 12;
							aiVertical0 = Math.max(aiVertical0, aiVertical);
							aiHorizontal0 = Math.max(aiHorizontal0, aiHorizontal);
						}
					}
					if (aiVertical0 != 0)
						aiHorizontal0 = 15;
				}
			}
			for (int j = 0; j < MOVE_AROUND_SIZE; ++j) {
				if ((j & 1) == 0 || !mTmpAISolid[j - 1 & 7] && !mTmpAISolid[j + 1 & 7]) {
					final int vy = MOVE_AROUND_Y[j];
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
			final int ai0 = aiField0 | (aiVertical0 << 8) | (aiHorizontal0 << 12);
			if (mAI[val0] != ai0) {
				mAIModified[val0] = (short) ai0;
				mAIModifiedKeys.set(val0);
			}
		}

		for (int i = (mLightUpdate.size() + pN - 1) / pN; i > 0; --i) {
			final int val0 = mLightUpdate.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			final int sourceId = mLightSource[val0] << LIGHT_SOURCE_SIZE_LOG;
			int r0 = LIGHT_SOURCE[sourceId];
			int g0 = LIGHT_SOURCE[sourceId + 1];
			int b0 = LIGHT_SOURCE[sourceId + 2];
			final int resistanceId0 = mLightResistance[val0] << LIGHT_RESISTANCE_SIZE_LOG;
			int k = 0;
			for (int j = 0; j < DIRECTIONS_3D_AND_SQUARE_CORNERS_SIZE; ++j) {
				final int x = x0 - DIRECTIONS_3D_AND_SQUARE_CORNERS_X[j];
				final int y = y0 - DIRECTIONS_3D_AND_SQUARE_CORNERS_Y[j];
				final int vz = DIRECTIONS_3D_AND_SQUARE_CORNERS_Z[j];
				final int z = z0 - vz;
				final Cellularity chunk = getChunk(x, y);
				if (z != CHUNK_DEPTH_VACUUM && chunk != null) {
					final int localX = x & CHUNK_SIZE_MASK;
					final int localY = y & CHUNK_SIZE_MASK;
					if (z != CHUNK_DEPTH_SKY) {
						final int val = getVal(localX, localY, z);
						final int colorId = val << COLORS_LOG;
						final int r = chunk.mLight[colorId];
						final int g = chunk.mLight[colorId + 1];
						final int b = chunk.mLight[colorId + 2];
						final int resistanceId =
								vz == -1 ? resistanceId0
										: mLightResistance[val] << LIGHT_RESISTANCE_SIZE_LOG;
						r0 = Math.max(r0, r - LIGHT_RESISTANCE[resistanceId | k++]);
						g0 = Math.max(g0, g - LIGHT_RESISTANCE[resistanceId | k++]);
						b0 = Math.max(b0, b - LIGHT_RESISTANCE[resistanceId | k++]);
					} else {
						r0 =
								Math.max(r0, chunk.skyR(localX, localY)
										- LIGHT_RESISTANCE[resistanceId0 | k++]);
						g0 =
								Math.max(g0, chunk.skyG(localX, localY)
										- LIGHT_RESISTANCE[resistanceId0 | k++]);
						b0 =
								Math.max(b0, chunk.skyB(localX, localY)
										- LIGHT_RESISTANCE[resistanceId0 | k++]);
					}
				} else
					k += 3;
			}
			final int colorRId = val0 << COLORS_LOG;
			final int colorGId = colorRId + 1;
			final int colorBId = colorGId + 1;
			if (mLight[colorRId] != r0 || mLight[colorGId] != g0 || mLight[colorBId] != b0) {
				mLightModified[colorRId] = r0;
				mLightModified[colorGId] = g0;
				mLightModified[colorBId] = b0;
				mLightModifiedKeys.set(val0);
			}
		}

		for (int i = (mCellsUpdate.size() + pN - 1) / pN; i > 0; --i) {
			final int val0 = mCellsUpdate.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			final Cell cell = getCellHelper(x0, y0, z0).update(this, x0, y0, z0);
			if (cell != null) {
				mCellsModified[val0] = cell;
				mCellsModifiedKeys.set(val0);
			}
		}
	}

	public void update() {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).update();

		while (mAIModifiedKeys.size() > 0) {
			final int val0 = mAIModifiedKeys.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			mAI[val0] = mAIModified[val0];
			for (int j = 0; j < SQUARE_3_SIZE; ++j) {
				final int x = x0 + SQUARE_3_X[j];
				final int y = y0 + SQUARE_3_Y[j];
				final int z = z0;
				final Cellularity chunk = getChunk(x, y);
				if (chunk != null) {
					final int val = getVal(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z);
					chunk.mAIUpdate.set(val);
					if (Settings.AI_DEBUG_RENDER)
						chunk.mRender.set(val);
				}
			}
		}

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
				final Cellularity chunk = getChunk(x, y);
				if (CHUNK_MIN_DEPTH <= z && z <= CHUNK_MAX_DEPTH && chunk != null) {
					final int val = getVal(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z);
					chunk.mLightUpdate.set(val);
					chunk.mLightRefresh.set(val);
					chunk.mRender.set(val);
				}
			}
		}

		while (mCellsModifiedKeys.size() > 0) {
			final int val0 = mCellsModifiedKeys.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			setCellPure(x0, y0, z0, mCellsModified[val0]);
			mCellsModified[val0] = null;
			for (int i = 0; i < CELL_UPDATE_SIZE; ++i) {
				final int x = x0 + CELL_UPDATE_X[i];
				final int y = y0 + CELL_UPDATE_Y[i];
				final int z = z0 + CELL_UPDATE_Z[i];
				final Cellularity chunk = getChunk(x, y);
				if (CHUNK_MIN_DEPTH <= z && z <= CHUNK_MAX_DEPTH && chunk != null) {
					final int val = getVal(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z);
					chunk.mCellsUpdate.set(val);
					chunk.mCellsRefresh.set(val);
					chunk.mRender.set(val);
					chunk.mLightUpdate.set(val);
					chunk.mAIUpdate.set(val);
				}
			}
			invalidateDropHelper(x0, y0, z0);
			for (int i = 0; i < DIRECTIONS_3D_SIZE; ++i)
				invalidateDrop(x0 + DIRECTIONS_3D_X[i], y0 + DIRECTIONS_3D_Y[i], z0
						+ DIRECTIONS_3D_Z[i]);
		}
	}

	public void refresh() {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).refresh();

		final int[] light = mTmpLightAround;
		while (mLightRefresh.size() > 0) {
			final int val0 = mLightRefresh.pop();
			final int x0 = getValX(val0);
			final int y0 = getValY(val0);
			final int z0 = getValZ(val0);
			for (int i = 0; i < MOVE_AROUND_SIZE; ++i) {
				final int x = x0 + MOVE_AROUND_X[i];
				final int y = y0 + MOVE_AROUND_Y[i];
				final int tmp = i << COLORS_LOG;
				final Cellularity chunk = getChunk(x, y);
				if (chunk != null) {
					final int colorId =
							getVal(x & CHUNK_SIZE_MASK, y & CHUNK_SIZE_MASK, z0) << COLORS_LOG;
					light[tmp] = chunk.mLight[colorId];
					light[tmp + 1] = chunk.mLight[colorId + 1];
					light[tmp + 2] = chunk.mLight[colorId + 2];
				} else {
					light[tmp] = 0;
					light[tmp + 1] = 0;
					light[tmp + 2] = 0;
				}
			}
			final int colorId = val0 << COLORS_LOG;
			final int r = mLight[colorId];
			final int g = mLight[colorId + 1];
			final int b = mLight[colorId + 2];
			final int cornerId = val0 << LIGHT_CORNERS_SIZE_LOG;
			mLightCorners[cornerId] = Math.min(r, Math.min(light[0], Math.min(light[4], light[8])));
			mLightCorners[cornerId + 1] =
					Math.min(g, Math.min(light[1], Math.min(light[5], light[9])));
			mLightCorners[cornerId + 2] =
					Math.min(b, Math.min(light[2], Math.min(light[6], light[10])));
			mLightCorners[cornerId + 4] =
					Math.min(r, Math.min(light[8], Math.min(light[12], light[16])));
			mLightCorners[cornerId + 5] =
					Math.min(g, Math.min(light[9], Math.min(light[13], light[17])));
			mLightCorners[cornerId + 6] =
					Math.min(b, Math.min(light[10], Math.min(light[14], light[18])));
			mLightCorners[cornerId + 8] =
					Math.min(r, Math.min(light[16], Math.min(light[20], light[24])));
			mLightCorners[cornerId + 9] =
					Math.min(g, Math.min(light[17], Math.min(light[21], light[25])));
			mLightCorners[cornerId + 10] =
					Math.min(b, Math.min(light[18], Math.min(light[22], light[26])));
			mLightCorners[cornerId + 12] =
					Math.min(r, Math.min(light[24], Math.min(light[28], light[0])));
			mLightCorners[cornerId + 13] =
					Math.min(g, Math.min(light[25], Math.min(light[29], light[1])));
			mLightCorners[cornerId + 14] =
					Math.min(b, Math.min(light[26], Math.min(light[30], light[2])));
		}

		while (mCellsRefresh.size() > 0) {
			final int val = mCellsRefresh.pop();
			final int x = getValX(val);
			final int y = getValY(val);
			final int z = getValZ(val);
			getCellHelper(x, y, z).refresh(this, x, y, z);
		}
	}

	public void render() {

		Cellularity chunk = isChunk() ? this : mChunk;
		Tissularity tissularity = chunk.mTissularity;

		GraphicsModule graphicsModule = tissularity.getGraphicsModule();

		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i)) {
			Cellularity cellularity = (Cellularity) mCellularities.val(i);
			graphicsModule.commandMoveDynamic(cellularity.mGraphicsModuleKey, cellularity.mMetaBody
					.getWorldPositionX(), cellularity.mMetaBody.getWorldPositionY(),
					cellularity.mMetaBody.getAngle(), cellularity.mMetaBody.getMassCenterX(),
					cellularity.mMetaBody.getMassCenterY());
			cellularity.render();
		}
		float c = 1f;
		float s = 0f;
		if (isChunk()) {
			graphicsModule.commandSetChunk(mX, mY);
		} else {
			graphicsModule.commandSetDynamic(mGraphicsModuleKey);
			float angle = mMetaBody.getAngle();
			c = (float) Math.cos(angle);
			s = (float) Math.sin(angle);
			for (int i = 0; i < mCellsKeys.size(); ++i)
				mRender.set(mCellsKeys.key(i));
		}

		while (mRender.size() > 0) {
			int val = mRender.pop();
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			graphicsModule.commandUpdateUnit(val);
			getCellHelper(x, y, z).render(this, x, y, z, c, s);
		}
	}

	public void tick() {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).tick();
		for (int i = mCellsTick.size() - 1; i >= 0; --i) {
			final int val = mCellsTick.key(i);
			final int x = getValX(val);
			final int y = getValY(val);
			final int z = getValZ(val);
			getCellHelper(x, y, z).tick(this, x, y, z);
		}
	}

	public void drop() {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).drop();
		boolean isStatic = isChunk();
		Cellularity chunk = isStatic ? this : mChunk;
		while (mDropUpdate.size() > 0) {
			int val0 = mDropUpdate.pop();
			int x0 = getValX(val0);
			int y0 = getValY(val0);
			int z0 = getValZ(val0);
			mDropMinX = mDropMaxX = x0;
			mDropMinY = mDropMaxY = y0;
			mDropCount = 0;
			if (dropDfs(x0, y0, z0) && (isStatic || mDropCurrent.size() != mCellsKeys.size())) {
				int offset = isChunk() ? CHUNK_HSIZE : 0;
				mTmpVec2.set(mDropMinX + offset, mDropMinY + offset);
				mTmpVec2.rotateRad(mMetaBody.getAngle());
				mTmpVec2.add(mMetaBody.getPositionX(), mMetaBody.getPositionY());
				Cellularity cellularity =
						Cellularity.obtain(BodyType.DynamicBody, mTmpVec2.x, mTmpVec2.y, mMetaBody
								.getAngle());
				float massCenterX = mMetaBody.getMassCenterX();
				float massCenterY = mMetaBody.getMassCenterY();
				float angularVelocity = mMetaBody.getAngularVelocity();
				float velocityX = mMetaBody.getVelocityX();
				float velocityY = mMetaBody.getVelocityY();
				chunk.pushCellularity(cellularity);
				for (int i = 0; i < 3; ++i)
					for (int j = 0; j < 3; ++j) {
						Cellularity curCellularity = mChunks[i][j];
						if (curCellularity != null) {
							while (curCellularity.mDropCurrent.size() > 0) {
								int val = curCellularity.mDropCurrent.pop();
								int x = getValX(val);
								int y = getValY(val);
								int z = getValZ(val);
								cellularity.setCell(x + (j - 1 << CHUNK_SIZE_LOG) - mDropMinX, y
										+ (i - 1 << CHUNK_SIZE_LOG) - mDropMinY, z, curCellularity
										.getCell(x, y, z).cpy());
								curCellularity.setCell(x, y, z, curCellularity.getDropDefaultCell(
										x, y, z));
							}
						}
					}
				MetaBody cellularityBody = cellularity.getBody();
				cellularityBody.setAngularVelocity(angularVelocity);
				float vX = (massCenterY - cellularityBody.getMassCenterY()) * angularVelocity;
				float vY = (cellularityBody.getMassCenterX() - massCenterX) * angularVelocity;
				cellularityBody.setVelocity(velocityX + vX, velocityY + vY);

				float vX2 = (massCenterY - mMetaBody.getMassCenterY()) * angularVelocity;
				float vY2 = (mMetaBody.getMassCenterX() - massCenterX) * angularVelocity;
				mMetaBody.setVelocity(velocityX + vX2, velocityY + vY2);
			} else {
				for (int i = 0; i < 3; ++i)
					for (int j = 0; j < 3; ++j) {
						Cellularity curCellularity = mChunks[i][j];
						if (curCellularity != null)
							curCellularity.mDropCurrent.clear();
					}
			}
		}
	}

	public void postDrop() {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).postDrop();
		mDropUsed.clear();
	}

	public void tickEnable(int pX, int pY, int pZ) {
		mCellsTick.set(getVal(pX, pY, pZ));
	}

	public void tickDisable(int pX, int pY, int pZ) {
		mCellsTick.remove(getVal(pX, pY, pZ));
	}

	public void invalidateCell(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateCellHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
	}

	public void invalidateDrop(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateDropHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
	}

	public void invalidateRender(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateRenderHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
	}

	public void pushCellularity(Cellularity pCellularity) {
		mCellularities.set(pCellularity);
		pCellularity.attachChunk(this);
	}

	public void removeCellularity(Cellularity pCellularity) {
		pCellularity.detachChunk();
		mCellularities.remove(pCellularity);
	}

	public void attach(Tissularity pTissularity, int pX, int pY) {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).detachChunk();
		mTissularity = pTissularity;
		mX = pX;
		mY = pY;
		refreshOffset();
		invalidateBorder();
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).attachChunk(this);
		tissularedAttachHelper();
	}

	public void detach() {
		if (mTissularity != null) {
			for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
					mCellularities.next(i))
				((Cellularity) mCellularities.val(i)).detachChunk();
			tissularedDetachHelper();
			mTissularity = null;
			mX = 0;
			mY = 0;
			refreshOffset();
			invalidateBorder();
			for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
					mCellularities.next(i))
				((Cellularity) mCellularities.val(i)).attachChunk(this);
		}
	}

	public void refreshOffset() {
		if (mTissularity != null || mChunk != null) {
			short groupIndexOffset;
			float offsetX;
			float offsetY;
			World world;
			if (mTissularity != null) {
				groupIndexOffset = getGroupIndexOffset();
				offsetX = (mX << CHUNK_SIZE_LOG) - mTissularity.getOffsetX();
				offsetY = (mY << CHUNK_SIZE_LOG) - mTissularity.getOffsetY();
				world = mTissularity.getOrganularity().getWorld();
			} else {
				groupIndexOffset = mChunk.getGroupIndexOffset();
				offsetX = mChunk.getBody().getOffsetX();
				offsetY = mChunk.getBody().getOffsetY();
				world = mChunk.getBody().getWorld();
			}
			if (mMetaBody.getGroupIndexOffset() != groupIndexOffset
					|| mMetaBody.getOffsetX() != offsetX || mMetaBody.getOffsetY() != offsetY
					|| mMetaBody.getWorld() != world) {
				mMetaBody.setGroupIndexOffset(groupIndexOffset);
				mMetaBody.setOffset(offsetX, offsetY);
				mMetaBody.setWorld(world);
				for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
						mCellularities.next(i))
					((Cellularity) mCellularities.val(i)).refreshOffset();
			}
		} else {
			if (mMetaBody.getWorld() != null) {
				mMetaBody.setWorld(null);
				mMetaBody.setOffset(0, 0);
				mMetaBody.setGroupIndexOffset((short) 0);
				for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
						mCellularities.next(i))
					((Cellularity) mCellularities.val(i)).refreshOffset();
			}
		}
	}

	public void refreshRender() {
		for (List.Node i = mCellularities.begin(); i != mCellularities.end(); i =
				mCellularities.next(i))
			((Cellularity) mCellularities.val(i)).refreshRender();
		for (int i = 0; i < CHUNK_VOLUME; ++i)
			mRender.set(i);
	}

	public short getGroupIndexOffset() {
		return mTissularity == null ? 0
				: (short) (mTissularity.getGroupIndexPackOffset() << CHUNK_DEPTH_LOG);
	}

	public void refreshBodies() {
		for (List.Node j = mCellularities.begin(); j != mCellularities.end(); j =
				mCellularities.next(j)) {
			Cellularity cellularity = (Cellularity) mCellularities.val(j);
			cellularity.mMetaBody.refresh();
		}
	}

	public void refreshCellularitiesChunks() {
		for (List.Node j = mCellularities.begin(); j != mCellularities.end();) {
			Cellularity dynamic = (Cellularity) mCellularities.val(j);
			MetaBody body = dynamic.getBody();
			int x = MathUtils.floor(body.getPositionX()) >> Settings.CHUNK_SIZE_LOG;
			int y = MathUtils.floor(body.getPositionY()) >> Settings.CHUNK_SIZE_LOG;
			j = mCellularities.next(j);
			if (x != 0 || y != 0) {
				Cellularity nextChunk = mChunks[1 + MathUtils.sign(y)][1 + MathUtils.sign(x)];
				if (nextChunk != null)
					moveCellularity(dynamic, nextChunk);
			}
			Cellularity chunk = dynamic.getChunk();
			boolean flag = false;
			for (int i = 0; i < ArrayConstants.MOVE_AROUND_SIZE; ++i)
				if (chunk.mChunks[1 + ArrayConstants.MOVE_AROUND_X[i]][1 + ArrayConstants.MOVE_AROUND_Y[i]] == null) {
					flag = true;
					break;
				}
			if (flag)
				dynamic.mMetaBody.freeze();
			else
				dynamic.mMetaBody.unfreeze();
		}
	}

	public void setCellLightData(int pX, int pY, int pZ, int pLightResistanceId, int pLightSourceId) {
		final int val = getVal(pX, pY, pZ);
		mLightResistance[val] = pLightResistanceId;
		mLightSource[val] = pLightSourceId;
	}

	public int getLightSource(int pX, int pY, int pZ) {
		return mLightSource[getVal(pX, pY, pZ)];
	}

	public void setAI(int pX, int pY, int pZ, int pAIResistance, int pAIVertical, int pAIHorizontal) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.setAIHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ, pAIResistance,
					pAIVertical, pAIHorizontal);
	}

	public int getAI(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (chunk != null)
			return chunk.getAIHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
		return 0;
	}

	public void setLight(int pX, int pY, int pZ, int pR, int pG, int pB) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.setLightHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ, pR, pG, pB);
	}

	public int[] getLight(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (pZ != CHUNK_DEPTH_VACUUM && chunk != null)
			return chunk.getLightHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
		mTmpLight[0] = 0;
		mTmpLight[1] = 0;
		mTmpLight[2] = 0;
		return mTmpLight;
	}

	public int getLightCornersOffset(int pX, int pY, int pZ) {
		return getVal(pX, pY, pZ) << LIGHT_CORNERS_SIZE_LOG;
	}

	public int getLightR(float pFX, float pFY, int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (chunk == null)
			return 0;
		int cornerId =
				getVal(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ) << LIGHT_CORNERS_SIZE_LOG;
		float t =
				(1.0f - pFX) * chunk.mLightCorners[cornerId + 4] + pFX
						* chunk.mLightCorners[cornerId];
		float b =
				(1.0f - pFX) * chunk.mLightCorners[cornerId + 8] + pFX
						* chunk.mLightCorners[cornerId + 12];
		float m = (1.0f - pFY) * b + pFY * t;
		return (int) m;
	}

	public int getLightG(float pFX, float pFY, int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (chunk == null)
			return 0;
		int cornerId =
				getVal(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ) << LIGHT_CORNERS_SIZE_LOG;
		float t =
				(1.0f - pFX) * chunk.mLightCorners[cornerId + 5] + pFX
						* chunk.mLightCorners[cornerId + 1];
		float b =
				(1.0f - pFX) * chunk.mLightCorners[cornerId + 9] + pFX
						* chunk.mLightCorners[cornerId + 13];
		float m = (1.0f - pFY) * b + pFY * t;
		return (int) m;
	}

	public int getLightB(float pFX, float pFY, int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (chunk == null)
			return 0;
		int cornerId =
				getVal(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ) << LIGHT_CORNERS_SIZE_LOG;
		float t =
				(1.0f - pFX) * chunk.mLightCorners[cornerId + 6] + pFX
						* chunk.mLightCorners[cornerId + 2];
		float b =
				(1.0f - pFX) * chunk.mLightCorners[cornerId + 10] + pFX
						* chunk.mLightCorners[cornerId + 14];
		float m = (1.0f - pFY) * b + pFY * t;
		return (int) m;
	}

	private void setAIHelper(int pX, int pY, int pZ, int pAIResistance, int pAIVertical,
			int pAIHorizontal) {
		final int val = getVal(pX, pY, pZ);
		mAI[val] = (short) (pAIResistance | (pAIVertical << 8) | (pAIHorizontal << 12));
		for (int j = 0; j < SQUARE_3_SIZE; ++j)
			invalidateAI(pX + SQUARE_3_X[j], pY + SQUARE_3_Y[j], pZ);
	}

	private int getAIHelper(int pX, int pY, int pZ) {
		return mAI[getVal(pX, pY, pZ)];
	}

	private void setLightHelper(int pX, int pY, int pZ, int pR, int pG, int pB) {
		final int colorId = getVal(pX, pY, pZ) << COLORS_LOG;
		mLight[colorId] = pR;
		mLight[colorId + 1] = pG;
		mLight[colorId + 2] = pB;
		for (int j = 0; j < CELL_UPDATE_SIZE; ++j)
			invalidateLight(pX + CELL_UPDATE_X[j], pY + CELL_UPDATE_Y[j], pZ + CELL_UPDATE_Z[j]);
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

	private void invalidateAI(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateAIHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
	}

	private void invalidateLight(int pX, int pY, int pZ) {
		final Cellularity chunk = getChunk(pX, pY);
		if (CHUNK_MIN_DEPTH <= pZ && pZ <= CHUNK_MAX_DEPTH && chunk != null)
			chunk.invalidateLightHelper(pX & CHUNK_SIZE_MASK, pY & CHUNK_SIZE_MASK, pZ);
	}

	private void moveCellularity(Cellularity pCellularity, Cellularity pChunk) {
		mCellularities.remove(pCellularity);
		pChunk.mCellularities.set(pCellularity);
		pCellularity.replaceChunk(pChunk);
	}

	private void replaceChunk(Cellularity pChunk) {
		mChunk = pChunk;
		float offsetX = mChunk.getBody().getOffsetX();
		float offsetY = mChunk.getBody().getOffsetY();
		mMetaBody.setOffsetMove(offsetX, offsetY);
	}

	private void attachChunk(Cellularity pChunk) {
		mChunk = pChunk;
		refreshOffset();
		if (mChunk.mTissularity != null)
			tissularedAttachHelper();
	}

	private void detachChunk() {
		if (mChunk != null) {
			if (mChunk.mTissularity != null)
				tissularedDetachHelper();
			mChunk = null;
			refreshOffset();
		}
	}

	private void tissularedAttachHelper() {
		Tissularity tissularity = isChunk() ? mTissularity : mChunk.mTissularity;
		GraphicsModule graphicsModule = tissularity.getGraphicsModule();
		if (isChunk())
			graphicsModule.commandPushChunk(mX, mY);
		else
			mGraphicsModuleKey = graphicsModule.commandPushDynamic();
		for (int i = mCellsKeys.size() - 1; i >= 0; --i) {
			int val = mCellsKeys.key(i);
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			mCells[val].tissularedAttach(this, x, y, z);
		}
	}

	private void tissularedDetachHelper() {
		Tissularity tissularity = isChunk() ? mTissularity : mChunk.mTissularity;
		GraphicsModule graphicsModule = tissularity.getGraphicsModule();
		if (isChunk())
			graphicsModule.commandRemoveChunk(mX, mY);
		else
			graphicsModule.commandRemoveDynamic(mGraphicsModuleKey);
		for (int i = mCellsKeys.size() - 1; i >= 0; --i) {
			int val = mCellsKeys.key(i);
			int x = getValX(val);
			int y = getValY(val);
			int z = getValZ(val);
			mCells[val].tissularedDetach(this, x, y, z);
		}
	}

	private boolean dropDfs(int pX, int pY, int pZ) {
		final Cellularity chunk0 = getChunk(pX, pY);
		final int x0 = pX & CHUNK_SIZE_MASK;
		final int y0 = pY & CHUNK_SIZE_MASK;
		final int val0 = getVal(x0, y0, pZ);
		if (chunk0.mDropCurrent.get(val0))
			return true;
		if (chunk0.mDropUsed.get(val0))
			return false;
		mDropMinX = Math.min(mDropMinX, pX);
		mDropMinY = Math.min(mDropMinY, pY);
		mDropMaxX = Math.max(mDropMaxX, pX);
		mDropMaxY = Math.max(mDropMaxY, pY);
		++mDropCount;
		if (mDropMaxX - mDropMinX + 1 > CHUNK_SIZE || mDropMaxY - mDropMinY + 1 > CHUNK_SIZE
				|| mDropCount > MAX_DROP_COUNT)
			return false;
		chunk0.mDropUsed.set(val0);
		chunk0.mDropCurrent.set(val0);
		chunk0.mDropUpdate.remove(val0);
		Cell cur = chunk0.getCellHelper(x0, y0, pZ);
		if (!cur.isDroppable(chunk0, x0, y0, pZ))
			return false;
		for (int i = 0; i < DIRECTIONS_3D_SIZE; ++i) {
			int vX = DIRECTIONS_3D_X[i];
			int vY = DIRECTIONS_3D_Y[i];
			int vZ = DIRECTIONS_3D_Z[i];
			int posX = pX + vX;
			int posY = pY + vY;
			int z = pZ + vZ;
			Cellularity chunk = getChunk(posX, posY);
			if (chunk == null) {
				if (isChunk())
					return false;
				else
					continue;
			}
			if (CHUNK_MIN_DEPTH <= z && z <= CHUNK_MAX_DEPTH) {
				int x = posX & CHUNK_SIZE_MASK;
				int y = posY & CHUNK_SIZE_MASK;
				Cell next = chunk.getCellHelper(x, y, z);
				boolean flag = true;
				if (cur.isConnected(chunk0, x0, y0, pZ, next, vX, vY, vZ)
						&& next.isConnected(chunk, x, y, z, cur, -vX, -vY, -vZ))
					flag = dropDfs(posX, posY, z);
				if (!flag)
					return false;
			}
		}
		return true;
	}

	private Cell getCellHelper(int pX, int pY, int pZ) {
		final Cell cell = mCells[getVal(pX, pY, pZ)];
		return cell != null ? cell : getDefaultCell(pX, pY, pZ);
	}

	private void setCellHelper(int pX, int pY, int pZ, Cell pCell) {
		setCellPure(pX, pY, pZ, pCell);
		for (int i = 0; i < CELL_UPDATE_SIZE; ++i)
			invalidateCell(pX + CELL_UPDATE_X[i], pY + CELL_UPDATE_Y[i], pZ + CELL_UPDATE_Z[i]);
		invalidateDropHelper(pX, pY, pZ);
		for (int i = 0; i < DIRECTIONS_3D_SIZE; ++i)
			invalidateDrop(pX + DIRECTIONS_3D_X[i], pY + DIRECTIONS_3D_Y[i], pZ
					+ DIRECTIONS_3D_Z[i]);
	}

	private void setCellPure(int pX, int pY, int pZ, Cell pCell) {
		final int val = getVal(pX, pY, pZ);
		final Cell defaultCell = getDefaultCell(pX, pY, pZ);
		final boolean tissulared = isTissulared();
		Cell oldCell = mCells[val];
		if (oldCell == null)
			oldCell = defaultCell;
		if (tissulared)
			oldCell.tissularedDetach(this, pX, pY, pZ);
		oldCell.detach(this, pX, pY, pZ);
		mCells[val] = null;
		mCellsKeys.remove(val);
		oldCell.recycle();
		if (pCell != defaultCell) {
			mCells[val] = pCell;
			mCellsKeys.set(val);
		}
		pCell.attach(this, pX, pY, pZ);
		if (tissulared)
			pCell.tissularedAttach(this, pX, pY, pZ);
	}

	private void invalidateCellHelper(int pX, int pY, int pZ) {
		final int val = getVal(pX, pY, pZ);
		mCellsUpdate.set(val);
		mCellsRefresh.set(val);
		mRender.set(val);
		mLightUpdate.set(val);
		mAIUpdate.set(val);
		if (mCellsModifiedKeys.remove(val)) {
			mCellsModified[val].recycle();
			mCellsModified[val] = null;
		}
	}

	private void invalidateDropHelper(int pX, int pY, int pZ) {
		mDropUpdate.set(getVal(pX, pY, pZ));
	}

	private void invalidateRenderHelper(int pX, int pY, int pZ) {
		mRender.set(getVal(pX, pY, pZ));
	}

	private void invalidateAIHelper(int pX, int pY, int pZ) {
		final int val = getVal(pX, pY, pZ);
		mAIUpdate.set(val);
		if (Settings.AI_DEBUG_RENDER)
			mRender.set(val);
		mAIModifiedKeys.remove(val);
	}

	private void invalidateLightHelper(int pX, int pY, int pZ) {
		final int val = getVal(pX, pY, pZ);
		mLightUpdate.set(val);
		mLightRefresh.set(val);
		mRender.set(val);
		mLightModifiedKeys.remove(val);
	}

	private void invalidateBorder() {
		final int left = CHUNK_LEFT + 1;
		final int right = CHUNK_RIGHT - 1;
		final int bottom = CHUNK_BOTTOM + 1;
		final int top = CHUNK_TOP - 1;
		boolean leftTop = false, rightTop = false, leftBottom = false, rightBottom = false;
		Cellularity chunk;
		chunk = mChunks[1][2];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_BOTTOM; i <= CHUNK_TOP; ++i) {
					chunk.invalidateAIHelper(CHUNK_LEFT, i, z);
					chunk.invalidateLightHelper(CHUNK_LEFT, i, z);
					chunk.invalidateCellHelper(CHUNK_LEFT, i, z);
					chunk.invalidateDropHelper(CHUNK_LEFT, i, z);
				}
				for (int i = bottom; i <= top; ++i) {
					invalidateAIHelper(CHUNK_RIGHT, i, z);
					invalidateLightHelper(CHUNK_RIGHT, i, z);
					invalidateCellHelper(CHUNK_RIGHT, i, z);
					invalidateDropHelper(CHUNK_RIGHT, i, z);
				}
			}
			rightBottom = true;
			rightTop = true;
		}
		chunk = mChunks[2][2];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateAIHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
				chunk.invalidateLightHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
				chunk.invalidateCellHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
				chunk.invalidateDropHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
			}
			rightTop = true;
		}
		chunk = mChunks[2][1];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_LEFT; i <= CHUNK_RIGHT; ++i) {
					chunk.invalidateAIHelper(i, CHUNK_BOTTOM, z);
					chunk.invalidateLightHelper(i, CHUNK_BOTTOM, z);
					chunk.invalidateCellHelper(i, CHUNK_BOTTOM, z);
					chunk.invalidateDropHelper(i, CHUNK_BOTTOM, z);
				}
				for (int i = left; i <= right; ++i) {
					invalidateAIHelper(i, CHUNK_TOP, z);
					invalidateLightHelper(i, CHUNK_TOP, z);
					invalidateCellHelper(i, CHUNK_TOP, z);
					invalidateDropHelper(i, CHUNK_TOP, z);
				}
			}
			leftTop = true;
			rightTop = true;
		}
		chunk = mChunks[2][0];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateAIHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
				chunk.invalidateLightHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
				chunk.invalidateCellHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
				chunk.invalidateDropHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
			}
			leftTop = true;
		}
		chunk = mChunks[1][0];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_BOTTOM; i <= CHUNK_TOP; ++i) {
					chunk.invalidateAIHelper(CHUNK_RIGHT, i, z);
					chunk.invalidateLightHelper(CHUNK_RIGHT, i, z);
					chunk.invalidateCellHelper(CHUNK_RIGHT, i, z);
					chunk.invalidateDropHelper(CHUNK_RIGHT, i, z);
				}
				for (int i = bottom; i <= top; ++i) {
					invalidateAIHelper(CHUNK_LEFT, i, z);
					invalidateLightHelper(CHUNK_LEFT, i, z);
					invalidateCellHelper(CHUNK_LEFT, i, z);
					invalidateDropHelper(CHUNK_LEFT, i, z);
				}
			}
			leftTop = true;
			leftBottom = true;
		}
		chunk = mChunks[0][0];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateAIHelper(CHUNK_RIGHT, CHUNK_TOP, z);
				chunk.invalidateLightHelper(CHUNK_RIGHT, CHUNK_TOP, z);
				chunk.invalidateCellHelper(CHUNK_RIGHT, CHUNK_TOP, z);
				chunk.invalidateDropHelper(CHUNK_RIGHT, CHUNK_TOP, z);
			}
			leftBottom = true;
		}
		chunk = mChunks[0][1];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				for (int i = CHUNK_LEFT; i <= CHUNK_RIGHT; ++i) {
					chunk.invalidateAIHelper(i, CHUNK_TOP, z);
					chunk.invalidateLightHelper(i, CHUNK_TOP, z);
					chunk.invalidateCellHelper(i, CHUNK_TOP, z);
					chunk.invalidateDropHelper(i, CHUNK_TOP, z);
				}
				for (int i = left; i <= right; ++i) {
					invalidateAIHelper(i, CHUNK_BOTTOM, z);
					invalidateLightHelper(i, CHUNK_BOTTOM, z);
					invalidateCellHelper(i, CHUNK_BOTTOM, z);
					invalidateDropHelper(i, CHUNK_BOTTOM, z);
				}
			}
			leftBottom = true;
			rightBottom = true;
		}
		chunk = mChunks[0][2];
		if (chunk != null) {
			for (int z = CHUNK_MIN_DEPTH; z <= CHUNK_MAX_DEPTH; ++z) {
				chunk.invalidateAIHelper(CHUNK_LEFT, CHUNK_TOP, z);
				chunk.invalidateLightHelper(CHUNK_LEFT, CHUNK_TOP, z);
				chunk.invalidateCellHelper(CHUNK_LEFT, CHUNK_TOP, z);
				chunk.invalidateDropHelper(CHUNK_LEFT, CHUNK_TOP, z);
			}
			rightBottom = true;
		}

		if (leftBottom)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateAIHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
				invalidateLightHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
				invalidateCellHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
				invalidateDropHelper(CHUNK_LEFT, CHUNK_BOTTOM, z);
			}
		if (rightBottom)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateAIHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
				invalidateLightHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
				invalidateCellHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
				invalidateDropHelper(CHUNK_RIGHT, CHUNK_BOTTOM, z);
			}
		if (leftTop)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateAIHelper(CHUNK_LEFT, CHUNK_TOP, z);
				invalidateLightHelper(CHUNK_LEFT, CHUNK_TOP, z);
				invalidateCellHelper(CHUNK_LEFT, CHUNK_TOP, z);
				invalidateDropHelper(CHUNK_LEFT, CHUNK_TOP, z);
			}
		if (rightTop)
			for (int z = 0; z < CHUNK_DEPTH; ++z) {
				invalidateAIHelper(CHUNK_RIGHT, CHUNK_TOP, z);
				invalidateLightHelper(CHUNK_RIGHT, CHUNK_TOP, z);
				invalidateCellHelper(CHUNK_RIGHT, CHUNK_TOP, z);
				invalidateDropHelper(CHUNK_RIGHT, CHUNK_TOP, z);
			}
	}

	private Cell getDefaultCell(int pX, int pY, int pZ) {
		return VacuumCell.obtain();
	}

	private Cell getDropDefaultCell(int pX, int pY, int pZ) {
		return isChunk() ? AirCell.obtain() : VacuumCell.obtain();
	}

	private int skyR(int pX, int pY) {
		return 256;
	}

	private int skyG(int pX, int pY) {
		return 256;
	}

	private int skyB(int pX, int pY) {
		return 256;
	}

	private Cellularity getChunk(int pX, int pY) {
		final int x = (pX >> CHUNK_SIZE_LOG) + 1;
		final int y = (pY >> CHUNK_SIZE_LOG) + 1;
		return 0 <= x && x < 3 && 0 <= y && y < 3 ? mChunks[y][x] : null;
	}

	private boolean isTissulared() {
		return mTissularity != null || mChunk != null && mChunk.mTissularity != null;
	}

	private static int getVal(int pX, int pY, int pZ) {
		return pX | (pY << CHUNK_SIZE_LOG) | (pZ << CHUNK_SQUARE_LOG);
	}

	private static int getValX(int pVal) {
		return pVal & CHUNK_SIZE_MASK;
	}

	private static int getValY(int pVal) {
		return (pVal >> CHUNK_SIZE_LOG) & CHUNK_SIZE_MASK;
	}

	private static int getValZ(int pVal) {
		return pVal >> CHUNK_SQUARE_LOG;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
