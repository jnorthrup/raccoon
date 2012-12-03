/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task.sequential;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedBatchType;
import org.msrg.raccoon.CodedCoefficients;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.task.CodingId;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.engine.task.sequential.Encoding_SequentialCodingTask;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;


public class EncodingReceivedCodedBatch_SequentialCodingTask extends
		Encoding_SequentialCodingTask {

	protected final int FINAL_STAGE = 1;

	protected SliceMatrix_CodingResult _smCodingResult;
	protected CodedCoefficients _cc;

	public EncodingReceivedCodedBatch_SequentialCodingTask(
			ICodingEngine engine, ICodingListener listener, CodingId id,
			CodedBatch codeBatch) {
		super(engine, listener, id, codeBatch);
	}

	@Override
	public CodedBatchType getSupportedCodedBatchType() {
		return CodedBatchType.RCV_CODED_BATCH;
	}

	@Override
	protected synchronized void runStagePrivately() {
		switch (_currentStage) {
		case -1:
		{
			CodedPiece[] codedSlices = ((ReceivedCodedBatch)_codeBatch).getCodedSlicesForCoding();
			if(codedSlices.length == 0) {
				failed();
				return;
			}
			
			SliceMatrix[] rowMatrices = new SliceMatrix[codedSlices.length];
			byte[][] bCC = new byte[codedSlices.length][];
			for(int i=0 ; i<rowMatrices.length ; i++) {
				rowMatrices[i] = codedSlices[i]._codedContent;
				bCC[i] = codedSlices[i]._cc.getByteArray()[0];
			}
			BulkMatrix codedSlicesAsBulkMatrix =
				((ReceivedCodedBatch)_codeBatch).createNewBulkMatrix(rowMatrices);
			
			CodedCoefficients cc =
				_codeBatch.getNewCodedCoefficients(codedSlices.length);
			cc.verify();
			
			byte[][]bb = cc.multiply(bCC);
			byte[] b = bb[0];
			_cc = _codeBatch.getNewCodedCoefficients(b);
			_cc.verify();
			
			if(cc.getByteArray() == null)
				throw new NullPointerException();
			if(cc.getByteArray()[0] == null)
				throw new NullPointerException();
			
			_smCodingResult =
				_engine.multiply(this, cc, codedSlicesAsBulkMatrix);
			
			setCurrentStage(0);

//			if(codedSlices==null) {
//				setCurrentStage(0);
//				setCurrentStage(1);
//			} else {
//				CodedCoefficients cc = new CodedCoefficients(codedSlices.length);
//				SliceMatrix[] rowMatrices = new SliceMatrix[codedSlices.length];
//				for(int i=0 ; i<rowMatrices.length ; i++)
//					rowMatrices[i] = codedSlices[i]._codedContent;
//	
//				BulkMatrix combinedMultiRowMatrix = new BulkMatrix(rowMatrices);
//				for(int i=0 ; i<codedSlices.length ; i++)
//					_cc = cc.multiply(codedSlices[i]._cc);
//				
//				_cc = cc;
//				_smCodingResult = _engine.multiply(this, _cc, combinedMultiRowMatrix);
//				
//				setCurrentStage(0);
//			}
			break;
		}
		
		case 0:
		{
			if(_smCodingResult.isFailed()) {
				failed();
			} else if (_smCodingResult.isFinished()) {
				SliceMatrix sm = _smCodingResult.getResult();
				CodedPiece codedSlice = sm.createEmptyCodedPiece(_cc, sm);
				((CodedSlice_CodingResult)_result).setResult(codedSlice);
				setCurrentStage(1);
			}
			break;
		}
		
		default:
			throw new IllegalStateException("" + _currentStage);
		}
	}

	@Override
	protected int getFinalStage() {
		return FINAL_STAGE;
	}
}
