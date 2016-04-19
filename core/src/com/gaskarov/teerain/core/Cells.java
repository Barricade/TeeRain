package com.gaskarov.teerain.core;

import static com.gaskarov.teerain.core.util.Settings.TILE_H;
import static com.gaskarov.teerain.core.util.Settings.TILE_W;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class Cells {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int CELL_TYPES_MAX_LOG = 16;
	public static final int CELL_TYPES_MAX = 1 << CELL_TYPES_MAX_LOG;
	public static final int CELL_TYPES_MAX_MASK = CELL_TYPES_MAX - 1;

	public static final int CELL_TYPES = 11;

	public static final int CELL_TYPE_DEFAULT = 0;
	public static final int CELL_TYPE_VOID = 1;
	public static final int CELL_TYPE_VACUUM = 2;
	public static final int CELL_TYPE_AIR = 3;
	public static final int CELL_TYPE_GROUND = 4;
	public static final int CELL_TYPE_ROCK = 5;
	public static final int CELL_TYPE_LAMP = 6;
	public static final int CELL_TYPE_TEE = 7;
	public static final int CELL_TYPE_UI = 8;
	public static final int CELL_TYPE_HAMMER = 9;
	public static final int CELL_TYPE_GRENADE_GUN = 10;

	public static final int CELL_RENDER_TYPE_NO = 0;
	public static final int CELL_RENDER_TYPE_TILE = 1;
	public static final int CELL_RENDER_TYPE_WALL = 2;
	public static final int CELL_RENDER_TYPE_TEE = 3;
	public static final int CELL_RENDER_TYPE_UI = 4;

	public static final int CELL_RENDER_LAYER_BACKGROUND = 3;
	public static final int CELL_RENDER_LAYER_MAIN = 2;
	public static final int CELL_RENDER_LAYER_FRONT = 1;
	public static final int CELL_RENDER_LAYER_SPECIAL = 0;

	public static final int CELL_RENDER_WEAPON_TYPE_NO = 0;
	public static final int CELL_RENDER_WEAPON_TYPE_DEFAULT = 1;
	public static final int CELL_RENDER_WEAPON_TYPE_SPECIAL = 2;

	public static final int CELL_TILE_CONNECTED_TYPE_NO = 0;
	public static final int CELL_TILE_CONNECTED_TYPE_SAME = 1;

	public static final int CELL_CONNECTION_TYPE_NO = 0;
	public static final int CELL_CONNECTION_TYPE_ALL = 1;

	public static final int CELL_PHYSICS_TYPE_NO = 0;
	public static final int CELL_PHYSICS_TYPE_WALL = 1;

	public static final float CELL_PHYSICS_BORDER_SIZE_AIR = 0f;
	public static final float CELL_PHYSICS_CORNER_SIZE_AIR = 0f;
	public static final float CELL_PHYSICS_CORNER_VALUE_AIR = 0f;
	public static final float CELL_PHYSICS_BORDER_SIZE_SOLID = 0.02f;
	public static final float CELL_PHYSICS_CORNER_SIZE_SOLID = 0.1f;
	public static final float CELL_PHYSICS_CORNER_VALUE_SOLID = 0.01f;
	public static final float CELL_PHYSICS_BORDER_SIZE_TEE = 0.02f;
	public static final float CELL_PHYSICS_CORNER_SIZE_TEE = 0.3f;
	public static final float CELL_PHYSICS_CORNER_VALUE_TEE = 0.125f;

	public static final float CELL_PHYSICS_DENSITY_AIR = 0f;
	public static final float CELL_PHYSICS_FRICTION_AIR = 0f;
	public static final float CELL_PHYSICS_RESTITUTION_AIR = 0f;
	public static final float CELL_PHYSICS_DENSITY_SOLID = 1f;
	public static final float CELL_PHYSICS_FRICTION_SOLID = 0.6f;
	public static final float CELL_PHYSICS_RESTITUTION_SOLID = 0f;
	public static final float CELL_PHYSICS_DENSITY_TEE = 1f;
	public static final float CELL_PHYSICS_FRICTION_TEE = 0f;
	public static final float CELL_PHYSICS_RESTITUTION_TEE = 0f;

	public static final int CELL_LIGHT_SOURCE_NO_R = 0;
	public static final int CELL_LIGHT_SOURCE_NO_G = 0;
	public static final int CELL_LIGHT_SOURCE_NO_B = 0;
	public static final int CELL_LIGHT_SOURCE_TORCH_R = 192;
	public static final int CELL_LIGHT_SOURCE_TORCH_G = 128;
	public static final int CELL_LIGHT_SOURCE_TORCH_B = 96;
	public static final int CELL_LIGHT_SOURCE_MAGIC_R = 64;
	public static final int CELL_LIGHT_SOURCE_MAGIC_G = 192;
	public static final int CELL_LIGHT_SOURCE_MAGIC_B = 256;

	public static final int[] NO_LIGHT_RESISTANCE = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public static final int[] AIR_LIGHT_RESISTANCE = new int[] { 16, 16, 16, 16, 16, 16, 32, 32,
			32, 16, 16, 16, 16, 16, 16, 0, 0, 0, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24 };

	public static final int[] SOLID_LIGHT_RESISTANCE = new int[] { 32, 32, 32, 32, 32, 32, 64, 64,
			64, 32, 32, 32, 32, 32, 32, 1024, 1024, 1024, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
			48, 48 };

	public static final int CELL_AI_RESISTANCE_AIR = 2;
	public static final int CELL_AI_DIAGONAL_RESISTANCE_AIR = 3;
	public static final int CELL_AI_RESISTANCE_SOLID = 255;
	public static final int CELL_AI_DIAGONAL_RESISTANCE_SOLID = 255;

	public static final int CELL_ITEM_ACTION_NO = 0;
	public static final int CELL_ITEM_ACTION_DEFAULT = 1;
	public static final int CELL_ITEM_ACTION_HAMMER = 2;
	public static final int CELL_ITEM_ACTION_GUN = 3;

	public static final int[] CELLS_RENDER_TYPE = new int[CELL_TYPES];
	public static final float[][] CELLS_RENDER_TILE_DATA = new float[CELL_TYPES][];
	public static final int[] CELLS_RENDER_LAYER = new int[CELL_TYPES];
	public static final int[] CELLS_RENDER_WEAPON_TYPE = new int[CELL_TYPES];
	public static final float[][] CELLS_RENDER_WEAPON_DATA = new float[CELL_TYPES][];

	public static final int[] CELLS_TILE_CONNECTED_TYPE = new int[CELL_TYPES];

	public static final int[] CELLS_CONNECTION_TYPE = new int[CELL_TYPES];
	public static final boolean[] CELLS_IS_DROPPABLE = new boolean[CELL_TYPES];

	public static final boolean[] CELLS_IS_SOLID = new boolean[CELL_TYPES];
	public static final boolean[] CELLS_IS_SQUARE = new boolean[CELL_TYPES];
	public static final boolean[] CELLS_IS_SHELLABLE = new boolean[CELL_TYPES];

	public static final boolean[] CELLS_IS_BLOCKING = new boolean[CELL_TYPES];

	public static final int[] CELLS_PHYSICS_TYPE = new int[CELL_TYPES];

	public static final float[] CELLS_PHYSICS_BORDER_SIZE = new float[CELL_TYPES];
	public static final float[] CELLS_PHYSICS_CORNER_SIZE = new float[CELL_TYPES];
	public static final float[] CELLS_PHYSICS_CORNER_VALUE = new float[CELL_TYPES];

	public static final float[] CELLS_PHYSICS_DENSITY = new float[CELL_TYPES];
	public static final float[] CELLS_PHYSICS_FRICTION = new float[CELL_TYPES];
	public static final float[] CELLS_PHYSICS_RESTITUTION = new float[CELL_TYPES];

	public static final int[] CELLS_LIGHT_SOURCE_R = new int[CELL_TYPES];
	public static final int[] CELLS_LIGHT_SOURCE_G = new int[CELL_TYPES];
	public static final int[] CELLS_LIGHT_SOURCE_B = new int[CELL_TYPES];
	public static final boolean[] CELLS_IS_DYNAMIC_LIGHT_SOURCE = new boolean[CELL_TYPES];

	public static final int[][] CELLS_LIGHT_RESISTANCE = new int[CELL_TYPES][];

	public static final int[] CELLS_AI_RESISTANCE = new int[CELL_TYPES];
	public static final int[] CELLS_AI_DIAGONAL_RESISTANCE = new int[CELL_TYPES];

	public static final int[] CELLS_ITEM_ACTION = new int[CELL_TYPES];

	public static final int SYMBOLS = 37;
	public static final float[] SYMBOLS_TILE_X = new float[SYMBOLS];
	public static final float[] SYMBOLS_TILE_Y = new float[SYMBOLS];

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	static {
		// 1
		SYMBOLS_TILE_X[1] = TILE_W * 5;
		SYMBOLS_TILE_Y[1] = TILE_H * 5;
		// 2
		SYMBOLS_TILE_X[2] = TILE_W * 6;
		SYMBOLS_TILE_Y[2] = TILE_H * 5;
		// 3
		SYMBOLS_TILE_X[3] = TILE_W * 7;
		SYMBOLS_TILE_Y[3] = TILE_H * 5;
		// 4
		SYMBOLS_TILE_X[4] = TILE_W * 8;
		SYMBOLS_TILE_Y[4] = TILE_H * 5;
		// 5
		SYMBOLS_TILE_X[5] = TILE_W * 9;
		SYMBOLS_TILE_Y[5] = TILE_H * 5;
		// 6
		SYMBOLS_TILE_X[6] = TILE_W * 10;
		SYMBOLS_TILE_Y[6] = TILE_H * 5;
		// 7
		SYMBOLS_TILE_X[7] = TILE_W * 11;
		SYMBOLS_TILE_Y[7] = TILE_H * 5;
		// 8
		SYMBOLS_TILE_X[8] = TILE_W * 12;
		SYMBOLS_TILE_Y[8] = TILE_H * 5;
		// 9
		SYMBOLS_TILE_X[9] = TILE_W * 13;
		SYMBOLS_TILE_Y[9] = TILE_H * 5;
		// 0
		SYMBOLS_TILE_X[10] = TILE_W * 14;
		SYMBOLS_TILE_Y[10] = TILE_H * 5;
		// A
		SYMBOLS_TILE_X[11] = TILE_W * 5;
		SYMBOLS_TILE_Y[11] = TILE_H * 7;
		// B
		SYMBOLS_TILE_X[12] = TILE_W * 6;
		SYMBOLS_TILE_Y[12] = TILE_H * 7;
		// C
		SYMBOLS_TILE_X[13] = TILE_W * 7;
		SYMBOLS_TILE_Y[13] = TILE_H * 7;
		// D
		SYMBOLS_TILE_X[14] = TILE_W * 8;
		SYMBOLS_TILE_Y[14] = TILE_H * 7;
		// E
		SYMBOLS_TILE_X[15] = TILE_W * 9;
		SYMBOLS_TILE_Y[15] = TILE_H * 7;
		// F
		SYMBOLS_TILE_X[16] = TILE_W * 10;
		SYMBOLS_TILE_Y[16] = TILE_H * 7;
		// G
		SYMBOLS_TILE_X[17] = TILE_W * 11;
		SYMBOLS_TILE_Y[17] = TILE_H * 7;
		// H
		SYMBOLS_TILE_X[18] = TILE_W * 12;
		SYMBOLS_TILE_Y[18] = TILE_H * 7;
		// I
		SYMBOLS_TILE_X[19] = TILE_W * 13;
		SYMBOLS_TILE_Y[19] = TILE_H * 7;
		// J
		SYMBOLS_TILE_X[20] = TILE_W * 14;
		SYMBOLS_TILE_Y[20] = TILE_H * 7;
		// K
		SYMBOLS_TILE_X[21] = TILE_W * 15;
		SYMBOLS_TILE_Y[21] = TILE_H * 7;
		// L
		SYMBOLS_TILE_X[22] = TILE_W * 5;
		SYMBOLS_TILE_Y[22] = TILE_H * 8;
		// M
		SYMBOLS_TILE_X[23] = TILE_W * 6;
		SYMBOLS_TILE_Y[23] = TILE_H * 8;
		// N
		SYMBOLS_TILE_X[24] = TILE_W * 7;
		SYMBOLS_TILE_Y[24] = TILE_H * 8;
		// O
		SYMBOLS_TILE_X[25] = TILE_W * 8;
		SYMBOLS_TILE_Y[25] = TILE_H * 8;
		// P
		SYMBOLS_TILE_X[26] = TILE_W * 9;
		SYMBOLS_TILE_Y[26] = TILE_H * 8;
		// Q
		SYMBOLS_TILE_X[27] = TILE_W * 10;
		SYMBOLS_TILE_Y[27] = TILE_H * 8;
		// R
		SYMBOLS_TILE_X[28] = TILE_W * 11;
		SYMBOLS_TILE_Y[28] = TILE_H * 8;
		// S
		SYMBOLS_TILE_X[29] = TILE_W * 12;
		SYMBOLS_TILE_Y[29] = TILE_H * 8;
		// T
		SYMBOLS_TILE_X[30] = TILE_W * 13;
		SYMBOLS_TILE_Y[30] = TILE_H * 8;
		// U
		SYMBOLS_TILE_X[31] = TILE_W * 14;
		SYMBOLS_TILE_Y[31] = TILE_H * 8;
		// V
		SYMBOLS_TILE_X[32] = TILE_W * 15;
		SYMBOLS_TILE_Y[32] = TILE_H * 8;
		// W
		SYMBOLS_TILE_X[33] = TILE_W * 5;
		SYMBOLS_TILE_Y[33] = TILE_H * 9;
		// X
		SYMBOLS_TILE_X[34] = TILE_W * 6;
		SYMBOLS_TILE_Y[34] = TILE_H * 9;
		// Y
		SYMBOLS_TILE_X[35] = TILE_W * 7;
		SYMBOLS_TILE_Y[35] = TILE_H * 9;
		// Z
		SYMBOLS_TILE_X[36] = TILE_W * 8;
		SYMBOLS_TILE_Y[36] = TILE_H * 9;

		for (int i = 0; i < CELL_TYPES; ++i) {
			CELLS_RENDER_TYPE[i] = CELL_RENDER_TYPE_NO;
			CELLS_RENDER_TILE_DATA[i] = null;
			CELLS_RENDER_LAYER[i] = CELL_RENDER_LAYER_MAIN;
			CELLS_RENDER_WEAPON_TYPE[i] = CELL_RENDER_WEAPON_TYPE_DEFAULT;
			CELLS_RENDER_WEAPON_DATA[i] = null;
			CELLS_TILE_CONNECTED_TYPE[i] = CELL_TILE_CONNECTED_TYPE_NO;
			CELLS_CONNECTION_TYPE[i] = CELL_CONNECTION_TYPE_NO;
			CELLS_IS_DROPPABLE[i] = false;
			CELLS_IS_SOLID[i] = false;
			CELLS_IS_SQUARE[i] = true;
			CELLS_IS_SHELLABLE[i] = false;
			CELLS_IS_BLOCKING[i] = false;
			CELLS_PHYSICS_TYPE[i] = CELL_PHYSICS_TYPE_NO;
			CELLS_PHYSICS_BORDER_SIZE[i] = CELL_PHYSICS_BORDER_SIZE_AIR;
			CELLS_PHYSICS_CORNER_SIZE[i] = CELL_PHYSICS_CORNER_SIZE_AIR;
			CELLS_PHYSICS_CORNER_VALUE[i] = CELL_PHYSICS_CORNER_VALUE_AIR;
			CELLS_PHYSICS_DENSITY[i] = CELL_PHYSICS_DENSITY_AIR;
			CELLS_PHYSICS_FRICTION[i] = CELL_PHYSICS_FRICTION_AIR;
			CELLS_PHYSICS_RESTITUTION[i] = CELL_PHYSICS_RESTITUTION_AIR;
			CELLS_LIGHT_SOURCE_R[i] = CELL_LIGHT_SOURCE_NO_R;
			CELLS_LIGHT_SOURCE_G[i] = CELL_LIGHT_SOURCE_NO_G;
			CELLS_LIGHT_SOURCE_B[i] = CELL_LIGHT_SOURCE_NO_B;
			CELLS_IS_DYNAMIC_LIGHT_SOURCE[i] = false;
			CELLS_LIGHT_RESISTANCE[i] = NO_LIGHT_RESISTANCE;
			CELLS_AI_RESISTANCE[i] = CELL_AI_RESISTANCE_AIR;
			CELLS_AI_DIAGONAL_RESISTANCE[i] = CELL_AI_DIAGONAL_RESISTANCE_AIR;
			CELLS_ITEM_ACTION[i] = CELL_ITEM_ACTION_DEFAULT;
		}

		int i;

		// Void
		i = CELL_TYPE_VOID;
		CELLS_CONNECTION_TYPE[i] = CELL_CONNECTION_TYPE_ALL;
		CELLS_IS_SOLID[i] = true;
		CELLS_IS_SHELLABLE[i] = true;
		CELLS_IS_BLOCKING[i] = true;

		// Vacuum
		i = CELL_TYPE_VACUUM;

		// Air
		i = CELL_TYPE_AIR;
		CELLS_LIGHT_RESISTANCE[i] = AIR_LIGHT_RESISTANCE;

		// Ground
		i = CELL_TYPE_GROUND;
		CELLS_RENDER_TYPE[i] = CELL_RENDER_TYPE_WALL;
		CELLS_RENDER_TILE_DATA[i] =
				new float[] { TILE_W * 0, TILE_H * 0, TILE_W * 1, TILE_H * 0, TILE_W * 2,
						TILE_H * 0, TILE_W * 3, TILE_H * 0, TILE_W * 4, TILE_H * 0 };
		CELLS_TILE_CONNECTED_TYPE[i] = CELL_TILE_CONNECTED_TYPE_SAME;
		CELLS_CONNECTION_TYPE[i] = CELL_CONNECTION_TYPE_ALL;
		CELLS_IS_DROPPABLE[i] = true;
		CELLS_IS_SOLID[i] = true;
		CELLS_IS_SHELLABLE[i] = true;
		CELLS_IS_BLOCKING[i] = true;
		CELLS_PHYSICS_TYPE[i] = CELL_PHYSICS_TYPE_WALL;
		CELLS_PHYSICS_BORDER_SIZE[i] = CELL_PHYSICS_BORDER_SIZE_SOLID;
		CELLS_PHYSICS_CORNER_SIZE[i] = CELL_PHYSICS_CORNER_SIZE_SOLID;
		CELLS_PHYSICS_CORNER_VALUE[i] = CELL_PHYSICS_CORNER_VALUE_SOLID;
		CELLS_PHYSICS_DENSITY[i] = CELL_PHYSICS_DENSITY_SOLID;
		CELLS_PHYSICS_FRICTION[i] = CELL_PHYSICS_FRICTION_SOLID;
		CELLS_PHYSICS_RESTITUTION[i] = CELL_PHYSICS_RESTITUTION_SOLID;
		CELLS_LIGHT_RESISTANCE[i] = SOLID_LIGHT_RESISTANCE;
		CELLS_AI_RESISTANCE[i] = CELL_AI_RESISTANCE_SOLID;
		CELLS_AI_DIAGONAL_RESISTANCE[i] = CELL_AI_DIAGONAL_RESISTANCE_SOLID;

		// Rock
		i = CELL_TYPE_ROCK;
		CELLS_RENDER_TYPE[i] = CELL_RENDER_TYPE_WALL;
		CELLS_RENDER_TILE_DATA[i] =
				new float[] { TILE_W * 0, TILE_H * 1, TILE_W * 1, TILE_H * 1, TILE_W * 2,
						TILE_H * 1, TILE_W * 3, TILE_H * 1, TILE_W * 4, TILE_H * 1 };
		CELLS_TILE_CONNECTED_TYPE[i] = CELL_TILE_CONNECTED_TYPE_SAME;
		CELLS_CONNECTION_TYPE[i] = CELL_CONNECTION_TYPE_ALL;
		CELLS_IS_DROPPABLE[i] = true;
		CELLS_IS_SOLID[i] = true;
		CELLS_IS_SHELLABLE[i] = true;
		CELLS_IS_BLOCKING[i] = true;
		CELLS_PHYSICS_TYPE[i] = CELL_PHYSICS_TYPE_WALL;
		CELLS_PHYSICS_BORDER_SIZE[i] = CELL_PHYSICS_BORDER_SIZE_SOLID;
		CELLS_PHYSICS_CORNER_SIZE[i] = CELL_PHYSICS_CORNER_SIZE_SOLID;
		CELLS_PHYSICS_CORNER_VALUE[i] = CELL_PHYSICS_CORNER_VALUE_SOLID;
		CELLS_PHYSICS_DENSITY[i] = CELL_PHYSICS_DENSITY_SOLID;
		CELLS_PHYSICS_FRICTION[i] = CELL_PHYSICS_FRICTION_SOLID;
		CELLS_PHYSICS_RESTITUTION[i] = CELL_PHYSICS_RESTITUTION_SOLID;
		CELLS_LIGHT_RESISTANCE[i] = SOLID_LIGHT_RESISTANCE;
		CELLS_AI_RESISTANCE[i] = CELL_AI_RESISTANCE_SOLID;
		CELLS_AI_DIAGONAL_RESISTANCE[i] = CELL_AI_DIAGONAL_RESISTANCE_SOLID;

		// Lamp
		i = CELL_TYPE_LAMP;
		CELLS_RENDER_TYPE[i] = CELL_RENDER_TYPE_WALL;
		CELLS_RENDER_TILE_DATA[i] =
				new float[] { TILE_W * 0, TILE_H * 4, TILE_W * 1, TILE_H * 4, TILE_W * 2,
						TILE_H * 4, TILE_W * 3, TILE_H * 4, TILE_W * 4, TILE_H * 4 };
		CELLS_TILE_CONNECTED_TYPE[i] = CELL_TILE_CONNECTED_TYPE_SAME;
		CELLS_CONNECTION_TYPE[i] = CELL_CONNECTION_TYPE_ALL;
		CELLS_IS_DROPPABLE[i] = false;
		CELLS_IS_SOLID[i] = true;
		CELLS_IS_SHELLABLE[i] = true;
		CELLS_IS_BLOCKING[i] = true;
		CELLS_PHYSICS_TYPE[i] = CELL_PHYSICS_TYPE_WALL;
		CELLS_PHYSICS_BORDER_SIZE[i] = CELL_PHYSICS_BORDER_SIZE_SOLID;
		CELLS_PHYSICS_CORNER_SIZE[i] = CELL_PHYSICS_CORNER_SIZE_SOLID;
		CELLS_PHYSICS_CORNER_VALUE[i] = CELL_PHYSICS_CORNER_VALUE_SOLID;
		CELLS_PHYSICS_DENSITY[i] = CELL_PHYSICS_DENSITY_SOLID;
		CELLS_PHYSICS_FRICTION[i] = CELL_PHYSICS_FRICTION_SOLID;
		CELLS_PHYSICS_RESTITUTION[i] = CELL_PHYSICS_RESTITUTION_SOLID;
		CELLS_LIGHT_SOURCE_R[i] = CELL_LIGHT_SOURCE_MAGIC_R;
		CELLS_LIGHT_SOURCE_G[i] = CELL_LIGHT_SOURCE_MAGIC_G;
		CELLS_LIGHT_SOURCE_B[i] = CELL_LIGHT_SOURCE_MAGIC_B;
		CELLS_IS_DYNAMIC_LIGHT_SOURCE[i] = true;
		CELLS_LIGHT_RESISTANCE[i] = SOLID_LIGHT_RESISTANCE;
		CELLS_AI_RESISTANCE[i] = CELL_AI_RESISTANCE_SOLID;
		CELLS_AI_DIAGONAL_RESISTANCE[i] = CELL_AI_DIAGONAL_RESISTANCE_SOLID;

		// Tee
		i = CELL_TYPE_TEE;
		CELLS_RENDER_TYPE[i] = CELL_RENDER_TYPE_TEE;
		CELLS_RENDER_TILE_DATA[i] =
				new float[] { TILE_W * 6, TILE_H * 0, TILE_W * 7, TILE_H * 0, TILE_W * 7,
						TILE_H * 0.25f };
		CELLS_IS_DROPPABLE[i] = true;
		CELLS_IS_SOLID[i] = true;
		CELLS_IS_BLOCKING[i] = true;
		CELLS_PHYSICS_TYPE[i] = CELL_PHYSICS_TYPE_WALL;
		CELLS_PHYSICS_BORDER_SIZE[i] = CELL_PHYSICS_BORDER_SIZE_TEE;
		CELLS_PHYSICS_CORNER_SIZE[i] = CELL_PHYSICS_CORNER_SIZE_TEE;
		CELLS_PHYSICS_CORNER_VALUE[i] = CELL_PHYSICS_CORNER_VALUE_TEE;
		CELLS_PHYSICS_DENSITY[i] = CELL_PHYSICS_DENSITY_TEE;
		CELLS_PHYSICS_FRICTION[i] = CELL_PHYSICS_FRICTION_TEE;
		CELLS_PHYSICS_RESTITUTION[i] = CELL_PHYSICS_RESTITUTION_TEE;
		CELLS_LIGHT_SOURCE_R[i] = CELL_LIGHT_SOURCE_TORCH_R;
		CELLS_LIGHT_SOURCE_G[i] = CELL_LIGHT_SOURCE_TORCH_G;
		CELLS_LIGHT_SOURCE_B[i] = CELL_LIGHT_SOURCE_TORCH_B;
		CELLS_IS_DYNAMIC_LIGHT_SOURCE[i] = true;
		CELLS_LIGHT_RESISTANCE[i] = SOLID_LIGHT_RESISTANCE;
		CELLS_AI_RESISTANCE[i] = CELL_AI_RESISTANCE_SOLID;
		CELLS_AI_DIAGONAL_RESISTANCE[i] = CELL_AI_DIAGONAL_RESISTANCE_SOLID;

		// UI
		i = CELL_TYPE_UI;
		CELLS_RENDER_TYPE[i] = CELL_RENDER_TYPE_UI;
		CELLS_RENDER_TILE_DATA[i] =
				new float[] { TILE_W * 0, TILE_H * 4, TILE_W * 1, TILE_H * 4, TILE_W * 2,
						TILE_H * 4, TILE_W * 3, TILE_H * 4, TILE_W * 4, TILE_H * 4 };
		CELLS_TILE_CONNECTED_TYPE[i] = CELL_TILE_CONNECTED_TYPE_SAME;

		// Hammer
		i = CELL_TYPE_HAMMER;
		CELLS_RENDER_WEAPON_TYPE[i] = CELL_RENDER_WEAPON_TYPE_SPECIAL;
		CELLS_RENDER_WEAPON_DATA[i] =
				new float[] { 2.0f, 1.0f, TILE_W * 5, TILE_H * 1, TILE_W * 2, TILE_H * 1 };
		CELLS_ITEM_ACTION[i] = CELL_ITEM_ACTION_HAMMER;

		// Grenade Gun
		i = CELL_TYPE_GRENADE_GUN;
		CELLS_RENDER_WEAPON_TYPE[i] = CELL_RENDER_WEAPON_TYPE_SPECIAL;
		CELLS_RENDER_WEAPON_DATA[i] =
				new float[] { 3.0f, 1.0f, TILE_W * 5, TILE_H * 2, TILE_W * 3, TILE_H * 1 };
		CELLS_ITEM_ACTION[i] = CELL_ITEM_ACTION_GUN;
	}

	private Cells() {
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
