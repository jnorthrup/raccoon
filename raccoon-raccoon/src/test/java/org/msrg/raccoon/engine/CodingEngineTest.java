/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import junit.framework.TestCase;
import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.SourceCodedBatch;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.CodingTask;
import org.msrg.raccoon.engine.task.result.BulkMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.sequential.EncodingDecodingEqual_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.EncodingReceivedEncodingDecodingEqual_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.MultiplyInverseMultiplyEqual_SequentialCodingTask;
import org.msrg.raccoon.engine.task.sequential.SequentialCodingTask;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;

import java.util.*;

public class CodingEngineTest extends TestCase implements ICodingListener {

    protected static final int THREAD_COUNT = 4;
    protected static final int TASK_SIZE = 1;

    protected static final int MAX_WAIT_TIME_SMALL = 1 * 1000;
    protected static final int MAX_WAIT_TIME_LARGE = 2000000;

    protected static final int COLS = 10000;
    protected static final int ROWS = 200;

    protected ByteMatrix _m;
    protected SliceMatrix _sm;
    protected BulkMatrix _bm;
    protected BulkMatrix _multipliedBM;
    protected SourceCodedBatch _sourceCodedBatch;

    protected final Map<CodingResult, String> _allCodingResults = new HashMap<CodingResult, String>();
    protected int _finished = 0, _subfinished = 0;
    protected int _failed = 0, _subfailed = 0;
    protected int _equals = 0, _unequals = 0;

    protected Collection<CodingTask> _mainTasks = new HashSet<CodingTask>();
    protected ICodingEngine _engine;
    protected final static String _filename = "D:/temp/filerepo/file.code";
    protected final static CodingEngineTestType _testType =
//		CodingEngineTestType.TEST_PAGEIN_PAGEOUT_EQUALS_TASKS;
//		CodingEngineTestType.TEST_CODING_TASKS;
//		CodingEngineTestType.TEST_SEQUENTIAL_CODING_TASKS;
//		CodingEngineTestType.TEST_FULL_MULTYPLY_TASKS;
//		CodingEngineTestType.TEST_ENCODING_DECODING_EQUALS_TASKS;
            CodingEngineTestType.TEST_ENCODING_RECEIVED_ENCODING_DECODING_EQUALS_TASKS;
//		CodingEngineTestType.TEST_ENCODING_TASKS;
//		CodingEngineTestType.TEST_EQUALITY_TASKS;

    @Override
    public void setUp() {
        System.out.println("Workload: " + ROWS * COLS + " x " + CodingEngineTest.TASK_SIZE + " x " + CodingEngineTest.THREAD_COUNT);
        switch (CodingEngineTest._testType) {
            case TEST_PAGEIN_PAGEOUT_EQUALS_TASKS:
                throw new UnsupportedOperationException();

//			_engine = new FileCodingEngine_ForTest(THREAD_COUNT, _filename);
//			_engine.init();
//
//			_sm = FileSliceMatrix.createRandomSliceMatrix(MATRIX_SIZE, SLICE_WIDTH, 0, ((FileCodingEngineImpl)_engine).getFileTable());
//			_bm = FileBulkMatrix.createBulkMatixRandomData(ROWS, PIECE_WIDTH, ((FileCodingEngineImpl)_engine).getFileTable());
//			break;

            default:
                _engine = new CodingEngine_ForTest(CodingEngineTest.THREAD_COUNT);
                _engine.init();

                _sm = SliceMatrix.createRandomSliceMatrix(CodingEngineTest.COLS);
//			_bm = BulkMatrix.createBulkMatixRandomData(ROWS, COLS);
                _bm = BulkMatrix.createBulkMatixIncrementalData(CodingEngineTest.ROWS, CodingEngineTest.COLS);
        }

        _m = ByteMatrix.createRandomByteMatrix(CodingEngineTest.ROWS, CodingEngineTest.ROWS);
        _sourceCodedBatch = new SourceCodedBatch(_bm);

        _engine.registerCodingListener(this);
        _engine.startComponent();

//		_multipliedBM = _m.multiply(_bm);
        System.out.println("Setup finished, running test: " + CodingEngineTest._testType);
    }

    Collection<CodingTask> generateEqualityCodingTasks(int count) {
        Collection<CodingTask> mainTasks = new LinkedList<CodingTask>();
        for (int i = 0; i < count; i++) {
            synchronized (_allCodingResults) {
                CodingResult result = _engine.checkEquality(null, _bm, _bm);
                _allCodingResults.put(result, "Task#" + i);
                mainTasks.add(result._cTask);
            }
        }
        return mainTasks;
    }

