package com.gaskarov.teerain.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gaskarov.teerain.TeeRain;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		config.fullscreen = true;
//		config.width = 1920 + 1280 * 0;
//		config.height = 1080 + 720 * 0;
		config.width = 1000;
		config.height = 1000;
		// config.samples = 0;
		// config.foregroundFPS = 0;
		// config.vSyncEnabled = false;
		new LwjglApplication(new TeeRain(), config);
	}
}
