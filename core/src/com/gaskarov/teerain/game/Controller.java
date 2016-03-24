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
import com.gaskarov.teerain.game.game.cell.HammerCell;
import com.gaskarov.util.common.IntVector1;
import com.gaskarov.util.common.KeyValuePair;
import com.gaskarov.util.common.MathUtils;
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

	private final IntVector1 mTmp = IntVector1.obtain(0);
	private final int[][] mTmpA33 = new int[3][3];
	private final int[][] mTmpB33 = new int[3][3];

	private Player mPlayer;
	private Player mBot;

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
		obj.mBot = Player.obtain();
		obj.mBot.setItem(1, HammerCell.obtain());
		obj.mControlMove = 0;
		obj.mJump = false;
		for (int i = 0; i < obj.mPointersControlMove.length; ++i)
			obj.mPointersControlMove[i] = 0;
		for (int i = 0; i < obj.mPointersJump.length; ++i)
			obj.mPointersJump[i] = false;
		return obj;
	}

	public static void recycle(Controller pObj) {
		Player.recycle(pObj.mBot);
		pObj.mBot = null;
		recyclePure(pObj);
	}

	public void tick(Tissularity pTissularity) {
		LinkedHashTable controllables = pTissularity.getControllables();
		{
			KeyValuePair pair = (KeyValuePair) controllables.get(mTmp.set(0));
			if (pair != null) {
				LinkedHashTable tmp = (LinkedHashTable) pair.mB;
				for (List.Node i = tmp.begin(); i != tmp.end(); i = tmp.next(i))
					control(pTissularity, (ControlOrganoid) tmp.val(i));
			}
		}
		{
			KeyValuePair pair = (KeyValuePair) controllables.get(mTmp.set(1));
			if (pair != null) {
				LinkedHashTable tmp = (LinkedHashTable) pair.mB;
				for (List.Node i = tmp.begin(); i != tmp.end(); i = tmp.next(i))
					controlBot(pTissularity, (ControlOrganoid) tmp.val(i));
			}
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

		int posX = pTissularity.getOffsetX() + MathUtils.floor(nextCameraX);
		int posY = pTissularity.getOffsetX() + MathUtils.floor(nextCameraY);
		int ai = pTissularity.getAI(posX, posY, z);
		pTissularity.setAI(posX, posY, z, 255, (ai >>> 8) & 15, ai >>> 12);
	}

	private void controlBot(Tissularity pTissularity, ControlOrganoid pControlOrganoid) {
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
		float posX =
				body.getOffsetX() + body.getPositionX() + (x - offset + 0.5f) * c
						- (y - offset + 0.5f) * s;
		float posY =
				body.getOffsetY() + body.getPositionY() + (x - offset + 0.5f) * s
						+ (y - offset + 0.5f) * c;
		int coordX = tissularity.getOffsetX() + MathUtils.floor(posX);
		int coordY = tissularity.getOffsetY() + MathUtils.floor(posY);

		int maxX = 0;
		int maxY = 0;
		boolean flag = !pControlOrganoid.isGrounded() && body.getVelocityY() < 1.0f;
		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j) {
				int ai = tissularity.getAI(coordX + j, coordY + i, z);
				if (flag) {
					mTmpA33[1 + i][1 + j] =
							i == -1 || i == 0 && posY - coordY > 0.5f ? (ai >>> 8) & 15 : 0;
					mTmpB33[1 + i][1 + j] = ai & 255;
				} else {
					mTmpA33[1 + i][1 + j] = ai & 255;
					mTmpB33[1 + i][1 + j] =
							i == -1 || i == 0 && posY - coordY > 0.5f ? (ai >>> 8) & 15 : 0;
				}
				if (mTmpA33[1 + i][1 + j] > mTmpA33[maxY][maxX]
						|| mTmpA33[1 + i][1 + j] == mTmpA33[maxY][maxX]
						&& mTmpB33[1 + i][1 + j] > mTmpB33[maxY][maxX]) {
					maxX = 1 + j;
					maxY = 1 + i;
				}
			}
		if (maxX == 0)
			pControlOrganoid.setControlMove(-1);
		else if (maxX == 2)
			pControlOrganoid.setControlMove(1);
		else {
			if (posX - coordX < 0.45f)
				pControlOrganoid.setControlMove(1);
			else if (posX - coordX > 0.55f)
				pControlOrganoid.setControlMove(-1);
			else
				pControlOrganoid.setControlMove(0);
		}
		if (maxY == 2
				&& !(maxX == 2 && (posX - coordX < 0.2f || body.getVelocityX() < 0) || maxX == 0
						&& (posX - coordX > 0.8f || body.getVelocityX() > 0)))
			pControlOrganoid.setJump(true);
		else
			pControlOrganoid.setJump(false);

		mBot.control(cellularity, x, y, z, pControlOrganoid);
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
		// touchDragged(pTissularity, pScreenX, pScreenY, pPointer);
	}

	public void touchUp(Tissularity pTissularity, int pScreenX, int pScreenY, int pPointer,
			int pButton) {
		// if (mPointersControlMove[pPointer] == -1)
		// leftUp();
		// if (mPointersControlMove[pPointer] == 1)
		// rightUp();
		// mPointersControlMove[pPointer] = 0;
		// if (mPointersJump[pPointer])
		// jumpUp();
		// mPointersJump[pPointer] = false;
	}

	public void touchDragged(Tissularity pTissularity, int pScreenX, int pScreenY, int pPointer) {
		// if (pScreenX < Gdx.graphics.getWidth() / 3) {
		// if (mPointersControlMove[pPointer] == 1)
		// rightUp();
		// if (mPointersControlMove[pPointer] != -1) {
		// leftDown();
		// mPointersControlMove[pPointer] = -1;
		// }
		// } else if (pScreenX > Gdx.graphics.getWidth() / 3 * 2) {
		// if (mPointersControlMove[pPointer] == -1)
		// leftUp();
		// if (mPointersControlMove[pPointer] != 1) {
		// rightDown();
		// mPointersControlMove[pPointer] = 1;
		// }
		// } else {
		// if (mPointersControlMove[pPointer] == 1)
		// rightUp();
		// else if (mPointersControlMove[pPointer] == -1)
		// leftUp();
		// mPointersControlMove[pPointer] = 0;
		// }
		// if (pScreenY < Gdx.graphics.getHeight() / 2) {
		// if (!mPointersJump[pPointer]) {
		// jumpDown();
		// mPointersJump[pPointer] = true;
		// }
		// } else {
		// if (mPointersJump[pPointer]) {
		// jumpUp();
		// mPointersJump[pPointer] = false;
		// }
		// }
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
