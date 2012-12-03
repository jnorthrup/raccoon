/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix.bulk;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.bulk.BulkMatrix1D;
import org.msrg.raccoon.matrix.bulk.SliceMatrix;

public class BulkMatrix1D extends BulkMatrix {

	/**
	 * Auto Generated.
	 */
	private static final long serialVersionUID = 840814543035438705L;

	public BulkMatrix1D(int cols) {
		super(1, cols);
	}
	
	public static BulkMatrix1D createBulkMatrix1D(byte[] b, int bOffset, int size) {
		BulkMatrix1D bm1d = new BulkMatrix1D(size);
		SliceMatrix sm = bm1d.slice(0);
		sm.loadWithCopy(b, bOffset);
		
		return bm1d;
	}
	
	public static BulkMatrix1D createBulkMatixIncrementalData(int size) {
		BulkMatrix1D bm1d = new BulkMatrix1D(size);
		byte[][] b = bm1d.getContent();
		for(int i=0 ; i<size ; i++)
			b[0][i] = (byte)i;
		
		return bm1d;
	}
}
