/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import org.jetbrains.annotations.NotNull;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.SourceCodedBatch;
import org.msrg.raccoon.engine.CodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;

import java.util.Collection;
import java.util.HashSet;


public class EncodingReceivedEncodingDecodingEqual_SequentialCodingTask extends
        SequentialCodingTask {

    protected final int FINAL_STAGE = 3;

    final SourceCodedBatch _srcCodedBatch;
    @NotNull
    final ReceivedCodedBatch _destCodedBatch1;
    @NotNull
    final ReceivedCodedBatch _destCodedBatch2;
    final Collection<CodedSlice_CodingResult> _activeCodingResults1 =
            new HashSet<CodedSlice_CodingResult>();
    final Collection<CodedSlice_CodingResult> _activeCodingResults2 =
            new HashSet<CodedSlice_CodingResult>();

    Equals_CodingResult _decodeCodingResult;
    Equals_CodingResult _equalsCodingResult;

    public EncodingReceivedEncodingDecodingEqual_SequentialCodingTask(
            SourceCodedBatch srcCodedBatch,
            CodingEngine engine, ICodingListener listener, CodingId id) {
        super(engine, listener, id, SequentialCodingTaskType.ENCODE_RECEIVED_ENCODE_DECODE_EQUAL);

        _srcCodedBatch = srcCodedBatch;
        _destCodedBatch1 = new ReceivedCodedBatch(_srcCodedBatch.getSize(), _srcCodedBatch.getRows());
        _destCodedBatch2 = new ReceivedCodedBatch(_srcCodedBatch.getSize(), _srcCodedBatch.getRows());
    }


    public synchronized void codingFailed(CodingResult result) {
        ((ICodingListener) _engine).codingFailed(result);
        failed();
    }


    public synchronized void codingFinished(CodingResult result) {
        if (result == _decodeCodingResult) {
            runStagePrivately();
            return;
        }

        if (result == _equalsCodingResult) {
            runStagePrivately();
            return;
        }

        CodedSlice_CodingResult sliceResult =
                (CodedSlice_CodingResult) result;

        if (_activeCodingResults1.remove(result)) {
            _destCodedBatch1.addCodedSlice(sliceResult.getResult());
            CodedSlice_CodingResult sliceResult2 = _engine.encode(this, _destCodedBatch1);
            _activeCodingResults2.add(sliceResult2);
        } else if (_activeCodingResults2.remove(result)) {
            _destCodedBatch2.addCodedSlice(sliceResult.getResult());
        } else
            throw new IllegalStateException();

        runStagePrivately();
    }


    public void codingStarted(CodingResult result) {
    }


    protected int getFinalStage() {
        return FINAL_STAGE;
    }

    @NotNull

    protected CodingResult getEmptyCodingResults() {
        return new Equals_CodingResult(this, _id);
    }


    protected void runStagePrivately() {
        if (isFailed())
            return;

        switch (_currentStage) {
            case -1:
                for (int i = 0; i < _srcCodedBatch.getAvailableCodedPieceCount() * 2; i++) {
                    CodedSlice_CodingResult encodingResult = _engine.encode(this, _srcCodedBatch);
                    _activeCodingResults1.add(encodingResult);
                }

                setCurrentStage(0);
                break;

            case 0:
                if (!_activeCodingResults1.isEmpty())
                    return;
                if (!_activeCodingResults2.isEmpty())
                    return;

                _decodeCodingResult = _engine.decode(this, _destCodedBatch2);
                setCurrentStage(1);
                break;

            case 1:
                if (!_activeCodingResults1.isEmpty() || !_activeCodingResults2.isEmpty())
                    return;

                _equalsCodingResult =
                        _engine.checkEquality(this, _srcCodedBatch.getBulkMatrix(), _destCodedBatch2.getBulkMatrix());
                setCurrentStage(2);
                break;

            case 2:
                boolean finalResult = _equalsCodingResult.getResult();
                ((Equals_CodingResult) _result).setResult(finalResult);

                setCurrentStage(3);
                break;
        }

        if (reachedFinalStage())
            finished();
    }

    public void codingPreliminaryStageCompleted(CodingResult result) {
    }

}
