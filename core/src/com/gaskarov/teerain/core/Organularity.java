package com.gaskarov.teerain.core;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.gaskarov.teerain.core.util.Collidable;
import com.gaskarov.teerain.core.util.OperationSolver;
import com.gaskarov.teerain.debug.TimeMeasure;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;
import com.gaskarov.util.container.IntArray;
import com.gaskarov.util.container.LinkedHashTable;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Organularity implements ContactListener {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int COMMAND_KEY_DOWN = 0;
	public static final int COMMAND_KEY_UP = 1;
	public static final int COMMAND_KEY_TYPED = 2;
	public static final int COMMAND_TOUCH_DOWN = 3;
	public static final int COMMAND_TOUCH_UP = 4;
	public static final int COMMAND_TOUCH_DRAGGED = 5;
	public static final int COMMAND_MOUSE_MOVED = 6;
	public static final int COMMAND_SCROLLED = 7;

	// ===========================================================
	// Fields
	// ===========================================================

	private Box2DDebugRenderer mBox2DDebugRenderer;
	private Matrix4 mBox2DDebugCameraMatrix;

	private OrthographicCamera mCamera;
	private SpriteBatch mSpriteBatch;

	private final Object mRenderMonitor = new Object();
	private final FloatArray[] mRenderBuffers = new FloatArray[Settings.LAYERS];
	private FloatArray mRenderBufferA;
	private FloatArray mRenderBufferB;
	private FloatArray mRenderBufferC;
	private boolean mRenderSwapped;

	private World mWorld;

	private short mGroupIndexPacksSize;
	private IntArray mGroupIndexPacks;

	private OperationSolver mOperationSolver;
	private OperationSolver mUpdateOperationSolver;

	private LinkedHashTable mTissularities;
	private Tissularity mMainTissularity;

	private long mUpdateLastTime;
	private float mUpdateAccumulatedTime;

	private Updater mUpdater;

	private final Object mInputMonitor = new Object();
	private IntArray mInputBufferA;
	private IntArray mInputBufferB;

	private final Object mRandomMonitor = new Object();
	private long mRandomSeed;
	private long mRandomNumber;

	// ===========================================================
	// Constructors
	// ===========================================================

	private Organularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public Box2DDebugRenderer getBox2DDebugRenderer() {
		return mBox2DDebugRenderer;
	}

	public Matrix4 getBox2DDebugCameraMatrix() {
		return mBox2DDebugCameraMatrix;
	}

	public OrthographicCamera getCamera() {
		return mCamera;
	}

	public SpriteBatch getSpriteBatch() {
		return mSpriteBatch;
	}

	public Object getRenderMonitor() {
		return mRenderMonitor;
	}

	public FloatArray[] getRenderBuffers() {
		return mRenderBuffers;
	}

	public FloatArray getRenderBufferA() {
		return mRenderBufferA;
	}

	public FloatArray getRenderBufferC() {
		return mRenderBufferC;
	}

	public World getWorld() {
		return mWorld;
	}

	public long getUpdateLastTime() {
		return mUpdateLastTime;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void beginContact(Contact pContact) {

		Collidable dataA = (Collidable) pContact.getFixtureA().getUserData();
		Collidable dataB = (Collidable) pContact.getFixtureB().getUserData();

		dataA.beginContact(pContact, pContact.getFixtureB(),
				pContact.getFixtureA(), false, dataB);

		dataB.beginContact(pContact, pContact.getFixtureA(),
				pContact.getFixtureB(), true, dataA);
	}

	@Override
	public void endContact(Contact pContact) {

		Collidable dataA = (Collidable) pContact.getFixtureA().getUserData();
		Collidable dataB = (Collidable) pContact.getFixtureB().getUserData();

		dataA.endContact(pContact, pContact.getFixtureB(),
				pContact.getFixtureA(), false, dataB);

		dataB.endContact(pContact, pContact.getFixtureA(),
				pContact.getFixtureB(), true, dataA);
	}

	@Override
	public void preSolve(Contact pContact, Manifold pOldManifold) {

		Collidable dataA = (Collidable) pContact.getFixtureA().getUserData();
		Collidable dataB = (Collidable) pContact.getFixtureB().getUserData();

		dataA.preSolve(pContact, pOldManifold, pContact.getFixtureB(),
				pContact.getFixtureA(), false, dataB);

		dataB.preSolve(pContact, pOldManifold, pContact.getFixtureA(),
				pContact.getFixtureB(), true, dataA);
	}

	@Override
	public void postSolve(Contact pContact, ContactImpulse pContactImpulse) {

		Collidable dataA = (Collidable) pContact.getFixtureA().getUserData();
		Collidable dataB = (Collidable) pContact.getFixtureB().getUserData();

		dataA.postSolve(pContact, pContactImpulse, pContact.getFixtureB(),
				pContact.getFixtureA(), false, dataB);

		dataB.postSolve(pContact, pContactImpulse, pContact.getFixtureA(),
				pContact.getFixtureB(), true, dataA);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static Organularity obtain() {
		Organularity obj = new Organularity();
		obj.mBox2DDebugRenderer = new Box2DDebugRenderer(true, true, false,
				false, false, true);
		obj.mBox2DDebugCameraMatrix = new Matrix4();
		obj.mCamera = new OrthographicCamera();
		String vertexShader = "attribute vec4 "
				+ ShaderProgram.POSITION_ATTRIBUTE
				+ ";\n" //
				+ "attribute vec4 "
				+ ShaderProgram.COLOR_ATTRIBUTE
				+ ";\n" //
				+ "attribute vec2 "
				+ ShaderProgram.TEXCOORD_ATTRIBUTE
				+ "0;\n" //
				+ "uniform mat4 u_projTrans;\n" //
				+ "varying vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "\n" //
				+ "void main() {\n" //
				+ "  v_color = "
				+ ShaderProgram.COLOR_ATTRIBUTE
				+ ";\n" //
				+ "  v_color.a = v_color.a * (255.0/254.0);\n" //
				+ "  v_texCoords = "
				+ ShaderProgram.TEXCOORD_ATTRIBUTE
				+ "0;\n" //
				+ "  gl_Position = u_projTrans * "
				+ ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
				+ "#define LOWP lowp\n" //
				+ "precision mediump float;\n" //
				+ "#else\n" //
				+ "#define LOWP \n" //
				+ "#endif\n" //
				+ "varying LOWP vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "uniform sampler2D u_texture;\n" //
				+ "void main() {\n" //
				+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
				+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false)
			throw new IllegalArgumentException("Error compiling shader: "
					+ shader.getLog());
		obj.mSpriteBatch = new SpriteBatch(Settings.SPRITE_BATCH_SIZE, shader);
		for (int i = 0; i < obj.mRenderBuffers.length; ++i)
			obj.mRenderBuffers[i] = FloatArray.obtain();
		obj.mRenderBufferA = FloatArray.obtain();
		obj.mRenderBufferB = FloatArray.obtain();
		obj.mRenderBufferC = FloatArray.obtain();
		obj.mRenderSwapped = false;
		obj.mWorld = new World(new Vector2(Settings.GRAVITY_X,
				Settings.GRAVITY_Y), true);
		obj.mWorld.setContactListener(obj);
		obj.mGroupIndexPacksSize = 1;
		obj.mGroupIndexPacks = IntArray.obtain();
		obj.mOperationSolver = new OperationSolver(
				Settings.OPERATION_SOLVER_MAX_THREADS);
		obj.mUpdateOperationSolver = new OperationSolver(1);
		obj.mTissularities = LinkedHashTable.obtain();
		obj.mMainTissularity = null;
		obj.mInputBufferA = IntArray.obtain();
		obj.mInputBufferB = IntArray.obtain();
		obj.mRandomSeed = NoiseMath.hash(System.currentTimeMillis());
		obj.mRandomNumber = 0;
		return obj;
	}

	public static void recycle(Organularity pObj) {
		LinkedHashTable.recycle(pObj.mTissularities);
		pObj.mTissularities = null;
		pObj.mOperationSolver.dispose();
		pObj.mOperationSolver = null;
		pObj.mUpdateOperationSolver.dispose();
		pObj.mUpdateOperationSolver = null;
		IntArray.recycle(pObj.mGroupIndexPacks);
		pObj.mGroupIndexPacks = null;
		pObj.mWorld.dispose();
		pObj.mWorld = null;
		pObj.mBox2DDebugRenderer.dispose();
		pObj.mBox2DDebugRenderer = null;
		pObj.mBox2DDebugCameraMatrix = null;
		pObj.mCamera = null;
		pObj.mSpriteBatch.dispose();
		pObj.mSpriteBatch = null;
		for (int i = 0; i < pObj.mRenderBuffers.length; ++i) {
			FloatArray.recycle(pObj.mRenderBuffers[i]);
			pObj.mRenderBuffers[i] = null;
		}
		FloatArray.recycle(pObj.mRenderBufferA);
		pObj.mRenderBufferA = null;
		FloatArray.recycle(pObj.mRenderBufferB);
		pObj.mRenderBufferB = null;
		FloatArray.recycle(pObj.mRenderBufferC);
		pObj.mRenderBufferC = null;
		IntArray.recycle(pObj.mInputBufferA);
		pObj.mInputBufferA = null;
		IntArray.recycle(pObj.mInputBufferB);
		pObj.mInputBufferB = null;
	}

	public synchronized void start(Tissularity pMainTissularity) {
		long curTime = System.currentTimeMillis();
		mUpdateLastTime = curTime;
		mUpdateAccumulatedTime = 0f;
		mUpdater = Updater.obtain(this);
		mUpdateOperationSolver.pushOperation(mUpdater);
		mMainTissularity = pMainTissularity;
		pushTissularity(pMainTissularity);
	}

	public synchronized void stop() {
		removeTissularity(mMainTissularity);
		mMainTissularity = null;
		mUpdater.finish();
		mUpdater = null;
		waitUpdater();
	}

	public synchronized void pushTissularity(Tissularity pTissularity) {
		mTissularities.set(pTissularity);
		pTissularity.attach(this);
	}

	public synchronized void removeTissularity(Tissularity pTissularity) {
		pTissularity.detach();
		mTissularities.remove(pTissularity);
	}

	public boolean pushOperation(Runnable pRunnable) {
		return mOperationSolver.pushOperation(pRunnable);
	}

	public boolean removeOperation(Runnable pRunnable) {
		return mOperationSolver.removeOperation(pRunnable);
	}

	public void waitOperations() {
		try {
			while (!mOperationSolver.isEmpty())
				wait(Settings.WAIT_LOOP_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void waitUpdater() {
		try {
			while (!mUpdateOperationSolver.isEmpty())
				wait(Settings.WAIT_LOOP_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean operationsEmpty() {
		return mOperationSolver.isEmpty();
	}

	public short addGroupIndexPack() {
		if (mGroupIndexPacks.size() == 0)
			return mGroupIndexPacksSize++;
		return (short) mGroupIndexPacks.pop();
	}

	public void removeGroupIndex(short pGroupIndexPack) {
		mGroupIndexPacks.push(pGroupIndexPack);
	}

	public void render() {
		synchronized (mRenderMonitor) {
			while (!mRenderSwapped) {
				try {
					mRenderMonitor.wait(Settings.WAIT_LOOP_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			FloatArray tmp = mRenderBufferC;
			mRenderBufferC = mRenderBufferB;
			mRenderBufferB = tmp;
			mRenderSwapped = false;
		}
		TimeMeasure.sM14.start();
		mMainTissularity.render();
		TimeMeasure.sM14.end();
	}

	public synchronized void resize(int pWidth, int pHeight) {
		float w = pWidth;
		float h = pHeight;
		float aspectRatio = w / h;

		mCamera.viewportWidth = 2f * Math.min(aspectRatio, 1f);
		mCamera.viewportHeight = 2f / Math.max(aspectRatio, 1f);
		mCamera.update();

		mSpriteBatch.setProjectionMatrix(mCamera.combined);
	}

	public void keyDown(int pKeycode) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_KEY_DOWN);
			mInputBufferA.push(pKeycode);
		}
	}

	public void keyUp(int pKeycode) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_KEY_UP);
			mInputBufferA.push(pKeycode);
		}
	}

	public void keyTyped(char pCharacter) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_KEY_TYPED);
			mInputBufferA.push(pCharacter);
		}
	}

	public void touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_TOUCH_DOWN);
			mInputBufferA.push(pScreenX);
			mInputBufferA.push(pScreenY);
			mInputBufferA.push(pPointer);
			mInputBufferA.push(pButton);
		}
	}

	public void touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_TOUCH_UP);
			mInputBufferA.push(pScreenX);
			mInputBufferA.push(pScreenY);
			mInputBufferA.push(pPointer);
			mInputBufferA.push(pButton);
		}
	}

	public void touchDragged(int pScreenX, int pScreenY, int pPointer) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_TOUCH_DRAGGED);
			mInputBufferA.push(pScreenX);
			mInputBufferA.push(pScreenY);
			mInputBufferA.push(pPointer);
		}
	}

	public void mouseMoved(int pScreenX, int pScreenY) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_MOUSE_MOVED);
			mInputBufferA.push(pScreenX);
			mInputBufferA.push(pScreenY);
		}
	}

	public void scrolled(int pAmount) {
		synchronized (mInputMonitor) {
			mInputBufferA.push(COMMAND_SCROLLED);
			mInputBufferA.push(pAmount);
		}
	}

	private void tick() {
		TimeMeasure.start();
		TimeMeasure.sM1.start();
		mUpdateAccumulatedTime += Settings.TIME_STEP_MILLIS;
		long millis = (long) mUpdateAccumulatedTime;
		mUpdateAccumulatedTime -= millis;
		mUpdateLastTime += millis;
		synchronized (mInputMonitor) {
			IntArray tmp = mInputBufferA;
			mInputBufferA = mInputBufferB;
			mInputBufferB = tmp;
		}
		int id = 0;
		while (id < mInputBufferB.size()) {
			switch (mInputBufferB.get(id++)) {
			case COMMAND_KEY_DOWN: {
				int keycode = mInputBufferB.get(id++);
				mMainTissularity.keyDown(keycode);
				break;
			}
			case COMMAND_KEY_UP: {
				int keycode = mInputBufferB.get(id++);
				mMainTissularity.keyUp(keycode);
				break;
			}

			case COMMAND_KEY_TYPED: {
				int character = mInputBufferB.get(id++);
				mMainTissularity.keyTyped((char) character);
				break;
			}
			case COMMAND_TOUCH_DOWN: {
				int screenX = mInputBufferB.get(id++);
				int screenY = mInputBufferB.get(id++);
				int pointer = mInputBufferB.get(id++);
				int button = mInputBufferB.get(id++);
				mMainTissularity.touchDown(screenX, screenY, pointer, button);
				break;
			}
			case COMMAND_TOUCH_UP: {
				int screenX = mInputBufferB.get(id++);
				int screenY = mInputBufferB.get(id++);
				int pointer = mInputBufferB.get(id++);
				int button = mInputBufferB.get(id++);
				mMainTissularity.touchUp(screenX, screenY, pointer, button);
				break;
			}
			case COMMAND_TOUCH_DRAGGED: {
				int screenX = mInputBufferB.get(id++);
				int screenY = mInputBufferB.get(id++);
				int pointer = mInputBufferB.get(id++);
				mMainTissularity.touchDragged(screenX, screenY, pointer);
				break;
			}
			case COMMAND_MOUSE_MOVED: {
				int screenX = mInputBufferB.get(id++);
				int screenY = mInputBufferB.get(id++);
				mMainTissularity.mouseMoved(screenX, screenY);
				break;
			}
			case COMMAND_SCROLLED: {
				int amount = mInputBufferB.get(id++);
				mMainTissularity.scrolled(amount);
				break;
			}
			default:
				break;
			}
		}
		mInputBufferB.clear();
		TimeMeasure.sM2.start();
		mWorld.step(Settings.TIME_STEP, Settings.VELOCITY_ITERATIONS,
				Settings.POSITION_ITERATIONS);
		TimeMeasure.sM2.end();
		TimeMeasure.sM3.start();
		mMainTissularity.tick();
		TimeMeasure.sM3.end();
		synchronized (mRenderMonitor) {
			FloatArray tmp = mRenderBufferA;
			mRenderBufferA = mRenderBufferB;
			mRenderBufferB = tmp;
			mRenderBufferA.clear();
			mRenderSwapped = true;
		}
		TimeMeasure.sM1.end();
		TimeMeasure.end();
	}

	public long random() {
		synchronized (mRandomMonitor) {
			return NoiseMath.combine(mRandomSeed, mRandomNumber++);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class Updater implements Runnable {

		private static final Array sPool = Array.obtain();

		private Organularity mOrganularity;
		private boolean mIsAlive;

		@Override
		public void run() {
			synchronized (mOrganularity) {
				while (true) {
					if (!mIsAlive) {
						recycle(this);
						return;
					}
					long time = -MathUtils
							.floorToLong((System.currentTimeMillis()
									- mOrganularity.mUpdateLastTime
									- Settings.TIME_STEP_MILLIS - mOrganularity.mUpdateAccumulatedTime));
					if (time <= 0)
						break;
					try {
						mOrganularity.wait(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				mOrganularity.tick();
				mOrganularity.mUpdateOperationSolver.pushOperation(this);
			}
		}

		private static Updater obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (Updater.class) {
					return sPool.size() == 0 ? new Updater() : (Updater) sPool
							.pop();
				}
			return new Updater();
		}

		private static void recyclePure(Updater pObj) {
			if (GlobalConstants.POOL)
				synchronized (Updater.class) {
					sPool.push(pObj);
				}
		}

		public static Updater obtain(Organularity pOrganularity) {
			Updater obj = obtainPure();
			obj.mOrganularity = pOrganularity;
			obj.mIsAlive = true;
			return obj;
		}

		public static void recycle(Updater pObj) {
			pObj.mOrganularity = null;
			recyclePure(pObj);
		}

		public void finish() {
			synchronized (mOrganularity) {
				mIsAlive = false;
			}
		}

	}

}
