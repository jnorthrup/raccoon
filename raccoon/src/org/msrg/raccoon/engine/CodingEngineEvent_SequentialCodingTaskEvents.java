/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;

import org.msrg.raccoon.engine.CodingEngineEvent;
import org.msrg.raccoon.engine.CodingEngineEventType;
import org.msrg.raccoon.engine.CodingEngineEvent_SequentialCodingTaskEvents;

public abstract class CodingEngineEvent_SequentialCodingTaskEvents extends CodingEngineEvent {

	public final SequentialCodingTask _seqCodingTask;
	
	protected CodingEngineEvent_SequentialCodingTaskEvents(CodingEngineEventType eventType, SequentialCodingTask seqCodingTask) {
		super(eventType);
		
		_seqCodingTask = seqCodingTask;
	}
}


class CodingEngineEvent_SequentialCodingTaskFinished extends CodingEngineEvent_SequentialCodingTaskEvents {

	protected CodingEngineEvent_SequentialCodingTaskFinished(SequentialCodingTask seqCodingTask) {
		super(CodingEngineEventType.END_ET_SEQ_TASK_FINISHED, seqCodingTask);
	}
}

class CodingEngineEvent_SequentialCodingTaskFailed extends CodingEngineEvent_SequentialCodingTaskEvents {

	protected CodingEngineEvent_SequentialCodingTaskFailed(SequentialCodingTask seqCodingTask) {
		super(CodingEngineEventType.END_ET_SEQ_TASK_FAILED, seqCodingTask);
	}
}