/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.BulkMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTaskType;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;


public class Decoding_SequentialCodingTask extends SequentialCodingTask {

	protected final int FINAL_STAGE = 1;
	
	public final ReceivedCodedBatch _receivedCodeBatch;
	protected BulkMatrix_CodingResult _decodedBMResult;
	protected Equals_CodingResult _equalityResult;
	
	public Decoding_SequentialCodingTask(
			ICodingEngine engine, ICodingListener listener, CodingId id, ReceivedCodedBatch receivedCodeBatch) {
		super(engine, listener, id, SequentialCodingTaskType.DECODE);
		
		_receivedCodeBatch = receivedCodeBatch;
	}

	@Override
	protected CodingResult getEmptyCodingResults() {
		return new Equals_CodingResult(this, _id);
	}

	@Override
	public synchronized void codingFailed(CodingResult result) {
		((ICodingListener)_engine).codingFailed(result);
		failed();
	}

	@Override
	public synchronized void codingFinished(CodingResult result) {
		if(isFinished())
			return;
		
		((ICodingListener)_engine).codingFinished(result);
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
		if(isFinished())
			return;
		
		switch (_currentStage) {
		case -1:
		{
			if(_receivedCodeBatch.isSolved()) {
				((Equals_CodingResult)_result).setResult(true);
				setCurrentStage(0);
				setCurrentStage(1);
				return;
			}
			
			CodedPiece[] solvingCodedSlices = _receivedCodeBatch.canSolve();
			if(solvingCodedSlices==null) {
				((Equals_CodingResult)_result).setResult(false);
				setCurrentStage(0);
				setCurrentStage(1);
				return;
			}

			_listener.codingPreliminaryStageCompleted(_result);
			
			// Now solve the equations
			BulkMatrix contentMatrix = _receivedCodeBatch.createNewBulkMatrix(solvingCodedSlices);
			ByteMatrix coefficientsMatrixInverse2 = _receivedCodeBatch.getInverseCoefficientsForSolvingCodedSlices();
			if(coefficientsMatrixInverse2 == null)
				throw new IllegalStateException();

			_decodedBMResult = _engine.multiply(this, coefficientsMatrixInverse2, contentMatrix);
			setCurrentStage(0);
			
			break;
		}
		
		case 0:
		{
			if(_decodedBMResult.isFailed()) {
				failed();
			} else if (_decodedBMResult.isFinished()) {
				BulkMatrix content = _decodedBMResult.getResult();
				_receivedCodeBatch.setContent(content);
				((Equals_CodingResult)_result).setResult(true);
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
