package com.gaskarov.teerain.core.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

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

	public static void loadResources() {
		MAIN_TEXTURE = new Texture(Gdx.files.internal("textures/main.png"));
		MAIN_TEXTURE.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}

	public static void disposeResources() {
		MAIN_TEXTURE.dispose();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
