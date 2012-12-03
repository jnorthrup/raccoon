/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.CodingTaskStatus;
import org.msrg.raccoon.engine.task.Inverse_CodingTask;
import org.msrg.raccoon.engine.task.Multiply_CodingTask;
import org.msrg.raccoon.engine.task.SlicesEqual_CodingTask;
import org.msrg.raccoon.engine.task.result.BulkMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.ByteMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.engine.task.sequential.BulkMatrixEqual_CodingTask;
import org.msrg.raccoon.engine.task.sequential.Decoding_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.Encoding_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.MultiplyBulkMatrix_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.thread.CodingThread;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;

import org.msrg.raccoon.engine.CodingEngine;
import org.msrg.raccoon.engine.CodingEngineEvent;
import org.msrg.raccoon.engine.CodingEngineEvent_BusyThreadEvent;
import org.msrg.raccoon.engine.CodingEngineEvent_ExecutionEvent;
import org.msrg.raccoon.engine.CodingEngineEvent_FreeThreadEvent;
import org.msrg.raccoon.engine.CodingEngineEvent_NewCodingTask;
import org.msrg.raccoon.engine.CodingEngineEvent_NewThreadEvent;
import org.msrg.raccoon.engine.CodingEngineEvent_SequentialCodingTaskFailed;
import org.msrg.raccoon.engine.CodingEngineEvent_SequentialCodingTaskFinished;
import org.msrg.raccoon.engine.ICodingListener;

public class CodingEngineImpl extends CodingEngine implements ICodingListener {
	
	public CodingEngineImpl(int threadCount) {
		super(threadCount);
	}
	
	@Override
	public void codingTaskStarted(CodingThread codingThread, CodingTask codingTask) {
		if(DEBUG)
			System.out.println("TaskStarted:" + codingTask);
	}

	@Override
	public void codingThreadFailed(CodingThread codingThread) {
		synchronized(_lock) {
			_busyThreads.remove(codingThread);
			_freeThreads.remove(codingThread);
			_threads.remove(codingThread);
		}
	}

	@Override
	public Equals_CodingResult checkEquality(ICodingListener listener, BulkMatrix bm1, BulkMatrix bm2) {
		CodingId id = CodingId.getNewCodingId();
		CodingTask cTask = new BulkMatrixEqual_CodingTask(this, listener, id, bm1, bm2);
		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
		addCodingTaskEngineEvent(newCodingEvent);
		return (Equals_CodingResult) cTask._result;
	}
	
	@Override
	public Equals_CodingResult checkEquality(ICodingListener listener, SliceMatrix sm1, SliceMatrix sm2) {
		CodingId id = CodingId.getNewCodingId();
		CodingTask cTask = new SlicesEqual_CodingTask(listener, id, sm1, sm2);
		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
		addCodingTaskEngineEvent(newCodingEvent);
		return (Equals_CodingResult) cTask._result;
	}
	
	@Override
	public ByteMatrix_CodingResult inverse(ICodingListener listener, ByteMatrix m) {
		CodingId id = CodingId.getNewCodingId();
		CodingTask cTask = new Inverse_CodingTask(listener, id, m);
		
		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
		addCodingTaskEngineEvent(newCodingEvent);
		return (ByteMatrix_CodingResult) cTask._result;
	}
	
