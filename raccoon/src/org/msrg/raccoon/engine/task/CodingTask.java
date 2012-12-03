/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task;

import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTaskStatus;
import org.msrg.raccoon.engine.task.CodingTaskType;

import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.result.CodingResult;


public abstract class CodingTask {

	public final CodingTaskType _taskType;
	public final CodingId _id;
	public final CodingResult _result;
	public final ICodingListener _listener;

	protected CodingTaskStatus _status = CodingTaskStatus.CREATED;
	
	protected CodingTask(ICodingListener listener, CodingId id, CodingTaskType taskType) {
		_id = id;
		_taskType = taskType;
		_listener = listener;
//		if(_listener==null)
//			throw new IllegalArgumentException();
		
		_result = getEmptyCodingResults();
	}
	
	protected abstract CodingResult getEmptyCodingResults();
	
	public CodingResult getCodingResults() {
		return _result;
	}
	
	public CodingTaskStatus getStatus() {
		return _status;
	}
	
	public boolean isStarted() {
		return _status == CodingTaskStatus.STARTED;
	}
	
	public final boolean isFinished() {
		return _status == CodingTaskStatus.FINISHED;
	}

	public boolean isFailed() {
		return _status == CodingTaskStatus.FAILED;
	}

	protected void changeStatus(CodingTaskStatus prevAllowedStatus, CodingTaskStatus newStatus) {
		synchronized (_id) {
			if(prevAllowedStatus != null && prevAllowedStatus != _status)
				throw new IllegalStateException("Expected: " + prevAllowedStatus + ", found: " + _status + ", newstate: " + newStatus);

			_status = newStatus;
		}
		
		_result.setStatus(_status);
	}
	
	public synchronized void failed() {
		changeStatus(null, CodingTaskStatus.FAILED);
	}
	
	public synchronized void started() {
		changeStatus(CodingTaskStatus.CREATED, CodingTaskStatus.STARTED);
	}
	
	public synchronized void finished() {
		changeStatus(CodingTaskStatus.STARTED, CodingTaskStatus.FINISHED);
	}
	
	public boolean isSequencial() {
		return _taskType._isSequencial;
	}
	
	@Override
	public final boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public final int hashCode() {
		return super.hashCode();
	}
}
