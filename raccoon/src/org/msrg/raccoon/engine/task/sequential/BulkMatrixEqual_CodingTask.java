/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import java.util.HashSet;
import java.util.Set;

import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTaskType;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;


public class BulkMatrixEqual_CodingTask extends SequentialCodingTask {

	protected final int FINAL_STAGE = 1;
	
	protected final BulkMatrix _bm1;
	protected final BulkMatrix _bm2;
	protected Equals_CodingResult[] _slicesEqualityResults;
	protected Set<Equals_CodingResult> _slicesEqualityResultsSet;
	
	public BulkMatrixEqual_CodingTask(ICodingEngine engine,
			ICodingListener listener, CodingId id,
			BulkMatrix bm1, BulkMatrix bm2) {
		super(engine, listener, id, SequentialCodingTaskType.BULK_MATRIX_EQUAL);
		
		_bm1 = bm1;
		_bm2 = bm2;
	}

	@Override
	public synchronized void codingFailed(CodingResult result) {
		((ICodingListener)_engine).codingFailed(result);
		failed();
	}

	@Override
	public synchronized void codingFinished(CodingResult result) {
		((ICodingListener)_engine).codingFinished(result);
		if(!_slicesEqualityResultsSet.remove(result))
			throw new IllegalStateException(result.toString());
		
		runStagePrivately();

		if(reachedFinalStage())
			finished();
	}

	@Override
	public synchronized void codingStarted(CodingResult result) {
		return;
	}

	@Override
	protected void runStagePrivately(){
		switch (_currentStage) {
		case -1:
		{
			int slices1 = _bm1.getSliceCount();
			int slices2 = _bm2.getSliceCount();
			
			if(_bm1._cols != _bm2._cols || _bm1._rows != _bm2._rows || slices1 != slices2) {
				((Equals_CodingResult)_result).setResult(false);
				setCurrentStage(0);
				setCurrentStage(1);
			} else {
				_slicesEqualityResults = new Equals_CodingResult[slices1];
				_slicesEqualityResultsSet = new HashSet<Equals_CodingResult>();
				for(int i=0 ; i<slices1 ; i++) {
					SliceMatrix sm1 = _bm1.slice(i);
					SliceMatrix sm2 = _bm2.slice(i);
					
					Equals_CodingResult sliceEqualityResult = _engine.checkEquality(this, sm1, sm2);
					_slicesEqualityResultsSet.add(sliceEqualityResult);
					_slicesEqualityResults[i] = sliceEqualityResult;
				}
				setCurrentStage(0);
			}
			break;
		}
		
		case 0:
		{
			if(_slicesEqualityResultsSet.isEmpty()) {
				boolean result = true;
				for(int i=0 ; i<_slicesEqualityResults.length && result; i++)
					if(!_slicesEqualityResults[i].getResult())
						result = false;
				
				((Equals_CodingResult)_result).setResult(result);
				setCurrentStage(1);
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
