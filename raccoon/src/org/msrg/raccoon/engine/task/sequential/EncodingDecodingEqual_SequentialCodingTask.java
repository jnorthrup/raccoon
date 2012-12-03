/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import java.util.HashSet;
import java.util.Set;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTaskType;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;


public class EncodingDecodingEqual_SequentialCodingTask extends SequentialCodingTask {

	protected final int FINAL_STAGE = 3;
	
	public final CodedBatch _codedBatch;
	public final ReceivedCodedBatch _receiverCodedBatch;
	
	protected CodedSlice_CodingResult[] _encodingResults;
	protected Set<CodedSlice_CodingResult> _encodintResultsSet;
	protected Equals_CodingResult _decodingResults;
	protected Equals_CodingResult _decodedEqualityResult;
	
	public EncodingDecodingEqual_SequentialCodingTask(CodedBatch codedBatch,
			ICodingEngine engine,
			ICodingListener listener, CodingId id) {
		super(engine, listener, id, SequentialCodingTaskType.ENCODE_DECODE_EQUAL);
		
		_codedBatch = codedBatch;
		_receiverCodedBatch = new ReceivedCodedBatch(_codedBatch.getSize(), _codedBatch.getBulkMatrix()._rows);

		setCurrentStage(-1);
	}

	@Override
	public synchronized void codingFailed(CodingResult result) {
		((ICodingListener)_engine).codingFailed(result);
		failed();
	}

	@Override
	public synchronized void codingFinished(CodingResult result) {
		if(isFailed())
			return;
		
		((ICodingListener)_engine).codingFinished(result);
		_encodintResultsSet.remove(result);

		try{
			runStagePrivately();
		}catch(Exception x) {
			System.out.println("***ERROR: " + result);
			x.printStackTrace();
			return;
		}
		
		if(reachedFinalStage())
			finished();
	}

	@Override
	public synchronized void codingStarted(CodingResult result) {
		return;
	}
	
	@Override
	protected int getFinalStage() {
		return FINAL_STAGE;
	}

	@Override
	protected void runStagePrivately() {
		if(isFailed())
			return;
		
		switch (_currentStage) {
		case -1:
		{
			int requiredSlices = _codedBatch.getAvailableCodedPieceCount();
			_encodingResults = new CodedSlice_CodingResult[requiredSlices];
			_encodintResultsSet = new HashSet<CodedSlice_CodingResult>();
			
			for(int i=0 ; i<requiredSlices ; i++) {
				CodedSlice_CodingResult encodingResult = _engine.encode(this, _codedBatch);
				_encodingResults[i] = encodingResult;
				_encodintResultsSet.add(encodingResult);
			}
			setCurrentStage(0);
			break;
		}
		
		case 0:
		{
			if(_encodintResultsSet.isEmpty()) {
				for(CodedSlice_CodingResult codedResult : _encodingResults) {
					if(!codedResult.isFinished()) {
						throw new IllegalStateException();
					} else {
						CodedPiece cSlice = codedResult.getResult();
						_receiverCodedBatch.addCodedSlice(cSlice);
					}
				}
				
				_decodingResults = _engine.decode(this, _receiverCodedBatch);
				setCurrentStage(1);
			}
			
			break;
		}
		
		case 1:
		{
			if(_decodingResults.isFailed())
				failed();

			else if(_decodingResults.isFinished()) {
				if (!_decodingResults.getResult()) {
					((Equals_CodingResult)_result).setResult(false);
					setCurrentStage(2);
					setCurrentStage(3);
					setCurrentStage(4);
					setCurrentStage(5);
				} else {
					BulkMatrix bm1 = _codedBatch.getBulkMatrix();
					BulkMatrix bm2 = _receiverCodedBatch.getBulkMatrix();
					if(bm2==null) {
						((Equals_CodingResult)_result).setResult(false);
						setCurrentStage(2);
						setCurrentStage(3);
						setCurrentStage(4);
					} else {
						_decodedEqualityResult = _engine.checkEquality(this, bm1, bm2);
						setCurrentStage(2);
					}
				}
			}
			
			break;
		}
		
		case 2:
		{
			if(_decodedEqualityResult.isFailed()) {
				failed();
			}else if(_decodedEqualityResult.isFinished()) {
				boolean result = _decodedEqualityResult.getResult();
				((Equals_CodingResult)_result).setResult(result);
				setCurrentStage(3);
			}
				
			break;
		}
		
		default:
			throw new IllegalStateException("" + _currentStage + ":" + _status);
		}
	}

	@Override
	protected CodingResult getEmptyCodingResults() {
		return new Equals_CodingResult(this, _id);
	}
}
