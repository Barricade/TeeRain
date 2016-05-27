package com.gaskarov.teerain.core.util;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.gaskarov.teerain.core.cellularity.Cellularity;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public interface Collidable {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public int getX();

	public int getY();

	public int getZ();

	public Cellularity getCellularity();

	public void beginContact(Contact pContact, Fixture pFixture, Fixture pThisFixture,
			boolean pSwap, Collidable pData);

	public void endContact(Contact pContact, Fixture pFixture, Fixture pThisFixture, boolean pSwap,
			Collidable pData);

	public void preSolve(Contact pContact, Manifold pOldManifold, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap, Collidable pData);

	public void postSolve(Contact pContact, ContactImpulse pContactImpulse, Fixture pFixture,
			Fixture pThisFixture, boolean pSwap, Collidable pData);

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
