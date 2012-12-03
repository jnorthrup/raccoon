/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.thread;

import org.msrg.raccoon.engine.thread.ThreadId;

public class ThreadId {

	private static int _lastId = 0;
	
	private final int _id;
	
	private ThreadId(int id) {
		_id = id;
	}
	
	public static synchronized ThreadId getNewThreadId() {
		return new ThreadId(_lastId++);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null)
			return false;
		if(!obj.getClass().isAssignableFrom(this.getClass()))
			return false;
		
		ThreadId idObj = (ThreadId) obj;
		return _id == idObj._id;
	}
	
	@Override
	public int hashCode() {
		return _id;
	}
	
	@Override
	public String toString() {
		return "TID[" + _id + "]";
	}

}