    Collection<CodingTask> generateCodingTasks(int count) {
        Collection<CodingTask> mainTasks = new LinkedList<CodingTask>();
        for (int i = 0; i < count; i++) {
            synchronized (_allCodingResults) {
                CodingResult result = _engine.multiply(null, _m, _bm);
                _allCodingResults.put(result, "Task#" + i);
                mainTasks.add(result._cTask);
            }
        }
        return mainTasks;
    }

    Collection<CodingTask> generateSequenctialCodingTasks(int count) {
        Collection<CodingTask> mainTasks = new LinkedList<CodingTask>();
        for (int i = 0; i < count; i++) {
            synchronized (_allCodingResults) {
                CodingResult result = ((CodingEngine_ForTest) _engine).multiplyInverseMultiplyEqual(null, _m, _bm);
                _allCodingResults.put(result, "Task#" + i);
                mainTasks.add(result._cTask);
            }
        }
        return mainTasks;
    }

    Collection<CodingTask> generateFullMultiplyTasks(int count) {
        Collection<CodingTask> mainTasks = new LinkedList<CodingTask>();
        for (int i = 0; i < count; i++) {
            synchronized (_allCodingResults) {
                CodingResult result = ((CodingEngine_ForTest) _engine).multiply(null, _m, _bm);
                _allCodingResults.put(result, "Task#" + i);
                mainTasks.add(result._cTask);
            }
        }
        return mainTasks;
    }

    Collection<CodingTask> generateEncodingTasks(int count) {
        Collection<CodingTask> mainTasks = new LinkedList<CodingTask>();
        for (int i = 0; i < count; i++) {
            synchronized (_allCodingResults) {
                CodingResult result = ((CodingEngine_ForTest) _engine).encode(null, _sourceCodedBatch);
                _allCodingResults.put(result, "Task#" + i);
                mainTasks.add(result._cTask);
            }
        }
        return mainTasks;
    }

    Collection<CodingTask> generatePageInPageOutEqualsCodingTasks(int count) {
        throw new UnsupportedOperationException();
//		for(int i=0 ; i<count ; i++) {
//			synchronized (_allCodingResults) {
//				CodingResult result = ((FileCodingEngine_ForTest)_engine).pageInPageOutEquals(null, _bm);
//				_allCodingResults.put(result, "Task#" + i);	
//			}
//		}
    }

    Collection<CodingTask> generateEncodingReceivedEncodingDecodingEqualTasks(int count) {
        Collection<CodingTask> mainTasks = new LinkedList<CodingTask>();
        for (int i = 0; i < count; i++) {
            CodingResult result =
                    ((CodingEngine_ForTest) _engine).encodingReceivedEncodingDecodingEqual(
                            null, _sourceCodedBatch);
            synchronized (_allCodingResults) {
                _allCodingResults.put(result, "Task#" + i);
                mainTasks.add(result._cTask);
            }
        }

        return mainTasks;
    }

    Collection<CodingTask> generateEncodingDecodingEqualTasks(int count) {
        Collection<CodingTask> mainTasks = new LinkedList<CodingTask>();
        for (int i = 0; i < count; i++) {
            synchronized (_allCodingResults) {
                CodingResult result =
                        ((CodingEngine_ForTest) _engine).encodingDecodingEqual(null, _sourceCodedBatch);
                _allCodingResults.put(result, "Task#" + i);
                mainTasks.add(result._cTask);
            }
        }

        return mainTasks;
    }

    @Override
    public void codingFailed(CodingResult result) {
        print(result, "FAILED");
        synchronized (_allCodingResults) {
            if (!result.isFailed())
                throw new IllegalStateException();
            if (_allCodingResults.remove(result) != null)
                _failed++;
            else
                _subfailed++;
        }
    }

    @Override
    public void codingFinished(CodingResult result) {
        print(result, "FINISHED");
        synchronized (_allCodingResults) {

            if (!result.isFinished())
                throw new IllegalStateException(result.toString());

            processFinishedResult(result);

            if (_allCodingResults.remove(result) != null)
                _finished++;
            else
                _subfinished++;
        }

        reportTaskFinishTimes(result._cTask);

        checkTestEnd();
    }

    protected void reportTaskFinishTimes(CodingTask cTask) {
        if (cTask == null)
            return;

        if (!_mainTasks.contains(cTask))
            return;

        if (cTask.isSequencial()) {
            String str = "";
            long[] stageTimes = ((SequentialCodingTask) cTask).getStageTimes();
            for (int i = 1; i < stageTimes.length; i++) {
                long duration = stageTimes[i] - stageTimes[i - 1];
                str += (i == 1 ? "" : ",") + duration;
            }

            System.out.println("Task staged times: " + str);
        }
    }

