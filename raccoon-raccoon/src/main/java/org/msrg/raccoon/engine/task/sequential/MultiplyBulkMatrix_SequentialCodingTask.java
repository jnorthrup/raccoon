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
import org.msrg.raccoon.engine.task.result.BulkMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTaskType;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;


public class MultiplyBulkMatrix_SequentialCodingTask extends SequentialCodingTask {

	protected final int FINAL_STAGE = 1;
	
	protected final ByteMatrix _m;
	protected final BulkMatrix _bm;
	
	protected Equals_CodingResult _finalEqualityResult;
	protected Set<SliceMatrix_CodingResult> _multipliedSMResultsSet;
	protected SliceMatrix_CodingResult[] _multipliedSMResults;
	
	public MultiplyBulkMatrix_SequentialCodingTask(
			ICodingEngine engine, ICodingListener listener, CodingId id,
			ByteMatrix m, BulkMatrix bm) {
		super(engine, listener, id, SequentialCodingTaskType.MULTIPLY_BULK_MATRIX);
		
		_m = m;
		_bm = bm;
	}

	@Override
	public synchronized void codingFailed(CodingResult result) {
		_multipliedSMResultsSet.clear();
		((ICodingListener)_engine).codingFailed(result);
		failed();
	}

	@Override
	public synchronized void codingFinished(CodingResult result) {
		((ICodingListener)_engine).codingFinished(result);
		
		if(!result.isFinished())
			throw new IllegalArgumentException(result.toString());

		if(!_multipliedSMResultsSet.remove(result))
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
	protected void runStagePrivately() {
		switch (_currentStage) {
		case -1:
		{
			setCurrentStage(0);
			
			int rows = _m.getRowSize();
			
			_multipliedSMResultsSet = new HashSet<SliceMatrix_CodingResult>();
			_multipliedSMResults = new SliceMatrix_CodingResult[rows];
			
			if (rows==0) {
				setCurrentStage(1);
			} else {
				for(int i=0 ; i<rows ; i++) {
					Byte[] b = _m.toArray()[i];
					if(b == null)
						throw new NullPointerException("" + i);
					SliceMatrix_CodingResult smCodingResult =
						(SliceMatrix_CodingResult) _engine.multiply(this,
								new ByteMatrix1D(b), _bm);
					_multipliedSMResultsSet.add(smCodingResult);
					_multipliedSMResults[i] = smCodingResult;
				}
			}
			
			break;
		}
		
		case 0:
		{
			if(_multipliedSMResultsSet.isEmpty()) {
				int reultSlices = _multipliedSMResults.length;
				int sliceOffset = 0;
				int rows = _m.getRowSize();
				
				BulkMatrix bmResult;
				if(rows==1)
					bmResult = _bm.createEmptyMatrix(rows, _bm._cols);
					//new BulkMatrix1D(_bm._cols);
				else
					bmResult = _bm.createEmptyMatrix(_m.getRowSize(), _bm._cols);
//					bmResult = new BulkMatrix(_m.getRowSize(), _bm._cols);
				
				for(int i=0 ; i<reultSlices ; i++) {
					SliceMatrix sm = _multipliedSMResults[i].getResult();
					bmResult.add(i, sm);
					sliceOffset += sm._cols;
				}
				
				((BulkMatrix_CodingResult)_result).setResult(bmResult);

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
		return new BulkMatrix_CodingResult(this, _id);
	}

	@Override
	protected int getFinalStage() {
		return FINAL_STAGE;
	}
}
