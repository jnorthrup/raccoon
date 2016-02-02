/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.thread;

import java.util.LinkedList;
import java.util.List;

import org.msrg.raccoon.engine.CodingEngine;
import org.msrg.raccoon.engine.task.CodingTask;

import org.msrg.raccoon.engine.thread.ThreadId;
import org.msrg.raccoon.engine.thread.ThreadType;

public abstract class CodingThread extends Thread {

	public static final int MAX_WAIT_TIME = 1000;
	
	public final ThreadType _threadType;
	public final CodingEngine _engine;
	public final ThreadId _id;
	protected final List<CodingTask> _taskList = new LinkedList<CodingTask>();
	
	protected Object _LOCK = _taskList;
	protected boolean _continue = true;
	
	protected CodingThread(CodingEngine engine) {
		this(engine, ThreadType.CODING_THREAD);
	}
	
	protected CodingThread(CodingEngine engine, ThreadType threadType) {
		_engine = engine;
		_id = ThreadId.getNewThreadId();
		_threadType = threadType;
	}

	protected void runTask(CodingTask codingTask) {
		if(CodingEngine.DEBUG)
			System.out.println("runTask: " + codingTask);
	}
	
	public void addNewTask(CodingTask codingTask) {
		synchronized (_LOCK) {
			_taskList.add(codingTask);
			_LOCK.notify();
		}
	}
	
	protected final void runMe(CodingTask codingTask) {
		try{
			codingTask.started();
			_engine.codingTaskStarted(this, codingTask);
			runTask(codingTask);
		} catch (Exception x) {
			codingTask.failed();
			x.printStackTrace();
		}
		
		if(codingTask.isFailed()) {
			if(!codingTask.isSequencial())
				_engine.codingTaskFailed(this, codingTask);
			
		} else if(codingTask.isFinished()) {
			if(!codingTask.isSequencial())
				_engine.codingTaskFinished(this, codingTask);
		}
	}
	
	public boolean init() {
		_continue = true;
		return true;
	}
	
	public void retire() {
		return;
	}
	
	@Override
	public final void run() {
		init();
		
		CodingTask codingTask = null;
		while(_continue)
		{
			synchronized (_LOCK) {
				while(_taskList.isEmpty()) {
					try {
						_engine.threadIsFree(this);
						_LOCK.wait(MAX_WAIT_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
						_engine.codingThreadFailed(this);
					}
					_engine.threadCheckin(this);
				}

				codingTask = _taskList.remove(0);
			}
			
			try{
				runMe(codingTask);
			}catch(Exception iox) {
				codingTask.failed();
				iox.printStackTrace();
//				_engine.codingThreadFailed(this);
			}
		}
		
		retire();
	}
	
	@Override
	public String toString() {
		return _id.toString();
	}
}
