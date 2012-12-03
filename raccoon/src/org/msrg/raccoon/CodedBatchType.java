/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

public enum CodedBatchType {

	SRC_CODED_BATCH		("SBatch"),
	RCV_CODED_BATCH		("RBatch"),
	;
	
	private String _str;
	
	CodedBatchType(String str) {
		_str = str;
	}
	
	@Override
	public String toString() {
		return _str;
	}
}