    protected void processFinishedResult(CodingResult result) {
        switch (result._resultsType) {
            case BOOLEAN:
                if (((Equals_CodingResult) result).getResult())
                    _equals++;
                else
                    _unequals++;
                break;

            case BULK_MATRIX: {
                BulkMatrix_CodingResult smResult = (BulkMatrix_CodingResult) result;
                BulkMatrix sm = smResult.getResult();
                if (CodingEngineTest._testType == CodingEngineTestType.TEST_FULL_MULTYPLY_TASKS && !_multipliedBM.equals(sm))
                    throw new IllegalStateException("BulkMatrix is not equal to the product!");
                break;
            }

            case CODED_SLICE_MATRIX:
                CodedSlice_CodingResult smResult = (CodedSlice_CodingResult) result;
                CodedPiece sm = smResult.getResult();
                if (CodingEngine.DEBUG)
                    System.out.println(sm._codedContent);
                break;

            default:
                break;
        }
    }

    protected boolean checkTestEnd() {
        if (_finished + _failed != CodingEngineTest.TASK_SIZE)
            return false;

        _end = System.currentTimeMillis();
        return true;
    }

    protected void print(CodingResult result, String comment) {
        String str;
        synchronized (_allCodingResults) {
            str = _allCodingResults.get(result);
        }
        if (CodingEngine.DEBUG)
            System.out.println(comment + ": " + result + ":" + str);
    }

    long _start = -1;
    long _end = -1;

    public void testEngine() {
        _start = System.currentTimeMillis();
        Collection<CodingTask> mainTasks;
        switch (CodingEngineTest._testType) {
            case TEST_PAGEIN_PAGEOUT_EQUALS_TASKS:
                mainTasks = generatePageInPageOutEqualsCodingTasks(CodingEngineTest.TASK_SIZE);
                break;

            case TEST_CODING_TASKS:
                mainTasks = generateCodingTasks(CodingEngineTest.TASK_SIZE);
                break;

            case TEST_SEQUENTIAL_CODING_TASKS:
                mainTasks = generateSequenctialCodingTasks(CodingEngineTest.TASK_SIZE);
                break;

            case TEST_FULL_MULTYPLY_TASKS:
                mainTasks = generateFullMultiplyTasks(CodingEngineTest.TASK_SIZE);
                break;

            case TEST_EQUALITY_TASKS:
                mainTasks = generateEqualityCodingTasks(CodingEngineTest.TASK_SIZE);
                break;

            case TEST_ENCODING_TASKS:
                mainTasks = generateEncodingTasks(CodingEngineTest.TASK_SIZE);
                break;

            case TEST_ENCODING_DECODING_EQUALS_TASKS:
                mainTasks = generateEncodingDecodingEqualTasks(CodingEngineTest.TASK_SIZE);
                break;

            case TEST_ENCODING_RECEIVED_ENCODING_DECODING_EQUALS_TASKS:
                mainTasks = generateEncodingReceivedEncodingDecodingEqualTasks(CodingEngineTest.TASK_SIZE);
                break;

            default:
                throw new UnsupportedOperationException("Unknown test type: " + CodingEngineTest._testType);
        }

        _mainTasks.addAll(mainTasks);
        waitToFinishTasks();

        switch (CodingEngineTest._testType) {
            case TEST_ENCODING_RECEIVED_ENCODING_DECODING_EQUALS_TASKS:
                System.out.println("Final result: " + _mainTasks.iterator().next()._result);
                break;

            default:
                break;
        }
        System.out.println("ALL TASKS FINISHED (" + getStatistics() + ").");
    }

    protected void waitToFinishTasks() {
        try {
            synchronized (_allCodingResults) {
                for (int i = 0; i < MAX_WAIT_TIME_LARGE / MAX_WAIT_TIME_SMALL && !checkTestEnd(); i++) {
                    System.out.print(getStatistics());
                    System.out.println(", LT" + ((CodingEngine) _engine).getLateThreads());
                    _allCodingResults.wait(CodingEngineTest.MAX_WAIT_TIME_SMALL);
                }

                if (!checkTestEnd())
                    throw new IllegalStateException("It is taking a long time..(" + getStatistics() + ")");
                else {
                }
            }
        } catch (InterruptedException itx) {
            itx.printStackTrace();
        }
    }

