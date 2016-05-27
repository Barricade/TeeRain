package com.gaskarov.teerain.resource;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gaskarov.util.pool.BodyDefPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Settings {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final float TIME_STEP = 1f / 60;
	public static final float TIME_STEP_MILLIS = TIME_STEP * 1000;
	public static final int OPERATION_SOLVER_MAX_THREADS = 1;
	public static final int VISITOR_EXTRA_BORDER_SIZE = 16;
	public static final int VISITOR_SOFT_BORDER_SIZE = 16;
	public static final int MAX_CENTER_OFFSET = 1024;
	public static final int CELL_STEPS_PER_TICK = 30;
	public static final int AI_STEPS_PER_TICK = 4;
	public static final int LIGHT_STEPS_PER_TICK = 2;
	public static final long WAIT_LOOP_DELAY = 1;

	public static final float GRAVITY_X = 0.0f;
	public static final float GRAVITY_Y = -40.0f;
	public static final int VELOCITY_ITERATIONS = 8;
	public static final int POSITION_ITERATIONS = 8;

	public static final int MAX_CHUNK_PUSH_PER_TICK = 1;

	public static final int CHUNK_SIZE_LOG = 7;
	public static final int CHUNK_SIZE = 1 << CHUNK_SIZE_LOG;
	public static final int CHUNK_HSIZE = CHUNK_SIZE >> 1;
	public static final int CHUNK_SIZE_MASK = CHUNK_SIZE - 1;
	public static final int CHUNK_DEPTH_LOG = 2;
	public static final int CHUNK_DEPTH = 1 << CHUNK_DEPTH_LOG;
	public static final int CHUNK_DEPTH_MASK = CHUNK_DEPTH - 1;
	public static final int CHUNK_MIN_DEPTH = 0;
	public static final int CHUNK_MAX_DEPTH = CHUNK_DEPTH - 1;
	public static final int CHUNK_LEFT = 0;
	public static final int CHUNK_RIGHT = CHUNK_SIZE - 1;
	public static final int CHUNK_BOTTOM = 0;
	public static final int CHUNK_TOP = CHUNK_SIZE - 1;
	public static final int CHUNK_SQUARE_LOG = CHUNK_SIZE_LOG << 1;
	public static final int CHUNK_SQUARE = 1 << CHUNK_SQUARE_LOG;
	public static final int CHUNK_SQUARE_MASK = CHUNK_SQUARE - 1;
	public static final int CHUNK_VOLUME_LOG = CHUNK_SQUARE_LOG
			+ CHUNK_DEPTH_LOG;
	public static final int CHUNK_VOLUME = 1 << CHUNK_VOLUME_LOG;
	public static final int CHUNK_VOLUME_MASK = CHUNK_VOLUME - 1;

	public static final int CHUNK_DEPTH_SKY = CHUNK_MAX_DEPTH + 1;
	public static final int CHUNK_DEPTH_VACUUM = CHUNK_MIN_DEPTH - 1;

	public static final int MAX_DROP_SIZE_LOG = 3;
	public static final int MAX_DROP_SIZE = 1 << MAX_DROP_SIZE_LOG;
	public static final int MAX_DROP_SIZE_MASK = MAX_DROP_SIZE - 1;
	public static final int MAX_DROP_HSIZE = MAX_DROP_SIZE >> 1;
	public static final int MAX_DROP_DEPTH_LOG = CHUNK_DEPTH_LOG;
	public static final int MAX_DROP_DEPTH = 1 << MAX_DROP_DEPTH_LOG;
	public static final int MAX_DROP_DEPTH_MASK = MAX_DROP_DEPTH - 1;
	public static final int MAX_DROP_MIN_DEPTH = 0;
	public static final int MAX_DROP_MAX_DEPTH = MAX_DROP_DEPTH - 1;
	public static final int MAX_DROP_LEFT = 0;
	public static final int MAX_DROP_RIGHT = MAX_DROP_SIZE - 1;
	public static final int MAX_DROP_BOTTOM = 0;
	public static final int MAX_DROP_TOP = MAX_DROP_SIZE - 1;
	public static final int MAX_DROP_SQUARE_LOG = MAX_DROP_SIZE_LOG << 1;
	public static final int MAX_DROP_SQUARE = 1 << MAX_DROP_SQUARE_LOG;
	public static final int MAX_DROP_SQUARE_MASK = MAX_DROP_SQUARE - 1;
	public static final int MAX_DROP_VOLUME_LOG = MAX_DROP_SQUARE_LOG
			+ MAX_DROP_DEPTH_LOG;
	public static final int MAX_DROP_VOLUME = 1 << MAX_DROP_VOLUME_LOG;
	public static final int MAX_DROP_VOLUME_MASK = MAX_DROP_VOLUME - 1;
	public static final int MAX_DROP_COUNT = 64;

	public static final int CHUNK_REGIONS_SIZE_LOG = CHUNK_SIZE_LOG
			- MAX_DROP_SIZE_LOG;
	public static final int CHUNK_REGIONS_SIZE = 1 << CHUNK_REGIONS_SIZE_LOG;
	public static final int CHUNK_REGIONS_SIZE_MASK = CHUNK_REGIONS_SIZE - 1;
	public static final int CHUNK_REGIONS_LEFT = 0;
	public static final int CHUNK_REGIONS_RIGHT = CHUNK_REGIONS_SIZE - 1;
	public static final int CHUNK_REGIONS_BOTTOM = 0;
	public static final int CHUNK_REGIONS_TOP = CHUNK_REGIONS_SIZE - 1;
	public static final int CHUNK_REGIONS_SQUARE_LOG = CHUNK_REGIONS_SIZE_LOG << 1;
	public static final int CHUNK_REGIONS_SQUARE = 1 << CHUNK_REGIONS_SQUARE_LOG;
	public static final int CHUNK_REGIONS_SQUARE_MASK = CHUNK_REGIONS_SQUARE - 1;

	public static final int CHUNK_REGION_SIZE_LOG = CHUNK_SIZE_LOG
			- CHUNK_REGIONS_SIZE_LOG;
	public static final int CHUNK_REGION_SIZE = 1 << CHUNK_REGION_SIZE_LOG;
	public static final int CHUNK_REGION_MASK = CHUNK_REGION_SIZE - 1;

	public static final int COLORS_LOG = 2;
	public static final int COLORS = 1 << COLORS_LOG;
	public static final int COLORS_MASK = COLORS - 1;

	public static final int CHUNK_VOLUME_COLORS = CHUNK_VOLUME << COLORS_LOG;

	public static final int CELL_UPDATE_SIZE = 11;
	public static final int[] CELL_UPDATE_X = new int[] { -1, 0, 1, -1, 0, 1,
			-1, 0, 1, 0, 0 };
	public static final int[] CELL_UPDATE_Y = new int[] { -1, -1, -1, 0, 0, 0,
			1, 1, 1, 0, 0 };
	public static final int[] CELL_UPDATE_Z = new int[] { 0, 0, 0, 0, 0, 0, 0,
			0, 0, -1, 1 };

	public static final int VISITOR_HEIGHT = 40;
	public static final int VISITOR_WIDTH = 40;

	public static final int SPRITE_BATCH_SIZE = 5460;
	public static final float TILE_W = 1 / 16f;
	public static final float TILE_H = 1 / 16f;
	public static final float TILE_HALF_W = TILE_W / 2;
	public static final float TILE_HALF_H = TILE_H / 2;
	public static final float TILE_QUARTER_W = TILE_W / 4;
	public static final float TILE_QUARTER_H = TILE_H / 4;
	public static final float DEPTH_FACTOR = 1.1f;
	public static final float[] DEPTH_FACTORS = new float[CHUNK_DEPTH];

	public static final float TILE_RENDER = 2f / 30;
	public static final boolean BOX2D_DEBUG_DRAW = false;
	public static final boolean AI_DEBUG_RENDER = false;
	public static final float TILE_RENDER_HUD = 2f / 20;
	public static final float TILE_RENDER_BACKGROUND = 2f / 30;

	public static final int LAYERS_PER_DEPTH = 4;
	public static final int LAYERS = CHUNK_DEPTH * LAYERS_PER_DEPTH;

	public static final int ELEMENTS_PER_TEXTURE = 20;
	public static final int ELEMENTS_PER_TEXTURE_2 = ELEMENTS_PER_TEXTURE * 2;
	public static final int ELEMENTS_PER_TEXTURE_3 = ELEMENTS_PER_TEXTURE * 3;
	public static final int ELEMENTS_PER_TILED_TEXTURE = ELEMENTS_PER_TEXTURE * 4;

	public static final float JUMP_DELAY = 0.25f;
	public static final float GROUND_JUMP_VELOCITY = 21f;

	public static final float GROUND_CONTROL_SPEED = 12f;
	public static final float GROUND_CONTROL_ACCEL = 200f;
	public static final float GROUND_FRICTION = 80f;

	public static final float AIR_CONTROL_SPEED = 8f;
	public static final float AIR_CONTROL_ACCEL = 150f;
	public static final float AIR_FRICTION = 10f;

	public static final boolean STATIC_BODY_ACTIVE = true;
	public static final boolean STATIC_BODY_ALLOW_SLEEP = true;
	public static final float STATIC_BODY_ANGLE = 0.0f;
	public static final float STATIC_BODY_ANGULAR_DAMPING = 0.0f;
	public static final float STATIC_BODY_ANGULAR_VELOCITY = 0.0f;
	public static final boolean STATIC_BODY_AWAKE = true;
	public static final boolean STATIC_BODY_BULLET = false;
	public static final boolean STATIC_BODY_FIXED_ROTATION = false;
	public static final float STATIC_BODY_GRAVITY_SCALE = 1.0f;
	public static final float STATIC_BODY_LINEAR_DAMPING = 0.0f;
	public static final float STATIC_BODY_LINEAR_VELOCITY_X = 0.0f;
	public static final float STATIC_BODY_LINEAR_VELOCITY_Y = 0.0f;
	public static final float STATIC_BODY_POSITION_X = 0.0f;
	public static final float STATIC_BODY_POSITION_Y = 0.0f;
	public static final BodyType STATIC_BODY_TYPE = BodyType.StaticBody;
	public static final BodyDef STATIC_BODY = BodyDefPool.obtain(
			STATIC_BODY_ACTIVE, STATIC_BODY_ALLOW_SLEEP, STATIC_BODY_ANGLE,
			STATIC_BODY_ANGULAR_DAMPING, STATIC_BODY_ANGULAR_VELOCITY,
			STATIC_BODY_AWAKE, STATIC_BODY_BULLET, STATIC_BODY_FIXED_ROTATION,
			STATIC_BODY_GRAVITY_SCALE, STATIC_BODY_LINEAR_DAMPING,
			STATIC_BODY_LINEAR_VELOCITY_X, STATIC_BODY_LINEAR_VELOCITY_Y,
			STATIC_BODY_POSITION_X, STATIC_BODY_POSITION_Y, STATIC_BODY_TYPE);

	public static final boolean DYNAMIC_BODY_ACTIVE = true;
	public static final boolean DYNAMIC_BODY_ALLOW_SLEEP = true;
	public static final float DYNAMIC_BODY_ANGLE = 0.0f;
	public static final float DYNAMIC_BODY_ANGULAR_DAMPING = 0.0f;
	public static final float DYNAMIC_BODY_ANGULAR_VELOCITY = 0.0f;
	public static final boolean DYNAMIC_BODY_AWAKE = true;
	public static final boolean DYNAMIC_BODY_BULLET = false;
	public static final boolean DYNAMIC_BODY_FIXED_ROTATION = false;
	public static final float DYNAMIC_BODY_GRAVITY_SCALE = 1.0f;
	public static final float DYNAMIC_BODY_LINEAR_DAMPING = 0.0f;
	public static final float DYNAMIC_BODY_LINEAR_VELOCITY_X = 0.0f;
	public static final float DYNAMIC_BODY_LINEAR_VELOCITY_Y = 0.0f;
	public static final float DYNAMIC_BODY_POSITION_X = 0.0f;
	public static final float DYNAMIC_BODY_POSITION_Y = 0.0f;
	public static final BodyType DYNAMIC_BODY_TYPE = BodyType.DynamicBody;
	public static final BodyDef DYNAMIC_BODY = BodyDefPool.obtain(
			DYNAMIC_BODY_ACTIVE, DYNAMIC_BODY_ALLOW_SLEEP, DYNAMIC_BODY_ANGLE,
			DYNAMIC_BODY_ANGULAR_DAMPING, DYNAMIC_BODY_ANGULAR_VELOCITY,
			DYNAMIC_BODY_AWAKE, DYNAMIC_BODY_BULLET,
			DYNAMIC_BODY_FIXED_ROTATION, DYNAMIC_BODY_GRAVITY_SCALE,
			DYNAMIC_BODY_LINEAR_DAMPING, DYNAMIC_BODY_LINEAR_VELOCITY_X,
			DYNAMIC_BODY_LINEAR_VELOCITY_Y, DYNAMIC_BODY_POSITION_X,
			DYNAMIC_BODY_POSITION_Y, DYNAMIC_BODY_TYPE);

	public static final float MAX_ACTION_RADIUS = 2f;
	public static final float MAX_ACTION_RADIUS_2 = MAX_ACTION_RADIUS
			* MAX_ACTION_RADIUS;

	public static final int ZOMBIE_ID = 0;
	public static final int PLAYER_ID = 1;

	public static float SOUND_DISTANCE = 30f;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	static {
		DEPTH_FACTORS[0] = 1f;
		for (int i = 1; i < CHUNK_DEPTH; ++i)
			DEPTH_FACTORS[i] = DEPTH_FACTORS[i - 1] * DEPTH_FACTOR;
	}

	private Settings() {
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
