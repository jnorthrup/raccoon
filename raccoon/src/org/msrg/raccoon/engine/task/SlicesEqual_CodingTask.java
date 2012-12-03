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
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;


public class SlicesEqual_CodingTask extends CodingTask {
	
	public final SliceMatrix _sm1;
	public final SliceMatrix _sm2;
	
	public SlicesEqual_CodingTask(
			ICodingListener listener, CodingId id, SliceMatrix sm1,  SliceMatrix sm2) {
		super(listener, id, CodingTaskType.SLICES_EQUAL);
		
		_sm1 = sm1;
		_sm2 = sm2;
	}
	
	@Override
	protected CodingResult getEmptyCodingResults() {
		return new Equals_CodingResult(this, _id);
	}
}