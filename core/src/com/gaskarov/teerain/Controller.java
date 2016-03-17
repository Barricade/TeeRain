package com.gaskarov.teerain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.gaskarov.teerain.cellularity.Cellularity;
import com.gaskarov.teerain.organoid.ControlOrganoid;
import com.gaskarov.teerain.tissularity.Tissularity;
import com.gaskarov.teerain.util.MetaBody;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

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

	public static Controller obtain() {
		Controller obj = obtainPure();
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

	public void control(Cellularity pCellularity, int pX, int pY, int pZ,
			ControlOrganoid pControlOrganoid) {
		pControlOrganoid.setControlMove(move());
		pControlOrganoid.setJump(jump());
		Cellularity cellularity = pCellularity;
		MetaBody body = cellularity.getBody();
		float c = (float) Math.cos(body.getAngle());
		float s = (float) Math.sin(body.getAngle());
		float offset = cellularity.isChunk() ? 0 : Settings.CHUNK_HSIZE;
		Cellularity chunk = cellularity.isChunk() ? cellularity : cellularity.getChunk();
		Tissularity tissularity = chunk.getTissularity();
		float nextCameraX =
				body.getOffsetX() + body.getPositionX() + (pX - offset + 0.5f) * c
						- (pY - offset + 0.5f) * s;
		float nextCameraY =
				body.getOffsetY() + body.getPositionY() + (pX - offset + 0.5f) * s
						+ (pY - offset + 0.5f) * c;
		tissularity.setCameraX(nextCameraX);
		tissularity.setCameraY(nextCameraY);
	}

	public void keyDown(int pKeycode) {
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
		}
	}

	public void keyUp(int pKeycode) {
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

	public void keyTyped(char pCharacter) {
	}

	public void touchDown(int pScreenX, int pScreenY, int pPointer, int pButton) {
		touchDragged(pScreenX, pScreenY, pPointer);
	}

	public void touchUp(int pScreenX, int pScreenY, int pPointer, int pButton) {
		if (mPointersControlMove[pPointer] == -1)
			leftUp();
		if (mPointersControlMove[pPointer] == 1)
			rightUp();
		mPointersControlMove[pPointer] = 0;
		if (mPointersJump[pPointer])
			jumpUp();
		mPointersJump[pPointer] = false;
	}

	public void touchDragged(int pScreenX, int pScreenY, int pPointer) {
		if (pScreenX < Gdx.graphics.getWidth() / 3) {
			if (mPointersControlMove[pPointer] == 1)
				rightUp();
			if (mPointersControlMove[pPointer] != -1) {
				leftDown();
				mPointersControlMove[pPointer] = -1;
			}
		} else if (pScreenX > Gdx.graphics.getWidth() / 3 * 2) {
			if (mPointersControlMove[pPointer] == -1)
				leftUp();
			if (mPointersControlMove[pPointer] != 1) {
				rightDown();
				mPointersControlMove[pPointer] = 1;
			}
		} else {
			if (mPointersControlMove[pPointer] == 1)
				rightUp();
			else if (mPointersControlMove[pPointer] == -1)
				leftUp();
			mPointersControlMove[pPointer] = 0;
		}
		if (pScreenY < Gdx.graphics.getHeight() / 2) {
			if (!mPointersJump[pPointer]) {
				jumpDown();
				mPointersJump[pPointer] = true;
			}
		} else {
			if (mPointersJump[pPointer]) {
				jumpUp();
				mPointersJump[pPointer] = false;
			}
		}
	}

	public void mouseMoved(int pScreenX, int pScreenY) {
	}

	public void scrolled(int pAmount) {
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
