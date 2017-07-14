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
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.utils.BytesUtil;

import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.*;

public class ReceivedCodedBatch extends CodedBatch {

    public static final int MIN_AVAILBLE_SLICES_FOR_CODING = 2;
    private final int _rows;
    @NotNull
    protected Random _rand = new SecureRandom();
    @Nullable
    CodedPiece[] _solvingCodedSlices;
    @NotNull
    private Vector<CodedPiece> _cbbs = new Vector<CodedPiece>();
    @Nullable
    private ByteMatrix _inverse;

    public ReceivedCodedBatch(int batchSize, int rows) {
        super(CodedBatchType.RCV_CODED_BATCH, batchSize);
        _rows = rows;
    }

//	public CodedPiece[] getCodedSlices(BitSet bv, int count) {
//		CodedPiece[] slices = new CodedPiece[count];
//		int i=0;
//		for(int j = bv.nextSetBit(0); j >= 0; j = bv.nextSetBit(j+1)) { 
//			slices[i++] = _cbbs.get(j);
//		}
//		
//		return slices;
//	}

//	Byte[][] getCoefficients(BitSet bv, int count) {
//		Byte[][] coefs = new Byte[count][];
//		int i=0;
//		for(int j = bv.nextSetBit(0); j >= 0; j = bv.nextSetBit(j+1)) { 
//			coefs[i++] = _cbbs.get(j)._cc.getCoefficients();
//		}
//		
//		return coefs;
//	}


    public final void addCodedSlice(CodedPiece cSlice) {
        synchronized (_lock) {
            if (isSolved())
                return;

            _cbbs.add(cSlice);
        }
    }


    public boolean canPotentiallyBeSolved() {
        synchronized (_lock) {
            return _cbbs.size() >= _rows;
        }
    }

    public final CodedPiece[] canSolve() {
        Thread currThread = Thread.currentThread();
        int initialPriority = currThread.getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        CodedPiece[] solvingCodedSlices = null;
        synchronized (_lock) {
            if (_solvingCodedSlices != null)
                return null;

            int requiredSlices = getRequiredCodedPieceCount();
            int receivedSlices = _cbbs.size();

            for (int retry = 0; retry <= receivedSlices - requiredSlices; retry++) {
                if (retry != 0)
                    System.out.println("RETRYING TO DECODE: " + retry + "[" + _cbbs.size() + "-" + requiredSlices + "]");

                solvingCodedSlices = canSolveHighPriority();
                if (solvingCodedSlices != null)
                    break;
                receivedSlices = _cbbs.size();
            }
        }
        Thread.currentThread().setPriority(initialPriority);

        return solvingCodedSlices;
    }

    @Nullable
    protected CodedPiece[] canSolveHighPriority() {
        int requiredSlices = getRequiredCodedPieceCount();

        ByteMatrix ncm = null;

        List<Integer> indexSet = getRandomIndex(_cbbs.size(), requiredSlices);
        Byte[][] coefs = new Byte[requiredSlices][];
        int i = 0;
        for (Integer j : indexSet)
            coefs[i++] = _cbbs.get(j)._cc.getCoefficients();

        ncm = new ByteMatrix(coefs);
        if (!ncm.isInversable()) {
            int badRow = ncm.getInverseRowZero();
            removeBadRow(badRow);
            return null;
        }

        _solvingCodedSlices = new CodedPiece[indexSet.size()];
        i = 0;
        for (Integer index : indexSet)
            _solvingCodedSlices[i++] = _cbbs.get(index.intValue());
        _inverse = (ByteMatrix) ncm.inverseMatrix();
        return _solvingCodedSlices;

//		Combinations combinations = Combinations.makeAllCombinations(requiredSlices, receivedSlices);
//		BitSet[] bvs = combinations.getAllCombinations();
//		for(BitSet bv : bvs) {
//			Byte[][] coefs = getCoefficients(bv, requiredSlices);
//			ByteMatrix ncm = new ByteMatrix(coefs);
//			if(ncm.isInversable()) {
//				solvingCodedSlices = getCodedSlices(bv, requiredSlices);
//				break;
//			}
//		}
    }


    public boolean isInversed() {
        return _inverse != null;
    }

    @Nullable
    public ByteMatrix getInverseCoefficientsForSolvingCodedSlices() {
        return _inverse;
    }


    public int getRequiredCodedPieceCount() {
        return _rows;
    }


