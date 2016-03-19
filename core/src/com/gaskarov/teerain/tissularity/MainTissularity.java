package com.gaskarov.teerain.tissularity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gaskarov.teerain.Organularity;
import com.gaskarov.teerain.Player;
import com.gaskarov.teerain.Settings;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.organoid.ControlOrganoid;
import com.gaskarov.teerain.util.TimeMeasure;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class MainTissularity extends Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private Player mPlayer;

	private GameTissularity mGameTissularity;
	private HUDTissularity mHUDTissularity;

	// ===========================================================
	// Constructors
	// ===========================================================

	private MainTissularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void control(Cellularity pCellularity, int pX, int pY, int pZ,
			ControlOrganoid pControlOrganoid) {
	}

	@Override
	public Runnable chunkLoader(int pX, int pY, ChunkHolder pChunkHolder) {
		return ChunkLoader.obtain(this, pX, pY, pChunkHolder, true);
	}

	@Override
	public Runnable chunkUnloader(int pX, int pY, ChunkHolder pChunkHolder) {
		return ChunkLoader.obtain(this, pX, pY, pChunkHolder, false);
	}

	@Override
	public void attach(Organularity pOrganularity) {
		super.attach(pOrganularity);
		pOrganularity.pushTissularity(mHUDTissularity);
		pOrganularity.pushTissularity(mGameTissularity);
	}

	@Override
	public void detach() {
		mOrganularity.removeTissularity(mGameTissularity);
		mOrganularity.removeTissularity(mHUDTissularity);
		super.detach();
	}

	@Override
	public void tick() {
		if (mOrganularity == null)
			return;
		super.tick();
		mGameTissularity.tick();
		mHUDTissularity.tick();
	}

	@Override
	public void render(long pTime) {
		if (mOrganularity == null)
			return;
		SpriteBatch spriteBatch = mOrganularity.getSpriteBatch();

		TimeMeasure.sM10.start();
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		TimeMeasure.sM10.end();

		TimeMeasure.sM10.start();
		spriteBatch.begin();
		TimeMeasure.sM10.end();
		OrthographicCamera camera = mOrganularity.getCamera();
		render(Settings.TILE_RENDER, camera.viewportWidth / Settings.TILE_RENDER,
				camera.viewportHeight / Settings.TILE_RENDER, pTime);
		mGameTissularity.render(pTime);
		mHUDTissularity.render(pTime);
		TimeMeasure.sM10.start();
		spriteBatch.end();
		TimeMeasure.sM10.end();
		mGameTissularity.renderDebug();
	}

	@Override
	public void resize(int pWidth, int pHeight) {
		float w = pWidth;
		float h = pHeight;
		float aspectRatio = w / h;

		synchronized (mOrganularity) {
			OrthographicCamera camera = mOrganularity.getCamera();
			camera.viewportWidth = 2f * Math.min(aspectRatio, 1f);
			camera.viewportHeight = 2f / Math.max(aspectRatio, 1f);
			camera.update();

			mOrganularity.getSpriteBatch().setProjectionMatrix(mOrganularity.getCamera().combined);
		}
	}

	@Override
	public boolean keyDown(int pKeycode) {
		return mHUDTissularity.keyDown(pKeycode) && mGameTissularity.keyDown(pKeycode);
	}

	@Override
	public boolean keyUp(int pKeycode) {
		return mHUDTissularity.keyUp(pKeycode) && mGameTissularity.keyUp(pKeycode);
	}

	@Override
	public boolean keyTyped(char pCharacter) {
		return mHUDTissularity.keyTyped(pCharacter) && mGameTissularity.keyTyped(pCharacter);
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		return mHUDTissularity.touchDown(pScreenX, pScreenY, pPointer, pButton)
				&& mGameTissularity.touchDown(pScreenX, pScreenY, pPointer, pButton);
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		return mHUDTissularity.touchUp(pScreenX, pScreenY, pPointer, pButton)
				&& mGameTissularity.touchUp(pScreenX, pScreenY, pPointer, pButton);
	}

	@Override
	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		return mHUDTissularity.touchDragged(pScreenX, pScreenY, pPointer)
				&& mGameTissularity.touchDragged(pScreenX, pScreenY, pPointer);
	}

	@Override
	public boolean mouseMoved(int pScreenX, int pScreenY) {
		return mHUDTissularity.mouseMoved(pScreenX, pScreenY)
				&& mGameTissularity.mouseMoved(pScreenX, pScreenY);
	}

	@Override
	public boolean scrolled(int pAmount) {
		return mHUDTissularity.scrolled(pAmount) && mGameTissularity.scrolled(pAmount);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static MainTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (MainTissularity.class) {
				return sPool.size() == 0 ? new MainTissularity() : (MainTissularity) sPool.pop();
			}
		return new MainTissularity();
	}

	private static void recyclePure(MainTissularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (MainTissularity.class) {
				sPool.push(pObj);
			}
	}

	public static MainTissularity obtain() {
		MainTissularity obj = obtainPure();
		obj.init();
		obj.mPlayer = Player.obtain();
		obj.mGameTissularity = GameTissularity.obtain();
		obj.mHUDTissularity = HUDTissularity.obtain(obj.mPlayer);
		return obj;
	}

	public static void recycle(MainTissularity pObj) {
		GameTissularity.recycle(pObj.mGameTissularity);
		pObj.mGameTissularity = null;
		HUDTissularity.recycle(pObj.mHUDTissularity);
		pObj.mHUDTissularity = null;
		pObj.dispose();
		Player.recycle(pObj.mPlayer);
		pObj.mPlayer = null;
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ChunkLoader implements Runnable {

		private static final Array sPool = Array.obtain();

		private MainTissularity mTissularity;
		private ChunkHolder mChunkHolder;
		private boolean mLoad;

		@Override
		public void run() {
			if (mLoad)
				load();
			else
				unload();
			recycle(this);
		}

		private static ChunkLoader obtainPure() {
			if (GlobalConstants.POOL)
				synchronized (ChunkLoader.class) {
					return sPool.size() == 0 ? new ChunkLoader() : (ChunkLoader) sPool.pop();
				}
			return new ChunkLoader();
		}

		private static void recyclePure(ChunkLoader pObj) {
			if (GlobalConstants.POOL)
				synchronized (ChunkLoader.class) {
					sPool.push(pObj);
				}
		}

		public static ChunkLoader obtain(MainTissularity pTissularity, int pX, int pY,
				ChunkHolder pChunkHolder, boolean pLoad) {
			ChunkLoader obj = obtainPure();
			obj.mTissularity = pTissularity;
			obj.mChunkHolder = pChunkHolder;
			obj.mLoad = pLoad;
			return obj;
		}

		public static void recycle(ChunkLoader pObj) {
			pObj.mTissularity = null;
			pObj.mChunkHolder = null;
			recyclePure(pObj);
		}

		private void load() {

			Cellularity chunk = Cellularity.obtain(BodyType.StaticBody, 0, 0, 0);

			mChunkHolder.setChunk(chunk);

			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

		private void unload() {
			Cellularity chunk = mChunkHolder.getChunk();
			mChunkHolder.setChunk(null);
			Cellularity.recycle(chunk);
			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

	}

}
