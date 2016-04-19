package com.gaskarov.teerain.game;

import static com.gaskarov.util.constants.ArrayConstants.MOVE_AROUND_SIZE;
import static com.gaskarov.util.constants.ArrayConstants.MOVE_AROUND_X;
import static com.gaskarov.util.constants.ArrayConstants.MOVE_AROUND_Y;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gaskarov.teerain.core.Cells;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.Cellularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.core.util.Settings;
import com.gaskarov.teerain.game.game.ControlOrganoid;
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
	private final boolean[] mTmpAISolid = new boolean[MOVE_AROUND_SIZE];
	private final int[] mTmpAIAround = new int[MOVE_AROUND_SIZE];

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
		obj.mBot.setItem(1, Cells.CELL_TYPE_HAMMER, null);
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
		float offset = cellularity.isChunk() ? 0 : Settings.MAX_DROP_HSIZE;
		Tissularity tissularity = cellularity.getTissularity();
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
		pTissularity.setAI(posX, posY, z, 255 | (ai & ~255));
	}

	private void controlBot(Tissularity pTissularity, ControlOrganoid pControlOrganoid) {
		Cellularity cellularity = pControlOrganoid.getCellularity();
		int x = pControlOrganoid.getX();
		int y = pControlOrganoid.getY();
		int z = pControlOrganoid.getZ();
		MetaBody body = cellularity.getBody();
		float c = (float) Math.cos(body.getAngle());
		float s = (float) Math.sin(body.getAngle());
		float offset = cellularity.isChunk() ? 0 : Settings.MAX_DROP_HSIZE;
		Tissularity tissularity = cellularity.getTissularity();
		float posX =
				body.getOffsetX() + body.getPositionX() + (x - offset + 0.5f) * c
						- (y - offset + 0.5f) * s;
		float posY =
				body.getOffsetY() + body.getPositionY() + (x - offset + 0.5f) * s
						+ (y - offset + 0.5f) * c;
		int coordX = tissularity.getOffsetX() + MathUtils.floor(posX);
		int coordY = tissularity.getOffsetY() + MathUtils.floor(posY);

		for (int j = 0; j < MOVE_AROUND_SIZE; ++j) {
			final int tmpX = coordX + MOVE_AROUND_X[j];
			final int tmpY = coordY + MOVE_AROUND_Y[j];
			mTmpAISolid[j] = pTissularity.isSolid(tmpX, tmpY, z);
			mTmpAIAround[j] = pTissularity.getAI(tmpX, tmpY, z);
		}

		int tmpAI = pTissularity.getAI(coordX, coordY, z);
		int tmpAIField = tmpAI & 255;
		int tmpAIVertical = (tmpAI >>> 8) & 15;

		int maxX = 0;
		int maxY = 0;
		int maxA;
		int maxB;
		boolean flag = !pControlOrganoid.isGrounded() && body.getVelocityY() < 0f;
		if (flag) {
			maxA = tmpAIVertical;
			maxB = tmpAIField;
		} else {
			maxA = tmpAIField;
			maxB = tmpAIVertical;
		}
		for (int j = 0; j < MOVE_AROUND_SIZE; ++j) {
			if ((j & 1) == 0 || !mTmpAISolid[j - 1 & 7] && !mTmpAISolid[j + 1 & 7]) {
				int vx = MOVE_AROUND_X[j];
				int vy = MOVE_AROUND_Y[j];
				if (vy == 1 && body.getVelocityY() < 0)
					continue;
				if (vy == 0 && body.getVelocityY() < 0 && posY - coordY < 0.5f)
					continue;
				int ai = mTmpAIAround[j];
				int aiField = ai & 255;
				int aiVertical = (ai >>> 8) & 15;
				int a;
				int b;
				if (flag) {
					a = aiVertical;
					b = aiField;
				} else {
					a = aiField;
					b = aiVertical;
				}
				if (a > maxA || a == maxA && b > maxB) {
					maxA = a;
					maxB = b;
					maxX = vx;
					maxY = vy;
				}
			}
		}

		if (maxX == -1)
			pControlOrganoid.setControlMove(-1);
		else if (maxX == 1)
			pControlOrganoid.setControlMove(1);
		else {
			float dx = 0.5f - (posX - coordX);
			float vx = body.getVelocityX();
			float accel = -0.5f * vx * vx / dx;
			float frictionX =
					posX - coordX + -0.5f * vx * vx / Settings.AIR_FRICTION * (vx > 0 ? -1 : 1);
			if (0.48f < frictionX && frictionX < 0.52f)
				pControlOrganoid.setControlMove(0);
			else if (posX - coordX < 0.5f) {
				if (vx <= 0 || accel > -Settings.AIR_CONTROL_ACCEL)
					pControlOrganoid.setControlMove(1);
				else
					pControlOrganoid.setControlMove(-1);
			} else {
				if (vx >= 0 || accel < Settings.AIR_CONTROL_ACCEL)
					pControlOrganoid.setControlMove(-1);
				else
					pControlOrganoid.setControlMove(1);
			}
		}
		if (maxY == 1
				&& !(maxX == 1 && (posX - coordX < 0.75f || body.getVelocityX() < 0) || maxX == -1
						&& (posX - coordX > 0.25f || body.getVelocityX() > 0)))
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
				cellularity.touchDown(mPlayer.getItem(mPlayer.getUseItem()), mPlayer
						.getItemData(mPlayer.getUseItem()), cellularity.getCell(x, y, z), x, y, z,
						clickX, clickY, controlOrganoid, mPlayer);
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
		mPlayer.setUseItem(MathUtils.mod(useItem - Player.USE_ITEM_MIN_ID, Player.USE_ITEM_MAX_ID
				- Player.USE_ITEM_MIN_ID + 1)
				+ Player.USE_ITEM_MIN_ID);
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
