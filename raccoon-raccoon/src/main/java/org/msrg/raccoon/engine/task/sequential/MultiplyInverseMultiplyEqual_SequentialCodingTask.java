/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.BulkMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.ByteMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTaskFailed;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTaskType;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;


public class MultiplyInverseMultiplyEqual_SequentialCodingTask extends SequentialCodingTask {

	protected final int FINAL_STAGE = 3;
	
	protected final ByteMatrix _m;
	protected final BulkMatrix _bm;
	
	protected Equals_CodingResult _finalEqualityResult;
	protected BulkMatrix_CodingResult _multipliedInverseMultipliedSMResult;
	protected BulkMatrix_CodingResult _multipliedSMResult;
	protected ByteMatrix_CodingResult _mInverseResult;
	
	public MultiplyInverseMultiplyEqual_SequentialCodingTask(
			ICodingEngine engine, ICodingListener listener, CodingId id,
			ByteMatrix m, BulkMatrix bm) {
		super(engine, listener, id, SequentialCodingTaskType.MULTIPLY_INVERSE_MULTIPLY_EQUAL);
		
		_m = m;
		_bm = bm;
	}

	@Override
	public synchronized void codingFinished(CodingResult result) {
		((ICodingListener)_engine).codingFinished(result);
		
		runStagePrivately();
		
		if(reachedFinalStage())
			finished();
	}

	@Override
	public synchronized void codingFailed(CodingResult result) {
		((ICodingListener)_engine).codingFailed(result);
		failed();
	}

	@Override
	public synchronized void codingStarted(CodingResult result) {
		return;
	}

	@Override
	protected void runStagePrivately() {
		switch (_currentStage) {
		case -1:
		{
			setCurrentStage(0);
			_multipliedSMResult =
				(BulkMatrix_CodingResult) _engine.multiply(this, _m, _bm);
			_mInverseResult = (ByteMatrix_CodingResult) _engine.inverse(this, _m);
			break;
		}
		
		case 0:
		{
			if(_mInverseResult.isFinished() && _multipliedSMResult.isFinished()) {
				setCurrentStage(1);
				_multipliedInverseMultipliedSMResult =
					(BulkMatrix_CodingResult) _engine.multiply(this, _mInverseResult.getResult(), _multipliedSMResult.getResult());
			}
			else if(_mInverseResult.isFailed() || _multipliedSMResult.isFailed())
				failed();
			
			break;
		}
		
		case 1:
		{
			if(_multipliedInverseMultipliedSMResult.isFailed())
				throw new SequentialCodingTaskFailed(this);
			
			else if (_multipliedInverseMultipliedSMResult.isFinished()) {
				setCurrentStage(2);
				_finalEqualityResult = (Equals_CodingResult) _engine.checkEquality(this, _multipliedInverseMultipliedSMResult.getResult(), _bm);
			}
			break;
		}
		
		case 2:
		{
			if(_finalEqualityResult.isFailed())
				failed();
			else if (_finalEqualityResult.isFinished()) {
				setCurrentStage(3);
				((Equals_CodingResult)_result).setResult(_finalEqualityResult.getResult());
			}
			break;
		}
		
		default:
			throw new IllegalStateException("" + _currentStage);
		}
	}

	@Override
	protected CodingResult getEmptyCodingResults() {
		return new Equals_CodingResult(this, _id);
	}

	@Override
	protected int getFinalStage() {
		return FINAL_STAGE;
	}
}
