/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.jetbrains.annotations.NotNull;
import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.engine.task.*;
import org.msrg.raccoon.engine.task.result.*;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.thread.CodingThread;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public abstract class CodingEngine {

    public static final long LATE_THREAD_CHECKIN_TIMEOUT = 5000;
    public static final ExecutorService CACHED_THREAD_POOL = (Executors.newCachedThreadPool());
    public static boolean DEBUG = false;
    protected final Object _lock = new Object();
    /**
     * Tasks priority queues
     */
    protected final List<CodingEngineEvent> _lowPriorityEventQueue =
            new LinkedList<CodingEngineEvent>();
    protected final List<CodingEngineEvent> _normalPriorityEventQueue =
            new LinkedList<CodingEngineEvent>();
    protected final List<CodingEngineEvent> _highPriorityEventQueue =
            new LinkedList<CodingEngineEvent>();
    protected final List<CodingTask> _outstandingTasks = new LinkedList<CodingTask>();
    protected final Collection<CodingThread> _threads = new HashSet<CodingThread>();
    protected final Set<CodingThread> _freeThreads = new HashSet<CodingThread>();
    protected final Set<CodingThread> _busyThreads = new HashSet<CodingThread>();
    @NotNull
    private final MyThread thread;
    protected int _threadCount;

    //	public void threadIsFree(FileManagerThread fThread) {
//		CodingEngineEvent_FreeThreadEvent freeThreadEvent = new CodingEngineEvent_FreeThreadEvent(fThread);
//		addCodingTaskEngineEvent(freeThreadEvent);
//	}
    @NotNull
    protected Map<CodingThread, Long> _threadCheckins = new HashMap<CodingThread, Long>();
    /**
     * List of default coding listeners. These listeners are notified for all
     * coding task status updates.
     */
    @NotNull
    private Collection<ICodingListener> _listeners = new LinkedList<ICodingListener>();

    protected CodingEngine(int threadCount) {
        thread = new MyThread("CodingEngineT");
        _threadCount = threadCount;

        System.out.println("CodingEngine Thread count: " + _threadCount);
    }

    protected final void addCodingTaskEngineEvent(@NotNull CodingEngineEvent event) {
        synchronized (_lock) {
            switch (event._eventType) {
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

            _lock.notifyAll();
        }
    }

    public void registerCodingListener(ICodingListener listener) {
        synchronized (_lock) {
            if (!_listeners.contains(listener))
                _listeners.add(listener);
        }
    }

    public void deregisterCodingListener(ICodingListener listener) {
        synchronized (_lock) {
            _listeners.remove(listener);
        }
    }

    public void threadIsFree(CodingThread cThread) {
        CodingEngineEvent_FreeThreadEvent freeThreadEvent = new CodingEngineEvent_FreeThreadEvent(cThread);
        addCodingTaskEngineEvent(freeThreadEvent);
    }

    protected void threadIsBusy(CodingThread cThread) {
        CodingEngineEvent_BusyThreadEvent freeThreadEvent = new CodingEngineEvent_BusyThreadEvent(cThread);
        addCodingTaskEngineEvent(freeThreadEvent);
    }


    public void startComponent() {
        synchronized (_lock) {
            for (CodingThread cThread : _threads)
                CACHED_THREAD_POOL.submit(cThread);

            thread.start();
        }
    }

    protected void processCodingEvent(CodingEngineEvent event) throws CodingTaskFailed {
        if (DEBUG)
            System.out.println("EVENT_PROCESSING:" + event);
    }


    public void init() {
        synchronized (_lock) {
            for (int i = 0; i < _threadCount; i++) {
                CodingEngine engine = CodingEngine.this;
                CodingThread cThread = new CodingThread(engine) {
                    protected void runTask(@NotNull CodingTask codingTask) {
                        super.runTask(codingTask);

                        CodingTaskType taskType = codingTask._taskType;

                        switch (taskType) {
                            case SLICES_EQUAL:
                                SlicesEqual_CodingTask smeCodingTask = (SlicesEqual_CodingTask) codingTask;
                                SliceMatrix sm1 = smeCodingTask._sm1;
                                SliceMatrix sm2 = smeCodingTask._sm2;
                                boolean equals = sm1.equals(sm2);
                                ((Equals_CodingResult) smeCodingTask._result).setResult(equals);
                                smeCodingTask.finished();
                                break;

                            case INVERSE: {
                                Inverse_CodingTask inverseCodingTask = (Inverse_CodingTask) codingTask;
                                ByteMatrix m = inverseCodingTask._m;
                                ByteMatrix mInverse = (ByteMatrix) m.inverseMatrix();
                                ((ByteMatrix_CodingResult) inverseCodingTask._result).setResult(mInverse);
                                inverseCodingTask.finished();
                                break;
                            }

                            case MULTIPLY:
                                Multiply_CodingTask multiplyCodingTask = (Multiply_CodingTask) codingTask;
                                ByteMatrix1D m = multiplyCodingTask._m;
                                BulkMatrix bm = multiplyCodingTask._bm;
                                SliceMatrix result = null;
                                try {
                                    result = m.multiply1D(bm);
                                } catch (Exception x) {
                                    x.printStackTrace();
                                    multiplyCodingTask.failed();
                                    break;
                                }
                                ((SliceMatrix_CodingResult) multiplyCodingTask._result).setResult(result);
                                multiplyCodingTask.finished();
                                break;

                            case SEQUENCIAL:
                                SequentialCodingTask seqCodingTask = (SequentialCodingTask) codingTask;
                                seqCodingTask.runInitialSequencialTasks();
                                break;

                            default:
                                throw new UnsupportedOperationException("Unknown task type: " + taskType);
                        }
                    }
                };
                _threads.add(cThread);
            }
        }
    }

    public void codingThreadFailed(CodingThread codingThread) {
        // No idea what to do!
        throw new IllegalStateException();
    }

    public void codingTaskStarted(CodingThread cThread, CodingTask cTask) {
        CodingEngineEvent_ExecutionEvent event =
                new CodingEngineEvent_ExecutionEvent(cThread, cTask, CodingTaskStatus.STARTED);
        addCodingTaskEngineEvent(event);
    }

    public void codingTaskFinished(CodingThread codingThread, @NotNull CodingTask codingTask) {
        if (!codingTask.isFinished())
            throw new IllegalArgumentException("" + codingTask);

        notifyListener(codingTask);
    }

    public void codingTaskFailed(CodingThread codingThread, @NotNull CodingTask codingTask) {
        if (codingTask.isFinished())
            throw new IllegalArgumentException(codingTask.toString());

        notifyListener(codingTask);
    }

    protected void scheduleTask() {
        scheduleTask(null);
    }

    protected void scheduleTask(CodingTask cTask) {
        CodingTask cTask1 = cTask;
        CodingThread thread = null;
        synchronized (_lock) {
            if (cTask1 == null) {
                if (_outstandingTasks.isEmpty())
                    return;
                else if (_freeThreads.isEmpty())
                    return;
                else
                    cTask1 = _outstandingTasks.remove(0);
            } else if (_freeThreads.isEmpty()) {
                _outstandingTasks.add(cTask1);
                return;
            }

            thread = _freeThreads.iterator().next();
            _freeThreads.remove(thread);
            _busyThreads.add(thread);
        }

        thread.addNewTask(cTask1);
    }

    protected void notifyListener(@NotNull CodingTask cTask) {
        ICodingListener listener = cTask._listener;
        CodingResult result = cTask._result;
        if (cTask.isFinished()) {
            if (listener != null)
                listener.codingFinished(result);

            for (ICodingListener generalListener : _listeners)
                generalListener.codingFinished(result);
        } else if (cTask.isFailed()) {
            if (listener != null)
                listener.codingFailed(result);

            for (ICodingListener generalListener : _listeners)
                generalListener.codingFailed(result);
        } else
            throw new IllegalArgumentException("" + cTask);
    }


    public int getFreeThreadsCount() {
        synchronized (_lock) {
            return _freeThreads.size();
        }
    }


    public int getBusyThreadsCount() {
        synchronized (_lock) {
            return _busyThreads.size();
        }
    }


    public int getTotalThreadsCount() {
        synchronized (_lock) {
            return _threads.size();
        }
    }

    public void sequentialCodingTaskFailed(SequentialCodingTask seqCodingTask) {
        CodingEngineEvent_SequentialCodingTaskFailed seqCodingEvent = new CodingEngineEvent_SequentialCodingTaskFailed(seqCodingTask);
        addCodingTaskEngineEvent(seqCodingEvent);
    }

    public void sequentialCodingTaskFinished(SequentialCodingTask seqCodingTask) {
        CodingEngineEvent_SequentialCodingTaskFinished seqCodingEvent = new CodingEngineEvent_SequentialCodingTaskFinished(seqCodingTask);
        addCodingTaskEngineEvent(seqCodingEvent);
    }

    public void threadCheckin(CodingThread cThread) {
        synchronized (_lock) {
            _threadCheckins.put(cThread, new Long(System.currentTimeMillis()));
        }
    }

    @NotNull
    protected List<CodingThread> getLateThreads() {
        List<CodingThread> lateThreadList = new LinkedList<CodingThread>();
        synchronized (_lock) {
            long currentTime = System.currentTimeMillis();
            for (CodingThread cThread : _threads) {
                Long lastCheckin = _threadCheckins.get(cThread);
                if (lastCheckin != null && currentTime - lastCheckin > LATE_THREAD_CHECKIN_TIMEOUT)
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

    @NotNull
    public Thread getThread() {
        return thread;
    }

    public abstract CodedSlice_CodingResult encode(ICodingListener listener, CodedBatch codeBatch);

    public abstract Equals_CodingResult decode(ICodingListener listener, ReceivedCodedBatch codeBatch);

    public abstract BulkMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix m, BulkMatrix bm);

    public abstract SliceMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix1D m, BulkMatrix bm);

    public abstract ByteMatrix_CodingResult inverse(ICodingListener listener, ByteMatrix m);

    public abstract Equals_CodingResult checkEquality(ICodingListener listener, SliceMatrix sm1, SliceMatrix sm2);

    public abstract Equals_CodingResult checkEquality(ICodingListener listener, BulkMatrix bm1, BulkMatrix bm2);

    private class MyThread extends Thread {
        public MyThread(@NotNull String var1) {
            super(var1);
        }

        /*
        public void start() {
            throw new UnsupportedOperationException("To ignite the engine, use startComponent() instead.");
        }
*/

        public final void run() {
            CodingEngineEvent event = null;
            boolean eventFrom_highPriorityCodingTasksEventQueue = false;
            boolean eventFrom_normalPriorityCodingEventQueue = false;
            boolean eventFrom_lowPriorityCodingEventQueue = false;

            while (true) {
                synchronized (_lock) {
                    if (event != null) {
                        if (eventFrom_normalPriorityCodingEventQueue) {
                            eventFrom_normalPriorityCodingEventQueue = false;
                            if (_normalPriorityEventQueue.remove(0) != event)
                                throw new IllegalStateException();
                        }

                        if (eventFrom_highPriorityCodingTasksEventQueue) {
                            eventFrom_highPriorityCodingTasksEventQueue = false;
                            if (_highPriorityEventQueue.remove(0) != event)
                                throw new IllegalStateException();
                        }

                        if (eventFrom_lowPriorityCodingEventQueue) {
                            eventFrom_lowPriorityCodingEventQueue = false;
                            if (_lowPriorityEventQueue.remove(0) != event)
                                throw new IllegalStateException();
                        }
                    }

                    while (_normalPriorityEventQueue.isEmpty()
                            && _highPriorityEventQueue.isEmpty()
                            && _lowPriorityEventQueue.isEmpty()) {
                        try {
                            _lock.wait();
                        } catch (InterruptedException itx) {
                            itx.printStackTrace();
                        }
                    }

                    if (!_highPriorityEventQueue.isEmpty()) {
                        event = _highPriorityEventQueue.get(0);
                        eventFrom_highPriorityCodingTasksEventQueue = true;
                    } else if (!_normalPriorityEventQueue.isEmpty()) {
                        event = _normalPriorityEventQueue.get(0);
                        eventFrom_normalPriorityCodingEventQueue = true;
                    } else if (!_lowPriorityEventQueue.isEmpty()) {
                        event = _lowPriorityEventQueue.get(0);
                        eventFrom_lowPriorityCodingEventQueue = true;
                    } else
                        throw new IllegalStateException();
                }

                //
                try {
                    processCodingEvent(event);
                } catch (Exception x) {
                    x.printStackTrace();
                }

                Thread.yield();
            }
        }

        @NotNull

        public String toString() {
            return "CodingEngine:[" + _freeThreads.size() + "/" + _busyThreads.size() + "]";
        }
    }
}