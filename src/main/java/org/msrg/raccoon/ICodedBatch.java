/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;

public interface ICodedBatch {

    String toString();

    boolean equals(Object obj);

    boolean equalsExact(ICodedBatch cBatch);

    BulkMatrix getBulkMatrix();

    int getRows();

    int getCols();

    int getSize();

    int getSizeInByteBuffer();

    void addCodedSlice(CodedPiece cSlice);

    CodedBatchType getCodedBatchType();

    int getAvailableCodedPieceCount();

    int getRequiredCodedPieceCount();

    boolean isSolved();

    CodedPiece code();

    boolean decode();

    boolean canPotentiallyBeSolved();

    boolean isInversed();

}
