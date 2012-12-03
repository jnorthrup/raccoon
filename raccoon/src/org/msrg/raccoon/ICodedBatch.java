/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.msrg.raccoon.CodedBatchType;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ICodedBatch;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;

public interface ICodedBatch {

	public String toString();
	public boolean equals(Object obj);
	public boolean equalsExact(ICodedBatch cBatch);

	public BulkMatrix getBulkMatrix();
	public int getRows();
	public int getCols();
	public int getSize();
	
	public int getSizeInByteBuffer();
	
	public void addCodedSlice(CodedPiece cSlice);
	public CodedBatchType getCodedBatchType();
	public int getAvailableCodedPieceCount();
	public int getRequiredCodedPieceCount();
	public boolean isSolved();
	public CodedPiece code();
	public boolean decode();
	
	public boolean canPotentiallyBeSolved();
	public boolean isInversed();
	
}
