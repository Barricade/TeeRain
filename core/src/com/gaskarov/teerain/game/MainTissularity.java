package com.gaskarov.teerain.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Player;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.resource.Resources;
import com.gaskarov.teerain.resource.Settings;
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

	private String mMapName;
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
			mGameTissularity.removePlayer(Settings.PLAYER_ID, mPlayer);
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
			mGameTissularity.setMapName(mMapName);
			mOrganularity.pushTissularity(mGameTissularity);
			mGameTissularity.setPlayer(Settings.PLAYER_ID, mPlayer);
			// mPlayer.setItem(1, Cells.CELL_TYPE_HAMMER, null, 1);
			// mPlayer.setItem(2, Cells.CELL_TYPE_GRENADE_GUN, null, 1);
			// mPlayer.setItem(3, Cells.CELL_TYPE_TEE, TeeCellData.obtain(1),
			// 64);
			// mPlayer.setItem(4, Cells.CELL_TYPE_GRASS, null, 64);
			// mPlayer.setItem(5, Cells.CELL_TYPE_GLASS, null, 64);
			// mPlayer.setItem(7, Cells.CELL_TYPE_WOOD, null, 64);
			// mPlayer.setItem(8, Cells.CELL_TYPE_LEAVES, null, 64);
			// mPlayer.setItem(8, Cells.CELL_TYPE_SAND, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID,
			// Cells.CELL_TYPE_COAL_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 1,
			// Cells.CELL_TYPE_IRON_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 2,
			// Cells.CELL_TYPE_COPPER_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 3,
			// Cells.CELL_TYPE_TIN_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 4,
			// Cells.CELL_TYPE_ELECTRON_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 5,
			// Cells.CELL_TYPE_SILVER_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 6,
			// Cells.CELL_TYPE_GOLD_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 7,
			// Cells.CELL_TYPE_DIAMOND_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 8,
			// Cells.CELL_TYPE_URANIUM_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 9,
			// Cells.CELL_TYPE_GRAVITON_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 10,
			// Cells.CELL_TYPE_SPATIAL_RIFT_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 11,
			// Cells.CELL_TYPE_DARK_ENERGY_BLOCK, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 12,
			// Cells.CELL_TYPE_GROUND, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 13,
			// Cells.CELL_TYPE_STONE, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 14,
			// Cells.CELL_TYPE_OVERSTONE, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 15,
			// Cells.CELL_TYPE_SACREDSTONE, null, 64);
			// mPlayer.setItem(Player.INVENTORY_ITEM_MIN_ID + 16,
			// Cells.CELL_TYPE_FINALSTONE, null, 64);
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

		FloatArray buffer = mOrganularity.getRenderBufferC();

		spriteBatch.begin();
		spriteBatch.draw(Resources.MAIN_TEXTURE, buffer.data(), 0,
				buffer.size());
		spriteBatch.end();

		if (!mInMenu)
			mGameTissularity.renderDebug();
	}

	@Override
	public boolean keyDown(int pKeycode) {
		if (mInMenu)
			return mMenuTissularity.keyDown(pKeycode);
		return mHUDTissularity.keyDown(pKeycode)
				&& mGameTissularity.keyDown(pKeycode);
	}

	@Override
	public boolean keyUp(int pKeycode) {
		if (mInMenu)
			return mMenuTissularity.keyUp(pKeycode);
		return mHUDTissularity.keyUp(pKeycode)
				&& mGameTissularity.keyUp(pKeycode);
	}

	@Override
	public boolean keyTyped(char pCharacter) {
		if (mInMenu)
			return mMenuTissularity.keyTyped(pCharacter);
		return mHUDTissularity.keyTyped(pCharacter)
				&& mGameTissularity.keyTyped(pCharacter);
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer,
			int pButton) {
		if (mInMenu)
			return mMenuTissularity.touchDown(pScreenX, pScreenY, pPointer,
					pButton);
		return mHUDTissularity.touchDown(pScreenX, pScreenY, pPointer, pButton)
				&& mGameTissularity.touchDown(pScreenX, pScreenY, pPointer,
						pButton);
	}

	@Override
	public boolean touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		if (mInMenu)
			return mMenuTissularity.touchUp(pScreenX, pScreenY, pPointer,
					pButton);
		return mHUDTissularity.touchUp(pScreenX, pScreenY, pPointer, pButton)
				&& mGameTissularity.touchUp(pScreenX, pScreenY, pPointer,
						pButton);
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
		return mHUDTissularity.scrolled(pAmount)
				&& mGameTissularity.scrolled(pAmount);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static MainTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (MainTissularity.class) {
				return sPool.size() == 0 ? new MainTissularity()
						: (MainTissularity) sPool.pop();
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
		obj.mPlayer = Player.obtain(Settings.PLAYER_ID, false);
		obj.mController = Controller.obtain();
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

	public void toGame(String pMapName) {
		mToInMenu = false;
		mMapName = pMapName;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
