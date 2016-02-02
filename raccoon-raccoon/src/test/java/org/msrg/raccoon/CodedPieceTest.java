/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import junit.framework.TestCase;
import org.msrg.raccoon.CodedPiece;
import org.msrg.raccoon.matrix.bulk.BulkMatrix;

public class CodedPieceTest extends TestCase {

	public final int _rows = 5;
	public final int _cols = 1000;
	
	byte[][] _b;
	BulkMatrix _bm;
	CodedPiece[] _cp;
	
	@Override
	public void setUp() {
		_bm = BulkMatrix.createBulkMatixIncrementalData(_rows, _cols);
		_cp = new CodedPiece[_rows];
		for(int i=0 ; i<_rows ; i++)
			_cp[i] = CodedPiece.makeCodedPiece(_bm);
	}
	
	public void testCodedPiece() {
		System.out.println(_bm);
		for (CodedPiece a_cp : _cp) System.out.println(a_cp);
	}
}
