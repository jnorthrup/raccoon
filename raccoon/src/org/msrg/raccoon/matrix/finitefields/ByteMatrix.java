/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.finitefields;

import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.FFByteMatrix;

import org.msrg.raccoon.matrix.TypedMatrix;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.BulkMatrix1D;


public class ByteMatrix extends FFByteMatrix {

	public ByteMatrix(int cols, int rows) {
		super(cols, rows);
	}
	
	public ByteMatrix(byte[][] b) {
		this(wrap(b));
	}
	
	public static Byte[] wrap(byte[] b) {
		Byte[] B = new Byte[b.length];
		for(int j=0 ; j<b.length ; j++) {
			B[j] = b[j];
			
			if(B[j] == null)
				throw new NullPointerException("j=" + j + ", b[j]=" + b[j]);
		}
		
		return B;
	}
	
	public static Byte[][] wrap(byte[][] b) {
		Byte[][] B = new Byte[b.length][];
		int rowL = -1;
		for(int i=0 ; i<b.length ; i++) {
			int rowLen = b[i].length;
			if(rowL != -1)
				if(rowL != rowLen)
					throw new IllegalStateException("" + rowL + " vs. " + rowLen);
			rowL = rowLen;
			B[i] = new Byte[rowLen];
			for(int j=0 ; j<rowLen ; j++) {
				B[i][j] = b[i][j];
			}
		}
		
		return B;
	}
	
	public static byte[][] unwrap(Byte[][] B) {
		byte[][] b = new byte[B.length][];
		for(int i=0 ; i<B.length ; i++) {
			int rowLen = B[i].length;
			b[i] = new byte[rowLen];
			for(int j=0 ; j<rowLen ; j++) {
				b[i][j] = B[i][j];
			}
		}
		
		return b;
	}
	
	public ByteMatrix(Byte[][] b) {
		super(b);
	}
	
	public boolean isInversable() {
		return inverseMatrix() != null;
	}
	
	public byte[][] getByteArray() {
		return unwrap(_b);
	}
	
	protected BulkMatrix createByteMatrix(int rows, int cols) {
		if(rows==1)
			return new BulkMatrix1D(cols);
		else
			return new BulkMatrix(rows, cols);
	}
	
//	public BulkMatrix multiply2(BulkMatrix bm) {
//		int sliceCount = bm.getSliceCount();
//		
//		BulkMatrix outM = createByteMatrix(_rows, bm._cols);
//		
//		for(int i=0 ; i<sliceCount ; i++) {
//			SliceMatrix inSlice = bm.slice(i);
//			outM.add(i, multiply(inSlice));
//		}
//		
//		return outM;
//	}
	
	public BulkMatrix multiply(BulkMatrix bm) {
		if(_cols != bm._rows)
			throw new IllegalArgumentException("Mismatch: " + _cols + " vs. " + bm._rows);
		
		byte[][] bIn = bm.getContent();
		BulkMatrix outM = bm.createEmptyMatrix(_rows, bm._cols);
		
		for(int i=0 ; i<_rows ; i++){
			byte[] bRow = new byte[bm._cols];
			for(int j=0 ; j<bm._cols ; j++) {
				for(int k=0 ; k<_cols ; k++) {
					byte a = _b[i][k];
					byte b = bIn[k][j];
					
//					byte mult = _tables.FFMulFast(a, b);

					byte mult;
					if(a==0||b==0) {
						mult = 0;
					} else {
					   int t = ((_tables.LOG[(a & 0xff)] & 0xff) + (_tables.LOG[(b & 0xff)] & 0xff));
					   mult = _tables.EXP[((t>255)?t-255:t) & 0xff];
					}
					
					bRow[j] ^= (byte)(mult);
				}
			}
			outM.slice(i).loadNoCopy(bRow);
		}
		return outM;
	}

	public byte[][] multiply(byte[][] b) {
		Byte[][] wrapped = wrap(b);
		FFByteMatrix multM = (FFByteMatrix) multiply(wrapped);
		return unwrap(multM.toArray());
	}
	
	@Override
	public TypedMatrix<Byte> getZeroMatrix(int rows, int cols) {
		return new ByteMatrix(rows, cols);
	}
	
	public static ByteMatrix createRandomByteMatrix(int rows, int cols) {
		Byte[][] b = new Byte[rows][];
		for(int i=0 ; i<b.length ; i++)
			b[i] = random(b.length);
		
		return new ByteMatrix(b);
	}

	@Override
	protected ByteMatrix createNewMatrix(Byte[][] b) {
		return new ByteMatrix(b);
	}

	public static ByteMatrix createMatrix(int rows, String str) {
		String[] bStr = str.split(",");
		int count = bStr.length;
		int cols = count/rows;
		Byte[][] b = new Byte[rows][cols];
		for(int i=0 ; i<rows ; i++)
			for(int j=0 ; j<cols ; j++) {
				String subStr = bStr[i * cols + j];
				b[i][j] = new Byte(subStr);
			}
		
		return new ByteMatrix(b);
	}
	
	public boolean verifyNotNull() {
		for(int i=0 ; i<_rows ; i++)
			if(_b[i] == null)
				return false;
		
		return true;
	}
}
