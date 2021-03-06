package com.gaskarov.teerain.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.debug.TimeMeasure;
import com.gaskarov.teerain.game.game.GameTissularity;
import com.gaskarov.teerain.game.game.cell.AirCell;
import com.gaskarov.teerain.game.game.cell.GrenadeGunCell;
import com.gaskarov.teerain.game.game.cell.GroundCell;
import com.gaskarov.teerain.game.game.cell.HammerCell;
import com.gaskarov.teerain.game.game.cell.LampCell;
import com.gaskarov.teerain.game.game.cell.RockCell;
import com.gaskarov.teerain.game.game.cell.TeeCell;
import com.gaskarov.teerain.game.hud.HUDTissularity;
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
	private Controller mController;

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
	public Runnable chunkLoader(int pX, int pY, ChunkHolder pChunkHolder) {
		return null;
	}

	@Override
	public Runnable chunkUnloader(int pX, int pY, ChunkHolder pChunkHolder) {
		return null;
	}

	@Override
	public Runnable regionLoader(int pX, int pY, RegionDataHolder pRegionDataHolder) {
		return null;
	}

	@Override
	public Runnable regionUnloader(int pX, int pY, RegionDataHolder pRegionDataHolder) {
		return null;
	}

	@Override
	public void attach(Organularity pOrganularity) {
		super.attach(pOrganularity);
		pOrganularity.pushTissularity(mHUDTissularity);
		pOrganularity.pushTissularity(mGameTissularity);
		mGameTissularity.pushPlayer();
		mPlayer.setItem(1, HammerCell.obtain());
		mPlayer.setItem(2, RockCell.obtain());
		mPlayer.setItem(3, LampCell.obtain());
		mPlayer.setItem(4, GrenadeGunCell.obtain());
		mPlayer.setItem(5, GroundCell.obtain());
		mPlayer.setItem(6, AirCell.obtain());
		mPlayer.setItem(7, TeeCell.obtain(1));
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
		obj.mController = Controller.obtain(obj.mPlayer);
		obj.mGameTissularity = GameTissularity.obtain(obj.mController);
		obj.mHUDTissularity = HUDTissularity.obtain(obj.mPlayer);
		return obj;
	}

	public static void recycle(MainTissularity pObj) {
		GameTissularity.recycle(pObj.mGameTissularity);
		pObj.mGameTissularity = null;
		HUDTissularity.recycle(pObj.mHUDTissularity);
		pObj.mHUDTissularity = null;
		pObj.dispose();
		Controller.recycle(pObj.mController);
		pObj.mController = null;
		Player.recycle(pObj.mPlayer);
		pObj.mPlayer = null;
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
