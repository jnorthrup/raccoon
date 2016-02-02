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

public class Equals_CodingResult extends CodingResult {

	protected boolean _equals;
	
	public Equals_CodingResult(CodingTask cTask, CodingId id) {
		super(cTask, id, CodingResultsType.BOOLEAN);
	}

	public void setResult(boolean equals) {
		if(isFinished())
			throw new IllegalStateException();

		if(isFailed())
			throw new IllegalStateException();
		
		_equals = equals;
	}
	
	public boolean getResult() {
		if(!isFinished())
			throw new IllegalStateException();
		
		return _equals;
	}
	
	@Override
	public String toString() {
		return "EQ_RESULT[" + _equals + "]";
	}
}
