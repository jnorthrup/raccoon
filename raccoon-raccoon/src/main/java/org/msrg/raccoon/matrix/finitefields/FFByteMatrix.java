/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.finitefields;

import java.util.Random;

import org.msrg.raccoon.matrix.finitefields.FFByteMatrix;

import org.msrg.raccoon.finitefields.Tables;
import org.msrg.raccoon.matrix.TypedMatrix;

public class FFByteMatrix extends TypedMatrix<Byte> {

	protected static final char[] DIG = {'0','1','2','3','4','5','6','7',
        '8','9','a','b','c','d','e','f'};
	protected static final Tables _tables = new Tables();

	public FFByteMatrix(int cols, int rows) {
		this(makeZero2d(cols, rows));
	}
	
	protected static Byte[][] makeZero2d(int rows, int cols) {
		Byte[][] bTemp = new Byte[rows][];
		for(int i=0 ; i<rows ; i++) {
			bTemp[i] = new Byte[cols];
			for(int j=0 ; j<cols ; j++)
				bTemp[i][j] = 0;
		}
		
		return bTemp;
	}

	public FFByteMatrix(Byte[][] b) {
		super(b);
	}

	@Override
	public Byte add(Byte a, Byte b) {
		return new Byte((byte)(a ^ b));
	}

	@Override
	public FFByteMatrix decloneExtended() {
		int halfCols = _cols / 2;
		Byte[][] b = getEmptyArray(_rows, halfCols);
		for(int i=0 ; i<halfCols ; i++) {
			boolean rowZero = true;
			for(int j=0 ; j<halfCols ; j++) {
				b[i][j] = clone(_b[i][j + halfCols]);
				if(!isZero(_b[i][j]))
					rowZero = false;
			}
			
			if(rowZero) {
				_inverseRowZero = i;
				return null;
			}
		}
		
		return createNewMatrix(b);
	}
	
	@Override
	public FFByteMatrix cloneExtended() {
		Byte[][] b = getEmptyArray(_rows, _cols * 2);
		for(int i=0 ; i<getRowSize() ; i++) {
			for(int j=0 ; j<getColumnSize() ; j++)
				b[i][j] = clone(_b[i][j]);
			
			for(int j=0 ; j<getColumnSize() ; j++)
				if (j == i)
					b[i][j + _cols] = getOne();
				else
					b[i][j + _cols] = getZero();
		}
		
		return createNewMatrix(b);
	}

	protected FFByteMatrix createNewMatrix(Byte[][] b) {
		return new FFByteMatrix(b);
	}
	
	@Override
	public FFByteMatrix clone() {
		Byte[][] b = new Byte[_rows][_cols];
		for(int i=0 ; i<getRowSize() ; i++)
			for(int j=0 ; j<getColumnSize() ; j++)
				b[i][j] = clone(_b[i][j]);
			
		return new FFByteMatrix(b);
	}

	@Override
	public Byte getOne() {
		return 1;
	}

	@Override
	public Byte getZero() {
		return 0;
	}

	@Override
	public TypedMatrix<Byte> getZeroMatrix(int rows, int cols) {
		return new FFByteMatrix(rows, cols);
	}
	
	@Override
	public TypedMatrix<Byte> getNullMatrix(int rows, int cols) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Byte[][] getEmptyArray(int rows, int cols) {
		return new Byte[rows][cols];
	}

	@Override
	public Byte multiply(Byte a, Byte b) {
		return _tables.FFMulFast(a, b);
	}

	public static FFByteMatrix createRandomSquareByteMatrix(int rowsCols) {
		return createRandomByteMatrix(rowsCols, rowsCols);
	}
	
	public static FFByteMatrix createRandomByteMatrix(int rows, int cols) {
		Byte[][] b = new Byte[rows][];
		for(int i=0 ; i<b.length ; i++)
			b[i] = random(b.length);
		
		return new FFByteMatrix(b);
	}
	
