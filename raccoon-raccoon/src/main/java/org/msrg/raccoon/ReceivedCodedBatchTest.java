/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import java.util.List;

import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.ReceivedCodedBatch;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;

import junit.framework.TestCase;

public class ReceivedCodedBatchTest extends TestCase {

	public final int _rows = 100;
	public final int _cols = 10000;
	
	byte[][] _b;
	BulkMatrix _bm;
	ReceivedCodedBatch _rcb;
	
	@Override
	public void setUp() {
		_bm = BulkMatrix.createBulkMatixIncrementalData(_rows, _cols);
		_rcb = new ReceivedCodedBatch(_bm.getSize(), _bm._rows);
	}
	
	public void ttestEncodingDecodingReceivedCodedBatch() {
		System.out.print("Creating coded pieces [" + _rcb.getRequiredCodedPieceCount() + "]...");
		long start = System.currentTimeMillis();
		CodedPiece[] cp = new CodedPiece[_rows];
		for(int i=0 ; i<_rcb.getRequiredCodedPieceCount() ; i++) {
			assertTrue(_rcb.getAvailableCodedPieceCount() == i);
			assertTrue(_rcb.canSolve() == null);
			
			cp[i] = CodedPiece.makeCodedPiece(_bm);
			_rcb.addCodedSlice(cp[i]);
		}
		long end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");
		
		
		System.out.print("Decoding...");
		start = System.currentTimeMillis();
		assertTrue(_rcb.decode());
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");
		
		
		System.out.print("Verifying...");
		start = System.currentTimeMillis();
		BulkMatrix decodedBM = _rcb.getBulkMatrix();
		assertTrue(_bm.equals(decodedBM));
		end = System.currentTimeMillis();
		System.out.println(" " + (end - start) + " ms.");
	}
	
	public void testGetRandomIndex() {
		for(int maxIndex=0 ; maxIndex<1000 ; maxIndex++) {
			for(int maxReturnSize=0 ; maxReturnSize<1000 ; maxReturnSize++) {
				List<Integer> set;
				try{
					set = _rcb.getRandomIndex(maxIndex, maxReturnSize);
				} catch (IllegalArgumentException argEx) {
					if(maxIndex < maxReturnSize)
						continue;
					else
						throw argEx;
				}
				assertTrue(set.size() == maxReturnSize);
				for(Integer i:set)
					assertTrue(i < maxIndex);
			}
			
			System.out.println("OK! " + maxIndex);
		}
		
		System.out.println("OK!");
	}
}
