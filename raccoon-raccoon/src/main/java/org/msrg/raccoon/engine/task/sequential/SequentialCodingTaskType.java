/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

public enum SequentialCodingTaskType {

	PAGEIN_PAGEOUT_EQUALS,
	
	MULTIPLY_INVERSE_MULTIPLY_EQUAL,
	
	ENCODE_DECODE_EQUAL,
	ENCODE_RECEIVED_ENCODE_DECODE_EQUAL,
	
	MULTIPLY_BULK_MATRIX,
	BULK_MATRIX_EQUAL,
	ENCODE,
	DECODE,
	
}
