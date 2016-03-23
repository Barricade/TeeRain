package com.gaskarov.teerain.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gaskarov.teerain.core.Cellularity;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.game.ControlOrganoid;
import com.gaskarov.util.common.IntVector1;
import com.gaskarov.util.common.KeyValuePair;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.List;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Controller {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private IntVector1 mTmp = IntVector1.obtain(0);

	private Player mPlayer;

	private int mControlMove;
	private boolean mJump;

	private int[] mPointersControlMove = new int[64];
	private boolean[] mPointersJump = new boolean[64];

	// ===========================================================
	// Constructors
	// ===========================================================

	private Controller() {
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

	private static Controller obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (Controller.class) {
				return sPool.size() == 0 ? new Controller() : (Controller) sPool.pop();
			}
		return new Controller();
	}

	private static void recyclePure(Controller pObj) {
		if (GlobalConstants.POOL)
			synchronized (Controller.class) {
				sPool.push(pObj);
			}
	}

	public static Controller obtain(Player pPlayer) {
		Controller obj = obtainPure();
		obj.mPlayer = pPlayer;
		obj.mControlMove = 0;
		obj.mJump = false;
		for (int i = 0; i < obj.mPointersControlMove.length; ++i)
			obj.mPointersControlMove[i] = 0;
		for (int i = 0; i < obj.mPointersJump.length; ++i)
			obj.mPointersJump[i] = false;
		return obj;
	}

	public static void recycle(Controller pObj) {
		recyclePure(pObj);
	}

	public void tick(Tissularity pTissularity) {
		LinkedHashTable controllables = pTissularity.getControllables();
		KeyValuePair pair = (KeyValuePair) controllables.get(mTmp.set(0));
		if (pair != null) {
			LinkedHashTable tmp = (LinkedHashTable) pair.mB;
			for (List.Node i = tmp.begin(); i != tmp.end(); i = tmp.next(i))
				control(pTissularity, (ControlOrganoid) tmp.val(i));
		}
	}

	private void control(Tissularity pTissularity, ControlOrganoid pControlOrganoid) {
		pControlOrganoid.setControlMove(move());
		pControlOrganoid.setJump(jump());
		Cellularity cellularity = pControlOrganoid.getCellularity();
		int x = pControlOrganoid.getX();
		int y = pControlOrganoid.getY();
		int z = pControlOrganoid.getZ();
		MetaBody body = cellularity.getBody();
		float c = (float) Math.cos(body.getAngle());
		float s = (float) Math.sin(body.getAngle());
		float offset = cellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
		Cellularity chunk = cellularity.isChunk() ? cellularity : cellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		float nextCameraX =
				body.getOffsetX() + body.getPositionX() + (x - offset + 0.5f) * c
						- (y - offset + 0.5f) * s;
		float nextCameraY =
				body.getOffsetY() + body.getPositionY() + (x - offset + 0.5f) * s
						+ (y - offset + 0.5f) * c;
		tissularity.setCameraX(nextCameraX);
		tissularity.setCameraY(nextCameraY);
		mPlayer.control(cellularity, x, y, z, pControlOrganoid);
	}

	public void keyDown(Tissularity pTissularity, int pKeycode) {
		switch (pKeycode) {
		case Keys.A:
			leftDown();
			break;
		case Keys.D:
			rightDown();
			break;
		case Keys.SPACE:
			jumpDown();
			break;
		case Keys.NUM_1:
			mPlayer.setUseItem(1);
			break;
		case Keys.NUM_2:
			mPlayer.setUseItem(2);
			break;
		case Keys.NUM_3:
			mPlayer.setUseItem(3);
			break;
		case Keys.NUM_4:
			mPlayer.setUseItem(4);
			break;
		case Keys.NUM_5:
			mPlayer.setUseItem(5);
			break;
		case Keys.NUM_6:
			mPlayer.setUseItem(6);
			break;
		case Keys.NUM_7:
			mPlayer.setUseItem(7);
			break;
		case Keys.NUM_8:
			mPlayer.setUseItem(8);
			break;
		}
	}

	public void keyUp(Tissularity pTissularity, int pKeycode) {
		switch (pKeycode) {
		case Keys.A:
			leftUp();
			break;
		case Keys.D:
			rightUp();
			break;
		case Keys.SPACE:
			jumpUp();
			break;
		}
	}

	public void keyTyped(Tissularity pTissularity, char pCharacter) {
	}

	public void touchDown(Tissularity pTissularity, int pScreenX, int pScreenY, int pPointer,
			int pButton) {
		LinkedHashTable controllables = pTissularity.getControllables();
		KeyValuePair pair = (KeyValuePair) controllables.get(mTmp.set(0));
		if (pair != null) {
			LinkedHashTable tmp = (LinkedHashTable) pair.mB;
			for (List.Node i = tmp.begin(); i != tmp.end(); i = tmp.next(i)) {
				ControlOrganoid controlOrganoid = (ControlOrganoid) tmp.val(i);
				float clickX = screenToWorldX(pTissularity, pScreenX);
				float clickY = screenToWorldY(pTissularity, pScreenY);
				int x = controlOrganoid.getX();
				int y = controlOrganoid.getY();
				int z = controlOrganoid.getZ();
				Cellularity cellularity = controlOrganoid.getCellularity();
				mPlayer.getItem(mPlayer.getUseItem()).touchDown(cellularity, x, y, z, clickX,
						clickY, controlOrganoid, mPlayer, mPlayer.getUseItem());
			}
		}
	}

	public void touchUp(Tissularity pTissularity, int pScreenX, int pScreenY, int pPointer,
			int pButton) {
	}

	public void touchDragged(Tissularity pTissularity, int pScreenX, int pScreenY, int pPointer) {
	}

	public void mouseMoved(Tissularity pTissularity, int pScreenX, int pScreenY) {
	}

	public void scrolled(Tissularity pTissularity, int pAmount) {
		int useItem = mPlayer.getUseItem() + pAmount;
		if (useItem < Player.USE_ITEM_MIN_ID)
			useItem = Player.USE_ITEM_MAX_ID;
		else if (useItem > Player.USE_ITEM_MAX_ID)
			useItem = Player.USE_ITEM_MIN_ID;
		mPlayer.setUseItem(useItem);
	}

	private static float screenToWorldX(Tissularity pTissularity, int pScreenX) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraX() + (pScreenX - Gdx.graphics.getWidth() * 0.5f)
				/ Gdx.graphics.getWidth() / Settings.TILE_RENDER * camera.viewportWidth;
	}

	private static float screenToWorldY(Tissularity pTissularity, int pScreenY) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraY() - (pScreenY - Gdx.graphics.getHeight() * 0.5f)
				/ Gdx.graphics.getHeight() / Settings.TILE_RENDER * camera.viewportHeight;
	}

	private void leftDown() {
		--mControlMove;
	}

	private void leftUp() {
		++mControlMove;
	}

	private void rightDown() {
		++mControlMove;
	}

	private void rightUp() {
		--mControlMove;
	}

	private void jumpDown() {
		mJump = true;
	}

	private void jumpUp() {
		mJump = false;
	}

	private int move() {
		return mControlMove;
	}

	private boolean jump() {
		boolean flag = mJump;
		mJump = false;
		return flag;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
