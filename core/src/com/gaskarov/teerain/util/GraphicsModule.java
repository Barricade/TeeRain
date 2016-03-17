package com.gaskarov.teerain.util;

import static com.gaskarov.teerain.Settings.CHUNK_BOTTOM;
import static com.gaskarov.teerain.Settings.CHUNK_LEFT;
import static com.gaskarov.teerain.Settings.CHUNK_RIGHT;
import static com.gaskarov.teerain.Settings.CHUNK_SIZE_LOG;
import static com.gaskarov.teerain.Settings.CHUNK_SIZE_MASK;
import static com.gaskarov.teerain.Settings.CHUNK_SQUARE_LOG;
import static com.gaskarov.teerain.Settings.CHUNK_TOP;
import static com.gaskarov.teerain.Settings.CHUNK_VOLUME;
import static com.gaskarov.teerain.Settings.LIGHT_CORNERS_SIZE;
import static com.gaskarov.teerain.Settings.LIGHT_CORNERS_SIZE_LOG;

import com.badlogic.gdx.graphics.Color;
import com.gaskarov.teerain.Settings;
import com.gaskarov.util.common.IntVector2;
import com.gaskarov.util.common.KeyValuePair;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.Container;
import com.gaskarov.util.container.ContainerShadow;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.IntArray;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.LinkedIntTable;
import com.gaskarov.util.container.List;
import com.gaskarov.util.container.Queue;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class GraphicsModule {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int ELEMENTS_PER_UNIT_VERTEX = 8;
	public static final int ELEMENTS_PER_UNIT_TEXTURE = ELEMENTS_PER_UNIT_VERTEX << 2;

	private static final int COMMAND_PUSH_CHUNK = 0;
	private static final int COMMAND_REMOVE_CHUNK = 1;
	private static final int COMMAND_PUSH_DYNAMIC = 2;
	private static final int COMMAND_MOVE_DYNAMIC = 3;
	private static final int COMMAND_REMOVE_DYNAMIC = 4;
	private static final int COMMAND_UPDATE_UNIT_CHUNK = 5;
	private static final int COMMAND_UPDATE_UNIT_DYNAMIC = 6;
	private static final int COMMAND_UPDATE_UNIT_LIGHT_CORNERS = 7;
	private static final int COMMAND_MOVE_CAMERA = 8;
	private static final int COMMAND_OFFSET = 9;

	private static final int CAPACITY_UPDATES = 16;
	private static final int CAPACITY_DYNAMICS = 256;
	private static final int CAPACITY_BUFFERS = 4096;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private final IntVector2 mVec2 = IntVector2.obtain(0, 0);

	private LinkedHashTable mChunks;
	private final Container mDynamics = Container.obtain(CAPACITY_DYNAMICS);
	private final ContainerShadow mDynamicsShadow = ContainerShadow.obtain(CAPACITY_DYNAMICS);

	private int mOffsetX;
	private int mOffsetY;

	private float mCameraX;
	private float mCameraY;
	private float mCameraAngle;
	private float mCameraMassCenterX;
	private float mCameraMassCenterY;
	private float mCameraNextX;
	private float mCameraNextY;
	private float mCameraNextAngle;
	private float mCameraNextMassCenterX;
	private float mCameraNextMassCenterY;

	private long mLastTime;
	private float mAccumulatedTime;

	private final Queue mUpdates = Queue.obtain(CAPACITY_UPDATES);

	private GraphicsBuffer mBuffer;

	// ===========================================================
	// Constructors
	// ===========================================================

	private GraphicsModule() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	private static GraphicsModule obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (GraphicsModule.class) {
				return sPool.size() == 0 ? new GraphicsModule() : (GraphicsModule) sPool.pop();
			}
		return new GraphicsModule();
	}

	private static void recyclePure(GraphicsModule pObj) {
		if (GlobalConstants.POOL)
			synchronized (GraphicsModule.class) {
				sPool.push(pObj);
			}
	}

	public static GraphicsModule obtain() {
		GraphicsModule obj = obtainPure();

		obj.mChunks = LinkedHashTable.obtain();

		obj.mOffsetX = 0;
		obj.mOffsetY = 0;

		obj.mCameraX = 0;
		obj.mCameraY = 0;
		obj.mCameraAngle = 0;
		obj.mCameraMassCenterX = 0;
		obj.mCameraMassCenterY = 0;
		obj.mCameraNextX = 0;
		obj.mCameraNextY = 0;
		obj.mCameraNextAngle = 0;
		obj.mCameraNextMassCenterX = 0;
		obj.mCameraNextMassCenterY = 0;

		obj.mBuffer = GraphicsBuffer.obtain();

		return obj;
	}

	public static void recycle(GraphicsModule pObj) {
		while (pObj.mChunks.size() > 0) {
			KeyValuePair pair = (KeyValuePair) pObj.mChunks.remove(pObj.mChunks.front());
			IntVector2.recycle((IntVector2) pair.mA);
			GraphicsBody.recycle((GraphicsBody) pair.mB);
			KeyValuePair.recycle(pair);
		}
		LinkedHashTable.recycle(pObj.mChunks);
		pObj.mChunks = null;
		while (pObj.mDynamics.size() > 0) {
			GraphicsBody.recycle((GraphicsBody) pObj.mDynamics.getVal(0));
			pObj.mDynamics.remove(pObj.mDynamics.getKey(0));
		}
		pObj.mDynamics.clear(CAPACITY_DYNAMICS);
		pObj.mDynamicsShadow.clear(CAPACITY_DYNAMICS);

		while (pObj.mUpdates.size() > 0)
			GraphicsBuffer.recycle((GraphicsBuffer) pObj.mUpdates.pop());
		pObj.mUpdates.clear(CAPACITY_UPDATES);

		GraphicsBuffer.recycle(pObj.mBuffer);
		pObj.mBuffer = null;

		recyclePure(pObj);
	}

	public void setTime(long pLastTime, float pAccumulatedTime) {
		mLastTime = pLastTime;
		mAccumulatedTime = pAccumulatedTime;
	}

	public FloatArray getFloatBuffer() {
		return mBuffer.mFloatBuffer;
	}

	public IntArray getIntBuffer() {
		return mBuffer.mIntBuffer;
	}

	public void commandPushChunk(int pX, int pY) {
		mBuffer.mIntBuffer.push(COMMAND_PUSH_CHUNK);
		mBuffer.mIntBuffer.push(pX);
		mBuffer.mIntBuffer.push(pY);
	}

	public void commandRemoveChunk(int pX, int pY) {
		mBuffer.mIntBuffer.push(COMMAND_REMOVE_CHUNK);
		mBuffer.mIntBuffer.push(pX);
		mBuffer.mIntBuffer.push(pY);
	}

	public int commandPushDynamic() {
		mBuffer.mIntBuffer.push(COMMAND_PUSH_DYNAMIC);
		return mDynamicsShadow.push();
	}

	public void commandMoveDynamic(int pKey, float pCenterX, float pCenterY, float pAngle,
			float pMassCenterX, float pMassCenterY) {
		mBuffer.mIntBuffer.push(COMMAND_MOVE_DYNAMIC);
		mBuffer.mIntBuffer.push(pKey);
		mBuffer.mFloatBuffer.push(pCenterX);
		mBuffer.mFloatBuffer.push(pCenterY);
		mBuffer.mFloatBuffer.push(pAngle);
		mBuffer.mFloatBuffer.push(pMassCenterX);
		mBuffer.mFloatBuffer.push(pMassCenterY);
	}

	public void commandRemoveDynamic(int pKey) {
		mBuffer.mIntBuffer.push(COMMAND_REMOVE_DYNAMIC);
		mBuffer.mIntBuffer.push(pKey);
		mDynamicsShadow.remove(pKey);
	}

	public void commandUpdateUnitChunk(int pChunkX, int pChunkY, int pVal) {
		mBuffer.mIntBuffer.push(COMMAND_UPDATE_UNIT_CHUNK);
		mBuffer.mIntBuffer.push(pChunkX);
		mBuffer.mIntBuffer.push(pChunkY);
		mBuffer.mIntBuffer.push(pVal);
	}

	public void commandUpdateUnitDynamic(int pKey, int pVal) {
		mBuffer.mIntBuffer.push(COMMAND_UPDATE_UNIT_DYNAMIC);
		mBuffer.mIntBuffer.push(pKey);
		mBuffer.mIntBuffer.push(pVal);
	}

	public void commandUpdateUnitLightCorners(int pChunkX, int pChunkY, int pVal) {
		mBuffer.mIntBuffer.push(COMMAND_UPDATE_UNIT_LIGHT_CORNERS);
		mBuffer.mIntBuffer.push(pChunkX);
		mBuffer.mIntBuffer.push(pChunkY);
		mBuffer.mIntBuffer.push(pVal);
	}

	public void commandMoveCamera(float pCenterX, float pCenterY, float pAngle, float pMassCenterX,
			float pMassCenterY) {
		mBuffer.mIntBuffer.push(COMMAND_MOVE_CAMERA);
		mBuffer.mFloatBuffer.push(pCenterX);
		mBuffer.mFloatBuffer.push(pCenterY);
		mBuffer.mFloatBuffer.push(pAngle);
		mBuffer.mFloatBuffer.push(pMassCenterX);
		mBuffer.mFloatBuffer.push(pMassCenterY);
	}

	public void commandOffset(int pOffsetX, int pOffsetY) {
		mBuffer.mIntBuffer.push(COMMAND_OFFSET);
		mBuffer.mIntBuffer.push(pOffsetX);
		mBuffer.mIntBuffer.push(pOffsetY);
	}

	public synchronized void commandsFlush() {
		mUpdates.push(mBuffer);
		mBuffer = GraphicsBuffer.obtain();
	}

	private void tick(GraphicsBuffer pGraphicsBuffer) {
		IntArray intBuffer = pGraphicsBuffer.mIntBuffer;
		FloatArray floatBuffer = pGraphicsBuffer.mFloatBuffer;
		int n = intBuffer.size();
		int intBufferId = 0;
		int floatBufferId = 0;
		while (intBufferId < n) {
			switch (intBuffer.get(intBufferId++)) {
			case COMMAND_PUSH_CHUNK: {
				int x = intBuffer.get(intBufferId++);
				int y = intBuffer.get(intBufferId++);
				mChunks.set(KeyValuePair.obtain(IntVector2.obtain(x, y), GraphicsBody.obtain()));
				break;
			}
			case COMMAND_REMOVE_CHUNK: {
				int x = intBuffer.get(intBufferId++);
				int y = intBuffer.get(intBufferId++);
				KeyValuePair pair = (KeyValuePair) mChunks.remove(mVec2.set(x, y));
				IntVector2.recycle((IntVector2) pair.mA);
				GraphicsBody.recycle((GraphicsBody) pair.mB);
				KeyValuePair.recycle(pair);
				break;
			}
			case COMMAND_PUSH_DYNAMIC: {
				mDynamics.push(GraphicsBody.obtain());
				break;
			}
			case COMMAND_MOVE_DYNAMIC: {
				int key = intBuffer.get(intBufferId++);
				float centerX = floatBuffer.get(floatBufferId++);
				float centerY = floatBuffer.get(floatBufferId++);
				float angle = floatBuffer.get(floatBufferId++);
				float massCenterX = floatBuffer.get(floatBufferId++);
				float massCenterY = floatBuffer.get(floatBufferId++);
				GraphicsBody graphicsBody = (GraphicsBody) mDynamics.get(key);
				graphicsBody.move(centerX, centerY, angle, massCenterX, massCenterY);
				break;
			}
			case COMMAND_REMOVE_DYNAMIC: {
				int key = intBuffer.get(intBufferId++);
				mDynamics.remove(key);
				break;
			}
			case COMMAND_UPDATE_UNIT_CHUNK: {
				int chunkX = intBuffer.get(intBufferId++);
				int chunkY = intBuffer.get(intBufferId++);
				int val = intBuffer.get(intBufferId++);
				GraphicsBody graphicsBody = getChunk(chunkX, chunkY);
				boolean flag = false;
				for (int i = 0; i < Settings.LAYERS_PER_DEPTH; ++i) {
					graphicsBody.mUnits[val][i].clear();
					int num = intBuffer.get(intBufferId++);
					flag |= num > 0;
					for (int j = 0; j < num; ++j)
						for (int k = 0; k < ELEMENTS_PER_UNIT_TEXTURE; ++k)
							graphicsBody.mUnits[val][i].push(floatBuffer.get(floatBufferId++));
				}
				if (flag)
					graphicsBody.mUnitsKeys.set(val);
				else
					graphicsBody.mUnitsKeys.remove(val);
				break;
			}
			case COMMAND_UPDATE_UNIT_DYNAMIC: {
				int key = intBuffer.get(intBufferId++);
				int val = intBuffer.get(intBufferId++);
				GraphicsBody graphicsBody = (GraphicsBody) mDynamics.get(key);
				boolean flag = false;
				for (int i = 0; i < Settings.LAYERS_PER_DEPTH; ++i) {
					graphicsBody.mUnits[val][i].clear();
					int num = intBuffer.get(intBufferId++);
					flag |= num > 0;
					for (int j = 0; j < num; ++j)
						for (int k = 0; k < ELEMENTS_PER_UNIT_TEXTURE; ++k)
							graphicsBody.mUnits[val][i].push(floatBuffer.get(floatBufferId++));
				}
				if (flag)
					graphicsBody.mUnitsKeys.set(val);
				else
					graphicsBody.mUnitsKeys.remove(val);
				break;
			}
			case COMMAND_UPDATE_UNIT_LIGHT_CORNERS: {
				int chunkX = intBuffer.get(intBufferId++);
				int chunkY = intBuffer.get(intBufferId++);
				int val = intBuffer.get(intBufferId++);
				GraphicsBody graphicsBody = getChunk(chunkX, chunkY);
				int j = val << LIGHT_CORNERS_SIZE_LOG;
				for (int i = 0; i < LIGHT_CORNERS_SIZE; ++i)
					graphicsBody.mUnitsLightCorners[j + i] = floatBuffer.get(floatBufferId++);
				break;
			}
			case COMMAND_MOVE_CAMERA: {
				float cameraX = floatBuffer.get(floatBufferId++);
				float cameraY = floatBuffer.get(floatBufferId++);
				float cameraAngle = floatBuffer.get(floatBufferId++);
				float cameraMassCenterX = floatBuffer.get(floatBufferId++);
				float cameraMassCenterY = floatBuffer.get(floatBufferId++);
				mCameraX = mCameraNextX;
				mCameraY = mCameraNextY;
				mCameraAngle = mCameraNextAngle;
				mCameraMassCenterX = mCameraNextMassCenterX;
				mCameraMassCenterY = mCameraNextMassCenterY;
				mCameraNextX = cameraX;
				mCameraNextY = cameraY;
				mCameraNextAngle = cameraAngle;
				mCameraNextMassCenterX = cameraMassCenterX;
				mCameraNextMassCenterY = cameraMassCenterY;
				break;
			}
			case COMMAND_OFFSET: {
				int offsetX = intBuffer.get(intBufferId++);
				int offsetY = intBuffer.get(intBufferId++);
				float x = mOffsetX - offsetX;
				float y = mOffsetY - offsetY;
				mOffsetX = offsetX;
				mOffsetY = offsetY;
				mCameraMassCenterX += x;
				mCameraMassCenterY += y;
				mCameraNextMassCenterX += x;
				mCameraNextMassCenterY += y;
				for (int i = 0; i < mDynamics.size(); ++i)
					((GraphicsBody) mDynamics.getVal(i)).offset(x, y);
				for (List.Node i = mChunks.begin(); i != mChunks.end(); i = mChunks.next(i))
					((GraphicsBody) ((KeyValuePair) mChunks.val(i)).mB).offset(x, y);
				break;
			}
			default:
				break;
			}
		}
	}

	private GraphicsBody getChunk(int pX, int pY) {
		KeyValuePair pair = (KeyValuePair) mChunks.get(mVec2.set(pX, pY));
		return pair != null ? (GraphicsBody) pair.mB : null;
	}

	public void render(FloatArray[] pRenderBuffers, float pCellSize, float pWidth, float pHeight,
			long pTime) {

		TimeMeasure.sM13.start();
		while (pTime - mLastTime - mAccumulatedTime >= Settings.TIME_STEP_MILLIS) {
			GraphicsBuffer graphicsBuffer;
			synchronized (this) {
				if (mUpdates.size() == 0)
					break;
				graphicsBuffer = (GraphicsBuffer) mUpdates.shift();
			}
			tick(graphicsBuffer);
			GraphicsBuffer.recycle(graphicsBuffer);
			mAccumulatedTime += Settings.TIME_STEP_MILLIS;
			long millis = (long) mAccumulatedTime;
			mAccumulatedTime -= millis;
			mLastTime += millis;
		}
		TimeMeasure.sM13.end();

		TimeMeasure.sM14.start();
		float timeRatio = (pTime - mLastTime - mAccumulatedTime) / Settings.TIME_STEP_MILLIS;
		float timeOneMinusRatio = 1f - timeRatio;

		float cameraAngle = mCameraNextAngle * timeRatio + mCameraAngle * timeOneMinusRatio;
		float cameraCos = (float) Math.cos(cameraAngle);
		float cameraSin = (float) Math.sin(cameraAngle);
		float cameraLocalX = mCameraX * cameraCos - mCameraY * cameraSin;
		float cameraLocalY = mCameraX * cameraSin + mCameraY * cameraCos;
		float cameraX =
				mCameraNextMassCenterX * timeRatio + mCameraMassCenterX * timeOneMinusRatio
						+ cameraLocalX;
		float cameraY =
				mCameraNextMassCenterY * timeRatio + mCameraMassCenterY * timeOneMinusRatio
						+ cameraLocalY;

		float depthMaxFactor = Settings.DEPTH_FACTORS[Settings.CHUNK_MAX_DEPTH];
		float depthMaxWidth = pWidth * depthMaxFactor;
		float depthMaxHeight = pHeight * depthMaxFactor;
		float maxLeft = cameraX - depthMaxWidth / 2;
		float maxBottom = cameraY - depthMaxHeight / 2;

		int extra = Settings.MAX_DROP_SIZE;

		int col1 = (mOffsetX + MathUtils.floor(maxLeft) - extra) >> Settings.CHUNK_SIZE_LOG;
		int col2 =
				(mOffsetX + MathUtils.floor(maxLeft + depthMaxWidth) + extra) >> Settings.CHUNK_SIZE_LOG;

		int row1 = (mOffsetY + MathUtils.floor(maxBottom) - extra) >> Settings.CHUNK_SIZE_LOG;
		int row2 =
				(mOffsetY + MathUtils.floor(maxBottom + depthMaxHeight) + extra) >> Settings.CHUNK_SIZE_LOG;

		for (int i = 0; i < mDynamics.size(); ++i) {
			GraphicsBody dynamic = (GraphicsBody) mDynamics.getVal(i);
			float massX =
					dynamic.mNextMassCenterX * timeRatio + dynamic.mMassCenterX * timeOneMinusRatio;
			float massY =
					dynamic.mNextMassCenterY * timeRatio + dynamic.mMassCenterY * timeOneMinusRatio;
			float angle = dynamic.mNextAngle * timeRatio + dynamic.mAngle * timeOneMinusRatio;
			float c = (float) Math.cos(angle);
			float s = (float) Math.sin(angle);
			float localX = dynamic.mCenterX - dynamic.mMassCenterX;
			float localY = dynamic.mCenterY - dynamic.mMassCenterY;
			float localC = (float) Math.cos(angle - dynamic.mAngle);
			float localS = (float) Math.sin(angle - dynamic.mAngle);
			float x = massX + localX * localC - localY * localS;
			float y = massY + localX * localS + localY * localC;
			float screenX = x - cameraX;
			float screenY = y - cameraY;
			if (Math.abs(screenX) <= depthMaxWidth / 2 + Settings.MAX_DROP_SIZE * depthMaxFactor
					&& Math.abs(screenY) <= depthMaxHeight / 2 + Settings.MAX_DROP_SIZE
							* depthMaxFactor) {
				for (int j = 0; j < dynamic.mUnitsKeys.size(); ++j) {
					int val = dynamic.mUnitsKeys.key(j);
					int z = val >> CHUNK_SQUARE_LOG;
					int renderBufferOffset = z * Settings.LAYERS_PER_DEPTH;
					float depthFactor = Settings.DEPTH_FACTORS[z];
					float cellSize = pCellSize / depthFactor;
					for (int layer = 0; layer < Settings.LAYERS_PER_DEPTH; ++layer) {
						FloatArray renderBuffer = pRenderBuffers[renderBufferOffset + layer];
						float[] data = dynamic.mUnits[val][layer].data();
						int n = dynamic.mUnits[val][layer].size();
						for (int k = 0; k < n; k += ELEMENTS_PER_UNIT_VERTEX) {
							float tmpX = data[k] * c - data[k + 1] * s;
							float tmpY = data[k] * s + data[k + 1] * c;
							renderBuffer.push((screenX + tmpX) * cellSize);
							renderBuffer.push((screenY + tmpY) * cellSize);
							tmpX += x;
							tmpY += y;
							int posX = MathUtils.floor(tmpX);
							int posY = MathUtils.floor(tmpY);
							tmpX -= posX;
							tmpY -= posY;
							GraphicsBody chunk =
									getChunk((posX + mOffsetX) >> CHUNK_SIZE_LOG,
											(posY + mOffsetY) >> CHUNK_SIZE_LOG);
							float lightR = 0, lightG = 0, lightB = 0;
							if (chunk != null) {
								posX &= CHUNK_SIZE_MASK;
								posY &= CHUNK_SIZE_MASK;
								int cornerId =
										(posX | (posY << CHUNK_SIZE_LOG) | (z << CHUNK_SQUARE_LOG)) << LIGHT_CORNERS_SIZE_LOG;
								float tmpXOneMinus = 1.0f - tmpX;
								float tmpYOneMinus = 1.0f - tmpY;
								{
									float t =
											tmpXOneMinus * chunk.mUnitsLightCorners[cornerId + 4]
													+ tmpX * chunk.mUnitsLightCorners[cornerId];
									float b =
											tmpXOneMinus * chunk.mUnitsLightCorners[cornerId + 8]
													+ tmpX
													* chunk.mUnitsLightCorners[cornerId + 12];
									lightR = tmpYOneMinus * b + tmpY * t;
								}
								{
									float t =
											tmpXOneMinus * chunk.mUnitsLightCorners[cornerId + 5]
													+ tmpX * chunk.mUnitsLightCorners[cornerId + 1];
									float b =
											tmpXOneMinus * chunk.mUnitsLightCorners[cornerId + 9]
													+ tmpX
													* chunk.mUnitsLightCorners[cornerId + 13];
									lightG = tmpYOneMinus * b + tmpY * t;
								}
								{
									float t =
											tmpXOneMinus * chunk.mUnitsLightCorners[cornerId + 6]
													+ tmpX * chunk.mUnitsLightCorners[cornerId + 2];
									float b =
											tmpXOneMinus * chunk.mUnitsLightCorners[cornerId + 10]
													+ tmpX
													* chunk.mUnitsLightCorners[cornerId + 14];
									lightB = tmpYOneMinus * b + tmpY * t;
								}
							}
							renderBuffer.push(Color.toFloatBits(data[k + 2] * lightR, data[k + 3]
									* lightG, data[k + 4] * lightB, data[k + 5]));
							renderBuffer.push(data[k + 6]);
							renderBuffer.push(data[k + 7]);
						}
					}
				}
			}
		}

		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z) {
			int renderBufferOffset = z * Settings.LAYERS_PER_DEPTH;
			float depthFactor = Settings.DEPTH_FACTORS[z];
			float depthWidth = pWidth * depthFactor;
			float depthHeight = pHeight * depthFactor;
			float cellSize = pCellSize / depthFactor;
			int tmp1 = MathUtils.floor(cameraX - depthWidth / 2);
			int tmp2 = MathUtils.floor(cameraX + depthWidth / 2);
			int tmp3 = MathUtils.floor(cameraY - depthHeight / 2);
			int tmp4 = MathUtils.floor(cameraY + depthHeight / 2);
			for (int row = row1; row <= row2; ++row)
				for (int col = col1; col <= col2; ++col) {
					GraphicsBody chunk = getChunk(col, row);
					if (chunk != null) {
						int chunkX = (col << Settings.CHUNK_SIZE_LOG) - mOffsetX;
						int chunkY = (row << Settings.CHUNK_SIZE_LOG) - mOffsetY;

						int x1 = Math.max(CHUNK_LEFT, tmp1 - chunkX);
						int x2 = Math.min(CHUNK_RIGHT, tmp2 - chunkX);
						int y1 = Math.max(CHUNK_BOTTOM, tmp3 - chunkY);
						int y2 = Math.min(CHUNK_TOP, tmp4 - chunkY);

						for (int y = y1; y <= y2; ++y)
							for (int x = x1; x <= x2; ++x) {
								int val = x | (y << CHUNK_SIZE_LOG) | (z << CHUNK_SQUARE_LOG);
								for (int layer = 0; layer < Settings.LAYERS_PER_DEPTH; ++layer) {
									FloatArray renderBuffer =
											pRenderBuffers[renderBufferOffset + layer];
									float[] data = chunk.mUnits[val][layer].data();
									int n = chunk.mUnits[val][layer].size();
									for (int k = 0; k < n; k += ELEMENTS_PER_UNIT_VERTEX) {
										float tmpX = data[k];
										float tmpY = data[k + 1];
										renderBuffer.push((tmpX - cameraX) * cellSize);
										renderBuffer.push((tmpY - cameraY) * cellSize);
										int posX = chunkX + x;
										int posY = chunkY + y;
										tmpX -= posX;
										tmpY -= posY;
										float lightR, lightG, lightB;
										int cornerId = val << LIGHT_CORNERS_SIZE_LOG;
										float tmpXOneMinus = 1.0f - tmpX;
										float tmpYOneMinus = 1.0f - tmpY;
										float[] lightCorners = chunk.mUnitsLightCorners;

										float tr =
												tmpXOneMinus * lightCorners[cornerId + 4] + tmpX
														* lightCorners[cornerId];
										float br =
												tmpXOneMinus * lightCorners[cornerId + 8] + tmpX
														* lightCorners[cornerId + 12];
										lightR = tmpYOneMinus * br + tmpY * tr;

										float tg =
												tmpXOneMinus * lightCorners[cornerId + 5] + tmpX
														* lightCorners[cornerId + 1];
										float bg =
												tmpXOneMinus * lightCorners[cornerId + 9] + tmpX
														* lightCorners[cornerId + 13];
										lightG = tmpYOneMinus * bg + tmpY * tg;

										float tb =
												tmpXOneMinus * lightCorners[cornerId + 6] + tmpX
														* lightCorners[cornerId + 2];
										float bb =
												tmpXOneMinus * lightCorners[cornerId + 10] + tmpX
														* lightCorners[cornerId + 14];
										lightB = tmpYOneMinus * bb + tmpY * tb;

										renderBuffer.push(Color.toFloatBits(data[k + 2] * lightR,
												data[k + 3] * lightG, data[k + 4] * lightB,
												data[k + 5]));
										renderBuffer.push(data[k + 6]);
										renderBuffer.push(data[k + 7]);
									}
								}
							}
					}
				}
		}
		TimeMeasure.sM14.end();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private static final class GraphicsBuffer {

		private static final Array sPool = Array.obtain();

		private final IntArray mIntBuffer = IntArray.obtain(CAPACITY_BUFFERS);
		private final FloatArray mFloatBuffer = FloatArray.obtain(CAPACITY_BUFFERS);

		private GraphicsBuffer() {
		}

		private static GraphicsBuffer obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (GraphicsBuffer.class) {
					return sPool.size() == 0 ? new GraphicsBuffer() : (GraphicsBuffer) sPool.pop();
				}
			return new GraphicsBuffer();
		}

		private static void recyclePure(GraphicsBuffer pObj) {
			if (GlobalConstants.POOL)
				synchronized (GraphicsBuffer.class) {
					sPool.push(pObj);
				}
		}

		public static GraphicsBuffer obtain() {
			GraphicsBuffer obj = obtainPure();
			return obj;
		}

		public static void recycle(GraphicsBuffer pObj) {
			pObj.mIntBuffer.clear(CAPACITY_BUFFERS);
			pObj.mFloatBuffer.clear(CAPACITY_BUFFERS);
			recyclePure(pObj);
		}

	}

	private static final class GraphicsBody {

		private static final int CAPACITY_UNIT = 128;

		private static final Array sPool = Array.obtain();

		private final FloatArray[][] mUnits =
				new FloatArray[CHUNK_VOLUME][Settings.LAYERS_PER_DEPTH];
		private final float[] mUnitsLightCorners =
				new float[CHUNK_VOLUME << LIGHT_CORNERS_SIZE_LOG];
		private final LinkedIntTable mUnitsKeys = LinkedIntTable.obtain(CHUNK_VOLUME);
		private float mCenterX;
		private float mCenterY;
		private float mAngle;
		private float mMassCenterX;
		private float mMassCenterY;
		private float mNextCenterX;
		private float mNextCenterY;
		private float mNextAngle;
		private float mNextMassCenterX;
		private float mNextMassCenterY;
		private boolean mHasPrevState;

		private GraphicsBody() {
			for (int i = 0; i < CHUNK_VOLUME; ++i) {
				for (int j = 0; j < Settings.LAYERS_PER_DEPTH; ++j)
					mUnits[i][j] = FloatArray.obtain(CAPACITY_UNIT);
			}
		}

		private static GraphicsBody obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (GraphicsBody.class) {
					return sPool.size() == 0 ? new GraphicsBody() : (GraphicsBody) sPool.pop();
				}
			return new GraphicsBody();
		}

		private static void recyclePure(GraphicsBody pObj) {
			if (GlobalConstants.POOL)
				synchronized (GraphicsBody.class) {
					sPool.push(pObj);
				}
		}

		public static GraphicsBody obtain() {
			GraphicsBody obj = obtainPure();
			obj.mHasPrevState = false;
			return obj;
		}

		public static void recycle(GraphicsBody pObj) {
			while (pObj.mUnitsKeys.size() > 0) {
				int val = pObj.mUnitsKeys.pop();
				for (int j = 0; j < Settings.LAYERS_PER_DEPTH; ++j)
					pObj.mUnits[val][j].clear(CAPACITY_UNIT);
			}
			recyclePure(pObj);
		}

		private void move(float pCenterX, float pCenterY, float pAngle, float pMassCenterX,
				float pMassCenterY) {
			if (mHasPrevState) {
				mCenterX = mNextCenterX;
				mCenterY = mNextCenterY;
				mAngle = mNextAngle;
				mMassCenterX = mNextMassCenterX;
				mMassCenterY = mNextMassCenterY;
				mNextCenterX = pCenterX;
				mNextCenterY = pCenterY;
				mNextAngle = pAngle;
				mNextMassCenterX = pMassCenterX;
				mNextMassCenterY = pMassCenterY;
			} else {
				mHasPrevState = true;
				mCenterX = mNextCenterX = pCenterX;
				mCenterY = mNextCenterY = pCenterY;
				mAngle = mNextAngle = pAngle;
				mMassCenterX = mNextMassCenterX = pMassCenterX;
				mMassCenterY = mNextMassCenterY = pMassCenterY;
			}
		}

		private void offset(float pX, float pY) {
			mCenterX += pX;
			mCenterY += pY;
			mMassCenterX += pX;
			mMassCenterY += pY;
			mNextCenterX += pX;
			mNextCenterY += pY;
			mNextMassCenterX += pX;
			mNextMassCenterY += pY;
		}
	}

}
