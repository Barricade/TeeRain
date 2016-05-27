package com.gaskarov.teerain.game;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gaskarov.teerain.core.Organularity;
import com.gaskarov.teerain.core.Tissularity;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.resource.Cells;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.util.common.MathUtils;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class MenuTissularity extends Tissularity {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_CENTER_X = 64;
	private static final int CAMERA_CENTER_Y = 64;

	private static final String[] MENU = new String[] {
			"111011011011000100101001", //
			"010010010010101010101101", //
			"010011011011001110101011", //
			"010010010010101010101001", //
			"010011011010101010101001", //
			"000000000000000000000000", //
			"000000000000000000000000", //
			"000000022222222220000000", //
			"000000000000000000000000", //
			"000000033333333330000000", //
			"000000000000000000000000", //
			"000000044444444440000000" //
	};

	private static final String[] MENU_NEW_GAME = new String[] {
			"111011011011000100101001", //
			"010010010010101010101101", //
			"010011011011001110101011", //
			"010010010010101010101001", //
			"010011011010101010101001", //
			"000000000000000000000000", //
			"000000000000000000000000", //
			"666666660777777777777777", //
			"000000000000000000000000", //
			"000000088888888880000000", //
			"000000000000000000000000", //
			"000000055555555550000000" //
	};

	private static final String[] MENU_LOAD = new String[] {
			"111011011011000100101001", //
			"010010010010101010101101", //
			"010011011011001110101011", //
			"010010010010101010101001", //
			"010011011010101010101001", //
			"000000000000000000000000", //
			"000000000000000000000000", //
			"000009999999999999900000", //
			"000000000000000000000000", //
			"00000aaaa0cccc0bbbb00000", //
			"000000000000000000000000", //
			"000000055555555550000000" //
	};

	private static final String PLAY_STR = " new game ";
	private static final String LOAD_STR = "   load   ";
	private static final String EXIT_STR = "   exit   ";
	private static final String MAP_NAME_STR = "map name";
	private static final String BACK_STR = "   back   ";
	private static final String CREATE_STR = "  create  ";
	private static final String EXISTS_STR = "  exists  ";
	private static final String EMPTY_STR = "  empty   ";
	private static final String PREV_STR = "prev";
	private static final String NEXT_STR = "next";
	private static final String PLAY2_STR = "play";

	private static final int INPUT_LENGTH = 15;

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private MainTissularity mMainTissularity;

	private int mFileId;
	private final int[] mInput = new int[INPUT_LENGTH];
	private int mInputPosition;
	private boolean mInNewGame;

	// ===========================================================
	// Constructors
	// ===========================================================

	private MenuTissularity() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

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
		addVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
		mCameraX = CAMERA_CENTER_X - mOffsetX;
		mCameraY = CAMERA_CENTER_Y - mOffsetY;
		waitChunks();
		mFileId = 0;
		clearInput();
		mInNewGame = false;
	}

	@Override
	public void detach() {
		removeVisitor(CAMERA_CENTER_X, CAMERA_CENTER_Y, 1, 1);
		super.detach();
	}

	@Override
	public float getTileRender() {
		return Settings.TILE_RENDER_BACKGROUND;
	}

	@Override
	public boolean keyDown(int pKeycode) {
		if (mInNewGame) {
			if (pKeycode == Keys.BACKSPACE) {
				if (mInputPosition > 0) {
					mInput[--mInputPosition] = Cells.charToSymbol(' ');
					setMenu(getChunk(0, 0), MENU_NEW_GAME, mInput, isExists());
				}
			} else {
				char c = keycodeToChar(pKeycode);
				if ('0' <= c && c <= '9' || 'a' <= c && c <= 'z') {
					if (mInputPosition < INPUT_LENGTH) {
						mInput[mInputPosition++] = Cells.charToSymbol(c);
						setMenu(getChunk(0, 0), MENU_NEW_GAME, mInput,
								isExists());
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean touchDown(int pScreenX, int pScreenY, int pPointer,
			int pButton) {
		float clickX = screenToWorldX(this, pScreenX);
		float clickY = screenToWorldY(this, pScreenY);
		int x = mOffsetX + MathUtils.floor(clickX);
		int y = mOffsetY + MathUtils.floor(clickY);
		ChunkCellularity chunk = getChunk(x >> Settings.CHUNK_SIZE_LOG,
				y >> Settings.CHUNK_SIZE_LOG);
		if (chunk != null) {
			int cell = chunk.getCell(x & Settings.CHUNK_SIZE_MASK, y
					& Settings.CHUNK_SIZE_MASK, 0);
			int cellType = cell & Cells.CELL_TYPES_MAX_MASK;
			int cellParam = cell >>> Cells.CELL_TYPES_MAX_LOG;
			if (cellType == Cells.CELL_TYPE_UI) {
				int command = (cellParam >>> 8) & 255;
				switch (command) {
				case 1:
					mInNewGame = true;
					setMenu(getChunk(0, 0), MENU_NEW_GAME, mInput, false);
					break;
				case 2:
					Gdx.app.exit();
					break;
				case 3: {
					clearInput();
					Gdx.files.local("maps").file().mkdir();
					String[] list = Gdx.files.local("maps").file().list();
					Arrays.sort(list);
					mFileId = 0;
					if (list.length != 0)
						setInput(list[mFileId]);
					setMenu(getChunk(0, 0), MENU_LOAD, mInput, false);
					break;
				}
				case 4:
					clearInput();
					setMenu(getChunk(0, 0), MENU, mInput, false);
					break;
				case 5:
					break;
				case 6: {
					Gdx.files.local("maps").file().mkdir();
					String mapName = inputToString();
					if (mapName.length() > 0 && !isExists()) {
						Gdx.files.local("maps/" + mapName + "/players").file()
								.mkdirs();
						mMainTissularity.toGame(mapName);
					}
					break;
				}
				case 9: {
					Gdx.files.local("maps").file().mkdir();
					String mapName = inputToString();
					if (mapName.length() > 0 && isExists()) {
						Gdx.files.local("maps/" + mapName + "/players").file()
								.mkdirs();
						mMainTissularity.toGame(mapName);
					}
					break;
				}
				case 7: {
					Gdx.files.local("maps").file().mkdir();
					String[] list = Gdx.files.local("maps").file().list();
					Arrays.sort(list);
					--mFileId;
					if (mFileId < 0)
						mFileId = 0;
					if (mFileId >= list.length)
						mFileId = list.length - 1;
					if (list.length != 0) {
						setInput(list[mFileId]);
						setMenu(getChunk(0, 0), MENU_LOAD, mInput, false);
					}
					break;
				}
				case 8: {
					Gdx.files.local("maps").file().mkdir();
					String[] list = Gdx.files.local("maps").file().list();
					Arrays.sort(list);
					++mFileId;
					if (mFileId < 0)
						mFileId = 0;
					if (mFileId >= list.length)
						mFileId = list.length - 1;
					if (list.length != 0) {
						setInput(list[mFileId]);
						setMenu(getChunk(0, 0), MENU_LOAD, mInput, false);
					}
					break;
				}
				default:
					break;
				}
			}
		}
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static MenuTissularity obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (MenuTissularity.class) {
				return sPool.size() == 0 ? new MenuTissularity()
						: (MenuTissularity) sPool.pop();
			}
		return new MenuTissularity();
	}

	private static void recyclePure(MenuTissularity pObj) {
		if (GlobalConstants.POOL)
			synchronized (MenuTissularity.class) {
				sPool.push(pObj);
			}
	}

	public static MenuTissularity obtain(MainTissularity pMainTissularity) {
		MenuTissularity obj = obtainPure();
		obj.init();
		obj.mMainTissularity = pMainTissularity;
		return obj;
	}

	public static void recycle(MenuTissularity pObj) {
		pObj.mMainTissularity = null;
		pObj.dispose();
		recyclePure(pObj);
	}

	private void clearInput() {
		for (int i = 0; i < mInput.length; ++i)
			mInput[i] = Cells.charToSymbol(' ');
		mInputPosition = 0;
	}

	private void setInput(String pS) {
		clearInput();
		while (mInputPosition < pS.length() && mInputPosition < INPUT_LENGTH) {
			mInput[mInputPosition] = Cells.charToSymbol(pS
					.charAt(mInputPosition));
			++mInputPosition;
		}
	}

	private String inputToString() {
		String mapName = "";
		for (int i = 0; i < mInput.length
				&& Cells.symbolToChar(mInput[i]) != ' '; ++i)
			mapName += Cells.symbolToChar(mInput[i]);
		return mapName;
	}

	private boolean isExists() {
		String mapName = inputToString();
		if (mapName.length() == 0)
			return false;
		return Gdx.files.local("maps/" + mapName).file().exists();
	}

	private static void setMenu(ChunkCellularity pChunk, String[] pMenu,
			int[] pInput, boolean pIsExists) {
		int playStr = 0;
		int loadStr = 0;
		int exitStr = 0;
		int backStr = 0;
		int mapNameStr = 0;
		int createStr = 0;
		int inputStr = 0;
		int prevStr = 0;
		int nextStr = 0;
		int play2Str = 0;
		for (int i = 0; i < pMenu.length; ++i)
			for (int j = 0; j < pMenu[i].length(); ++j) {
				int x = CAMERA_CENTER_X + j - 12;
				int y = CAMERA_CENTER_Y - (i - 5);
				int cell;
				switch (pMenu[i].charAt(j)) {
				case '0':
					cell = Cells.CELL_TYPE_VOID;
					break;
				case '1':
					cell = Cells.CELL_TYPE_GROUND;
					break;
				case '2':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(PLAY_STR.charAt(playStr++)) | (1 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case '3':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(LOAD_STR.charAt(loadStr++)) | (3 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case '4':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(EXIT_STR.charAt(exitStr++)) | (2 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case '5':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(BACK_STR.charAt(backStr++)) | (4 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case '6':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(MAP_NAME_STR
									.charAt(mapNameStr++)) | (0 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case '7':
					cell = Cells.CELL_TYPE_UI
							| ((((pInput != null ? pInput[inputStr++] : Cells
									.charToSymbol(' ')) | (0 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case '8':
					String text = Cells.symbolToChar(pInput[0]) == ' ' ? EMPTY_STR
							: pIsExists ? EXISTS_STR : CREATE_STR;
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(text.charAt(createStr++)) | (6 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case '9':
					if (Cells.symbolToChar(pInput[0]) == ' ')
						cell = Cells.CELL_TYPE_VOID;
					else
						cell = Cells.CELL_TYPE_UI
								| ((((pInput != null ? pInput[inputStr++]
										: Cells.charToSymbol(' ')) | (0 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case 'a':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(PREV_STR.charAt(prevStr++)) | (7 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case 'b':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(NEXT_STR.charAt(nextStr++)) | (8 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				case 'c':
					cell = Cells.CELL_TYPE_UI
							| (((Cells.charToSymbol(PLAY2_STR
									.charAt(play2Str++)) | (9 << 8)) << Cells.CELL_TYPES_MAX_LOG));
					break;
				default:
					cell = Cells.CELL_TYPE_VOID;
					break;
				}
				pChunk.setCell(x, y, 0, cell, null);
			}
	}

	private static float screenToWorldX(Tissularity pTissularity, int pScreenX) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraX()
				+ (pScreenX - Gdx.graphics.getWidth() * 0.5f)
				/ Gdx.graphics.getWidth() / pTissularity.getTileRender()
				* camera.viewportWidth;
	}

	private static float screenToWorldY(Tissularity pTissularity, int pScreenY) {
		Organularity organularity = pTissularity.getOrganularity();
		OrthographicCamera camera = organularity.getCamera();
		return pTissularity.getCameraY()
				- (pScreenY - Gdx.graphics.getHeight() * 0.5f)
				/ Gdx.graphics.getHeight() / pTissularity.getTileRender()
				* camera.viewportHeight;
	}

	public static char keycodeToChar(int pKeycode) {
		switch (pKeycode) {
		case Keys.NUM_0:
			return '0';
		case Keys.NUM_1:
			return '1';
		case Keys.NUM_2:
			return '2';
		case Keys.NUM_3:
			return '3';
		case Keys.NUM_4:
			return '4';
		case Keys.NUM_5:
			return '5';
		case Keys.NUM_6:
			return '6';
		case Keys.NUM_7:
			return '7';
		case Keys.NUM_8:
			return '8';
		case Keys.NUM_9:
			return '9';
		case Keys.A:
			return 'a';
		case Keys.B:
			return 'b';
		case Keys.C:
			return 'c';
		case Keys.D:
			return 'd';
		case Keys.E:
			return 'e';
		case Keys.F:
			return 'f';
		case Keys.G:
			return 'g';
		case Keys.H:
			return 'h';
		case Keys.I:
			return 'i';
		case Keys.J:
			return 'j';
		case Keys.K:
			return 'k';
		case Keys.L:
			return 'l';
		case Keys.M:
			return 'm';
		case Keys.N:
			return 'n';
		case Keys.O:
			return 'o';
		case Keys.P:
			return 'p';
		case Keys.Q:
			return 'q';
		case Keys.R:
			return 'r';
		case Keys.S:
			return 's';
		case Keys.T:
			return 't';
		case Keys.U:
			return 'u';
		case Keys.V:
			return 'v';
		case Keys.W:
			return 'w';
		case Keys.X:
			return 'x';
		case Keys.Y:
			return 'y';
		case Keys.Z:
			return 'z';
		default:
			return '\0';
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ChunkLoader implements Runnable {

		private static final Array sPool = Array.obtain();

		private MenuTissularity mTissularity;
		private int mX;
		private int mY;
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
					return sPool.size() == 0 ? new ChunkLoader()
							: (ChunkLoader) sPool.pop();
				}
			return new ChunkLoader();
		}

		private static void recyclePure(ChunkLoader pObj) {
			if (GlobalConstants.POOL)
				synchronized (ChunkLoader.class) {
					sPool.push(pObj);
				}
		}

		public static ChunkLoader obtain(MenuTissularity pTissularity, int pX,
				int pY, ChunkHolder pChunkHolder, boolean pLoad) {
			ChunkLoader obj = obtainPure();
			obj.mTissularity = pTissularity;
			obj.mX = pX;
			obj.mY = pY;
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

			ChunkCellularity chunk = ChunkCellularity
					.obtain(mTissularity.mOrganularity.random());
			chunk.setSky(256, 256, 256);

			for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
				for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
					for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
						chunk.setCell(x, y, z, Cells.CELL_TYPE_VOID, null);
						chunk.setLight(x, y, z, 0, 0, 0);
						chunk.setAI(x, y, z, 0);
					}
			if (mX == 0 && mY == 0) {
				setMenu(chunk, MENU, null, false);
			}
			for (int i = 0; i < 16; ++i) {
				chunk.precalcCells(chunk.cellUpdate());
				chunk.updateCells();
			}

			for (int i = 0; i < 16; ++i) {
				chunk.precalcLight(chunk.lightUpdate());
				chunk.updateLight();
			}
			for (int i = 0; i < 16; ++i) {
				chunk.precalcAI(chunk.aiUpdate());
				chunk.updateAI();
			}
			chunk.drop();
			chunk.postDrop();
			chunk.refreshCells();
			chunk.precalcCells(chunk.cellUpdate());
			chunk.precalcLight(chunk.lightUpdate());
			chunk.precalcAI(chunk.aiUpdate());
			mChunkHolder.setChunk(chunk);

			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}

		private void unload() {
			ChunkCellularity chunk = mChunkHolder.getChunk();
			mChunkHolder.setChunk(null);

			ChunkCellularity.recycle(chunk);
			synchronized (mTissularity.getOrganularity()) {
				mChunkHolder.finish();
			}
		}
	}

}
