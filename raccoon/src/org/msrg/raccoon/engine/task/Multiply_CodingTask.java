/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task;

import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.CodingTaskType;

import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;


public class Multiply_CodingTask extends CodingTask {
	
	public final ByteMatrix1D _m;
	public final BulkMatrix _bm;
	
	public Multiply_CodingTask(ICodingListener listener, CodingId id, ByteMatrix1D m, BulkMatrix bm) {
		super(listener, id, CodingTaskType.MULTIPLY);
		
		_m = m;
		_bm = bm;
		if(_m==null)
			throw new NullPointerException();
		if(!_m.verifyNotNull())
			throw new NullPointerException();
		if(_bm==null)
			throw new NullPointerException();
	}
	
//	@Override
//	public synchronized void finished() {
//		throw new UnsupportedOperationException("Do not use this directly. Use setInverse(.) instead.");
//	}
//
//	public void setResult(SliceMatrix result) {
//		((SliceMatrix_CodingResult)_result).setResult(result);
//
//		super.finished();
//	}

	@Override
	protected CodingResult getEmptyCodingResults() {
		return new SliceMatrix_CodingResult(this, _id);
	}
}