package com.gaskarov.teerain;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.FPSLogger;
import com.gaskarov.teerain.tissularity.MainTissularity;
import com.gaskarov.teerain.util.TimeMeasure;
import com.gaskarov.util.common.MathUtils;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class TeeRain extends ApplicationAdapter implements InputProcessor {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private Organularity mOrganularity;
	private MainTissularity mMainTissularity;
	private long mLastTime;
	private float mAccumulatedTime;
	private FPSLogger mFPSLogger;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void create() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Gdx.input.setInputProcessor(this);
		TimeMeasure.sMainThreadId = Thread.currentThread().getId();
		Resources.loadResources();
		mOrganularity = Organularity.obtain();
		mMainTissularity = MainTissularity.obtain();

		synchronized (mOrganularity) {
			mOrganularity.pushTissularity(mMainTissularity);
		}

		mLastTime = System.currentTimeMillis();
		mAccumulatedTime = 0f;

		mFPSLogger = new FPSLogger();
	}

	@Override
	public void dispose() {
		synchronized (mOrganularity) {
			mOrganularity.removeTissularity(mMainTissularity);
			MainTissularity.recycle(mMainTissularity);
			mMainTissularity = null;
			Organularity.recycle(mOrganularity);
			mOrganularity = null;
		}
		Resources.disposeResources();
	}

	@Override
	public void resume() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void render() {
		mFPSLogger.log();
		TimeMeasure.start2();
		TimeMeasure.sM9.start();
		synchronized (mOrganularity) {
			TimeMeasure.sM11.start();
			long curTime = System.currentTimeMillis();
			float val = (curTime - mLastTime - mAccumulatedTime) / Settings.TIME_STEP_MILLIS;
			int n = val < 1.0 ? MathUtils.ceil(val) : MathUtils.floor(val);
			for (int i = 0; i < n; ++i) {
				TimeMeasure.start();
				TimeMeasure.sM1.start();
				mOrganularity.tick();
				TimeMeasure.sM1.end();
				TimeMeasure.end();
				mAccumulatedTime += Settings.TIME_STEP_MILLIS;
				long millis = (long) mAccumulatedTime;
				mAccumulatedTime -= millis;
				mLastTime += millis;
			}
			TimeMeasure.sM11.end();
			TimeMeasure.sM12.start();
			mMainTissularity.render();
			TimeMeasure.sM12.end();
		}
		TimeMeasure.sM9.end();
		TimeMeasure.end2();
		TimeMeasure.log();
	}

	@Override
	public void resize(int pWidth, int pHeight) {
		synchronized (mOrganularity) {
			mMainTissularity.resize(pWidth, pHeight);
		}
	}

	@Override
	public boolean keyDown(int pKeycode) {
		synchronized (mOrganularity) {
			mMainTissularity.keyDown(pKeycode);
		}
		return false;
	}

	@Override
	public boolean keyUp(int pKeycode) {
		synchronized (mOrganularity) {
			mMainTissularity.keyUp(pKeycode);
		}
		return false;
	}

	@Override
	public boolean keyTyped(char pCharacter) {
		synchronized (mOrganularity) {
			mMainTissularity.keyTyped(pCharacter);
		}
		return false;
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		synchronized (mOrganularity) {
			mMainTissularity.touchDown(pScreenX, pScreenY, pPointer, pButton);
		}
		return false;
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		synchronized (mOrganularity) {
			mMainTissularity.touchUp(pScreenX, pScreenY, pPointer, pButton);
		}
		return false;
	}

	@Override
	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		synchronized (mOrganularity) {
			mMainTissularity.touchDragged(pScreenX, pScreenY, pPointer);
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int pScreenX, int pScreenY) {
		synchronized (mOrganularity) {
			mMainTissularity.mouseMoved(pScreenX, pScreenY);
		}
		return false;
	}

	@Override
	public boolean scrolled(int pAmount) {
		synchronized (mOrganularity) {
			mMainTissularity.scrolled(pAmount);
		}
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
