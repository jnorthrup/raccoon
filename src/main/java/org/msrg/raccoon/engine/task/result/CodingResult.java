/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.result;

import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.CodingTaskStatus;

public abstract class CodingResult {

    public final CodingId _id;
    public final CodingResultsType _resultsType;
    public final CodingTask _cTask;
    protected CodingTaskStatus _status;

    protected CodingResult(CodingTask cTask, CodingId id, CodingResultsType resultsType) {
        _id = id;
        _resultsType = resultsType;
        _cTask = cTask;
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    public CodingTaskStatus getStatus() {
        return _status;
    }

    public void setStatus(CodingTaskStatus status) {
        _status = status;
    }

    public boolean isStarted() {
        return _status == CodingTaskStatus.STARTED;
    }

    public boolean isFailed() {
        return _status == CodingTaskStatus.FAILED;
    }

    public boolean isFinished() {
        return _status == CodingTaskStatus.FINISHED;
    }
}