	public static FFByteMatrix createMatrix(int rows, String str) {
		String[] bStr = str.split(",");
		int count = bStr.length;
		int cols = count/rows;
		Byte[][] b = new Byte[rows][cols];
		for(int i=0 ; i<rows ; i++)
			for(int j=0 ; j<cols ; j++) {
				String subStr = bStr[i * cols + j];
				b[i][j] = new Byte(subStr);
			}
		
		return new FFByteMatrix(b);
	}

	static final Random _RANDOM = new Random();
	public static Byte[] random(int size) {
		Byte[] b = new Byte[size];
		byte[] bytes = new byte[1];
		for(int i=0 ; i<b.length ; i++) {
			_RANDOM.nextBytes(bytes);
			b[i] = bytes[0];
		}
		
		return b;
	}
	

	@Override
	public Byte clone(Byte a) {
		return new Byte(a);
	}

	protected final int m[] = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};
	@Override
	public int compareToAbs(Byte A, Byte B) {
		byte a = A.byteValue();
		byte b = B.byteValue();
		
		for(int i=0 ; i<8 ; i++) {
			int aBit = (a & m[i]) >> i;
			int bBit = (b & m[i]) >> i;
			if(aBit > bBit)
				return 1;
			else if (aBit < bBit)
				return -1;
		}
		
		return 0;
	}

//	@Override
//	public int compareToAbs(Byte A, Byte B) {
//		byte a = A.byteValue();
//		byte b = B.byteValue();
//		
//		for(int i=0 ; i<8 ; i++) {
//			int aBit = _tables.ithBit(a, i);
//			int bBit = _tables.ithBit(b, i);
//			if(aBit > bBit)
//				return 1;
//			else if (aBit < bBit)
//				return -1;
//		}
//		return 0;
//	}

	@Override
	public Byte divide(Byte a, Byte b) {
		return multiply(a, inverse(b));
	}

	@Override
	public Byte inverse(Byte a) {
		return _tables.FFInv(a);
	}

	@Override
	public boolean isOne(Byte a) {
		return a == 1;
	}

	@Override
	public boolean isZero(Byte a) {
		return a == 0;
	}

	@Override
	public Byte multiplyAndAddInPlace(Byte total, Byte a, Byte b) {
		total = (byte) (total.byteValue() ^ multiply(a, b));
		return total;
	}

	@Override
	public Byte subtract(Byte a, Byte b) {
		return (byte) (a ^ b);
	}

	@Override
	protected String toString(Byte a) {
	      return "" + DIG[(a & 0xff) >> 4] + DIG[a & 0x0f];
	}
	
//	@Override
	protected final TypedMatrix<Byte> multiply2(Byte[][] a) {
		int aRows = a.length;
		if(aRows!=_cols)
			throw new IllegalArgumentException("Row/column count mismatch: " + _rows + " vs. " + a.length);
		
		if(aRows == 0)
			return getZeroMatrix(0, 0);
		
		int aCols = a[0].length;
		
		TypedMatrix<Byte> multM = getZeroMatrix(_rows, aCols);
		for(int i=0 ; i<_rows ; i++) {
			if (aCols != a[i].length)
				throw new IllegalArgumentException("Row '" + i + "' of array is not of length " + aCols);
			
			for(int k=0 ; k<aCols ; k++) {
				multM._b[i][k] = 0;
				for(int j=0 ; j<getColumnSize() ; j++) {
//					multM._b[i][k] = multiplyAndAddInPlace(multM._b[i][k], _b[i][j], a[j][k]);
					if (_b[i][j] == 0 || a[j][k] == 0) {
						multM._b[i][k] = (byte)(multM._b[i][k] ^ (byte)0);
					} else {
						int t = 0;
						t = (_tables.LOG[(_b[i][j] & 0xff)] & 0xff) + (_tables.LOG[(a[j][k] & 0xff)] & 0xff);
						if (t > 255) t = t - 255;
						multM._b[i][k] = (byte)(multM._b[i][k] ^ _tables.EXP[(t & 0xff)]);
					}
				}
			}
		}
		
		return multM;
	}
}
