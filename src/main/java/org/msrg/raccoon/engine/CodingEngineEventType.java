/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.CodingTaskStatus;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.engine.thread.CodingRunnable;

public enum CodingEngineEventType {

    ENG_ET_FILE_TASK {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._lowPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            throw new UnsupportedOperationException(this.name());
        }
    },

    ENG_ET_THREAD_FREE {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_FreeThreadEvent tEvent = (CodingEngineEvent_FreeThreadEvent) event;
            CodingRunnable cThread = tEvent._cThread;
            codingEngine.threadBecameFree(cThread);

        }
    },
    ENG_ET_THREAD_NEW {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_NewThreadEvent tEvent = (CodingEngineEvent_NewThreadEvent) event;
            CodingRunnable cThread = tEvent._cThread;
            codingEngine.threadAdded(cThread);

        }
    },
    ENG_ET_THREAD_BUSY {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_BusyThreadEvent tEvent = (CodingEngineEvent_BusyThreadEvent) event;
            CodingRunnable cThread = tEvent._cThread;
            codingEngine.threadBecameBusy(cThread);

        }
    },

    ENG_ET_NEW_TASK {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._normalPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_NewCodingTask codingEvent = (CodingEngineEvent_NewCodingTask) event;
            CodingTask cTask = codingEvent._cTask;
            CodingEngine.scheduleTask(codingEngine, cTask);
        }
    },
    END_ET_SEQ_TASK_FINISHED {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_SequentialCodingTaskFinished seqEvent = (CodingEngineEvent_SequentialCodingTaskFinished) event;
            SequentialCodingTask seqCodingTask = seqEvent._seqCodingTask;
            codingEngine.notifyListener(seqCodingTask);

        }
    },
    END_ET_SEQ_TASK_FAILED {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_SequentialCodingTaskFailed seqEvent = (CodingEngineEvent_SequentialCodingTaskFailed) event;
            SequentialCodingTask seqCodingTask = seqEvent._seqCodingTask;
            codingEngine.notifyListener(seqCodingTask);

        }
    },

    ENG_ET_TASK_FAILED {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_ExecutionEvent execEvent = (CodingEngineEvent_ExecutionEvent) event;
            CodingTask cTask = execEvent._cTask;
            CodingTaskStatus status = cTask.getStatus();
            assert status == CodingTaskStatus.FAILED;
            ICodingListener listener = cTask._listener;
            listener.codingFailed(cTask._result);

        }
    },
    ENG_ET_TASK_FINISHED {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_ExecutionEvent execEvent = (CodingEngineEvent_ExecutionEvent) event;
            CodingTask cTask = execEvent._cTask;
            CodingTaskStatus status = cTask.getStatus();
            assert status == CodingTaskStatus.FINISHED;
            ICodingListener listener = cTask._listener;
            listener.codingFinished(cTask._result);

        }
    },
    ENG_ET_TASK_STARTED {
        @Override
        public void call(CodingEngine codingEngine, CodingEngineEvent event) {
            codingEngine._highPriorityEventQueue.add(event);

        }

        @Override
        public void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event) {
            CodingEngineEvent_ExecutionEvent execEvent = (CodingEngineEvent_ExecutionEvent) event;
            CodingTask cTask = execEvent._cTask;
            ICodingListener listener = cTask._listener;
            listener.codingStarted(cTask._result);

        }
    },;

    abstract public void call(CodingEngine codingEngine, CodingEngineEvent event);

    public abstract void processCodingEvent(CodingEngine codingEngine, CodingEngineEvent event);
}
