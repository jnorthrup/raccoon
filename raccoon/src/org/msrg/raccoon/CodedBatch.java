/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Random;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedBatchType;
import org.msrg.raccoon.CodedCoefficients;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ICodedBatch;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.utils.BytesUtil;

public abstract class CodedBatch implements ICodedBatch {
	
	protected static int _MAX_WRITE_SIZE = 15;
	
	protected static final int I_CONTENT_SIZE = 0;
	protected static final int I_CONTENT = I_CONTENT_SIZE + 4;
	
	protected Object _lock = new Object();
	protected BulkMatrix _bm;
	protected final int _size;
	protected final Random _rand = new Random();
	protected final CodedBatchType _codedBatchType;
	
	@Override
	public int getRequiredCodedPieceCount() {
		return _bm._rows;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null)
			return false;
		
		if(!CodedBatch.class.isAssignableFrom(obj.getClass()))
			return false;

		CodedBatch codedBatchObj = (CodedBatch) obj;
		return equalsExact(codedBatchObj);
	}
	
	@Override
	public boolean equalsExact(ICodedBatch codedBatchObj) {
		if(_size != codedBatchObj.getSize())
			return false;
		
		BulkMatrix objContent = codedBatchObj.getBulkMatrix();
		if(_bm == null)
			if (objContent == null)
				return true;
			else
				return false;
		
		else if (objContent == null)
			return false;
		
		return _bm.equals(objContent);
	}
	
	@Override
	public abstract int getAvailableCodedPieceCount();
	
	public CodedBatch(CodedBatchType codedBatchType, BulkMatrix content) {
		_bm = content;
		_size = _bm.getSize();
		_codedBatchType = codedBatchType;
	}
	
	protected CodedBatch(CodedBatchType codedBatchType, int size) {
		_size = size;
		_codedBatchType = codedBatchType;
	}
	
	@Override
	public int getSize() {
		return _size;
	}

	@Override
	public abstract boolean isSolved();
	
	public CodedCoefficients getNewCodedCoefficients(int length) {
		return new CodedCoefficients(length);
	}
	
	@Override
	public BulkMatrix getBulkMatrix() {
		synchronized (_lock) {
			return _bm;
		}
	}

	@Override
	public int getSizeInByteBuffer() {
		return 4 + (_bm==null? 0 : _size);
	}

	@Override
	public abstract CodedPiece code();
	
	@Override
	public String toString() {
		Writer ioWriter = new StringWriter(3 * (_size>_MAX_WRITE_SIZE?_MAX_WRITE_SIZE:_size) + 10);
		try {
			ioWriter.append(getCodedBatchType().toString());
			toString(ioWriter);
		} catch (IOException iox) {
			return "ERROR";
		}
		return ioWriter.toString();
	}
	
	public void toString(Writer ioWriter) throws IOException {
		ioWriter.append("{");
		
		for(int i=0 ; i<_size && i<_MAX_WRITE_SIZE ; i++)			
			ioWriter.append((i==0 ? "":",") + BytesUtil.hex(_bm.getByte(i)));

		int remaining = _size - _MAX_WRITE_SIZE;
		if (remaining > 0)
			ioWriter.append(",...(" + remaining + ")");
		
		ioWriter.append("}");
	}

	@Override
	public final CodedBatchType getCodedBatchType() {
		return _codedBatchType;
	}

	public BulkMatrix createNewBulkMatrix(CodedPiece[] codedPieces){
		int slicesCount = codedPieces.length;
		SliceMatrix[] psSlicesContent = new SliceMatrix[slicesCount];
		for(int i=0 ; i<slicesCount ; i++)
			psSlicesContent[i] = (SliceMatrix) codedPieces[i]._codedContent;

		return new BulkMatrix(psSlicesContent);
	}
	
	public BulkMatrix createNewBulkMatrix(SliceMatrix[] slicesContent) {
		return new BulkMatrix(slicesContent);
	}

	@Override
	public abstract void addCodedSlice(CodedPiece cSlice);

	public CodedCoefficients getNewCodedCoefficients(byte[] b) {
		return new CodedCoefficients(b);
	}
}
