/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedBatchType;
import org.msrg.raccoon.CodedCoefficients;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.engine.task.sequential.Encoding_SequentialCodingTask;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;


public class EncodingSourceCodedBatch_SequentialCodingTask extends
		Encoding_SequentialCodingTask {

	protected final int FINAL_STAGE = 1;

	protected SliceMatrix_CodingResult _smCodingResult;
	protected CodedCoefficients _cc;
	
	public EncodingSourceCodedBatch_SequentialCodingTask(ICodingEngine engine,
			ICodingListener listener, CodingId id, CodedBatch codeBatch) {
		super(engine, listener, id, codeBatch);
	}

	@Override
	public CodedBatchType getSupportedCodedBatchType() {
		return CodedBatchType.SRC_CODED_BATCH;
	}
	
	@Override
	protected synchronized void runStagePrivately() {
		switch (_currentStage) {
		case -1:
		{
			_cc =
				_codeBatch.getNewCodedCoefficients(_codeBatch.getBulkMatrix()._rows);
			_cc.verify();
			
			BulkMatrix bm = _codeBatch.getBulkMatrix();
			_smCodingResult = _engine.multiply(this, _cc, bm);

			setCurrentStage(0);
			break;
		}
		
		case 0:
		{
			if(_smCodingResult.isFailed()) {
				failed();
			} else if (_smCodingResult.isFinished()) {
				SliceMatrix sm = _smCodingResult.getResult();
				CodedPiece codedSlice = sm.createEmptyCodedPiece(_cc, sm);
				((CodedSlice_CodingResult)_result).setResult(codedSlice);
				setCurrentStage(1);
			}
			break;
		}
		
		default:
			throw new IllegalStateException("" + _currentStage);
		}
	}
	
	@Override
	protected int getFinalStage() {
		return FINAL_STAGE;
	}
}
