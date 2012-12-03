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
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;


public class ByteMatrix_CodingResult extends CodingResult {
	
	protected ByteMatrix _m;
	
	public ByteMatrix_CodingResult(CodingTask cTask, CodingId id) {
		super(cTask, id, CodingResultsType.NC_MATRIX);
	}
	
	public void setResult(ByteMatrix mResult) {
		if(isFinished())
			throw new IllegalStateException();

		if(isFailed())
			throw new IllegalStateException();

		_m = mResult;
		if(_m==null)
			throw new IllegalArgumentException();
	}
	
	public ByteMatrix getResult() {
		if(!isFinished())
			throw new IllegalStateException();

		return _m;
	}
}
