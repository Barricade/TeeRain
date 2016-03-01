package com.gaskarov.teerain;

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
import com.gaskarov.teerain.tissularity.Tissularity;
import com.gaskarov.teerain.util.Collidable;
import com.gaskarov.teerain.util.OperationSolver;
import com.gaskarov.teerain.util.TimeMeasure;
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

	// ===========================================================
	// Fields
	// ===========================================================

	private Box2DDebugRenderer mBox2DDebugRenderer;
	private Matrix4 mBox2DDebugCameraMatrix;

	private OrthographicCamera mCamera;
	private SpriteBatch mSpriteBatch;
	private FloatArray[] mRenderBuffers = new FloatArray[Settings.LAYERS];

	private World mWorld;

	private short mGroupIndexPacksSize;
	private IntArray mGroupIndexPacks;

	private OperationSolver mOperationSolver;

	private LinkedHashTable mTissularities;

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

	public FloatArray[] getRenderBuffers() {
		return mRenderBuffers;
	}

	public World getWorld() {
		return mWorld;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void beginContact(Contact pContact) {

		Object dataA = pContact.getFixtureA().getUserData();
		Object dataB = pContact.getFixtureB().getUserData();

		if (dataA instanceof Collidable) {
			((Collidable) dataA).beginContact(pContact, pContact.getFixtureB(), pContact
					.getFixtureA(), false);
		}

		if (dataB instanceof Collidable) {
			((Collidable) dataB).beginContact(pContact, pContact.getFixtureA(), pContact
					.getFixtureB(), true);
		}
	}

	@Override
	public void endContact(Contact pContact) {

		Object dataA = pContact.getFixtureA().getUserData();
		Object dataB = pContact.getFixtureB().getUserData();

		if (dataA instanceof Collidable) {
			((Collidable) dataA).endContact(pContact, pContact.getFixtureB(), pContact
					.getFixtureA(), false);
		}

		if (dataB instanceof Collidable) {
			((Collidable) dataB).endContact(pContact, pContact.getFixtureA(), pContact
					.getFixtureB(), true);
		}
	}

	@Override
	public void preSolve(Contact pContact, Manifold pOldManifold) {

		Object dataA = pContact.getFixtureA().getUserData();
		Object dataB = pContact.getFixtureB().getUserData();

		if (dataA instanceof Collidable) {
			((Collidable) dataA).preSolve(pContact, pOldManifold, pContact.getFixtureB(), pContact
					.getFixtureA(), false);
		}

		if (dataB instanceof Collidable) {
			((Collidable) dataB).preSolve(pContact, pOldManifold, pContact.getFixtureA(), pContact
					.getFixtureB(), true);
		}
	}

	@Override
	public void postSolve(Contact pContact, ContactImpulse pContactImpulse) {

		Object dataA = pContact.getFixtureA().getUserData();
		Object dataB = pContact.getFixtureB().getUserData();

		if (dataA instanceof Collidable) {
			((Collidable) dataA).postSolve(pContact, pContactImpulse, pContact.getFixtureB(),
					pContact.getFixtureA(), false);
		}

		if (dataB instanceof Collidable) {
			((Collidable) dataB).postSolve(pContact, pContactImpulse, pContact.getFixtureA(),
					pContact.getFixtureB(), true);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static Organularity obtain() {
		Organularity obj = new Organularity();
		obj.mBox2DDebugRenderer = new Box2DDebugRenderer(true, true, false, false, false, true);
		obj.mBox2DDebugCameraMatrix = new Matrix4();
		obj.mCamera = new OrthographicCamera();
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "uniform mat4 u_projTrans;\n" //
				+ "varying vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "\n" //
				+ "void main() {\n" //
				+ "  v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "  v_color.a = v_color.a * (255.0/254.0);\n" //
				+ "  v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "  gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
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
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		obj.mSpriteBatch = new SpriteBatch(Settings.SPRITE_BATCH_SIZE, shader);
		for (int i = 0; i < obj.mRenderBuffers.length; ++i)
			obj.mRenderBuffers[i] = FloatArray.obtain();
		obj.mWorld = new World(new Vector2(Settings.GRAVITY_X, Settings.GRAVITY_Y), true);
		obj.mWorld.setContactListener(obj);
		obj.mGroupIndexPacksSize = 1;
		obj.mGroupIndexPacks = IntArray.obtain();
		obj.mOperationSolver = new OperationSolver(Settings.OPERATION_SOLVER_MAX_THREADS);
		obj.mTissularities = LinkedHashTable.obtain();
		return obj;
	}

	public static void recycle(Organularity pObj) {
		while (pObj.mTissularities.size() > 0)
			pObj.removeTissularity((Tissularity) pObj.mTissularities.front());
		LinkedHashTable.recycle(pObj.mTissularities);
		pObj.mTissularities = null;
		pObj.mOperationSolver.dispose();
		pObj.mOperationSolver = null;
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
	}

	public void tick() {
		TimeMeasure.sM8.start();
		mWorld.step(Settings.TIME_STEP, Settings.VELOCITY_ITERATIONS, Settings.POSITION_ITERATIONS);
		TimeMeasure.sM8.end();
	}

	public void pushTissularity(Tissularity pTissularity) {
		mTissularities.set(pTissularity);
		pTissularity.attach(this);
	}

	public void removeTissularity(Tissularity pTissularity) {
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
