/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.bulk;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Random;

import org.msrg.raccoon.CodedCoefficients;
import org.msrg.raccoon.CodedPiece;

import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.utils.BytesUtil;


public class SliceMatrix implements Serializable {
	
	/**
	 * Auto Generated.
	 */
	private static final long serialVersionUID = -2394495616570970489L;
	public final int _cols;
	protected byte[] _b;
	
	public SliceMatrix(int cols) {
		_cols = cols;
	}
	
	public void loadNoCopy(byte[] b) {
		if(_b != null)
			throw new IllegalStateException();
		
		if(b.length != _cols)
			throw new IllegalArgumentException("Length mismatch: " + b.length + " vs. " + _cols);
		
		_b = b;
	}
	
	public void loadWithCopy(byte[] b, int bStartOffset) {
		if(_b != null)
			throw new IllegalStateException();

		_b = new byte[_cols];
		
		for(int j=0 ; j<_cols ; j++)
			_b[j] = b[bStartOffset + j];
	}

	public SliceMatrix(Byte[] B) {
		this(B.length);
		byte[] b = new byte[B.length];
		for(int i=0 ; i<b.length ; i++)
			b[i] = B[i];
		loadNoCopy(b);
	}
	
	public SliceMatrix(byte[] b) {
		this(b.length);
		
		loadNoCopy(b);
	}
	
	public byte[] getContent() {
		return _b;
	}
	
	public boolean hasContent() {
		return _b != null;
	}
	
	protected static Random _RANDOM = new Random();
	public static SliceMatrix createRandomSliceMatrix(int cols) {
		byte[] b = new byte[cols];
		for(int i=0 ; i<b.length ; i++)
			_RANDOM.nextBytes(b);
		
		SliceMatrix sm = new SliceMatrix(b);
		return sm;
	}
	
	public static SliceMatrix getEmptySliceMatrix(SliceMatrix sm) {
		byte[] b = new byte[sm._cols];
		return new SliceMatrix(b);
	}
	
	public SliceMatrix clone(SliceMatrix sm) {
		SliceMatrix smClone = getEmptySliceMatrix(sm);
		if(sm.hasContent())
			smClone.loadWithCopy(sm._b, 0);
		
		return smClone;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null)
			return false;
		if(!getClass().isAssignableFrom(obj.getClass()))
			return false;
		
		SliceMatrix smObj = (SliceMatrix) obj;
		if (_cols != smObj._cols)
			return false;
		
		for(int j=0 ; j<_cols ; j++)
			if(_b[j] != smObj._b[j])
				return false;
		
		return true;
	}

	public byte getByte(int i) {
		return _b[i];
	}
	
	public static int _MAX_COLS_PRINT = 2;
	public int toString(Writer ioWriter) throws IOException {
		return toString(ioWriter, _MAX_COLS_PRINT);
	}
	
	public int toString(Writer ioWriter, int maxCols) throws IOException {
		int strLen = 0;
		if(_b == null) {
			String str = "NULL_ALL";
			ioWriter.append(str);
			strLen += str.length();
			return strLen;
		}
		
		ioWriter.append('[');
		for(int j=0 ; j<_cols && j<maxCols ; j++)
			if(_b == null)
				ioWriter.append((j==0?"":",") + "NULL[" + j + "]");
			else
				ioWriter.append((j==0?"":",") + BytesUtil.hex(_b[j]));
		
		int remaining = _cols - maxCols;
		if(remaining > 0)
			ioWriter.append("...(" + remaining + ")]");
		
		return strLen;
	}
	
	@Override
	public String toString() {
		if(_b==null)
			return "{NULL}";
		
		Writer ioWriter = new StringWriter();
		try {
			toString(ioWriter);
		} catch(IOException iox) {
			return "ERROR";
		}
		return ioWriter.toString();
	}

	public SliceMatrix createNewSliceMatrix(int cols) {
		return new SliceMatrix(cols);
	}
	
	public int getSizeInByteBuffer() {
		return 4 + 4 + (_cols);
	}

	public CodedPiece createEmptyCodedPiece(CodedCoefficients cc, SliceMatrix sm) {
		return new CodedPiece(cc, sm);
	}
	
	// Change this in the future to allow sub-block segmentation
//	public int putObjectInByteBuffer(ByteBuffer bb, int offset) {
//		int inputOffset = offset;
//
//		bb.putInt(_cols);
//		offset += 4;
//		
//		bb.putInt(_colsOffset);
//		offset += 4;
//		
//		bb.position(offset);
//		bb.put(_b);
//		
//		offset += _b.length;
//		return (offset - inputOffset);
//	}
}
