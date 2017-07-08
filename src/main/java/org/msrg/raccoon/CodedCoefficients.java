/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;
import org.msrg.raccoon.utils.BytesUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

public class CodedCoefficients extends ByteMatrix1D {

    final static Random _rand = new Random();
    protected static int _MAX_COEFFICIENTS_PRINT = 2;

    public CodedCoefficients(@NotNull byte[] coefficients) {
        super(coefficients);
    }

    public CodedCoefficients(Byte[] coefficients) {
        super(coefficients);
    }

    public CodedCoefficients(int length) {
        this(CodedCoefficients.makeRand1dArray(length));
    }

    @NotNull
    protected static byte[] makeRand1dArray(int length) {
        byte[] bTemp = new byte[length];
        synchronized (CodedCoefficients._rand) {
            CodedCoefficients._rand.nextBytes(bTemp);
        }

        return bTemp;
    }

//	public ByteMatrix transpose() {
//		byte[][] b = new byte[_cols][];
//		for(int i=0 ; i<_cols ; i++) {
//			b[i] = new byte[0];
//			b[i][0] = _b[0][i];
//		}
//		
//		return new ByteMatrix(b);
//	}

//	public CodedCoefficients(CodedCoefficients[] cc) {
//		this(cc[0].getLength(), cc.length);
//
//		{
////			for(int i=0 ; i<cc[0].getLength() ; i++)
////				_b[0][i] = 0;
////			for(int i=0 ; i<cc.length ; i++) {
////				for(int j=0 ; j<cc[i].getLength() ; j++) {
////					_b[0][i] = add(_b[0][i], multiply(b[i], cc[i]._b[0][j]));
////				}
////			}
//		}
//		
//		{
//			byte[] b = makeRand1dArray(cc.length, cc.length);
//			SliceMatrix[] ccSlices = new SliceMatrix[cc.length];
//			for(int i=0 ; i<ccSlices.length ; i++)
//				ccSlices[i] = new SliceMatrix(cc[i]._b[0]);
//			BulkMatrix bm = new BulkMatrix(ccSlices);
//			ByteMatrix1D m = new ByteMatrix1D(b);
//			BulkMatrix result = m.multiply(bm);
//			
//			for(int i=0 ; i<_b[0].length ; i++)
//				_b[0][i] = result.getContent()[0][i];
//		}
//	}

//	public CodedCoefficients multiply(CodedCoefficients[] cc) {
//		CodedCoefficients retCC = createCodedCoefficients(cc[0].getLength());
//		
//		int length = _b[0].length;
//		if(length != cc.length)
//			throw new IllegalArgumentException("Invalid arguments: " + length + " vs. " + cc.getLength());
//		
//		for(int i=0 ; i<length ; i++)
//			_b[0][i] = multiply(_b[0][i], cc._b[0][i]);
//		
//		return retCC;
//	}

    public Byte[] getCoefficients() {
        return _b[0];
    }

    public int getSizeInByteBuffer() {
        return 4 + getLength();
    }

    public int getLength() {
        return _b[0].length;
    }

    public String toString() {
        try {
            StringWriter ioWriter = new StringWriter();
            toString(ioWriter);
            return ioWriter.toString();
        } catch (IOException iox) {
            return "ERROR";
        }
    }

    public void toString(@NotNull Appendable ioWriter) throws IOException {
        ioWriter.append("{");
        for (int i = 0; i < _b[0].length && i < CodedCoefficients._MAX_COEFFICIENTS_PRINT; i++)
            ioWriter.append((i == 0 ? "" : ",") + BytesUtil.hex(_b[0][i]));

        int remaining = _b[0].length - CodedCoefficients._MAX_COEFFICIENTS_PRINT;
        if (remaining > 0)
            ioWriter.append("...(" + remaining + ")");

        ioWriter.append("}");
    }


    public boolean equals(@Nullable Object obj) {
        if (obj == null || !obj.getClass().isInstance(this))
            return false;

        CodedCoefficients ccObj = (CodedCoefficients) obj;
        if (_b[0].length != ccObj._b[0].length)
            return false;
        for (int i = 0; i < _b[0].length; i++)
            if (_b[0][i] != ccObj._b[0][i])
                return false;

        return true;
    }
}
