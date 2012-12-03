/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.thread;

import org.msrg.raccoon.engine.CodingEngine;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.CodingTaskType;
import org.msrg.raccoon.engine.task.Inverse_CodingTask;
import org.msrg.raccoon.engine.task.Multiply_CodingTask;
import org.msrg.raccoon.engine.task.SlicesEqual_CodingTask;
import org.msrg.raccoon.engine.task.result.ByteMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;

import org.msrg.raccoon.engine.thread.CodingThread;

public class CodingThreadImpl extends CodingThread {

	public CodingThreadImpl(CodingEngine engine) {
		super(engine);
	}

	@Override
	protected void runTask(CodingTask codingTask) {
		super.runTask(codingTask);
		
		CodingTaskType taskType = codingTask._taskType;
		
		switch(taskType) {
		case SLICES_EQUAL:
		{
			SlicesEqual_CodingTask smeCodingTask = (SlicesEqual_CodingTask) codingTask;
			SliceMatrix sm1 = smeCodingTask._sm1;
			SliceMatrix sm2 = smeCodingTask._sm2;
			boolean equals = sm1.equals(sm2);
			((Equals_CodingResult)smeCodingTask._result).setResult(equals);
			smeCodingTask.finished();
			break;
		}
		
		case INVERSE:
		{
			Inverse_CodingTask inverseCodingTask = (Inverse_CodingTask) codingTask;
			ByteMatrix m = inverseCodingTask._m;
			ByteMatrix mInverse = (ByteMatrix) m.inverseMatrix();
			((ByteMatrix_CodingResult)inverseCodingTask._result).setResult(mInverse);
			inverseCodingTask.finished();
			break;
		}
		
		case MULTIPLY:
		{
			Multiply_CodingTask multiplyCodingTask = (Multiply_CodingTask) codingTask;
			ByteMatrix1D m = multiplyCodingTask._m;
			BulkMatrix bm = multiplyCodingTask._bm;
			SliceMatrix result = null;
			try{
				result = m.multiply1D(bm);
			}catch(Exception x){
				x.printStackTrace();
				multiplyCodingTask.failed();
				break;
			}
			((SliceMatrix_CodingResult)multiplyCodingTask._result).setResult(result);
			multiplyCodingTask.finished();
			break;
		}
		
		case SEQUENCIAL:
		{
			SequentialCodingTask seqCodingTask = (SequentialCodingTask) codingTask;
			seqCodingTask.runInitialSequencialTasks();
			break;
		}
		
		default:
			throw new UnsupportedOperationException("Unknown task type: " + taskType);
		}
	}
}
