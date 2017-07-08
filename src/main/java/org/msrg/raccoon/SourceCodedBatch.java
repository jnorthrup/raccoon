/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.jetbrains.annotations.NotNull;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;

public class SourceCodedBatch extends CodedBatch {

    public SourceCodedBatch(BulkMatrix content) {
        super(CodedBatchType.SRC_CODED_BATCH, content);
    }


    public boolean isSolved() {
        return true;
    }

    @NotNull

    @Deprecated
    public CodedPiece code() {
        return CodedPiece.makeCodedPiece(_bm);
    }


    public int getAvailableCodedPieceCount() {
        return getRequiredCodedPieceCount();
    }


    public final void addCodedSlice(CodedPiece cSlice) {
        throw new UnsupportedOperationException();
    }


    public int getCols() {
        return _bm._cols;
    }


    public int getRows() {
        return _bm._rows;
    }


    public boolean canPotentiallyBeSolved() {
        throw new UnsupportedOperationException();
    }


    public boolean isInversed() {
        return true;
    }


    public boolean decode() {
        return true;
    }
}
