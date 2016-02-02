/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.result;

import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResultsType;


public class CodedSlice_CodingResult extends CodingResult {
	
	protected CodedPiece _codedSlice;
	
	public CodedSlice_CodingResult(CodingTask cTask, CodingId id) {
		super(cTask, id, CodingResultsType.CODED_SLICE_MATRIX);
	}
	
	public void setResult(CodedPiece codedSliceResult) {
		if(isFinished())
			throw new IllegalStateException();

		if(isFailed())
			throw new IllegalStateException();
		
		_codedSlice = codedSliceResult;
		if(_codedSlice==null)
			throw new IllegalArgumentException();
	}
	
	public CodedPiece getResult() {
		if(!isFinished())
			throw new IllegalStateException();

		return _codedSlice;
	}
}
