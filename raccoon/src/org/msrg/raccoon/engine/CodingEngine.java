/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.CodingTaskFailed;
import org.msrg.raccoon.engine.task.CodingTaskStatus;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.thread.CodingThread;
import org.msrg.raccoon.engine.thread.CodingThreadImpl;

/**
 * This abstract class implements ICodingEngine and deals with management of
 * the engine's worker threads and internal state maintenance. It leaves out
 * implementation of all matrix-related operations to its subclass(es).
 * <p>
 * The coding engine has three tasks priority queues, i.e., low, normal, and
 * high. The server's main thread picks coding tasks from these queues and
 * assigns them to available worker threads in the free threads pool. Once
 * the task is terminated (with success or failure) the thread is added to the
 * free threads pool and the listener of coding tasks is notified of the
 * results. For sequential tasks, the listener is notified only after
 * completion of all sub-tasks.
 * 
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * @since 0.1
 */
public abstract class CodingEngine extends Thread implements ICodingEngine {

	public static boolean DEBUG = false;
	
	protected final Object _lock = new Object();
	
	/**
	 * List of default coding listeners. These listeners are notified for all
	 * coding task status updates.
	 */
	private List<ICodingListener> _listeners =
		new LinkedList<ICodingListener>();

	/**
	 * Tasks priority queues
	 */
	protected final List<CodingEngineEvent> _lowPriorityEventQueue =
		new LinkedList<CodingEngineEvent>();
	protected final List<CodingEngineEvent> _normalPriorityEventQueue =
		new LinkedList<CodingEngineEvent>();
	protected final List<CodingEngineEvent> _highPriorityEventQueue =
		new LinkedList<CodingEngineEvent>();
	
	protected final void addCodingTaskEngineEvent(CodingEngineEvent event) {
		synchronized (_lock)
		{
			switch(event._eventType){
			case ENG_ET_FILE_TASK:
				_lowPriorityEventQueue.add(event);
				break;
				
			case ENG_ET_NEW_TASK:
				_normalPriorityEventQueue.add(event);
				break;
				
			case END_ET_SEQ_TASK_FINISHED:
			case END_ET_SEQ_TASK_FAILED:
			case ENG_ET_TASK_FAILED:
			case ENG_ET_TASK_FINISHED:
			case ENG_ET_TASK_STARTED:
			case ENG_ET_THREAD_BUSY:
			case ENG_ET_THREAD_FREE:
			case ENG_ET_THREAD_NEW:
				_highPriorityEventQueue.add(event);
				break;
				
			default:
				throw new UnsupportedOperationException("" + event);
			}
			
			_lock.notify();
		}
	}
		
	protected int _threadCount;
	protected CodingEngine(int threadCount) {
		super("CodingEngineT");
		_threadCount = threadCount;
		
		System.out.println("CodingEngine Thread count: " + _threadCount);
	}
	
	public void registerCodingListener(ICodingListener listener) {
		synchronized (_lock) {
			if(!_listeners.contains(listener))
				_listeners.add(listener);
		}
	}
	
	public void deregisterCodingListener(ICodingListener listener) {
		synchronized (_lock) {
			_listeners.remove(listener);
		}
	}
	
//	public void threadIsFree(FileManagerThread fThread) {
//		CodingEngineEvent_FreeThreadEvent freeThreadEvent = new CodingEngineEvent_FreeThreadEvent(fThread);
//		addCodingTaskEngineEvent(freeThreadEvent);
//	}
	
	public void threadIsFree(CodingThread cThread) {
		CodingEngineEvent_FreeThreadEvent freeThreadEvent = new CodingEngineEvent_FreeThreadEvent(cThread);
		addCodingTaskEngineEvent(freeThreadEvent);
	}

	protected void threadIsBusy(CodingThread cThread) {
		CodingEngineEvent_BusyThreadEvent freeThreadEvent = new CodingEngineEvent_BusyThreadEvent(cThread);
		addCodingTaskEngineEvent(freeThreadEvent);
	}

