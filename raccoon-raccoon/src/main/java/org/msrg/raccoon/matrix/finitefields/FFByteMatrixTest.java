/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.finitefields;

import org.msrg.raccoon.matrix.finitefields.FFByteMatrix;

import org.msrg.raccoon.matrix.MatrixFactory;

import junit.framework.TestCase;

public class FFByteMatrixTest extends TestCase {

	int matrixSize = 100;
	
	@Override
	public void setUp() {
		FFByteMatrix._MAX_ROWS_PRINT = 25;
		FFByteMatrix._MAX_COLS_PRINT = 25;
	}
	
	public void testM2timesInvIsM() {
		int rows = matrixSize, cols = rows;
		System.out.println("***** " + "testM2timesInvIsM");
		System.out.print("Creating [" + rows + "x" + cols + "] ...");
		long start = System.currentTimeMillis();
		FFByteMatrix m = FFByteMatrix.createRandomByteMatrix(rows, cols);
		long end = System.currentTimeMillis();
		System.out.println(" " + (end-start) + "ms.");

		System.out.print("Multiplying ...");
		start = System.currentTimeMillis();
		FFByteMatrix mP2 = (FFByteMatrix) MatrixFactory.multiply(m, m);
		end = System.currentTimeMillis();
		System.out.println(" " + (end-start) + "ms.");
		
		
		System.out.print("Inversing ...");
		start = System.currentTimeMillis();
		FFByteMatrix mInv = (FFByteMatrix) m.inverseMatrix();
		end = System.currentTimeMillis();
		System.out.println(" " + (end-start) + "ms.");
		
		
		System.out.print("Multiplying ...");
		start = System.currentTimeMillis();
		FFByteMatrix mInvTimesmP2 = (FFByteMatrix) MatrixFactory.multiply(mP2, mInv);
		end = System.currentTimeMillis();
		System.out.println(" " + (end-start) + "ms.");
		

		System.out.print("Verifying ...");
		start = System.currentTimeMillis();
		end = System.currentTimeMillis();
		System.out.println(" " + (end-start) + "ms.");
		
		assertTrue(mInvTimesmP2.equals(m));
		System.out.println("OK!");
		System.out.println();
	}
	
	public void testMtimesMInvIsIdentity() {
		System.out.println("***** " + "testMtimesMInvIsIdentity");
		System.out.print("Creating...");
		long start = System.currentTimeMillis();
		FFByteMatrix m = FFByteMatrix.createRandomSquareByteMatrix(matrixSize);
		long end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + "ms.");
				
		
		System.out.print("Inversing ...");
		start = System.currentTimeMillis();		
		FFByteMatrix mInv = (FFByteMatrix) m.inverseMatrix();
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + "ms.");

		
		System.out.print("Verifying ...");
		start = System.currentTimeMillis();
		FFByteMatrix mTimesMInv = (FFByteMatrix) MatrixFactory.multiply(m, mInv);
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + "ms.");

		assertTrue(mTimesMInv.isIdentity());
		System.out.println("OK!");
		System.out.println();
	}
}
