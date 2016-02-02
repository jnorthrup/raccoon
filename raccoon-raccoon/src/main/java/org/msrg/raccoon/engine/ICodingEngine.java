/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.engine.task.result.*;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix1D;

public interface ICodingEngine {

	void init();
	void startComponent();
	
	void registerCodingListener(ICodingListener listeners);
	void deregisterCodingListener(ICodingListener listeners);
	
	CodedSlice_CodingResult encode(ICodingListener listener, CodedBatch codeBatch);
	Equals_CodingResult decode(ICodingListener listener, ReceivedCodedBatch codeBatch);
	
	BulkMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix m, BulkMatrix bm);
	SliceMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix1D m, BulkMatrix bm);
	
	ByteMatrix_CodingResult inverse(ICodingListener listener, ByteMatrix m);
	Equals_CodingResult checkEquality(ICodingListener listener, SliceMatrix sm1, SliceMatrix sm2);
	Equals_CodingResult checkEquality(ICodingListener listener, BulkMatrix bm1, BulkMatrix bm2);
	
	int getFreeThreadsCount();
	int getBusyThreadsCount();
	int getTotalThreadsCount();
	
}