	@Override
	public void startComponent () {
		synchronized (_lock) {
			for(CodingThread cThread : _threads)
				cThread.start();
			
			super.start();
		}
	}
	
	@Override
	public void start() {
		throw new UnsupportedOperationException("To ignite the engine, use startComponent() instead.");
	}
	
	@Override
	public final void run() {
		CodingEngineEvent event = null;
		boolean eventFrom_highPriorityCodingTasksEventQueue = false;
		boolean eventFrom_normalPriorityCodingEventQueue = false;
		boolean eventFrom_lowPriorityCodingEventQueue = false;
		
		while(true) {
			synchronized (_lock) {
				if(event!=null) {
					if(eventFrom_normalPriorityCodingEventQueue) {
						eventFrom_normalPriorityCodingEventQueue = false;
						if(_normalPriorityEventQueue.remove(0) != event)
							throw new IllegalStateException();
					}
					
					if(eventFrom_highPriorityCodingTasksEventQueue) {
						eventFrom_highPriorityCodingTasksEventQueue = false;
						if(_highPriorityEventQueue.remove(0) != event)
							throw new IllegalStateException();
					}
					
					if(eventFrom_lowPriorityCodingEventQueue) {
						eventFrom_lowPriorityCodingEventQueue = false;
						if(_lowPriorityEventQueue.remove(0) != event)
							throw new IllegalStateException();
					}
				}
				
				while(_normalPriorityEventQueue.isEmpty()
						&& _highPriorityEventQueue.isEmpty()
						&& _lowPriorityEventQueue.isEmpty()) {
					try {
						_lock.wait();
					} catch (InterruptedException itx) {
						itx.printStackTrace();
					}
				}
				
				if(!_highPriorityEventQueue.isEmpty()) {
					event = _highPriorityEventQueue.get(0);
					eventFrom_highPriorityCodingTasksEventQueue = true;
				} else if(!_normalPriorityEventQueue.isEmpty()) {
					event = _normalPriorityEventQueue.get(0);
					eventFrom_normalPriorityCodingEventQueue = true;
				} else if (!_lowPriorityEventQueue.isEmpty()) {
					event = _lowPriorityEventQueue.get(0);
					eventFrom_lowPriorityCodingEventQueue = true;
				} else
					throw new IllegalStateException();
			}
			
			//
			try{
				processCodingEvent(event);
			} catch (Exception x) {
				x.printStackTrace();
			}
			
			Thread.yield();
		}
	}
	
	protected void processCodingEvent(CodingEngineEvent event) throws CodingTaskFailed {
		if(DEBUG)
			System.out.println("EVENT_PROCESSING:" + event);
	}
	
	@Override
	public void init() {
		synchronized (_lock) {
			for(int i=0 ; i<_threadCount ; i++) {
				CodingThread cThread = new CodingThreadImpl(this);
				_threads.add(cThread);
			}
		}
	}
	
	protected final List<CodingTask> _outstandingTasks = new LinkedList<CodingTask>();
	protected final Set<CodingThread> _threads = new HashSet<CodingThread>();
	protected final Set<CodingThread> _freeThreads = new HashSet<CodingThread>();
	protected final Set<CodingThread> _busyThreads = new HashSet<CodingThread>();
		
	public void codingThreadFailed(CodingThread codingThread) {
		// No idea what to do!
		throw new IllegalStateException();
	}
	
	public void codingTaskStarted(CodingThread cThread, CodingTask cTask) {
		CodingEngineEvent_ExecutionEvent event =
			new CodingEngineEvent_ExecutionEvent(cThread, cTask, CodingTaskStatus.STARTED);
		addCodingTaskEngineEvent(event);
	}
	
	public void codingTaskFinished(CodingThread codingThread, CodingTask codingTask) {
		if(!codingTask.isFinished())
			throw new IllegalArgumentException("" + codingTask);
		
		notifyListener(codingTask);
	}
	