    public String getStatistics() {
        if (_start < 0)
            return "NOT STARTED";

        long end = _end < 0 ? System.currentTimeMillis() : _end;
        return "C" + _finished + "/" +
                "F" + _failed + "/" +
                "T" + CodingEngineTest.TASK_SIZE + "/" +
                "SC" + _subfinished + "/" +
                "SF" + _subfailed + "[" +
                "EQ" + _equals + "/" +
                "NE" + _unequals + "] in " +
                (end - _start) + "ms" +
                "{" +
                ((CodingEngine) _engine).getPendingLowPriorityEventsCount() + "," +
                ((CodingEngine) _engine).getPendingNormalPriorityEventsCount() + "," +
                ((CodingEngine) _engine).getPendingHighPriorityEventsCount() +
                "}";
    }

    public void codingStarted(CodingResult id) {
        print(id, "STARTED");
    }

    public void codingPreliminaryStageCompleted(CodingResult result) {
    }
}

enum CodingEngineTestType {
    TEST_PAGEIN_PAGEOUT_EQUALS_TASKS,
    TEST_CODING_TASKS,
    TEST_SEQUENTIAL_CODING_TASKS,
    TEST_FULL_MULTYPLY_TASKS,
    TEST_ENCODING_DECODING_EQUALS_TASKS,
    TEST_ENCODING_RECEIVED_ENCODING_DECODING_EQUALS_TASKS,
    TEST_ENCODING_TASKS,
    TEST_EQUALITY_TASKS,
}

//class FileCodingEngine_ForTest extends FileCodingEngineImpl {
//	
//	FileCodingEngine_ForTest(int threadCount, String filename) {
//		super(filename, threadCount);
//	}
//	
//	Equals_CodingResult multiplyInverseMultiplyEqual(ICodingListener listener, ByteMatrix m, SliceMatrix sm) {
//		CodingId id = CodingId.getNewCodingId();
//		CodingTask cTask = new MultiplyInverseMultiplyEqual_SequentialCodingTask(this, listener, id, m, sm);
//		
//		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
//		addCodingTaskEngineEvent(newCodingEvent);
//		return (Equals_CodingResult) cTask._result;
//	}
//
//	Equals_CodingResult pageInPageOutEquals(ICodingListener listener, BulkMatrix bm) {
//		CodingId id = CodingId.getNewCodingId();
//		CodingTask cTask = new PageInPageOutEqual_SequentialCodingTask(bm, this, listener, id);
//		
//		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
//		addCodingTaskEngineEvent(newCodingEvent);
//		return (Equals_CodingResult) cTask._result;
//	}
//	
//	Equals_CodingResult encodingDecodingEqual(ICodingListener listener, CodedBatch cb) {
//		CodingId id = CodingId.getNewCodingId();
//		CodingTask cTask = new EncodingDecodingEqual_SequentialCodingTask(cb, this, listener, id);
//		
//		CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
//		addCodingTaskEngineEvent(newCodingEvent);
//		return (Equals_CodingResult) cTask._result;
//	}
//}

class CodingEngine_ForTest extends CodingEngineImpl {

    CodingEngine_ForTest(int threadCount) {
        super(threadCount);
    }

    Equals_CodingResult multiplyInverseMultiplyEqual(ICodingListener listener, ByteMatrix m, BulkMatrix bm) {
        CodingId id = CodingId.getNewCodingId();
        CodingTask cTask = new MultiplyInverseMultiplyEqual_SequentialCodingTask(this, listener, id, m, bm);

        CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
        addCodingTaskEngineEvent(newCodingEvent);
        return (Equals_CodingResult) cTask._result;
    }

    Equals_CodingResult encodingReceivedEncodingDecodingEqual(ICodingListener listener, SourceCodedBatch cb) {
        CodingId id = CodingId.getNewCodingId();
        CodingTask cTask =
                new EncodingReceivedEncodingDecodingEqual_SequentialCodingTask(cb, this, listener, id);

        CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
        addCodingTaskEngineEvent(newCodingEvent);
        return (Equals_CodingResult) cTask._result;
    }

    Equals_CodingResult encodingDecodingEqual(ICodingListener listener, CodedBatch cb) {
        CodingId id = CodingId.getNewCodingId();
        CodingTask cTask = new EncodingDecodingEqual_SequentialCodingTask(cb, this, listener, id);

        CodingEngineEvent_NewCodingTask newCodingEvent = new CodingEngineEvent_NewCodingTask(cTask);
        addCodingTaskEngineEvent(newCodingEvent);
        return (Equals_CodingResult) cTask._result;
    }
}
