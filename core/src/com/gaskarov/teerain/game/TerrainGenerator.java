package com.gaskarov.teerain.game;

import static com.gaskarov.teerain.resource.Settings.CHUNK_BOTTOM;
import static com.gaskarov.teerain.resource.Settings.CHUNK_DEPTH_MASK;
import static com.gaskarov.teerain.resource.Settings.CHUNK_LEFT;
import static com.gaskarov.teerain.resource.Settings.CHUNK_MAX_DEPTH;
import static com.gaskarov.teerain.resource.Settings.CHUNK_MIN_DEPTH;
import static com.gaskarov.teerain.resource.Settings.CHUNK_RIGHT;
import static com.gaskarov.teerain.resource.Settings.CHUNK_SIZE_LOG;
import static com.gaskarov.teerain.resource.Settings.CHUNK_SIZE_MASK;
import static com.gaskarov.teerain.resource.Settings.CHUNK_SQUARE_LOG;
import static com.gaskarov.teerain.resource.Settings.CHUNK_TOP;
import static com.gaskarov.teerain.resource.Settings.CHUNK_VOLUME_LOG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.gaskarov.teerain.core.cellularity.Cellularity.CellData;
import com.gaskarov.teerain.core.cellularity.ChunkCellularity;
import com.gaskarov.teerain.core.cellularity.DynamicCellularity;
import com.gaskarov.teerain.core.util.MetaBody;
import com.gaskarov.teerain.resource.Cells;
import com.gaskarov.teerain.resource.CellsAction;
import com.gaskarov.teerain.resource.LootCellData;
import com.gaskarov.teerain.resource.Settings;
import com.gaskarov.teerain.resource.TeeCellData;
import com.gaskarov.util.common.IntVector1;
import com.gaskarov.util.common.NoiseMath;
import com.gaskarov.util.container.LinkedHashTable;
import com.gaskarov.util.container.List;
import com.gaskarov.util.pool.BinaryByteArrayPool;

/**
 * Copyright (c) 2016 Ayrat Gaskarov <br>
 * All rights reserved.
 * 
 * @author Ayrat Gaskarov
 */
public final class TerrainGenerator {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int BUFFER_SIZE = 4096;

	private static final int HEIGHT_PARAM = 0;
	private static final int DENSITY_PARAM = 1;
	private static final int CAVES_PARAM_MIN = 2;
	private static final int CAVES_PARAM_MAX = 4;
	private static final int GRASS_PARAM = 5;

	public static final int HEIGHT_AMPLITUDE = 1024;

	public static final int ORE_SPAWN_TYPES_SIZE = 12;

	public static final int[] ORE_SPAWN_TYPES = new int[] {
			Cells.CELL_TYPE_COAL_BLOCK, Cells.CELL_TYPE_IRON_BLOCK,
			Cells.CELL_TYPE_COPPER_BLOCK, Cells.CELL_TYPE_TIN_BLOCK,
			Cells.CELL_TYPE_ELECTRON_BLOCK, Cells.CELL_TYPE_SILVER_BLOCK,
			Cells.CELL_TYPE_GOLD_BLOCK, Cells.CELL_TYPE_DIAMOND_BLOCK,
			Cells.CELL_TYPE_URANIUM_BLOCK, Cells.CELL_TYPE_GRAVITON_BLOCK,
			Cells.CELL_TYPE_SPATIAL_RIFT_BLOCK,
			Cells.CELL_TYPE_DARK_ENERGY_BLOCK };

	public static final int ORE_TYPE_COAL = 0;
	public static final int ORE_TYPE_IRON = 1;
	public static final int ORE_TYPE_COPPER = 2;
	public static final int ORE_TYPE_TIN = 3;
	public static final int ORE_TYPE_ELECTRON = 4;
	public static final int ORE_TYPE_SILVER = 5;
	public static final int ORE_TYPE_GOLD = 6;
	public static final int ORE_TYPE_DIAMOND = 7;
	public static final int ORE_TYPE_URANIUM = 8;
	public static final int ORE_TYPE_GRAVITON = 9;
	public static final int ORE_TYPE_SPATIAL_RIFT = 10;
	public static final int ORE_TYPE_DARK_ENERGY = 11;

	public static final int[] ORE_SPAWN_ALLOWED_PRESSURE_MIN = new int[ORE_SPAWN_TYPES_SIZE];
	public static final int[] ORE_SPAWN_ALLOWED_PRESSURE_MAX = new int[ORE_SPAWN_TYPES_SIZE];

	public static final int[][] ORE_SPAWN_ALLOWED = new int[ORE_SPAWN_TYPES_SIZE][];

	public static final int ORE_SPAWN_SIZE_LOG = 6;
	public static final int ORE_SPAWN_SIZE = 1 << ORE_SPAWN_SIZE_LOG;
	public static final int ORE_SPAWN_SIZE_MASK = ORE_SPAWN_SIZE - 1;

