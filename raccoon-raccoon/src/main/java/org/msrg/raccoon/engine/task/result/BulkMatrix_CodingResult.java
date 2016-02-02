/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.result;

import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResultsType;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;


public class BulkMatrix_CodingResult extends CodingResult {
	
	protected BulkMatrix _smResult;
	
	public BulkMatrix_CodingResult(CodingTask cTask, CodingId id) {
		super(cTask, id, CodingResultsType.BULK_MATRIX);
	}
	
	public void setResult(BulkMatrix smResult) {
		if(isFinished())
			throw new IllegalStateException();

		if(isFailed())
			throw new IllegalStateException();
		
		_smResult = smResult;
		if(_smResult==null)
			throw new IllegalArgumentException();
	}
	
	public BulkMatrix getResult() {
		if(!isFinished())
			throw new IllegalStateException();

		return _smResult;
	}
}
