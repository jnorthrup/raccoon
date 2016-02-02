/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task;

public enum CodingTaskType {
	
	FILE_TASK											(false),
	
	SLICES_EQUAL										(false),
	MULTIPLY											(false),
	INVERSE												(false),
	
	SEQUENCIAL											(true),
	
	;
	
	public final boolean _isSequencial;
	
	CodingTaskType(boolean isSequencial) {
		_isSequencial = isSequencial;
	}

}
