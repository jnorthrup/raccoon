/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedBatchType;
import org.msrg.raccoon.CodedPiece;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;

public class SourceCodedBatch extends CodedBatch {

	public SourceCodedBatch(BulkMatrix content) {
		super(CodedBatchType.SRC_CODED_BATCH, content);
	}

	@Override
	public boolean isSolved() {
		return true;
	}

	@Override @Deprecated
	public CodedPiece code() {
		return CodedPiece.makeCodedPiece(_bm);
	}
	
	@Override
	public int getAvailableCodedPieceCount() {
		return getRequiredCodedPieceCount();
	}

	@Override
	public final void addCodedSlice(CodedPiece cSlice) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCols() {
		return _bm._cols;
	}

	@Override
	public int getRows() {
		return _bm._rows;
	}

	@Override
	public boolean canPotentiallyBeSolved() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInversed() {
		return true;
	}

	@Override
	public boolean decode() {
		return true;
	}
}
