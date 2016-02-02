/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon;

import org.msrg.raccoon.CodedCoefficients;
import org.msrg.raccoon.CodedPiece;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;

public class CodedPiece {

	public final SliceMatrix _codedContent;
	public final CodedCoefficients _cc;
	
	public static CodedPiece makeCodedPiece(BulkMatrix bm) {
		CodedCoefficients cc = new CodedCoefficients(bm._rows);
		return new CodedPiece(cc, bm);
	}
	
	public static CodedPiece makeCodedSlice(CodedPiece[] codedSlices) {
		if(codedSlices == null)
			return null;
		
		CodedCoefficients cc = new CodedCoefficients(codedSlices[0]._cc.getLength());
		SliceMatrix[] rowMatrices = new SliceMatrix[codedSlices.length];
		for(int i=0 ; i<rowMatrices.length ; i++)
			rowMatrices[i] = codedSlices[i]._codedContent;

//		BulkMatrix combinedMultiRowMatrix = CombinedBulkMatrix.createCombinedBulkMatrix(rowMatrices);
		
		
		BulkMatrix combinedMultiRowMatrix = new BulkMatrix(rowMatrices);
//		for(int i=0 ; i<codedSlices.length ; i++)
//			cc = cc.multiply(codedSlices[i]._cc);
		throw new UnsupportedOperationException("Remove 2 lines comments above and 1 below");
//		return new CodedPiece(cc, combinedMultiRowMatrix);
	}
	
	public CodedPiece(CodedCoefficients cc, BulkMatrix bm) {
		ByteMatrix c = new ByteMatrix(cc.toArray());
		
		_codedContent = c.multiply(bm).slice(0);
		_cc = cc;
	}
	
	public CodedPiece(CodedCoefficients cc, SliceMatrix sm) {
		_codedContent = sm;
		_cc = cc;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().isInstance(this) )
			return false;
		
		CodedPiece csObj = (CodedPiece) obj;
		if(!_cc.equals(csObj._cc))
			return false;
		
		return _codedContent.equals(csObj._codedContent);
	}
	
	@Override
	public String toString() {
		return "CS[" + _cc + ":" + _codedContent + "]";
	}
}
