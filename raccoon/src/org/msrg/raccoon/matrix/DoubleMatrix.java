/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix;

import java.util.Random;

import org.msrg.raccoon.matrix.DATATYPES;
import org.msrg.raccoon.matrix.DoubleMatrix;
import org.msrg.raccoon.matrix.MatrixFactory;
import org.msrg.raccoon.matrix.TypedMatrix;

public class DoubleMatrix extends TypedMatrix<Float>{

	public DoubleMatrix(Float[][] b) {
		super(b);
	}

	public DoubleMatrix(int rows, int cols) {
		super(new Float[rows][cols]);
	}

	public DoubleMatrix getZeroMatrix(int rows, int cols) {
		return new DoubleMatrix(rows, cols);
	}

	@Override
	public Float getZero() {
		return GetZero();
	}
	
	public static Float GetZero() {
		return new Float(0);
	}
	
	@Override
	public Float getOne() {
		return GetOne();
	}
	
	public static Float GetOne() {
		return new Float(1);
	}
	
	public static DoubleMatrix createIdentityMatrix(int size) {
		Float[][] b = new Float[size][];
		for(int i=0 ; i<size ; i++) {
			Float[] randB = new Float[size];
			for(int j=0 ; j<size ; j++)
				randB[j] = (i==j? GetOne(): GetZero());
			b[i] = randB;
		}
		
		return new DoubleMatrix(b);
	}
	
	public static DoubleMatrix createRandomDoubleMatrix(int rows, int cols) {
		return createRandomMatrix(DATATYPES.DOUBLE, rows, cols);
	}
	
	public static DoubleMatrix createRandomSquareIntegerMatrix(int rowsCols) {
		return createRandomIntegerMatrix(rowsCols, rowsCols);
	}
	
	public static DoubleMatrix createRandomIntegerMatrix(int rows, int cols) {
		return createRandomMatrix(DATATYPES.INTEGER, rows, cols);
	}
	
	static final Random _RANDOM = new Random();
	static DoubleMatrix createRandomMatrix(DATATYPES dt, int rows, int cols) {	
		Float[][] b = new Float[rows][];
		for(int i=0 ; i<b.length ; i++) {
			Float[] randB = new Float[cols];
			for(int j=0 ; j<randB.length ; j++)
				switch(dt) {
				case DOUBLE:
					randB[j] = _RANDOM.nextFloat();
					break;
					
				case INTEGER:
					randB[j] = _RANDOM.nextFloat();
					break;
					
				default:
					throw new UnsupportedOperationException("Unknown: " + dt);
				}
			b[i] = randB;
		}
		
		return new DoubleMatrix(b);
	}

	@Override
	public TypedMatrix<Float> decloneExtended() {
		int halfCols = _cols/2;
		Float[][] b = new Float[_rows][halfCols];
		for(int i=0 ; i<halfCols ; i++)
			for(int j=0 ; j<halfCols ; j++)
				b[i][j] = clone(_b[i][j + halfCols]);

		return new DoubleMatrix(b);
	}
	
	@Override
	public TypedMatrix<Float> cloneExtended() {
		Float[][] b = new Float[_rows][_cols * 2];
		for(int i=0 ; i<getRowSize() ; i++) {
			
			for(int j=0 ; j<getColumnSize() ; j++)
				b[i][j] = clone(_b[i][j]);
			
			for(int j=0 ; j<getColumnSize() ; j++)
				if (i==j)
					b[i][j + _cols] = getOne();
				else
					b[i][j + _cols] = getZero();
			
			}
		return new DoubleMatrix(b);
	}
	
	@Override
	public DoubleMatrix clone() {
		Float[][] b = new Float[_rows][_cols];
		for(int i=0 ; i<getRowSize() ; i++)
			for(int j=0 ; j<getColumnSize() ; j++)
				b[i][j] = clone(_b[i][j]);
			
		return new DoubleMatrix(b);
	}

	@Override
	public Float add(Float a, Float b) {
		return a.floatValue() + b.byteValue();
	}

	@Override
	public Float multiply(Float a, Float b) {
		return a.floatValue() * b.floatValue();
	}

