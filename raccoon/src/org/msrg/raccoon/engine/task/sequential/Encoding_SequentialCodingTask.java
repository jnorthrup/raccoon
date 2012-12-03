/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedBatchType;
import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.sequential.EncodingReceivedCodedBatch_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.EncodingSourceCodedBatch_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.Encoding_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTaskType;


public abstract class Encoding_SequentialCodingTask extends SequentialCodingTask {

	public final CodedBatch _codeBatch;
	
	public static Encoding_SequentialCodingTask getEncoding_SequentialCodingTask(
			ICodingEngine engine, ICodingListener listener, CodingId id, CodedBatch codeBatch) {
		CodedBatchType codedBatchType = codeBatch.getCodedBatchType();
		switch(codedBatchType) {
		case RCV_CODED_BATCH:
			return new EncodingReceivedCodedBatch_SequentialCodingTask(engine, listener, id, codeBatch);
			
		case SRC_CODED_BATCH:
			return new EncodingSourceCodedBatch_SequentialCodingTask(engine, listener, id, codeBatch);
			
		default:
			throw new UnsupportedOperationException("Unknown type: " + codedBatchType);
		}
	}
	
	protected Encoding_SequentialCodingTask(ICodingEngine engine, ICodingListener listener, CodingId id, CodedBatch codeBatch) {
		super(engine, listener, id, SequentialCodingTaskType.ENCODE);
		
		_codeBatch = codeBatch;
		if(_codeBatch.getCodedBatchType() != getSupportedCodedBatchType())
			throw new IllegalArgumentException("Unsupported codedbatchtype: " + _codeBatch.getCodedBatchType());
	}

	@Override
	protected CodingResult getEmptyCodingResults() {
		return new CodedSlice_CodingResult(this, _id);
	}

	@Override
	protected abstract void runStagePrivately();
	
	public abstract CodedBatchType getSupportedCodedBatchType();
	
	@Override
	public final synchronized void codingFailed(CodingResult result) {
		((ICodingListener)_engine).codingFailed(result);
		failed();		
	}

	@Override
	public final synchronized void codingFinished(CodingResult result) {
		((ICodingListener)_engine).codingFinished(result);
		
		runStagePrivately();

		if(reachedFinalStage())
			finished();
	}

	@Override
	public final synchronized void codingStarted(CodingResult result) {
		return;
	}
}
