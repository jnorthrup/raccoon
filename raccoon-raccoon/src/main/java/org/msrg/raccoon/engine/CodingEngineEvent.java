/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.CodingTaskStatus;
import org.msrg.raccoon.engine.thread.CodingThread;

import org.msrg.raccoon.engine.CodingEngineEvent;
import org.msrg.raccoon.engine.CodingEngineEventType;
import org.msrg.raccoon.engine.CodingEngineEvent_ThreadEvent;

public abstract class CodingEngineEvent {
	
	public final CodingEngineEventType _eventType;
	
	protected CodingEngineEvent(CodingEngineEventType eventType) {
		_eventType = eventType;
	}

	@Override
	public String toString() {
		return _eventType.toString();
	}
}

class CodingEngineEvent_NewCodingTask extends CodingEngineEvent {
	
	protected final CodingTask _cTask;
	
	public CodingEngineEvent_NewCodingTask(CodingTask cTask) {
		super(CodingEngineEventType.ENG_ET_NEW_TASK);
		_cTask = cTask;
	}
}

class CodingEngineEvent_FreeThreadEvent extends CodingEngineEvent_ThreadEvent {
	CodingEngineEvent_FreeThreadEvent(CodingThread cThread) {
		super(cThread, CodingEngineEventType.ENG_ET_THREAD_FREE);
	}
}

class CodingEngineEvent_BusyThreadEvent extends CodingEngineEvent_ThreadEvent {
	CodingEngineEvent_BusyThreadEvent(CodingThread cThread) {
		super(cThread, CodingEngineEventType.ENG_ET_THREAD_BUSY);
	}
}

class CodingEngineEvent_NewThreadEvent extends CodingEngineEvent_ThreadEvent {
	CodingEngineEvent_NewThreadEvent(CodingThread cThread) {
		super(cThread, CodingEngineEventType.ENG_ET_THREAD_FREE);
	}
}

abstract class CodingEngineEvent_ThreadEvent extends CodingEngineEvent {
	
	protected final CodingThread _cThread; 
	
	CodingEngineEvent_ThreadEvent(CodingThread cThread, CodingEngineEventType threadEventType) {
		super(threadEventType);
		_cThread = cThread;
	}
	
}

class CodingEngineEvent_ExecutionEvent extends CodingEngineEvent {
	
	protected final CodingThread _cThread; 
	protected final CodingTask _cTask;
	protected final CodingTaskStatus _status;
	
	CodingEngineEvent_ExecutionEvent(CodingThread cThread, CodingTask cTask, CodingTaskStatus status) {
		super(CodingEngineEventType.ENG_ET_TASK_STARTED);
		_cTask = cTask;
		_status = status;
		_cThread = cThread;
	}
	
}