	public static synchronized DoubleMatrix createMatrix(int rows, String str) {
		String[] bStr = str.split(",");
		int count = bStr.length;
		int cols = count/rows;
		Float[][] b = new Float[rows][cols];
		for(int i=0 ; i<rows ; i++)
			for(int j=0 ; j<cols ; j++) {
				String subStr = bStr[i * cols + j];
				b[i][j] = new Float(subStr);
			}
		
		return new DoubleMatrix(b);
	}

	public static void main0(String[] argv) {
		System.out.println("Creating m1");
		DoubleMatrix m1 = createRandomIntegerMatrix(10, 11);
//		DoubleMatrix m1 = createIdentityMatrix(10);
		
		System.out.println("Creating m2");
		DoubleMatrix m2 = (DoubleMatrix) createRandomIntegerMatrix(11, 12).multiply(new Float(3));
//		DoubleMatrix m2 = (DoubleMatrix) createIdentityMatrix(10).multiply(new DoubleDataType(3));
		
		System.out.println("Creating m3");
		DoubleMatrix m3 = (DoubleMatrix) MatrixFactory.multiply(m1, m2);
		
		System.out.println("Creating m4");
		DoubleMatrix m4 = (DoubleMatrix) MatrixFactory.add(m2, m2);
		
		
		String multStr = TypedMatrix.toStringMult(m1, m2, m3);
		System.out.println(multStr);
		System.out.println();

		String addStr = TypedMatrix.toStringAdd(m2, m2, m4);
		System.out.println(addStr);
		
		System.out.println(m1.toStringShort());
		System.out.println(m2.toStringShort());
		System.out.println(m3.toStringShort());
		System.out.println(m4.toStringShort());
	}
	
	public static void main(String[] argv) {
		DoubleMatrix m = DoubleMatrix.createMatrix(3,
				"2,1,2,1,1,3,2,1,4"
//				"2,1,-1,-3,-1,2,-2,1,2"// + "," +		// 1
//				"2,1,-1,-3,-1,2,-2,1,2" + "," + 	// 2
//				"2,1,-1,-3,-1,2,-2,1,2" + "," + 	// 3
//				"2,1,-1,-3,1,2,-2,1,2" + "," + 		// 4
//				"2,1,-1,-3,-1,2,-2,1,2" + "," + 	// 5
//				"2,1,-1,-3,-1,2,-2,1,2" + "," + 	// 6
//				"2,1,-1,-3,-1,2,-2,1,2" + "," + 	// 7
//				"2,10,-1,-3,-1,2,-2,1,2" + "," + 	// 8
//				"2,1,-1,-3,-1,2,-2,1,2"				// 9
				);
//		DoubleMatrix m = DoubleMatrix.createRandomSquareIntegerMatrix(30);
		System.out.println(m);
		System.out.println();
				
		DoubleMatrix mInv = (DoubleMatrix) m.inverseMatrix();
		System.out.println(mInv);

		System.out.println();
		
		DoubleMatrix mTimesMInv = (DoubleMatrix) MatrixFactory.multiply(m, mInv);
		System.out.println(mTimesMInv);
		
		System.out.println(mTimesMInv.isIdentity());
	}

	@Override
	protected Float[][] getEmptyArray(int rows, int cols) {
		return new Float[rows][cols];
	}

	@Override
	protected TypedMatrix<Float> getNullMatrix(int rows, int cols) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float clone(Float a) {
		return new Float(a);
	}

	@Override
	public int compareToAbs(Float a, Float b) {
		if (a > b)
			return 1;
		else if (a < b)
			return -1;
		else
			return 0;
	}

	@Override
	public Float divide(Float a, Float b) {
		return a / b;
	}

	@Override
	public Float inverse(Float a) {
		return 1 / a;
	}

	@Override
	public boolean isOne(Float a) {
		return a == 1;
	}

	@Override
	public boolean isZero(Float a) {
		return a == 0;
	}

	@Override
	public Float multiplyAndAddInPlace(Float total, Float a, Float b) {
		total = total * (a + b);
		return total;
	}

	@Override
	public Float subtract(Float a, Float b) {
		return a - b;
	}

	@Override
	protected String toString(Float a) {
		return String.format("%1.5f", a);
	}
}

enum DATATYPES {
	INTEGER,
	DOUBLE
};
