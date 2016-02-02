/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.finitefields;

import org.msrg.raccoon.matrix.finitefields.ByteMatrix;

import org.msrg.raccoon.matrix.MatrixFactory;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;

import junit.framework.TestCase;

public class ByteMatrixTest extends TestCase {

	double BULK_SIZE = (1000 * 1000) * 1;
	int BYTE_MATRIX_SIZE = 100;
	
	@Override
	public void setUp() {
		ByteMatrix._MAX_ROWS_PRINT = 5;
		ByteMatrix._MAX_COLS_PRINT = 5;
	}
	
	public void testM2timesInvIsM_HasNoInvserse() {
		System.out.println("******* " + "testM2timesInvIsM");
		long start = System.currentTimeMillis();
		int rows = BYTE_MATRIX_SIZE, cols = BYTE_MATRIX_SIZE;
		System.out.print("Creating [" + rows + "x" + cols + "] ... ");
		long end = System.currentTimeMillis();
		ByteMatrix ncM = ByteMatrix.createMatrix(10,
				"-91,-115,-41,-102,-99,-67,66,70,-125,62," + 
				"14,3,-9,-3,-20,21,8,-64,-91,-93," +
				"-11,-81,84,-2,-15,2,102,5,-123,-67," +
				"-7,-24,56,-88,-12,-105,-17,-73,-105,-59," +
				"117,99,22,-27,10,92,24,67,-38,84," +
				"-89,15,-32,99,12,37,47,53,94,-63," +
				"69,-38,46,-36,114,-117,70,-6,-91,-111," +
				"-69,-112,25,85,-32,32,83,9,-100,37," +
				"119,-100,50,3,1,-86,-87,-8,-83,-44," +
				"59,-99,-36,86,39,-19,-126,120,42,21");
		System.out.println(" " + (end - start) + " ms.");
		
		runTestM2timesInvIsM(ncM, false);
	}
	
	public void testM2timesInvIsM_HasInvserse() {
		System.out.println("******* " + "testM2timesInvIsM");
		long start = System.currentTimeMillis();
		int rows = BYTE_MATRIX_SIZE, cols = BYTE_MATRIX_SIZE;
		System.out.print("Creating [" + rows + "x" + cols + "] ... ");
		long end = System.currentTimeMillis();
		ByteMatrix ncM = ByteMatrix.createRandomByteMatrix(rows, cols);
		System.out.println(" " + (end - start) + " ms.");
		
		runTestM2timesInvIsM(ncM, true);
	}
	
	protected void runTestM2timesInvIsM(ByteMatrix ncM, boolean hasInverse) {
		System.out.print("Multiplying... ");
		long start = System.currentTimeMillis();
		ByteMatrix ncMP2 = new ByteMatrix(
					ByteMatrix.wrap(
							ncM.multiply(ByteMatrix.unwrap(ncM.toArray()))
					)
				);
		long end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");
		
		
		System.out.print("Inversing[" + ncM.getRowSize() + "x" + ncM.getColumnSize() + "... ");
		start = System.currentTimeMillis();
		ByteMatrix ncMInv = (ByteMatrix) ncM.inverseMatrix();
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");

		int inverseRowZero = ncM.getInverseRowZero();
		if(!hasInverse) {
			assertNull(ncMInv);
			assertTrue(inverseRowZero >= 0);
			assertTrue(inverseRowZero < ncM.getRowSize());
			return;
		}
		
		assertNotNull(ncMInv);
		assertTrue(inverseRowZero == -1);
		System.out.print("Multiplying... ");
		start = System.currentTimeMillis();
		ByteMatrix ncMInvTimesncMP2 = (ByteMatrix) MatrixFactory.multiply(ncMP2, ncMInv);
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");

		System.out.print("Verifying... ");
		start = System.currentTimeMillis();
		assertTrue(ncMInvTimesncMP2.equals(ncM));
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");
		System.out.println("OK!");
	}

	public void testManyInverse() {
		System.out.println("******* " + "testManyInverse");
		int[] rowsAll = {25,50,75,100,120,150,175,200,225,250,400,500};
		for(int i=rowsAll.length -1 ; i>=0 ; i--)
			runTestManyInverse(rowsAll[i], 3);
	}
	
	public void runTestManyInverse(int rows, int runsPerRows) {
		for(int i=0 ; i<runsPerRows ; i++) {
			ByteMatrix ncM = ByteMatrix.createRandomByteMatrix(rows, rows);
			System.out.print("Inversing[" + ncM.getRowSize() + "x" + ncM.getColumnSize() + "]... ");
			double start = System.nanoTime();
			ByteMatrix ncMInv = (ByteMatrix)ncM.inverseMatrix();
			double end = System.nanoTime();
			System.out.println(String.format("%6f ms", (end - start)/1000000));
			assertNotNull(ncMInv);
		}
	}
	
	public void testMtimesBMtimesInvMIsBM() {
		System.out.println("******* " + "testMtimesBMtimesInvMIsBM");
		long start = System.currentTimeMillis();
		int rows = BYTE_MATRIX_SIZE, cols = BYTE_MATRIX_SIZE;
		System.out.print("Creating [" + rows + "x" + cols + "] ... ");
		long end = System.currentTimeMillis();
		ByteMatrix ncM = ByteMatrix.createRandomByteMatrix(rows, cols);
		System.out.println(" " + (end - start) + " ms.");
		
		
		System.out.print("Inversing[" + ncM.getRowSize() + "x" + ncM.getColumnSize() + "... ");
		start = System.currentTimeMillis();
		ByteMatrix ncMInv = (ByteMatrix) ncM.inverseMatrix();
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");
		
		
		System.out.print("Creating Bulk [" + BULK_SIZE + "] ... ");
		start = System.currentTimeMillis();
		BulkMatrix bm =
			BulkMatrix.createBulkMatixIncrementalData(
					ncM.getColumnSize(), (int) Math.ceil(BULK_SIZE/ncM.getColumnSize()));
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");

		
		System.out.print("Multiplying Bulk... ");
		start = System.currentTimeMillis();
		BulkMatrix ncMTimesBm = ncM.multiply(bm);
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");

		
		System.out.print("Multiplying Bulk and Inverse... ");
		start = System.currentTimeMillis();
		BulkMatrix ncMInvTimesncMTimesBm = ncMInv.multiply(ncMTimesBm);
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");
		
		
		System.out.print("Verifying equality... ");
		start = System.currentTimeMillis();
		assertTrue(ncMInvTimesncMTimesBm.equals(bm));
		end = System.currentTimeMillis();
		System.out.println("OK!");
	}
}
