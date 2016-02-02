/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

public enum CodingEngineEventType {

	ENG_ET_FILE_TASK,

	ENG_ET_THREAD_FREE,
	ENG_ET_THREAD_NEW,
	ENG_ET_THREAD_BUSY,
	
	ENG_ET_NEW_TASK,
	END_ET_SEQ_TASK_FINISHED,
	END_ET_SEQ_TASK_FAILED,
	
	ENG_ET_TASK_FAILED,
	ENG_ET_TASK_FINISHED,
	ENG_ET_TASK_STARTED,

}
