/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.engine.task.result.BulkMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.ByteMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodedSlice_CodingResult;
import org.msrg.raccoon.engine.task.result.Equals_CodingResult;
import org.msrg.raccoon.engine.task.result.SliceMatrix_CodingResult;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;

import org.msrg.raccoon.engine.ICodingListener;

public interface ICodingEngine {

	public void init();
	public void startComponent();
	
	public void registerCodingListener(ICodingListener listeners);
	public void deregisterCodingListener(ICodingListener listeners);
	
	public CodedSlice_CodingResult encode(ICodingListener listener, CodedBatch codeBatch);
	public Equals_CodingResult decode(ICodingListener listener, ReceivedCodedBatch codeBatch);
	
	public BulkMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix m, BulkMatrix bm);
	public SliceMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix1D m, BulkMatrix bm);
	
	public ByteMatrix_CodingResult inverse(ICodingListener listener, ByteMatrix m);
	public Equals_CodingResult checkEquality(ICodingListener listener, SliceMatrix sm1, SliceMatrix sm2);
	public Equals_CodingResult checkEquality(ICodingListener listener, BulkMatrix bm1, BulkMatrix bm2);
	
	public int getFreeThreadsCount();
	public int getBusyThreadsCount();
	public int getTotalThreadsCount();
	
}
