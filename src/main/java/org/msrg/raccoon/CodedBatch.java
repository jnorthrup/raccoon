/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.utils.BytesUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Random;

public abstract class CodedBatch implements ICodedBatch {

    protected static final int I_CONTENT_SIZE = 0;
    protected static final int I_CONTENT = CodedBatch.I_CONTENT_SIZE + 4;
    protected static int _MAX_WRITE_SIZE = 15;
    protected final int _size;
    protected final Random _rand = new Random();
    protected final CodedBatchType _codedBatchType;
    @NotNull
    protected Object _lock = new Object();
    protected BulkMatrix _bm;

    public CodedBatch(CodedBatchType codedBatchType, BulkMatrix content) {
        _bm = content;
        _size = _bm.getSize();
        _codedBatchType = codedBatchType;
    }

    protected CodedBatch(CodedBatchType codedBatchType, int size) {
        _size = size;
        _codedBatchType = codedBatchType;
    }


    public int getRequiredCodedPieceCount() {
        return _bm._rows;
    }


    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;

        if (!CodedBatch.class.isAssignableFrom(obj.getClass()))
            return false;

        ICodedBatch codedBatchObj = (CodedBatch) obj;
        return equalsExact(codedBatchObj);
    }


    public boolean equalsExact(@NotNull ICodedBatch codedBatchObj) {
        if (_size != codedBatchObj.getSize())
            return false;

        BulkMatrix objContent = codedBatchObj.getBulkMatrix();
        if (_bm == null)
            return objContent == null;

        else if (objContent == null)
            return false;

        return _bm.equals(objContent);
    }


    public abstract int getAvailableCodedPieceCount();


    public int getSize() {
        return _size;
    }


    public abstract boolean isSolved();

    @NotNull
    public CodedCoefficients getNewCodedCoefficients(int length) {
        return new CodedCoefficients(length);
    }


    public BulkMatrix getBulkMatrix() {
        synchronized (_lock) {
            return _bm;
        }
    }


    public int getSizeInByteBuffer() {
        return 4 + (_bm == null ? 0 : _size);
    }


    public abstract CodedPiece code();


    public String toString() {
        Writer ioWriter = new StringWriter(3 * (_size > CodedBatch._MAX_WRITE_SIZE ? CodedBatch._MAX_WRITE_SIZE : _size) + 10);
        try {
            ioWriter.append(getCodedBatchType().toString());
            toString(ioWriter);
        } catch (IOException iox) {
            return "ERROR";
        }
        return ioWriter.toString();
    }

    public void toString(@NotNull Writer ioWriter) throws IOException {
        ioWriter.append("{");

        for (int i = 0; i < _size && i < CodedBatch._MAX_WRITE_SIZE; i++)
            ioWriter.append((i == 0 ? "" : ",") + BytesUtil.hex(_bm.getByte(i)));

        int remaining = _size - CodedBatch._MAX_WRITE_SIZE;
        if (remaining > 0)
            ioWriter.append(",...(" + remaining + ")");

        ioWriter.append("}");
    }


    public final CodedBatchType getCodedBatchType() {
        return _codedBatchType;
    }

    @NotNull
    public BulkMatrix createNewBulkMatrix(@NotNull CodedPiece[] codedPieces) {
        int slicesCount = codedPieces.length;
        SliceMatrix[] psSlicesContent = new SliceMatrix[slicesCount];
        for (int i = 0; i < slicesCount; i++)
            psSlicesContent[i] = codedPieces[i]._codedContent;

        return new BulkMatrix(psSlicesContent);
    }

    @NotNull
    public BulkMatrix createNewBulkMatrix(SliceMatrix[] slicesContent) {
        return new BulkMatrix(slicesContent);
    }


    public abstract void addCodedSlice(CodedPiece cSlice);

    @NotNull
    public CodedCoefficients getNewCodedCoefficients(byte[] b) {
        return new CodedCoefficients(b);
    }
}
