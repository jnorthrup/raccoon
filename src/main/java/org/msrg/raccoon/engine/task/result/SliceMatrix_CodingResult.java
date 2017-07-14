/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.result;

import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;


public class SliceMatrix_CodingResult extends CodingResult {

    protected SliceMatrix _smResult;

    public SliceMatrix_CodingResult(CodingTask cTask, CodingId id) {
        super(cTask, id, CodingResultsType.SLICE_MATRIX);
    }

    public SliceMatrix getResult() {
        if (!isFinished())
            throw new IllegalStateException();

        return _smResult;
    }

    public void setResult(SliceMatrix smResult) {
        if (isFinished() || isFailed())
            throw new IllegalStateException();

        _smResult = smResult;
        if (_smResult == null)
            throw new IllegalArgumentException();
    }
}
