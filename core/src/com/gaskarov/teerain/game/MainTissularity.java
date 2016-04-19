package com.gaskarov.teerain.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.Resources;
import com.gaskarov.teerain.game.game.GameTissularity;
import com.gaskarov.teerain.game.game.TeeCellData;
import com.gaskarov.teerain.game.hud.BackgroundTissularity;
import com.gaskarov.teerain.game.hud.HUDTissularity;
import com.gaskarov.teerain.game.hud.MenuTissularity;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.FloatArray;

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

	private boolean mToInMenu;
	private boolean mInMenu;
	private BackgroundTissularity mBackgroundTissularity;
	private MenuTissularity mMenuTissularity;
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
	public void attach(Organularity pOrganularity) {
		super.attach(pOrganularity);
		mToInMenu = true;
		mInMenu = true;
		pOrganularity.pushTissularity(mBackgroundTissularity);
		pOrganularity.pushTissularity(mMenuTissularity);
	}

	@Override
	public void detach() {
		if (mInMenu) {
			mOrganularity.removeTissularity(mBackgroundTissularity);
			mOrganularity.removeTissularity(mMenuTissularity);
		} else {
			mOrganularity.removeTissularity(mGameTissularity);
			mOrganularity.removeTissularity(mHUDTissularity);
		}
		super.detach();
	}

	@Override
	public void tick() {
		if (mOrganularity == null)
			return;
		if (mInMenu && !mToInMenu) {
			mInMenu = false;
			mOrganularity.removeTissularity(mBackgroundTissularity);
			mOrganularity.removeTissularity(mMenuTissularity);
			mOrganularity.pushTissularity(mHUDTissularity);
			mOrganularity.pushTissularity(mGameTissularity);
			mGameTissularity.pushPlayer();
			mPlayer.setItem(1, Cells.CELL_TYPE_HAMMER, null);
			mPlayer.setItem(2, Cells.CELL_TYPE_ROCK, null);
			mPlayer.setItem(3, Cells.CELL_TYPE_LAMP, null);
			mPlayer.setItem(4, Cells.CELL_TYPE_GRENADE_GUN, null);
			mPlayer.setItem(5, Cells.CELL_TYPE_GROUND, null);
			mPlayer.setItem(6, Cells.CELL_TYPE_AIR, null);
			mPlayer.setItem(7, Cells.CELL_TYPE_TEE, TeeCellData.obtain(1));
		}
		super.tick();
		if (mInMenu) {
			mBackgroundTissularity.tick();
			mMenuTissularity.tick();
		} else {
			mGameTissularity.tick();
			mHUDTissularity.tick();
		}
	}

	@Override
	public void render() {
		if (mOrganularity == null)
			return;
		SpriteBatch spriteBatch = mOrganularity.getSpriteBatch();

		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		FloatArray buffer = mOrganularity.getRenderBufferC();

		spriteBatch.begin();
		spriteBatch.draw(Resources.MAIN_TEXTURE, buffer.data(), 0, buffer.size());
		spriteBatch.end();

		if (!mInMenu)
			mGameTissularity.renderDebug();
	}

	@Override
	public boolean keyDown(int pKeycode) {
		if (mInMenu)
			return mMenuTissularity.keyDown(pKeycode);
		return mHUDTissularity.keyDown(pKeycode) && mGameTissularity.keyDown(pKeycode);
	}

	@Override
	public boolean keyUp(int pKeycode) {
		if (mInMenu)
			return mMenuTissularity.keyUp(pKeycode);
		return mHUDTissularity.keyUp(pKeycode) && mGameTissularity.keyUp(pKeycode);
	}

	@Override
	public boolean keyTyped(char pCharacter) {
		if (mInMenu)
			return mMenuTissularity.keyDown(pCharacter);
		return mHUDTissularity.keyTyped(pCharacter) && mGameTissularity.keyTyped(pCharacter);
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		if (mInMenu)
			return mMenuTissularity.touchDown(pScreenX, pScreenY, pPointer, pButton);
		return mHUDTissularity.touchDown(pScreenX, pScreenY, pPointer, pButton)
				&& mGameTissularity.touchDown(pScreenX, pScreenY, pPointer, pButton);
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		if (mInMenu)
			return mMenuTissularity.touchUp(pScreenX, pScreenY, pPointer, pButton);
		return mHUDTissularity.touchUp(pScreenX, pScreenY, pPointer, pButton)
				&& mGameTissularity.touchUp(pScreenX, pScreenY, pPointer, pButton);
	}

	@Override
	public boolean touchDragged(int pScreenX, int pScreenY, int pPointer) {
		if (mInMenu)
			return mMenuTissularity.touchDragged(pScreenX, pScreenY, pPointer);
		return mHUDTissularity.touchDragged(pScreenX, pScreenY, pPointer)
				&& mGameTissularity.touchDragged(pScreenX, pScreenY, pPointer);
	}

	@Override
	public boolean mouseMoved(int pScreenX, int pScreenY) {
		if (mInMenu)
			return mMenuTissularity.mouseMoved(pScreenX, pScreenY);
		return mHUDTissularity.mouseMoved(pScreenX, pScreenY)
				&& mGameTissularity.mouseMoved(pScreenX, pScreenY);
	}

	@Override
	public boolean scrolled(int pAmount) {
		if (mInMenu)
			return mMenuTissularity.scrolled(pAmount);
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
		obj.mBackgroundTissularity = BackgroundTissularity.obtain();
		obj.mMenuTissularity = MenuTissularity.obtain(obj);
		obj.mGameTissularity = GameTissularity.obtain(obj.mController);
		obj.mHUDTissularity = HUDTissularity.obtain(obj.mPlayer);
		return obj;
	}

	public static void recycle(MainTissularity pObj) {
		BackgroundTissularity.recycle(pObj.mBackgroundTissularity);
		pObj.mBackgroundTissularity = null;
		MenuTissularity.recycle(pObj.mMenuTissularity);
		pObj.mMenuTissularity = null;
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

	public void inMenu(boolean pInMenu) {
		mToInMenu = pInMenu;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