    public boolean decode() {
        synchronized (_lock) {
            if (_solvingCodedSlices != null)
                return true;
        }
        CodedPiece[] solvingCodedSlices = canSolve();
        if (solvingCodedSlices == null)
            return false;

        // Now solve the equations
        int slicesCount = solvingCodedSlices.length;
        Byte[][] coefficientsArray = new Byte[slicesCount][];
        SliceMatrix[] slicesContent = new SliceMatrix[slicesCount];
        for (int i = 0; i < slicesCount; i++) {
            slicesContent[i] = solvingCodedSlices[i]._codedContent;
            coefficientsArray[i] = solvingCodedSlices[i]._cc.getCoefficients();
        }

        BulkMatrix contentMatrix = createNewBulkMatrix(slicesContent);
        ByteMatrix coefficientsMatrix = new ByteMatrix(coefficientsArray);

        ByteMatrix coefficientsMatrixInverse = (ByteMatrix) coefficientsMatrix.inverseMatrix();
        if (coefficientsMatrixInverse == null)
            return false;

        coefficientsMatrixInverse = _inverse;
        return decodeUsing(coefficientsMatrixInverse, contentMatrix);
    }

    public boolean decodeUsing(@NotNull ByteMatrix coefficientsMatrixInverse, @NotNull BulkMatrix contentMatrix) {
        synchronized (_lock) {
            if (_bm != null)
                return true;
        }
        BulkMatrix smDecoded = coefficientsMatrixInverse.multiply(contentMatrix);
        setContent(smDecoded);

        return true;
    }


    public boolean isSolved() {
        synchronized (_lock) {
            return _bm != null;
        }
    }


    public int getAvailableCodedPieceCount() {
        synchronized (_lock) {
            return getAvailableCodedSliceCountPrivately();
        }
    }

    protected int getAvailableCodedSliceCountPrivately() {
        return _cbbs.size();
    }

    @Nullable

    @Deprecated
    public CodedPiece code() {
        CodedPiece[] codedSlices = getCodedSlicesForCoding();
        return CodedPiece.makeCodedSlice(codedSlices);
    }

    @NotNull
    public CodedPiece[] getCodedSlicesForCoding() {
        CodedPiece[] codedSlices = getCodedSlices();
        return codedSlices;
    }

    @NotNull
    public CodedPiece[] getCodedSlices() {
        synchronized (_lock) {
            List<Integer> indexSet = getRandomIndex(_cbbs.size(), _cbbs.size() < _rows ? _cbbs.size() : _rows);
            CodedPiece[] codedSlices = new CodedPiece[indexSet.size()];
            int i = 0;
            for (Integer index : indexSet) {
                codedSlices[i++] = _cbbs.get(index.intValue());
            }

            return codedSlices;
        }
    }

    @Nullable
    protected List<Integer> getRandomIndex(int maxIndex, int maxReturnSize) {
        if (maxReturnSize > maxIndex)
            return null;
            //throw new IllegalArgumentException("Invalid arguments: " + maxReturnSize + " vs. " + maxIndex);

        ArrayList<Integer> ret = new ArrayList<Integer>();
        if (maxReturnSize == 0)
            return ret;

        for (int i = 0; i < maxIndex; i++)
            ret.add(i);

        for (int i = maxIndex; i > maxReturnSize; i--) {
            int removedIndex = _rand.nextInt(i);
            ret.remove(removedIndex);
            if (ret.size() != i - 1)
                throw new IllegalStateException();
        }

        return ret;
    }

    @Nullable
    public BulkMatrix getCodedSlicesAsBulkMatrix2() {
        CodedPiece[] codedSlices = getCodedSlicesForCoding();
        if (codedSlices == null)
            return null;

        SliceMatrix[] rowMatrices = new SliceMatrix[codedSlices.length];
        for (int i = 0; i < rowMatrices.length; i++)
            rowMatrices[i] = codedSlices[i]._codedContent;

        return createNewBulkMatrix(rowMatrices);
    }

    public void toString(@NotNull Writer ioWriter) throws IOException {
        synchronized (_lock) {
            if (_solvingCodedSlices != null) {
                if (_bm == null) {
                    ioWriter.append("{NO_CONTENT}");
                    return;
                }

                ioWriter.append("{");
                for (int i = 0; i < _size && i < CodedBatch._MAX_WRITE_SIZE; i++)
                    ioWriter.append((i == 0 ? "" : ",") + BytesUtil.hex(_bm.getByte(i)));

                int remaining = _size - CodedBatch._MAX_WRITE_SIZE;
                if (remaining > 0)
                    ioWriter.append(",...(" + remaining + ")");

                ioWriter.append("}");
                return;
            }
        }

        int i = 0;
        for (CodedPiece cs : _cbbs)
            ioWriter.append((i++ == 0 ? "" : ",") + cs);
    }

    public void setContent(@NotNull BulkMatrix content) {
        synchronized (_lock) {
            if (_bm != null)
                return;

            if (_size != content._size)
                throw new IllegalStateException(_size + " vs. " + content._size);

            _bm = content;
        }
    }


    public int getCols() {
        return getSize() / getRows();
    }


    public int getRows() {
        return _rows;
    }

    protected void removeBadRow(int badRow) {
        _cbbs.remove(badRow);
    }
}
