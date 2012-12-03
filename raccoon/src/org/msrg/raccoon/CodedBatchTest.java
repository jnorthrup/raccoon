/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.msrg.raccoon.CodedCoefficients;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ReceivedCodedBatch;
import org.msrg.raccoon.SourceCodedBatch;

import org.msrg.raccoon.matrix.bulk.BulkMatrix1D;

import junit.framework.TestCase;

public class CodedBatchTest extends TestCase {

	static {
		CodedCoefficients._MAX_COEFFICIENTS_PRINT = 3;
	}
	
	private int _contentSize = 1000 * 10;
	private byte[] _content;
	private SourceCodedBatch _sourceCodedBatch;
	private ReceivedCodedBatch _receivedCodedBatch;
	
	public void setUp() {
		_content = new byte[_contentSize];
		for(int i=0 ; i<_contentSize ; i++)
			_content[i] = (byte)i;
		
		_sourceCodedBatch =
			new SourceCodedBatch(BulkMatrix1D.createBulkMatrix1D(_content, 0, _contentSize));
		
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
			CodedPiece[] css = new CodedPiece[n];
			for(int i=0 ; i<n ; i++) {
				System.out.print("Coding...");
				long codingStart = System.currentTimeMillis();
				css[i] = _sourceCodedBatch.code();
				long codingEnd = System.currentTimeMillis();
				System.out.println("(" + (codingEnd - codingStart) + ")");
			}

			System.out.println("Receiving...");
			for(int i=0 ; i<n ; i++)
				_receivedCodedBatch.addCodedSlice(css[i]);
		}
		
//		{
//			System.out.print("Checking solvability...");
//			long solvabilityStart = SystemTime.currentTimeMillis();
//			CodedSlice[] solvingSlices = _receivedCodedBatch.canSolve();
//			long solvabilityEnd = System.currentTimeMillis();
//			System.out.println("(" + (solvabilityEnd - solvabilityStart) + ")");
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
			System.out.println(":" + decode + "(" + (decodeEnd - decodeStart) + ")");
		}
			
		System.out.println("Printing...");
		System.out.println(_receivedCodedBatch);
		System.out.println("Ending...");
		
		System.out.println(_sourceCodedBatch.equals(_receivedCodedBatch));
	}
}
