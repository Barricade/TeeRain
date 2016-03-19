package com.gaskarov.teerain;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.FPSLogger;
import com.gaskarov.teerain.tissularity.MainTissularity;
import com.gaskarov.teerain.util.TimeMeasure;

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
		Resources.loadResources();
		mOrganularity = Organularity.obtain();
		mMainTissularity = MainTissularity.obtain();
		mOrganularity.start(mMainTissularity);

		mFPSLogger = new FPSLogger();
	}

	@Override
	public void dispose() {
		mOrganularity.stop();
		MainTissularity.recycle(mMainTissularity);
		mMainTissularity = null;
		Organularity.recycle(mOrganularity);
		mOrganularity = null;
		Resources.disposeResources();
	}

	@Override
	public void resume() {
	}

	@Override
	public void pause() {
	}

	long timetime;

	@Override
	public void render() {
		if (System.currentTimeMillis() - timetime > 50)
			Gdx.app.log("TAG", "BAD: " + (System.currentTimeMillis() - timetime));
		timetime = System.currentTimeMillis();
		mFPSLogger.log();
		TimeMeasure.start2();
		TimeMeasure.sM12.start();
		mOrganularity.render();
		TimeMeasure.sM12.end();
		TimeMeasure.end2();
		TimeMeasure.log();
	}

	@Override
	public void resize(int pWidth, int pHeight) {
		mOrganularity.resize(pWidth, pHeight);
	}

	@Override
	public boolean keyDown(int pKeycode) {
		mOrganularity.keyDown(pKeycode);
		return true;
	}

	@Override
	public boolean keyUp(int pKeycode) {
		mOrganularity.keyUp(pKeycode);
		return true;
	}

	@Override
	public boolean keyTyped(char pCharacter) {
		mOrganularity.keyTyped(pCharacter);
		return true;
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		mOrganularity.touchDown(pScreenX, pScreenY, pPointer, pButton);
		return true;
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		mOrganularity.touchUp(pScreenX, pScreenY, pPointer, pButton);
		return true;
	}

	@Override
	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		mOrganularity.touchDragged(pScreenX, pScreenY, pPointer);
		return true;
	}

	@Override
	public boolean mouseMoved(int pScreenX, int pScreenY) {
		mOrganularity.mouseMoved(pScreenX, pScreenY);
		return true;
	}

	@Override
	public boolean scrolled(int pAmount) {
		mOrganularity.scrolled(pAmount);
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
