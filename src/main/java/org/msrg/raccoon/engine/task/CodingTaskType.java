/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task;

import org.msrg.raccoon.engine.task.result.ByteMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;

public enum CodingTaskType {

    FILE_TASK(false) {
        @Override
        public void runTask(CodingTask codingTask) {

        }
    },

    SLICES_EQUAL(false) {
        @Override
        public void runTask(CodingTask codingTask) {
            SlicesEqual_CodingTask smeCodingTask = (SlicesEqual_CodingTask) codingTask;
            SliceMatrix sm1 = smeCodingTask._sm1;
            SliceMatrix sm2 = smeCodingTask._sm2;
            boolean equals = sm1.equals(sm2);
            ((Equals_CodingResult) smeCodingTask._result).setResult(equals);
            smeCodingTask.finished();
        }
    },
    MULTIPLY(false) {
        @Override
        public void runTask(CodingTask codingTask) {
            Multiply_CodingTask multiplyCodingTask = (Multiply_CodingTask) codingTask;
            ByteMatrix1D m = multiplyCodingTask._m;
            BulkMatrix bm = multiplyCodingTask._bm;
            SliceMatrix result = null;
            try {
                result = m.multiply1D(bm);
                ((SliceMatrix_CodingResult) multiplyCodingTask._result).setResult(result);
                multiplyCodingTask.finished();
            } catch (Exception x) {
                x.printStackTrace();
                multiplyCodingTask.failed();
            }
        }
    },
    INVERSE(false) {
        @Override
        public void runTask(CodingTask codingTask) {
            Inverse_CodingTask inverseCodingTask = (Inverse_CodingTask) codingTask;
            ByteMatrix m = inverseCodingTask._m;
            ByteMatrix mInverse = (ByteMatrix) m.inverseMatrix();
            ((ByteMatrix_CodingResult) inverseCodingTask._result).setResult(mInverse);
            inverseCodingTask.finished();
        }
    },

    SEQUENCIAL(true) {
        @Override
        public void runTask(CodingTask codingTask) {
            SequentialCodingTask seqCodingTask = (SequentialCodingTask) codingTask;
            seqCodingTask.runInitialSequencialTasks();


        }
    },;

    public final boolean isSequential;

    CodingTaskType(boolean isSequencial) {
        isSequential = isSequencial;
    }

    public void runTask(CodingTask codingTask) {
        throw new UnsupportedOperationException("Unknown task type ");//impossible

    }
}
