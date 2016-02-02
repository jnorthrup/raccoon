/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix;

import org.msrg.raccoon.matrix.TypedMatrix;

public class MatrixFactory {

	public static TypedMatrix<?> multiply(TypedMatrix m1, TypedMatrix m2) {
		return m1.multiply(m2._b);
	}
	
	public static TypedMatrix<?> add(TypedMatrix m1, TypedMatrix m2) {
		TypedMatrix<?> addM = m1.add(m2._b);
		return addM;
	}
}
