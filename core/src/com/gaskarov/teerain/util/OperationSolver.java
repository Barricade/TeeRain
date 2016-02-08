package com.gaskarov.teerain.util;

import com.gaskarov.util.container.Array;
import com.gaskarov.util.container.LinkedHashTable;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class OperationSolver {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private LinkedHashTable mOperations;
	private Array mOperationThreads;
	private Array mFreeOperationThreads;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OperationSolver(int pMaxThreads) {
		mOperations = LinkedHashTable.obtain();
		mOperationThreads = Array.obtain();
		mFreeOperationThreads = Array.obtain();
		for (int i = 0; i < pMaxThreads; ++i) {
			OperationThread operationThread = new OperationThread(this);
			// operationThread.setPriority(Thread.MIN_PRIORITY);
			operationThread.start();
			mOperationThreads.push(operationThread);
			mFreeOperationThreads.push(operationThread);
		}
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

	public synchronized boolean pushOperation(Runnable pRunnable) {
		if (mOperations.get(pRunnable) == null) {
			mOperations.set(pRunnable);
			pushNext();
			return true;
		}
		return false;
	}

	public synchronized boolean removeOperation(Runnable pRunnable) {
		return mOperations.remove(pRunnable) != null;
	}

	public void dispose() {
		for (int i = 0; i < mOperationThreads.size(); ++i)
			((OperationThread) mOperationThreads.get(i)).kill();
	}

	public synchronized void freeOperationThread(OperationThread pOperation) {
		mFreeOperationThreads.push(pOperation);
		pushNext();
	}

	public synchronized boolean isEmpty() {
		return mFreeOperationThreads.size() == mOperationThreads.size() && mOperations.size() == 0;
	}

	private void pushNext() {
		if (mFreeOperationThreads.size() > 0 && mOperations.size() > 0) {
			((OperationThread) mFreeOperationThreads.pop()).operation((Runnable) mOperations
					.remove(mOperations.front()));
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
