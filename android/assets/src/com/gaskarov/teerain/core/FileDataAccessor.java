package com.gaskarov.teerain.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.badlogic.gdx.Gdx;
import com.gaskarov.util.constants.GlobalConstants;
import com.gaskarov.util.container.Array;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public class FileDataAccessor implements DataAccessor {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Array sPool = Array.obtain();

	private RandomAccessFile mFile;

	// ===========================================================
	// Constructors
	// ===========================================================

	private FileDataAccessor() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public int size() {
		try {
			long n = mFile.length();
			if ((n & 0xFFFFFFFF) != n)
				throw new RuntimeException("Size is too large.");
			return (int) n;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("IOException.");
		}
	}

	@Override
	public void read(byte[] pData, int pOffset, int pSize) {
		try {
			mFile.read(pData, pOffset, pSize);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("IOException.");
		}
	}

	@Override
	public void write(byte[] pData, int pOffset, int pSize) {
		try {
			while (mFile.length() < mFile.getFilePointer() + pSize)
				mFile.setLength(mFile.length() * 2);
			mFile.write(pData, pOffset, pSize);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("IOException.");
		}
	}

	@Override
	public void position(int pPos) {
		try {
			mFile.seek(pPos);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("IOException.");
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static FileDataAccessor obtainPure() {
		if (GlobalConstants.POOL)
			synchronized (FileDataAccessor.class) {
				return sPool.size() == 0 ? new FileDataAccessor() : (FileDataAccessor) sPool.pop();
			}
		return new FileDataAccessor();
	}

	private static void recyclePure(FileDataAccessor pObj) {
		if (GlobalConstants.POOL)
			synchronized (FileDataAccessor.class) {
				sPool.push(pObj);
			}
	}

	public static FileDataAccessor obtain(String pFileName) {
		FileDataAccessor obj = obtainPure();
		try {
			File file = Gdx.files.external(pFileName).file();
			obj.mFile = new RandomAccessFile(file, "rws");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public static void recycle(FileDataAccessor pObj) {
		try {
			pObj.mFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("IOException.");
		}
		pObj.mFile = null;
		recyclePure(pObj);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
