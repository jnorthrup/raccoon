/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.msrg.raccoon.CodedBatch;
import org.msrg.raccoon.CodedCoefficients;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.SourceCodedBatch;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;

import junit.framework.TestCase;

public class SlicedCodedBatchTest extends TestCase {

	static int ROWS_COUNT = 2;
	static int SLICE_WIDTH = 10000;
	static int PIECE_WIDTH = 1000 * SLICE_WIDTH;
	static int CONTENT_SIZE = PIECE_WIDTH * ROWS_COUNT;
	
	static {
		CodedCoefficients._MAX_COEFFICIENTS_PRINT = 4;
		BulkMatrix._MAX_ROWS_PRINT = 5;
		BulkMatrix._MAX_SLICES_PRINT = 5;
		SliceMatrix._MAX_COLS_PRINT = 6;
		CodedBatch._MAX_WRITE_SIZE = 25;
	}
	
	private int _contentSize = CONTENT_SIZE;
	private BulkMatrix _content;
	private SourceCodedBatch _sourceCodedBatch;
	private ReceivedCodedBatch _receivedCodedBatch;
	
	public void setUp() {
		_content =
//			BulkMatrix.createBulkMatixIncrementalData((int)(0.5 + CONTENT_SIZE / PIECE_WIDTH), PIECE_WIDTH);
			BulkMatrix.createBulkMatixRandomData((int)(0.5 + CONTENT_SIZE / PIECE_WIDTH), PIECE_WIDTH);
		_sourceCodedBatch = new SourceCodedBatch(_content);
		_receivedCodedBatch = new ReceivedCodedBatch(_sourceCodedBatch._size, _sourceCodedBatch.getBulkMatrix()._rows);
	}
	
	public void testSourceCodedBatch() {
//		CodedPiece cs0 = _sourceCodedBatch.code();
//		
//		ByteBuffer bb = ByteBuffer.allocate(cs0.getSizeInByteBuffer());
//		cs0.putObjectIntoByteBuffer(bb, 0);
//		CodedPiece cs1 = new CodedPiece(bb, 0);
//		
//		System.out.println(cs0);
//		System.out.println(cs1);
//		System.out.println(cs0.equals(cs1));
	}
	
	public void testReceivedCodedBatch() {
		System.out.println("Starting with content size " + _contentSize + " ...");
		int n = _sourceCodedBatch.getRequiredCodedPieceCount();
		
		{
			long totalEncodingTime = 0;
			CodedPiece[] css = new CodedPiece[n];
			for(int i=0 ; i<n ; i++) {
				System.out.print("Coding...");
				long codingStart = System.currentTimeMillis();
				css[i] = _sourceCodedBatch.code();
				long codingEnd = System.currentTimeMillis();
				long codingTime = (codingEnd - codingStart);
				totalEncodingTime += codingTime;
				System.out.println("(" + codingTime + "ms)");
			}
			System.out.println("Coding done(" + totalEncodingTime + ")");
			
			System.out.println("Receiving...");
			for(int i=0 ; i<n ; i++)
				_receivedCodedBatch.addCodedSlice(css[i]);
		}
		
//		{
//			System.out.print("Checking solvability...");
//			long solvabilityStart = SystemTime.currentTimeMillis();
//			CodedSlice[] solvingSlices = _receivedCodedBatch.canSolve();
//			long solvabilityEnd = SystemTime.currentTimeMillis();
//			System.out.println("(" + (solvabilityEnd - solvabilityStart) + "ms)");
//		}
		
		{
			System.out.println(_sourceCodedBatch);
			System.out.println(_receivedCodedBatch);
		}
		
		{
			System.out.print("Decoding...");
			long decodeStart = System.currentTimeMillis();
			boolean decode = _receivedCodedBatch.decode();
			long decodeEnd = System.currentTimeMillis();
			System.out.println(":" + decode + "(" + (decodeEnd - decodeStart) + "ms)");
		}
			
		System.out.println("Printing...");
		System.out.println(_receivedCodedBatch);
		System.out.println("Ending...");
		
		System.out.println(_sourceCodedBatch.equals(_receivedCodedBatch));
	}
}
