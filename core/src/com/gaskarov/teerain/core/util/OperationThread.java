package com.gaskarov.teerain.core.util;



/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class OperationThread extends Thread {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private volatile boolean mIsAlive;
	private Runnable mOperation;
	private OperationSolver mOperationSolver;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OperationThread(OperationSolver pOperationSolver) {
		mIsAlive = true;
		mOperation = null;
		mOperationSolver = pOperationSolver;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void run() {
		try {
			while (mIsAlive) {
				synchronized (this) {
					if (mOperation != null) {
						mOperation.run();
						mOperation = null;
						mOperationSolver.freeOperationThread(this);
					} else
						wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public synchronized void operation(Runnable pRunnable) {
		mOperation = pRunnable;
		notifyAll();
	}

	public synchronized void kill() {
		mIsAlive = false;
		notifyAll();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
