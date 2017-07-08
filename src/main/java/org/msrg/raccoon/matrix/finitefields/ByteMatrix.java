/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.finitefields;

import org.jetbrains.annotations.NotNull;
import org.msrg.raccoon.finitefields.Tables;
import org.msrg.raccoon.matrix.TypedMatrix;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.BulkMatrix1D;


public class ByteMatrix extends FFByteMatrix {

    public ByteMatrix(int cols, int rows) {
        super(cols, rows);
    }

    public ByteMatrix(@NotNull byte[][] b) {
        this(ByteMatrix.wrap(b));
    }

    public ByteMatrix(Byte[][] b) {
        super(b);
    }

    @NotNull
    public static Byte[] wrap(@NotNull byte[] b) {
        Byte[] B = new Byte[b.length];
        for (int j = 0; j < b.length; j++) {
            B[j] = b[j];

            if (B[j] == null)
                throw new NullPointerException("j=" + j + ", b[j]=" + b[j]);
        }

        return B;
    }

    @NotNull
    public static Byte[][] wrap(@NotNull byte[][] b) {
        Byte[][] B = new Byte[b.length][];
        int rowL = -1;
        for (int i = 0; i < b.length; i++) {
            int rowLen = b[i].length;
            if (rowL != -1)
                if (rowL != rowLen)
                    throw new IllegalStateException("" + rowL + " vs. " + rowLen);
            rowL = rowLen;
            B[i] = new Byte[rowLen];
            for (int j = 0; j < rowLen; j++) {
                B[i][j] = b[i][j];
            }
        }

        return B;
    }

    @NotNull
    public static byte[][] unwrap(@NotNull Byte[][] B) {
        byte[][] b = new byte[B.length][];
        for (int i = 0; i < B.length; i++) {
            int rowLen = B[i].length;
            b[i] = new byte[rowLen];
            for (int j = 0; j < rowLen; j++) {
                b[i][j] = B[i][j];
            }
        }

        return b;
    }

    public static ByteMatrix createRandomByteMatrix(int rows, int cols) {
        Byte[][] b = new Byte[rows][];
        for (int i = 0; i < b.length; i++)
            b[i] = FFByteMatrix.random(b.length);

        return new ByteMatrix(b);
    }

    public static ByteMatrix createMatrix(int rows, @NotNull String str) {
        String[] bStr = str.split(",");
        int count = bStr.length;
        int cols = count / rows;
        Byte[][] b = new Byte[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                String subStr = bStr[i * cols + j];
                b[i][j] = new Byte(subStr);
            }

        return new ByteMatrix(b);
    }

    public boolean isInversable() {
        return inverseMatrix() != null;
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

    @NotNull
    public byte[][] getByteArray() {
        return ByteMatrix.unwrap(_b);
    }

    @NotNull
    protected BulkMatrix createByteMatrix(int rows, int cols) {
        return rows == 1 ? new BulkMatrix1D(cols) : new BulkMatrix(rows, cols);
    }

    @NotNull
    public BulkMatrix multiply(@NotNull BulkMatrix bm) {
        if (_cols != bm._rows)
            throw new IllegalArgumentException("Mismatch: " + _cols + " vs. " + bm._rows);

        byte[][] bIn = bm.getContent();
        BulkMatrix outM = bm.createEmptyMatrix(_rows, bm._cols);

        for (int i = 0; i < _rows; i++) {
            byte[] bRow = new byte[bm._cols];
            for (int j = 0; j < bm._cols; j++) {
                for (int k = 0; k < _cols; k++) {
                    byte a = _b[i][k];
                    byte b = bIn[k][j];

//					byte mult = _tables.FFMulFast(a, b);

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
            outM.slice(i).loadNoCopy(bRow);
        }
        return outM;
    }

    @NotNull
    public byte[][] multiply(@NotNull byte[][] b) {
        Byte[][] wrapped = ByteMatrix.wrap(b);
        FFByteMatrix multM = (FFByteMatrix) multiply(wrapped);
        return ByteMatrix.unwrap(multM.toArray());
    }

    @NotNull

    public TypedMatrix<Byte> getZeroMatrix(int rows, int cols) {
        return new ByteMatrix(rows, cols);
    }

    @NotNull

    protected ByteMatrix createNewMatrix(Byte[][] b) {
        return new ByteMatrix(b);
    }

    public boolean verifyNotNull() {
        for (int i = 0; i < _rows; i++)
            if (_b[i] == null)
                return false;

        return true;
    }
}