	@Override
	public BulkMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix m, BulkMatrix bm) {
		CodingId id = CodingId.getNewCodingId();
		MultiplyBulkMatrix_SequentialCodingTask cTask = new MultiplyBulkMatrix_SequentialCodingTask(this, listener, id, m, bm);
		
		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
		addCodingTaskEngineEvent(newCodingEvent);
		return (BulkMatrix_CodingResult) cTask._result;
	}

	@Override
	public SliceMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix1D m, BulkMatrix bm) {
		CodingId id = CodingId.getNewCodingId();
		CodingTask cTask = new Multiply_CodingTask(listener, id, m, bm);
		
		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
		addCodingTaskEngineEvent(newCodingEvent);
		return (SliceMatrix_CodingResult) cTask._result;
	}

	@Override
	public void codingStarted(CodingResult result) {
		if(DEBUG)
			System.out.println("TaskStarted:" + result);
	}

	@Override
	public void codingFailed(CodingResult result) {
		if(DEBUG)
			System.out.println("TaskFailed:" + result);
	}

	@Override
	public void codingFinished(CodingResult result) {
		if(DEBUG)
			System.out.println("TaskFinished:" + result);
	}
	
	protected void threadAdded(CodingThread cThread) {
		_threads.add(cThread);
		scheduleTask();
	}
	
	protected void threadBecameFree(CodingThread cThread) {
		if(_threads.contains(cThread)) {
			_busyThreads.remove(cThread);
			_freeThreads.add(cThread);
			scheduleTask();
		}
	}

	protected void threadBecameBusy(CodingThread cThread) {
		if(_threads.contains(cThread)) {
			if(!_busyThreads.contains(cThread))
				throw new IllegalStateException("Not in the busy thread list: " + cThread);
			if(_freeThreads.contains(cThread))
				throw new IllegalStateException("In the free thread list: " + cThread);
		}
	}
	
	@Override
	protected void processCodingEvent(CodingEngineEvent event) {
		super.processCodingEvent(event);
		
		switch(event._eventType){
		case ENG_ET_NEW_TASK:
		{
			CodingEngineEvent_NewCodingTask codingEvent = (CodingEngineEvent_NewCodingTask) event;
			CodingTask cTask = codingEvent._cTask;
			scheduleTask(cTask);
			break;
		}
		
		case ENG_ET_TASK_FAILED:
		{
			CodingEngineEvent_ExecutionEvent execEvent = (CodingEngineEvent_ExecutionEvent) event;
			CodingTask cTask = execEvent._cTask;
			CodingTaskStatus status = cTask.getStatus();
			assert status == CodingTaskStatus.FAILED;
			ICodingListener listener = cTask._listener;
			listener.codingFailed(cTask._result);
			break;
		}
		
		case ENG_ET_TASK_FINISHED:
		{
			CodingEngineEvent_ExecutionEvent execEvent = (CodingEngineEvent_ExecutionEvent) event;
			CodingTask cTask = execEvent._cTask;
			CodingTaskStatus status = cTask.getStatus();
			assert status == CodingTaskStatus.FINISHED;
			ICodingListener listener = cTask._listener;
			listener.codingFinished(cTask._result);
			break;
		}
		
		case ENG_ET_TASK_STARTED:
		{
			CodingEngineEvent_ExecutionEvent execEvent = (CodingEngineEvent_ExecutionEvent) event;
			CodingTask cTask = execEvent._cTask;
			ICodingListener listener = cTask._listener;
			listener.codingStarted(cTask._result);
			break;
		}
		
		case ENG_ET_THREAD_NEW:
		{
			CodingEngineEvent_NewThreadEvent tEvent = (CodingEngineEvent_NewThreadEvent) event;
			CodingThread cThread = tEvent._cThread;
			threadAdded(cThread);
			break;
		}
		
		case ENG_ET_THREAD_FREE:
		{
			CodingEngineEvent_FreeThreadEvent tEvent = (CodingEngineEvent_FreeThreadEvent) event;
			CodingThread cThread = tEvent._cThread;
			threadBecameFree(cThread);
			break;
		}
		
		case ENG_ET_THREAD_BUSY:
		{
			CodingEngineEvent_BusyThreadEvent tEvent = (CodingEngineEvent_BusyThreadEvent) event;
			CodingThread cThread = tEvent._cThread;
			threadBecameBusy(cThread);
			break;
		}
		
		case END_ET_SEQ_TASK_FAILED:
		{
			CodingEngineEvent_SequentialCodingTaskFailed seqEvent = (CodingEngineEvent_SequentialCodingTaskFailed) event;
			SequentialCodingTask seqCodingTask = seqEvent._seqCodingTask;
			notifyListener(seqCodingTask);
			break;
		}

		case END_ET_SEQ_TASK_FINISHED:
		{
			CodingEngineEvent_SequentialCodingTaskFinished seqEvent = (CodingEngineEvent_SequentialCodingTaskFinished) event;
			SequentialCodingTask seqCodingTask = seqEvent._seqCodingTask;
			notifyListener(seqCodingTask);
			break;
		}
		
		default:
			throw new UnsupportedOperationException("Unknown event type: " + event);
		}
	}

	@Override
	public Equals_CodingResult decode(ICodingListener listener, ReceivedCodedBatch codeBatch) {
		CodingId id = CodingId.getNewCodingId();
		
		CodingTask cTask = new Decoding_SequentialCodingTask (this, listener, id, codeBatch);
		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
		addCodingTaskEngineEvent(newCodingEvent);
		
		return (Equals_CodingResult) cTask._result;
	}

	@Override
	public CodedSlice_CodingResult encode(ICodingListener listener, CodedBatch codeBatch) {
		CodingId id = CodingId.getNewCodingId();
		
		CodingTask cTask =
			Encoding_SequentialCodingTask.getEncoding_SequentialCodingTask(this, listener, id, codeBatch);
		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
		addCodingTaskEngineEvent(newCodingEvent);
		
		return (CodedSlice_CodingResult) cTask._result;
	}

	@Override
	public void codingPreliminaryStageCompleted(CodingResult result) {
		return;
	}
}
