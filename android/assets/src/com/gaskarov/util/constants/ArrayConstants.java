package com.gaskarov.util.constants;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class ArrayConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	/**
	 * -1- <br>
	 * 2-0 <br>
	 * -3-
	 */
	public static final int DIRECTIONS_2D_SIZE = 4;
	public static final int[] DIRECTIONS_2D_X = new int[] { 1, 0, -1, 0 };
	public static final int[] DIRECTIONS_2D_Y = new int[] { 0, 1, 0, -1 };

	/**
	 * 321 <br>
	 * 4-0 <br>
	 * 567
	 */
	public static final int MOVE_AROUND_SIZE = 8;
	public static final int[] MOVE_AROUND_X = new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[] MOVE_AROUND_Y = new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };

	/**
	 * 678 <br>
	 * 345 <br>
	 * 012
	 */
	public static final int SQUARE_3_SIZE = 9;
	public static final int[] SQUARE_3_X = new int[] { -1, 0, 1, -1, 0, 1, -1, 0, 1 };
	public static final int[] SQUARE_3_Y = new int[] { -1, -1, -1, 0, 0, 0, 1, 1, 1 };

	public static final int CUBE_3_SIZE = 27;
	public static final int[] CUBE_3_X = new int[] { -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0,
			1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1 };
	public static final int[] CUBE_3_Y = new int[] { -1, -1, -1, 0, 0, 0, 1, 1, 1, -1, -1, -1, 0,
			0, 0, 1, 1, 1, -1, -1, -1, 0, 0, 0, 1, 1, 1 };
	public static final int[] CUBE_3_Z = new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

	public static final int CUBE_AREA_3_SIZE = 26;
	public static final int[] CUBE_AREA_3_X = new int[] { -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1,
			-1, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1 };
	public static final int[] CUBE_AREA_3_Y = new int[] { -1, -1, -1, 0, 0, 0, 1, 1, 1, -1, -1, -1,
			0, 0, 1, 1, 1, -1, -1, -1, 0, 0, 0, 1, 1, 1 };
	public static final int[] CUBE_AREA_3_Z = new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0,
			0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

	public static final int DIRECTIONS_3D_SIZE = 6;
	public static final int[] DIRECTIONS_3D_X = new int[] { 1, 0, 0, -1, 0, 0 };
	public static final int[] DIRECTIONS_3D_Y = new int[] { 0, 1, 0, 0, -1, 0 };
	public static final int[] DIRECTIONS_3D_Z = new int[] { 0, 0, 1, 0, 0, -1 };

	public static final int DIRECTIONS_2D_AND_DEPTH_SIZE = 6;
	public static final int[] DIRECTIONS_2D_AND_DEPTH_X = new int[] { 1, 0, -1, 0, 0, 0 };
	public static final int[] DIRECTIONS_2D_AND_DEPTH_Y = new int[] { 0, 1, 0, -1, 0, 0 };
	public static final int[] DIRECTIONS_2D_AND_DEPTH_Z = new int[] { 0, 0, 0, 0, 1, -1 };

	/**
	 * 1-0 <br>
	 * --- <br>
	 * 2-3
	 */
	public static final int SQUARE_CORNERS_SIZE = 4;
	public static final int[] SQUARE_CORNERS_X = new int[] { 1, -1, -1, 1 };
	public static final int[] SQUARE_CORNERS_Y = new int[] { 1, 1, -1, -1 };

	public static final int DIRECTIONS_3D_AND_SQUARE_CORNERS_SIZE = 10;
	public static final int[] DIRECTIONS_3D_AND_SQUARE_CORNERS_X = new int[] { 1, 0, 0, -1, 0, 0,
			1, -1, -1, 1 };
	public static final int[] DIRECTIONS_3D_AND_SQUARE_CORNERS_Y = new int[] { 0, 1, 0, 0, -1, 0,
			1, 1, -1, -1 };
	public static final int[] DIRECTIONS_3D_AND_SQUARE_CORNERS_Z = new int[] { 0, 0, 1, 0, 0, -1,
			0, 0, 0, 0 };

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private ArrayConstants() {
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