	public void codingTaskFailed(CodingThread codingThread, CodingTask codingTask) {
		if(codingTask.isFinished())
			throw new IllegalArgumentException(codingTask.toString());
		
		notifyListener(codingTask);
	}

	protected void scheduleTask() {
		scheduleTask(null);
	}
	
	protected void scheduleTask(CodingTask cTask) {
		CodingThread thread = null;
		synchronized(_lock)
		{
			if(cTask==null)
			{
				if(_outstandingTasks.isEmpty())
					return;
				else if (_freeThreads.isEmpty())
					return;
				else
					cTask = _outstandingTasks.remove(0);
			} else if(_freeThreads.isEmpty()) {
				_outstandingTasks.add(cTask);
				return;
			}
			
			thread = _freeThreads.iterator().next();
			_freeThreads.remove(thread);
			_busyThreads.add(thread);
		}
		
		thread.addNewTask(cTask);
		return;
	}
	
	protected void notifyListener(CodingTask cTask) {
		ICodingListener listener = cTask._listener;
		CodingResult result = cTask._result;
		if(cTask.isFinished()) {
			if(listener != null)
				listener.codingFinished(result);
			
			for(ICodingListener generalListener : _listeners)
				generalListener.codingFinished(result);
		} else if(cTask.isFailed()) {
			if(listener != null)
				listener.codingFailed(result);
			
			for(ICodingListener generalListener : _listeners)
				generalListener.codingFailed(result);
		} else
			throw new IllegalArgumentException("" + cTask);
	}

	@Override
	public int getFreeThreadsCount() {
		synchronized(_lock){
			return _freeThreads.size();
		}
	}

	@Override
	public int getBusyThreadsCount() {
		synchronized(_lock){
			return _busyThreads.size();
		}
	}
	
	@Override
	public int getTotalThreadsCount() {
		synchronized(_lock){
			return _threads.size();
		}
	}
	
	@Override
	public String toString() {
		return "CodingEngine:[" + _freeThreads.size() + "/" + _busyThreads.size() + "]";
	}

	public void sequentialCodingTaskFailed(SequentialCodingTask seqCodingTask) {
		CodingEngineEvent_SequentialCodingTaskFailed seqCodingEvent = new CodingEngineEvent_SequentialCodingTaskFailed(seqCodingTask);
		addCodingTaskEngineEvent(seqCodingEvent);
	}

	public void sequentialCodingTaskFinished(SequentialCodingTask seqCodingTask) {
		CodingEngineEvent_SequentialCodingTaskFinished seqCodingEvent = new CodingEngineEvent_SequentialCodingTaskFinished(seqCodingTask);
		addCodingTaskEngineEvent(seqCodingEvent);
	}
	
	protected Map<CodingThread, Long> _threadCheckins = new HashMap<CodingThread, Long>();
	public void threadCheckin(CodingThread cThread) {
		synchronized (_lock) {
			_threadCheckins.put(cThread, new Long(System.currentTimeMillis()));
		}
	}
	
	public static final long LATE_THREAD_CHECKIN_TIMEOUT = 5000;
	protected List<CodingThread> getLateThreads() {
		List<CodingThread> lateThreadList = new LinkedList<CodingThread>();
		synchronized (_lock) {
			long currentTime = System.currentTimeMillis();
			for(CodingThread cThread : _threads) {
				Long lastCheckin = _threadCheckins.get(cThread);
				if(lastCheckin != null && currentTime - lastCheckin > LATE_THREAD_CHECKIN_TIMEOUT)
					lateThreadList.add(cThread);
			}
		}
		
		return lateThreadList;
	}
	
	public int getPendingLowPriorityEventsCount() {
		synchronized (_lock) {
			return _lowPriorityEventQueue.size();
		}
	}
	
	public int getPendingNormalPriorityEventsCount() {
		synchronized (_lock) {
			return _normalPriorityEventQueue.size();
		}
	}
	
	public int getPendingHighPriorityEventsCount() {
		synchronized (_lock) {
			return _highPriorityEventQueue.size();
		}
	}
}