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
import org.msrg.raccoon.engine.task.result.ByteMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;


public class Inverse_CodingTask extends CodingTask {
	
	public final ByteMatrix _m;
	
	public Inverse_CodingTask(ICodingListener listener, CodingId id, ByteMatrix m) {
		super(listener, id, CodingTaskType.INVERSE);
		
		_m = m;
	}

	@Override
	protected CodingResult getEmptyCodingResults() {
		return new ByteMatrix_CodingResult(this, _id);
	}
}