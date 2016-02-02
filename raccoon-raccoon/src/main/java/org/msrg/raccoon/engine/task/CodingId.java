/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task;

import org.msrg.raccoon.engine.task.CodingId;

public class CodingId {

	private static int _lastId = 0;
	
	private final int _id;
	
	private CodingId(int id) {
		_id = id;
	}
	
	public static synchronized CodingId getNewCodingId() {
		return new CodingId(_lastId++);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null)
			return false;
		if(!obj.getClass().isAssignableFrom(this.getClass()))
			return false;
		
		CodingId idObj = (CodingId) obj;
		return _id == idObj._id;
	}
	
	@Override
	public int hashCode() {
		return _id;
	}
	
	@Override
	public String toString() {
		return "ID[" + _id + "]";
	}
}