	public static final int[] ORE_SPAWN = new int[ORE_SPAWN_SIZE];

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	static {
		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_COAL] = 0;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_COAL] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_COAL] = new int[] { Cells.CELL_TYPE_STONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_IRON] = 512;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_IRON] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_IRON] = new int[] { Cells.CELL_TYPE_STONE,
				Cells.CELL_TYPE_OVERSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_COPPER] = 512;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_COPPER] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_COPPER] = new int[] { Cells.CELL_TYPE_STONE,
				Cells.CELL_TYPE_OVERSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_TIN] = 512;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_TIN] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_TIN] = new int[] { Cells.CELL_TYPE_STONE,
				Cells.CELL_TYPE_OVERSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_ELECTRON] = 1024;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_ELECTRON] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_ELECTRON] = new int[] {
				Cells.CELL_TYPE_STONE, Cells.CELL_TYPE_OVERSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_SILVER] = 1024 + 512;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_SILVER] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_SILVER] = new int[] { Cells.CELL_TYPE_STONE,
				Cells.CELL_TYPE_OVERSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_GOLD] = 1024 + 512 + 256;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_GOLD] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_GOLD] = new int[] { Cells.CELL_TYPE_STONE,
				Cells.CELL_TYPE_OVERSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_DIAMOND] = 2048;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_DIAMOND] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_DIAMOND] = new int[] {
				Cells.CELL_TYPE_OVERSTONE, Cells.CELL_TYPE_SACREDSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_URANIUM] = 2048 + 512;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_URANIUM] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_URANIUM] = new int[] {
				Cells.CELL_TYPE_OVERSTONE, Cells.CELL_TYPE_SACREDSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_GRAVITON] = 2048 + 512 + 256;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_GRAVITON] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_GRAVITON] = new int[] { Cells.CELL_TYPE_SACREDSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_SPATIAL_RIFT] = 2048 + 1024;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_SPATIAL_RIFT] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_SPATIAL_RIFT] = new int[] { Cells.CELL_TYPE_SACREDSTONE };

		ORE_SPAWN_ALLOWED_PRESSURE_MIN[ORE_TYPE_DARK_ENERGY] = 2048 + 1024 + 256;
		ORE_SPAWN_ALLOWED_PRESSURE_MAX[ORE_TYPE_DARK_ENERGY] = Integer.MAX_VALUE;
		ORE_SPAWN_ALLOWED[ORE_TYPE_DARK_ENERGY] = new int[] { Cells.CELL_TYPE_SACREDSTONE };

		int j = 0;
		for (int i = 0; i < 11; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_COAL;
		for (int i = 0; i < 8; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_IRON;
		for (int i = 0; i < 8; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_COPPER;
		for (int i = 0; i < 8; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_TIN;
		for (int i = 0; i < 8; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_ELECTRON;
		for (int i = 0; i < 7; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_SILVER;
		for (int i = 0; i < 5; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_GOLD;
		for (int i = 0; i < 3; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_DIAMOND;
		for (int i = 0; i < 3; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_URANIUM;
		for (int i = 0; i < 1; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_GRAVITON;
		for (int i = 0; i < 1; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_SPATIAL_RIFT;
		for (int i = 0; i < 1; ++i)
			ORE_SPAWN[j++] = ORE_TYPE_DARK_ENERGY;
	}

	private TerrainGenerator() {
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

	public static int read(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId) {
		if (pTmpId.x == pTmpN.x) {
			try {
				pTmpN.x = pFile.read(pBuffer, 0, BUFFER_SIZE);
				if (pTmpN.x == -1)
					return -1;
			} catch (IOException e) {
				e.printStackTrace();
			}
			pTmpId.x = 0;
		}
		return pBuffer[pTmpId.x++] + 128;
	}

	public static void write(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId, int pVal) {
		if (pTmpId.x == BUFFER_SIZE) {
			try {
				pFile.write(pBuffer, 0, BUFFER_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			pTmpId.x = 0;
		}
		pBuffer[pTmpId.x++] = (byte) (pVal - 128);
	}

	public static void write(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId) {
		try {
			pFile.write(pBuffer, 0, pTmpId.x);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int readInt(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId) {
		return read(pFile, pBuffer, pTmpN, pTmpId)
				| (read(pFile, pBuffer, pTmpN, pTmpId) << 8)
				| (read(pFile, pBuffer, pTmpN, pTmpId) << 16)
				| (read(pFile, pBuffer, pTmpN, pTmpId) << 24);
	}

	public static long readLong(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId) {
		return ((long) read(pFile, pBuffer, pTmpN, pTmpId))
				| (((long) read(pFile, pBuffer, pTmpN, pTmpId)) << 8)
				| (((long) read(pFile, pBuffer, pTmpN, pTmpId)) << 16)
				| (((long) read(pFile, pBuffer, pTmpN, pTmpId)) << 24)
				| (((long) read(pFile, pBuffer, pTmpN, pTmpId)) << 32)
				| (((long) read(pFile, pBuffer, pTmpN, pTmpId)) << 40)
				| (((long) read(pFile, pBuffer, pTmpN, pTmpId)) << 48)
				| (((long) read(pFile, pBuffer, pTmpN, pTmpId)) << 56);
	}

	public static float readFloat(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId) {
		return Float.intBitsToFloat(readInt(pFile, pBuffer, pTmpN, pTmpId));
	}

	public static void writeInt(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId, int pVal) {
		write(pFile, pBuffer, pTmpN, pTmpId, pVal & 255);
		write(pFile, pBuffer, pTmpN, pTmpId, (pVal >>> 8) & 255);
		write(pFile, pBuffer, pTmpN, pTmpId, (pVal >>> 16) & 255);
		write(pFile, pBuffer, pTmpN, pTmpId, pVal >>> 24);
	}

	public static void writeLong(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId, long pVal) {
		write(pFile, pBuffer, pTmpN, pTmpId, (int) (pVal & 255));
		write(pFile, pBuffer, pTmpN, pTmpId, (int) ((pVal >>> 8) & 255));
		write(pFile, pBuffer, pTmpN, pTmpId, (int) ((pVal >>> 16) & 255));
		write(pFile, pBuffer, pTmpN, pTmpId, (int) ((pVal >>> 24) & 255));
		write(pFile, pBuffer, pTmpN, pTmpId, (int) ((pVal >>> 32) & 255));
		write(pFile, pBuffer, pTmpN, pTmpId, (int) ((pVal >>> 40) & 255));
		write(pFile, pBuffer, pTmpN, pTmpId, (int) ((pVal >>> 48) & 255));
		write(pFile, pBuffer, pTmpN, pTmpId, (int) (pVal >>> 56));
	}

	public static void writeFloat(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId, float pVal) {
		writeInt(pFile, pBuffer, pTmpN, pTmpId, Float.floatToRawIntBits(pVal));
	}

	public static CellData readCellData(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId) {
		int type = read(pFile, pBuffer, pTmpN, pTmpId);
		switch (type) {
		case 0:
			return null;
		case 1: {
			TeeCellData teeCellData = TeeCellData.obtain(
					readInt(pFile, pBuffer, pTmpN, pTmpId), true);
			return teeCellData;
		}
		case 2: {
			LootCellData lootCellData = LootCellData.obtain();
			int num = readInt(pFile, pBuffer, pTmpN, pTmpId);
			for (int i = 0; i < num; ++i) {
				int item = readInt(pFile, pBuffer, pTmpN, pTmpId);
				CellData itemData = readCellData(pFile, pBuffer, pTmpN, pTmpId);
				int itemCount = readInt(pFile, pBuffer, pTmpN, pTmpId);
				lootCellData.push(item, itemData, itemCount);
			}
			return lootCellData;
		}
		default:
			return null;
		}
	}

	public static void writeCellData(RandomAccessFile pFile, byte[] pBuffer,
			IntVector1 pTmpN, IntVector1 pTmpId, CellData pCellData) {
		if (pCellData == null)
			write(pFile, pBuffer, pTmpN, pTmpId, 0);
		else if (pCellData instanceof TeeCellData) {
			write(pFile, pBuffer, pTmpN, pTmpId, 1);
			TeeCellData teeCellData = (TeeCellData) pCellData;
			writeInt(pFile, pBuffer, pTmpN, pTmpId, teeCellData.getId());
		} else if (pCellData instanceof LootCellData) {
			write(pFile, pBuffer, pTmpN, pTmpId, 2);
			LootCellData lootCellData = (LootCellData) pCellData;
			int num = lootCellData.getItems().size();
			writeInt(pFile, pBuffer, pTmpN, pTmpId, num);
			for (int i = 0; i < num; ++i) {
				int item = lootCellData.getItems().get(i);
				CellData itemData = (CellData) lootCellData.getItemsData().get(
						i);
				int itemCount = lootCellData.getItemsCount().get(i);
				writeInt(pFile, pBuffer, pTmpN, pTmpId, item);
				writeCellData(pFile, pBuffer, pTmpN, pTmpId, itemData);
				writeInt(pFile, pBuffer, pTmpN, pTmpId, itemCount);
			}
		} else {
			write(pFile, pBuffer, pTmpN, pTmpId, 0);
		}
	}

	public static void loadChunk(File pFile, ChunkCellularity pChunk, int pX,
			int pY) throws FileNotFoundException, IOException {

		RandomAccessFile randomAccessFile = new RandomAccessFile(pFile, "r");

		byte[] buffer = BinaryByteArrayPool.obtain(BUFFER_SIZE);
		IntVector1 tmpN = IntVector1.obtain(0);
		IntVector1 tmpId = IntVector1.obtain(0);
		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
			for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
				for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
					int cell = readInt(randomAccessFile, buffer, tmpN, tmpId);
					CellData cellData = readCellData(randomAccessFile, buffer,
							tmpN, tmpId);
					pChunk.setCell(x, y, z, cell, cellData);
					pChunk.setLight(x, y, z, 0, 0, 0);
					pChunk.setAI(x, y, z, 0);
				}
		int dynamicsN = readInt(randomAccessFile, buffer, tmpN, tmpId);
		for (int i = 0; i < dynamicsN; ++i) {
			float posX = readFloat(randomAccessFile, buffer, tmpN, tmpId);
			float posY = readFloat(randomAccessFile, buffer, tmpN, tmpId);
			float angle = readFloat(randomAccessFile, buffer, tmpN, tmpId);
			float vx = readFloat(randomAccessFile, buffer, tmpN, tmpId);
			float vy = readFloat(randomAccessFile, buffer, tmpN, tmpId);
			float angularVelocity = readFloat(randomAccessFile, buffer, tmpN,
					tmpId);
			DynamicCellularity cellularity = DynamicCellularity.obtain(
					pChunk.random(), posX, posY, angle);
			cellularity.getBody().setVelocity(vx, vy);
			cellularity.getBody().setAngularVelocity(angularVelocity);
			int num = readInt(randomAccessFile, buffer, tmpN, tmpId);
			for (int j = 0; j < num; ++j) {
				int x = read(randomAccessFile, buffer, tmpN, tmpId);
				int y = read(randomAccessFile, buffer, tmpN, tmpId);
				int z = read(randomAccessFile, buffer, tmpN, tmpId);
				int cell = readInt(randomAccessFile, buffer, tmpN, tmpId);
				CellData cellData = readCellData(randomAccessFile, buffer,
						tmpN, tmpId);
				cellularity.setCell(x, y, z, cell, cellData);
			}
			pChunk.pushCellularity(cellularity);
		}
		IntVector1.recycle(tmpN);
		IntVector1.recycle(tmpId);

		BinaryByteArrayPool.recycle(buffer);
		randomAccessFile.close();

		for (int i = 0; i < 16; ++i) {
			pChunk.precalcLight(pChunk.lightUpdate());
			pChunk.updateLight();
		}
		for (int i = 0; i < 16; ++i) {
			pChunk.precalcAI(pChunk.aiUpdate());
			pChunk.updateAI();
		}
		pChunk.drop();
		pChunk.postDrop();
		pChunk.refreshCells();
		pChunk.precalcCells(pChunk.cellUpdate());
		pChunk.precalcLight(pChunk.lightUpdate());
		pChunk.precalcAI(pChunk.aiUpdate());
	}

	public static void unloadChunk(File pFile, ChunkCellularity pChunk, int pX,
			int pY) throws IOException {

		RandomAccessFile randomAccessFile = new RandomAccessFile(pFile, "rw");

		randomAccessFile.setLength(0);
		byte[] buffer = BinaryByteArrayPool.obtain(BUFFER_SIZE);
		IntVector1 tmpN = IntVector1.obtain(0);
		IntVector1 tmpId = IntVector1.obtain(0);
		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
			for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
				for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
					int cell = pChunk.getCell(x, y, z);
					CellData cellData = pChunk.getCellData(x, y, z);
					writeInt(randomAccessFile, buffer, tmpN, tmpId, cell);
					writeCellData(randomAccessFile, buffer, tmpN, tmpId,
							cellData);
				}

		int dynamicsN = 0;
		LinkedHashTable[] dynamics = pChunk.getDynamics();
		for (int i = 0; i < Settings.CHUNK_REGIONS_SQUARE; ++i)
			dynamicsN += dynamics[i].size();
		writeInt(randomAccessFile, buffer, tmpN, tmpId, dynamicsN);
		for (int i = 0; i < Settings.CHUNK_REGIONS_SQUARE; ++i) {
			for (List.Node k = dynamics[i].begin(); k != dynamics[i].end(); k = dynamics[i]
					.next(k)) {
				DynamicCellularity cellularity = (DynamicCellularity) dynamics[i]
						.val(k);
				MetaBody body = cellularity.getBody();
				writeFloat(randomAccessFile, buffer, tmpN, tmpId,
						body.getPositionX());
				writeFloat(randomAccessFile, buffer, tmpN, tmpId,
						body.getPositionY());
				writeFloat(randomAccessFile, buffer, tmpN, tmpId,
						body.getAngle());
				writeFloat(randomAccessFile, buffer, tmpN, tmpId,
						body.getVelocityX());
				writeFloat(randomAccessFile, buffer, tmpN, tmpId,
						body.getVelocityY());
				writeFloat(randomAccessFile, buffer, tmpN, tmpId,
						body.getAngularVelocity());
				int num = cellularity.size();
				writeInt(randomAccessFile, buffer, tmpN, tmpId, num);
				for (int j = 0; j < num; ++j) {
					int x = cellularity.getCellKeyX(j);
					int y = cellularity.getCellKeyY(j);
					int z = cellularity.getCellKeyZ(j);
					write(randomAccessFile, buffer, tmpN, tmpId, x);
					write(randomAccessFile, buffer, tmpN, tmpId, y);
					write(randomAccessFile, buffer, tmpN, tmpId, z);
					writeInt(randomAccessFile, buffer, tmpN, tmpId,
							cellularity.getCell(x, y, z));
					writeCellData(randomAccessFile, buffer, tmpN, tmpId,
							cellularity.getCellData(x, y, z));
				}
			}
		}

		write(randomAccessFile, buffer, tmpN, tmpId);

		IntVector1.recycle(tmpN);
		IntVector1.recycle(tmpId);

		BinaryByteArrayPool.recycle(buffer);
		randomAccessFile.close();
	}

	public static void genBuilding(long pSeed, ChunkCellularity pChunk,
			int pOffsetX, int pOffsetY, int pWidth, int pStages) {
		int height = pStages * 6;
		for (int y = 0; y < 1; ++y)
			for (int j = -y; j < pWidth + y; ++j) {
				pChunk.setCell(pOffsetX + j, pOffsetY - 1 - y, 0,
						Cells.CELL_TYPE_SACREDSTONE, null);
				pChunk.setCell(pOffsetX + j, pOffsetY - 1 - y, 1,
						Cells.CELL_TYPE_SACREDSTONE, null);
			}
		for (int j = 0; j < height; ++j)
			for (int k = 0; k < pWidth; ++k)
				pChunk.setCell(pOffsetX + k, pOffsetY + j, 1,
						Cells.CELL_TYPE_VACUUM, null);
		for (int j = 0; j < height; ++j)
			for (int k = 0; k < pWidth; ++k)
				pChunk.setCell(pOffsetX + k, pOffsetY + j, 0,
						Cells.CELL_TYPE_AIR, null);
		for (int j = 0; j < height; ++j)
			pChunk.setCell(pOffsetX, pOffsetY + j, 0,
					Cells.CELL_TYPE_SACREDSTONE, null);
		for (int j = 0; j < height; ++j)
			pChunk.setCell(pOffsetX + pWidth - 1, pOffsetY + j, 0,
					Cells.CELL_TYPE_SACREDSTONE, null);
		for (int j = 5; j < height; j += 6)
			for (int k = 1; k < pWidth - 1; ++k)
				pChunk.setCell(pOffsetX + k, pOffsetY + j, 0,
						Cells.CELL_TYPE_SACREDSTONE, null);
		for (int j = 0; j < 3; ++j) {
			pChunk.setCell(pOffsetX, pOffsetY + j, 0, Cells.CELL_TYPE_AIR, null);
			pChunk.setCell(pOffsetX + pWidth - 1, pOffsetY + j, 0,
					Cells.CELL_TYPE_AIR, null);
		}
		for (int j = 7; j < height; j += 6) {
			int cell;
			if ((NoiseMath.combine(pSeed, NoiseMath.combine(j, 1)) & 1) != 0)
				cell = Cells.CELL_TYPE_GLASS;
			else
				cell = Cells.CELL_TYPE_SACREDSTONE;
			pChunk.setCell(pOffsetX, pOffsetY + j, 0, cell, null);
			pChunk.setCell(pOffsetX, pOffsetY + j + 1, 0, cell, null);
			pChunk.setCell(pOffsetX, pOffsetY + j + 2, 0, cell, null);
			if ((NoiseMath.combine(pSeed, NoiseMath.combine(j, 2)) & 1) != 0)
				cell = Cells.CELL_TYPE_GLASS;
			else
				cell = Cells.CELL_TYPE_SACREDSTONE;
			pChunk.setCell(pOffsetX + pWidth - 1, pOffsetY + j, 0, cell, null);
			pChunk.setCell(pOffsetX + pWidth - 1, pOffsetY + j + 1, 0, cell,
					null);
			pChunk.setCell(pOffsetX + pWidth - 1, pOffsetY + j + 2, 0, cell,
					null);
		}
		for (int j = 0; j < 8; ++j) {
			long tmp = NoiseMath.combine(pSeed, j) & Long.MAX_VALUE;
			int x1 = (int) (tmp % (pWidth - 10) + 1);
			tmp /= pWidth - 6;
			int y11 = (int) (tmp % (pStages - 1));
			tmp /= pStages - 1;
			int y12 = (int) (tmp % (pStages - 1));
			tmp /= pStages - 1;
			if (y11 > y12) {
				int y13 = y11;
				y11 = y12;
				y12 = y13;
			}
			boolean flag = false;
			for (int k = y11 * 6; k <= y12 * 6 + 6; ++k) {
				for (int i = x1; i < x1 + 5; ++i)
					if (pChunk.getCell(pOffsetX + i, pOffsetY + k, 1) != Cells.CELL_TYPE_VACUUM) {
						flag = true;
						break;
					}
				if (flag)
					break;
			}
			if (flag)
				continue;
			pChunk.setCell(pOffsetX + x1 + 1, pOffsetY + y11 * 6, 0,
					Cells.CELL_TYPE_OVERSTONE, null);
			for (int k = y11; k <= y12; ++k) {
				for (int i = 0; i < 4; ++i)
					pChunk.setCell(pOffsetX + x1 + i, pOffsetY + k * 6 + 5, 0,
							Cells.CELL_TYPE_AIR, null);
				pChunk.setCell(pOffsetX + x1 + 2, pOffsetY + k * 6 + 1, 0,
						Cells.CELL_TYPE_OVERSTONE, null);
				pChunk.setCell(pOffsetX + x1 + 3, pOffsetY + k * 6 + 2, 0,
						Cells.CELL_TYPE_OVERSTONE, null);
				pChunk.setCell(pOffsetX + x1, pOffsetY + k * 6 + 5, 0,
						Cells.CELL_TYPE_OVERSTONE, null);
				pChunk.setCell(pOffsetX + x1 + 1, pOffsetY + k * 6 + 4, 0,
						Cells.CELL_TYPE_OVERSTONE, null);
				for (int i = 0; i < 5; ++i)
					pChunk.setCell(pOffsetX + x1 + 4, pOffsetY + k * 6 + i, 0,
							Cells.CELL_TYPE_SACREDSTONE, null);
			}
			for (int k = y11 * 6; k <= y12 * 6 + 6; ++k)
				for (int i = x1; i < x1 + 5; ++i)
					pChunk.setCell(pOffsetX + i, pOffsetY + k, 1,
							Cells.CELL_TYPE_AIR, null);
		}
		for (int j = 0; j < height; ++j)
			for (int k = 0; k < pWidth; ++k)
				pChunk.setCell(pOffsetX + k, pOffsetY + j, 1,
						Cells.CELL_TYPE_SACREDSTONE, null);
	}

	public static void genCity(long pSeed, ChunkCellularity pChunk, int pX,
			int pY) {
		int cityL = 0;
		int cityR = 128;
		int cityB = 0;
		int cityT = 128;
		int buildingMinGap = 4;
		long cityParam = 75;
		long h = NoiseMath.combine(NoiseMath.combine(pSeed, cityParam),
				NoiseMath.combine(pX, pY));
		for (int i = 0; i < 32; ++i) {
			int x = (int) (NoiseMath.combine(h, i * 3) & 127);
			int stages = (int) (NoiseMath.combine(h, i * 3 + 1) & 15) + 2;
			int height = stages * 6;
			int width = (int) (NoiseMath.combine(h, i * 3 + 2) & 31) + 11;
			int y = Math.min(getSpawnY(pSeed, x + (pX << CHUNK_SIZE_LOG), 0)
					- (pY << CHUNK_SIZE_LOG),
					getSpawnY(pSeed, x + width - 1 + (pX << CHUNK_SIZE_LOG), 0)
							- (pY << CHUNK_SIZE_LOG));
			if (cityL <= x && x + width <= cityR && cityB <= y - 1
					&& y + height <= cityT) {
				boolean flag = true;
				for (int k = 0; k < height; ++k)
					for (int j = -buildingMinGap; j < width + buildingMinGap; ++j) {
						int cell = pChunk.getCell(cityL + x + j, y + k, 1);
						if (cell != Cells.CELL_TYPE_AIR
								&& cell != Cells.CELL_TYPE_GROUND
								&& cell != Cells.CELL_TYPE_STONE) {
							flag = false;
							break;
						}
					}
				if (flag) {
					genBuilding(NoiseMath.combine(h, i), pChunk, cityL + x, y,
							width, stages);
				}
			}
		}
	}

	public static void genChunk(long pSeed, ChunkCellularity pChunk, int pX,
			int pY) {
		int offsetX = pX << Settings.CHUNK_SIZE_LOG;
		int offsetY = pY << Settings.CHUNK_SIZE_LOG;
		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
			for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
				for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
					pChunk.setCell(x, y, z, TerrainGenerator.genWorld(pSeed,
							offsetX + x, offsetY + y, z), null);
					pChunk.setLight(x, y, z, 0, 0, 0);
					pChunk.setAI(x, y, z, 0);
				}
		// genCity(pSeed, pChunk, pX, pY);
		long h = NoiseMath.combine(pSeed, NoiseMath.combine(pX, pY));
		for (int i = 0; i < 128; ++i) {
			long val = (int) NoiseMath.combine(h, i);
			int x0 = (int) (val & CHUNK_SIZE_MASK);
			int y0 = (int) ((val >>> CHUNK_SIZE_LOG) & CHUNK_SIZE_MASK);
			int z0 = (int) ((val >>> CHUNK_SQUARE_LOG) & CHUNK_DEPTH_MASK);
			val >>>= CHUNK_VOLUME_LOG;
			int ore = ORE_SPAWN[(int) (val & ORE_SPAWN_SIZE_MASK)];
			val >>>= ORE_SPAWN_SIZE_LOG;
			int pressure = genHeight(pSeed, x0, z0) - offsetY - y0;
			if (ORE_SPAWN_ALLOWED_PRESSURE_MIN[ore] <= pressure
					&& pressure <= ORE_SPAWN_ALLOWED_PRESSURE_MAX[ore])
				for (int vz = -1; vz <= 1; ++vz)
					for (int vy = -1; vy <= 1; ++vy)
						for (int vx = -1; vx <= 1; ++vx) {
							if ((val & 1) != 0) {
								int x = x0 + vx;
								int y = y0 + vy;
								int z = z0 + vz;
								if (CHUNK_LEFT <= x && x <= CHUNK_RIGHT
										&& CHUNK_BOTTOM <= y && y <= CHUNK_TOP
										&& CHUNK_MIN_DEPTH <= z
										&& z <= CHUNK_MAX_DEPTH) {
									int cell = pChunk.getCell(x, y, z);
									boolean flag = false;
									int[] allowed = ORE_SPAWN_ALLOWED[ore];
									for (int k = 0; k < allowed.length; ++k)
										if (cell == allowed[k]) {
											flag = true;
											break;
										}
									if (flag)
										pChunk.setCell(x, y, z,
												ORE_SPAWN_TYPES[ore], null);
								}
							}
							val >>>= 1;
						}

		}
		for (int i = 0; i < 16; ++i) {
			pChunk.precalcCells(pChunk.cellUpdate());
			pChunk.updateCells();
		}
		for (int i = 0; i < 16; ++i) {
			pChunk.precalcLight(pChunk.lightUpdate());
			pChunk.updateLight();
		}
		long seedGrassParam = NoiseMath.combine(pSeed, GRASS_PARAM);
		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z) {
			long zSeedGrassParam = NoiseMath.combine(seedGrassParam, z);
			for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y) {
				long yzSeedGrassParam = NoiseMath.combine(zSeedGrassParam,
						offsetY + y);
				for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
					int[] light = pChunk.getLight(x, y, z);
					if (Math.max(light[0], Math.max(light[1], light[2])) >= 128
							&& pChunk.getCell(x, y, z) == Cells.CELL_TYPE_AIR
							&& pChunk.getCell(x, y - 1, z) == Cells.CELL_TYPE_GROUND) {
						long xyzSeedGrassParam = NoiseMath.combine(
								yzSeedGrassParam, offsetX + x);
						switch ((int) (xyzSeedGrassParam & 15)) {
						case 0:
						case 1:
						case 2:
							pChunk.setCell(x, y, z, Cells.CELL_TYPE_GRASS, null);
							break;
						case 3:
							if (CellsAction.checkTree(pChunk, x, y, z))
								CellsAction.genTree(pChunk, x, y, z);
							else
								pChunk.setCell(x, y, z,
										Cells.CELL_TYPE_SAPLING, null);
							break;
						}
					}
				}
			}
		}
		for (int i = 0; i < 16; ++i) {
			pChunk.precalcAI(pChunk.aiUpdate());
			pChunk.updateAI();
		}
		pChunk.drop();
		pChunk.postDrop();
		pChunk.refreshCells();
		pChunk.precalcCells(pChunk.cellUpdate());
		pChunk.precalcLight(pChunk.lightUpdate());
		pChunk.precalcAI(pChunk.aiUpdate());
	}

	public static strictfp int getSurfaceY(long pSeed, int pX, int pZ) {
		int height = genHeight(pSeed, pX, pZ);
		for (int i = height;; --i)
			if (genTerrain(pSeed, pX, i, pZ) != Cells.CELL_TYPE_AIR)
				return i;
	}

	public static strictfp int getSpawnY(long pSeed, int pX, int pZ) {
		int height = genHeight(pSeed, pX, pZ);
		int l = height - HEIGHT_AMPLITUDE, r = height + 1;
		while (l + 1 < r) {
			int m = l + r >> 1;
			if (genTerrain(pSeed, pX, m, pZ) == Cells.CELL_TYPE_AIR)
				r = m;
			else
				l = m;
		}
		return r;
	}

	public static strictfp int genHeight(long pSeed, int pX, int pZ) {
		return (int) (HEIGHT_AMPLITUDE * NoiseMath.perlinOctaveNoise(
				NoiseMath.combine(pSeed, HEIGHT_PARAM), pX / 4096.0, pZ / 64.0,
				2.0, 0.5, 4));
	}

	public static strictfp double genDensity(long pSeed, int pX, int pY, int pZ) {
		return NoiseMath.perlinOctaveNoise(
				NoiseMath.combine(pSeed, DENSITY_PARAM), pX / 256.0,
				pY / 256.0, pZ / 64.0, 2.0, 0.5, 6);
	}

	public static strictfp int genTerrain(long pSeed, int pX, int pY, int pZ) {

		int ans = Cells.CELL_TYPE_AIR;

		int height = genHeight(pSeed, pX, pZ);
		double pressure = (height - pY) / (float) HEIGHT_AMPLITUDE;
		double density = genDensity(pSeed, pX, pY, pZ);

		if (density + pressure >= 1.0) {
			if (density + pressure / 2 <= 0.85) {
				if (density + pressure <= 1.01
						&& (height - pY + 1) / (float) HEIGHT_AMPLITUDE
								+ genDensity(pSeed, pX, pY - 1, pZ) < density
								+ pressure)
					ans = Cells.CELL_TYPE_STONE;
				else {
					if (density * 2 < pressure
							&& density + pressure <= 1.05
							&& (height - pY - 1) / (float) HEIGHT_AMPLITUDE
									+ genDensity(pSeed, pX, pY + 1, pZ) < density
									+ pressure)
						ans = Cells.CELL_TYPE_SAND;
					else
						ans = Cells.CELL_TYPE_GROUND;
				}
			} else {
				if (density + pressure <= 2.0 - pZ * 0.3)
					ans = Cells.CELL_TYPE_STONE;
				else if (density + pressure <= 3.0 - pZ * 0.6)
					ans = Cells.CELL_TYPE_OVERSTONE;
				else if (density + pressure <= 4.0 - pZ * 0.9)
					ans = Cells.CELL_TYPE_SACREDSTONE;
				else
					ans = Cells.CELL_TYPE_FINALSTONE;
			}
		}

		return ans;
	}

	public static strictfp int genWorld(long pSeed, int pX, int pY, int pZ) {

		int ans = genTerrain(pSeed, pX, pY, pZ);

		// Caves
		if (Settings.CHUNK_MAX_DEPTH != pZ) {
			for (int i = CAVES_PARAM_MIN; i <= CAVES_PARAM_MAX; ++i) {
				double val = NoiseMath.perlinOctaveNoise(
						NoiseMath.combine(pSeed, i), pX / 512.0, pY / 256.0,
						pZ / 64.0, 2.0, 0.5, 6);
				if (0.5 - 0.005 < val && val < 0.5 + 0.005) {
					ans = Cells.CELL_TYPE_AIR;
					break;
				}
			}
		}

		return ans;
	}

	public static void genChunkBackground(long pSeed, ChunkCellularity pChunk,
			int pX, int pY) {
		int offsetX = pX << Settings.CHUNK_SIZE_LOG;
		int offsetY = pY << Settings.CHUNK_SIZE_LOG;
		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z)
			for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y)
				for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
					pChunk.setCell(
							x,
							y,
							z,
							TerrainGenerator.genWorldBackground(pSeed, offsetX
									+ x, offsetY + y, z), null);
					pChunk.setLight(x, y, z, 0, 0, 0);
					pChunk.setAI(x, y, z, 0);
				}
		for (int i = 0; i < 16; ++i) {
			pChunk.precalcCells(pChunk.cellUpdate());
			pChunk.updateCells();
		}
		for (int i = 0; i < 16; ++i) {
			pChunk.precalcLight(pChunk.lightUpdate());
			pChunk.updateLight();
		}
		long seedGrassParam = NoiseMath.combine(pSeed, GRASS_PARAM);
		for (int z = Settings.CHUNK_MIN_DEPTH; z <= Settings.CHUNK_MAX_DEPTH; ++z) {
			long zSeedGrassParam = NoiseMath.combine(seedGrassParam, z);
			for (int y = Settings.CHUNK_BOTTOM; y <= Settings.CHUNK_TOP; ++y) {
				long yzSeedGrassParam = NoiseMath.combine(zSeedGrassParam,
						offsetY + y);
				for (int x = Settings.CHUNK_LEFT; x <= Settings.CHUNK_RIGHT; ++x) {
					int[] light = pChunk.getLight(x, y, z);
					if (Math.max(light[0], Math.max(light[1], light[2])) >= 128
							&& pChunk.getCell(x, y, z) == Cells.CELL_TYPE_AIR
							&& pChunk.getCell(x, y - 1, z) == Cells.CELL_TYPE_GROUND) {
						long xyzSeedGrassParam = NoiseMath.combine(
								yzSeedGrassParam, offsetX + x);
						if ((xyzSeedGrassParam & 3) == 0)
							pChunk.setCell(x, y, z, Cells.CELL_TYPE_GRASS, null);
					}
				}
			}
		}
		for (int i = 0; i < 16; ++i) {
			pChunk.precalcAI(pChunk.aiUpdate());
			pChunk.updateAI();
		}
		pChunk.drop();
		pChunk.postDrop();
		pChunk.refreshCells();
		pChunk.precalcCells(pChunk.cellUpdate());
		pChunk.precalcLight(pChunk.lightUpdate());
		pChunk.precalcAI(pChunk.aiUpdate());
	}

	public static strictfp double genDensityBackground(long pSeed, int pX,
			int pY, int pZ) {
		return NoiseMath.perlinOctaveNoise(
				NoiseMath.combine(pSeed, DENSITY_PARAM), pX / 64.0, pY / 64.0,
				pZ / 64.0, 2.0, 1.0, 4);
	}

	public static strictfp int genTerrainBackground(long pSeed, int pX, int pY,
			int pZ) {

		int ans = Cells.CELL_TYPE_AIR;

		double density = genDensityBackground(pSeed, pX, pY, pZ);

		if (density >= 0.5) {
			if (density <= 0.6) {
				ans = Cells.CELL_TYPE_GROUND;
			} else {
				ans = Cells.CELL_TYPE_STONE;
			}
		}

		return ans;
	}

	public static strictfp int genWorldBackground(long pSeed, int pX, int pY,
			int pZ) {

		int ans = genTerrainBackground(pSeed, pX, pY, pZ);

		// Caves
		if (Settings.CHUNK_MAX_DEPTH != pZ) {
			for (int i = CAVES_PARAM_MIN; i <= CAVES_PARAM_MAX; ++i) {
				double val = NoiseMath.perlinOctaveNoise(
						NoiseMath.combine(pSeed, i), pX / 512.0, pY / 256.0,
						pZ / 64.0, 2.0, 0.5, 6);
				if (0.5 - 0.005 < val && val < 0.5 + 0.005) {
					ans = Cells.CELL_TYPE_AIR;
					break;
				}
			}
		}

		return ans;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
