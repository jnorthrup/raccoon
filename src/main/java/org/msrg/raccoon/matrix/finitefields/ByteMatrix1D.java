/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.finitefields;

import org.jetbrains.annotations.NotNull;
import org.msrg.raccoon.finitefields.Tables;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;

public class ByteMatrix1D extends ByteMatrix {

    public ByteMatrix1D(Byte[] b) {
        super(ByteMatrix1D.make1dArray2d(b));
    }

    public ByteMatrix1D(@NotNull byte[] b) {
        this(ByteMatrix.wrap(b));
    }

    public ByteMatrix1D(int cols) {
        super(cols, 1);
    }

    @NotNull
    protected static Byte[][] make1dArray2d(Byte[] bArray) {
        Byte[][] B = new Byte[1][];
        B[0] = bArray;

        return B;
    }

    @NotNull
    public SliceMatrix multiply1D(@NotNull BulkMatrix bm) {
        if (_cols != bm._rows)
            throw new IllegalArgumentException("Mismatch: " + _cols + " vs. " + bm._rows);

        SliceMatrix outputSlice = bm.createOneEmtpySlice(bm._cols);
        byte[][] bIn = bm.getContent();

        verify();
        byte[] bRow = new byte[bm._cols];
        for (int j = 0; j < bm._cols; j++) {
            for (int k = 0; k < _cols; k++) {
                byte a = _b[0][k];
                byte b = bIn[k][j];

//				byte mult = _tables.FFMulFast(a, b);

                byte mult;
                if (a == 0 || b == 0) {
                    mult = 0;
                } else {
                    int t = (Tables.LOG[a & 0xff] & 0xff) + (Tables.LOG[b & 0xff] & 0xff);
                    mult = Tables.EXP[(t > 255 ? t - 255 : t) & 0xff];
                }

                bRow[j] ^= mult;
            }
        }
        outputSlice.loadNoCopy(bRow);

        return outputSlice;
    }
}
