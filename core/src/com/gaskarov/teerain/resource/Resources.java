package com.gaskarov.teerain.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Vector2;
import com.gaskarov.teerain.core.cellularity.Cellularity;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class Resources {

	// ===========================================================
	// Constants
	// ===========================================================

	public static int JUMP_SOUNDS_LENGTH = 4;
	public static final Sound[] JUMP_SOUNDS = new Sound[JUMP_SOUNDS_LENGTH];
	public static Texture MAIN_TEXTURE;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	private Resources() {
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

	public static void jumpSoundPlay(float pVolume) {
		JUMP_SOUNDS[(int) (Math.random() * 4)].play(pVolume);
	}

	public static void jumpSoundPlay(Cellularity pCellularity, int pX, int pY) {
		Vector2 p = pCellularity.localToChunk(pX + 0.5f, pY + 0.5f);
		float x = p.x + pCellularity.getBody().getOffsetX()
				- pCellularity.getTissularity().getCameraX();
		float y = p.y + pCellularity.getBody().getOffsetY()
				- pCellularity.getTissularity().getCameraY();
		float volume = (float) Math.max(0,
				(Settings.SOUND_DISTANCE - Math.sqrt(x * x + y * y))
						/ Settings.SOUND_DISTANCE);
		jumpSoundPlay(volume);
	}

	public static void loadResources() {
		MAIN_TEXTURE = new Texture(Gdx.files.internal("textures/main.png"));
		MAIN_TEXTURE.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		for (int i = 0; i < JUMP_SOUNDS_LENGTH; ++i)
			JUMP_SOUNDS[i] = Gdx.audio.newSound(Gdx.files
					.internal("audio/foley_land-0" + (i + 1) + ".wav"));
	}

	public static void disposeResources() {
		MAIN_TEXTURE.dispose();
		for (int i = 0; i < JUMP_SOUNDS_LENGTH; ++i)
			JUMP_SOUNDS[i].dispose();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
