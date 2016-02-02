/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.matrix;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.msrg.raccoon.engine.CodingEngineImpl;

import org.msrg.raccoon.matrix.TypedMatrix;

public abstract class TypedMatrix<T> {

	public static int _MAX_ROWS_PRINT = 2;
	public static int _MAX_COLS_PRINT = 2;

	public final T[][] _b;
	protected final int _rows;
	protected final int _cols;
	protected int _inverseRowZero = -1;
	protected TypedMatrix<T> _inverse;
	protected final Object _lock = new Object();

	@Override
	public abstract TypedMatrix<T> clone();
	public abstract TypedMatrix<T> cloneExtended();
	public abstract TypedMatrix<T> decloneExtended();

	protected abstract String toString(T a);
	protected abstract TypedMatrix<T> getNullMatrix(int rows, int cols);
	protected abstract TypedMatrix<T> getZeroMatrix(int rows, int cols);
	protected abstract T[][] getEmptyArray(int rows, int cols);
	
	public abstract int compareToAbs(T a, T b);
	public abstract boolean isZero(T a);
	public abstract boolean isOne(T a);
	public abstract T getOne();
	public abstract T getZero();
	public abstract T inverse(T a);
	public abstract T clone(T a);
	public abstract T add(T a, T b);
	public abstract T subtract(T a, T b);
	public abstract T multiply(T a, T b);
	public abstract T multiplyAndAddInPlace(T total, T a, T b);
	public abstract T divide(T a, T b);
	
	public void verify() {
		if(!CodingEngineImpl.DEBUG)
			return;
		
		if(_b == null)
			throw new NullPointerException();
		for(int i=0 ; i<_rows ; i++) {
			if(_b[i] == null)
				throw new NullPointerException("" + i);
			
			if(_b[i].length != _cols)
				throw new IllegalArgumentException("" + i + " vs. " + _b[i].length + " vs. " + _cols);
			
			for(int j=0 ; j<_cols && CodingEngineImpl.DEBUG ; j++)
				if(_b[i][j] == null)
					throw new NullPointerException("" + i + " vs. " + j + "[" + _rows + "x" + _cols + "]");
		}
	}
	
	public TypedMatrix(T[][] b) {
		_b = b;
		_rows = b.length;
		
		int cols = -1;
		for(int i=0 ; i<_rows ; i++)
			if(_b[i] == null)
				throw new IllegalAccessError("Row '" + i + "' is null.");
			else if (cols == -1)
				cols = _b[i].length;
			else if (cols != _b[i].length)
				throw new IllegalAccessError("Row '" + i + "' is not of length " + _rows);
		
		if(_rows == 0)
			_cols = 0;
		else
			_cols = cols;
		
		verify();
	}
	
	public T[][] toArray() {
		return _b;
	}
	
	public int hasZeroRow() {
		T zero = getZero();
		for(int i=0 ; i<_rows ; i++)
			for(int j=0 ; j<_rows ; j++)
				if(!_b[i][j].equals(zero))
					break;
				else if(j==_rows-1)
					return i;
		
		return -1;
	}
	
	public TypedMatrix<T> inverseMatrix() {
		synchronized (_lock) {
			if(_inverse != null)
				return _inverse;
		}
		
		TypedMatrix<T> inverse = inverseMatrixPrivately();
		
		synchronized (_lock) {
			if (inverse == null)
				return null;
			else if(_inverse == null)
				_inverse = inverse;
			
			return _inverse;
		}
	}
	
	protected TypedMatrix<T> inverseMatrixPrivately() {
		TypedMatrix<T> em = eliminatedMatrix();
		TypedMatrix<T> inverse = em.decloneExtended();
		if(inverse == null) {
			if(em._inverseRowZero == -1)
				throw new IllegalStateException();
			
			_inverseRowZero = em._inverseRowZero;
		}
		
		return inverse;
	}
	
	public TypedMatrix<T> eliminatedMatrix() {
		TypedMatrix<T> em = cloneExtended();
		
		int i = 0;
		int j = 0;
		while (i < em._rows && j < em._cols/2) {
			int maxi = i;
			for (int k = i+1 ; k < em._rows ; k++ ) {
				if (compareToAbs(em._b[k][j], em._b[maxi][j]) > 0)
					maxi = k;
			}
			if (! isZero(em._b[maxi][j])) {
				em.swapRows(i, maxi);
				em.devideRow(i, em._b[i][j]);
				for (int u = i+1 ; u < _rows ; u++)
					em.subtractRowFromRow(clone(em._b[u][j]), i, u);
				i = i + 1;
			}
			j = j + 1;
		}
		
		for(i=em._rows - 1 ; i>0 ; i--) {
			assert(isOne(em._b[i][i]));
			
			for(j=i-1 ; j>=0 ; j--)
				em.subtractRowFromRow(clone(em._b[j][i]), i, j);
		}
		
		return em;
	}
	
	protected void devideRow(int i, T value) {
		multiplyRow(i, inverse(clone(value)));
	}
	
	protected void subtractRow(int i, T value) {
		for(int j=0 ; j<_rows ; j++)
			subtract(_b[i][j], value);
	}
	
	protected void subtractRowFromRow(T multiplier, int i, int j) {
		for(int k=0 ; k<_cols ; k++) {
			T multiplied = multiply(_b[i][k], multiplier);
			_b[j][k] = subtract(_b[j][k], multiplied);
		}
	}
	
	protected void addRow(int i, T value) {
		for(int j=0 ; j<_cols ; j++)
			_b[i][j] = add(_b[i][j], value);
	}
	
	protected void multiplyRow(int i, T value) {
		for(int j=0 ; j<_cols ; j++)
			_b[i][j] = multiply(_b[i][j], value);
	}
	
	protected void swapRows(int i, int j) {
		T[] temp = _b[j];
		_b[j] = _b[i];
		_b[i] = temp;
	}
	
	protected TypedMatrix<T> multiply(T b) {
		for(int i=0 ; i<getRowSize() ; i++) {
			for(int j=0 ; j<getColumnSize() ; j++) {
				T mult = multiply(get(i,j), b);
				set(mult, i, j);
			}
		}
		
		return this;
	}
	
	public String toStringShort() {
		return _rows + "x" + _cols;
	}
	
//	public String toStringFull() {
//		return toString(_rows, _cols);
//	}
	
	@Override
	public String toString() {
		return toString(_MAX_ROWS_PRINT, _MAX_COLS_PRINT);
	}
	
	protected String toString(int maxRows, int maxCols) {
		Writer ioWriter = new StringWriter(getRowSize() * getColumnSize() * 4);
		try {
			toString(ioWriter, ",", "\n", maxRows, maxCols);
		} catch (IOException e) {
			return "ERROR";
		}
		return ioWriter.toString();
	}
	
	protected void toString(Writer ioWriter, String fs, String rs, int maxRows, int maxCols) throws IOException {
		for(int i=0 ; i<getRowSize() && i<maxRows ;i++) {
			if(i!=0)
				ioWriter.append(rs);
			toStringRow(ioWriter, i, fs, maxCols);
		}
		
		int remaining = getRowSize() - maxRows;
		if(remaining > 0)
			ioWriter.append(rs + "...(" + remaining + " more)");
	}
	
	protected int toStringRow(Writer ioWriter, int i, String fs, int maxCols) throws IOException {
		if(i >= _b.length)
			return 0;
		
		int len = 0;
		for(int j=0 ; j<getColumnSize() && j<maxCols ; j++) {
			if(j!=0) {
				ioWriter.append(fs);
				len+=fs.length();
			}
			String str = _b[i][j].toString();
			len+=str.length();
			ioWriter.append(str);
		}
		
		int remaining = getColumnSize() - maxCols;
		if(remaining > 0)
			ioWriter.append("...(" + remaining + " more)");
		
		return len;
	}
	
	protected TypedMatrix<T> multiply(TypedMatrix<T> a) {
		return multiply(a._b);
	}
	
	protected final TypedMatrix<T> multiply(T[][] a) {
		int aRows = a.length;
		if(aRows!=_cols)
			throw new IllegalArgumentException("Row/column count mismatch: " + _rows + " vs. " + a.length);
		
		if(aRows == 0)
			return getZeroMatrix(0, 0);
		
		int aCols = a[0].length;
		
		TypedMatrix<T> multM = getZeroMatrix(_rows, aCols);
		for(int i=0 ; i<_rows ; i++) {
			if (aCols != a[i].length)
				throw new IllegalArgumentException("Row '" + i + "' of array is not of length " + aCols);
			
			for(int k=0 ; k<aCols ; k++) {
				multM._b[i][k] = getZero();
				for(int j=0 ; j<getColumnSize() ; j++)
					multM._b[i][k] = multiplyAndAddInPlace(multM._b[i][k], _b[i][j], a[j][k]);
			}
		}
		
		return multM;
	}
	
	public void set(T b, int i, int j) {
		_b[i][j] = b;
	}
	
	public T get(int i, int j) {
		return _b[i][j];
	}
	
	public int getColumnSize() {
		return _cols;
	}
	
	public int getRowSize() {
		return _rows;
	}
	
	protected TypedMatrix<T> add(T[][] a) {
		int aRows = a.length;
		if(aRows!=_rows)
			throw new IllegalArgumentException("Row/rows count mismatch: " + _rows + " vs. " + aRows);
		
		int aCols = a[0].length;
		if(aCols != _cols)
			throw new IllegalArgumentException("Row/rows count mismatch: " + _rows + " vs. " + aRows);
		
		
		TypedMatrix<T> addM = clone();
		for(int i=0 ; i<_rows ; i++)
			for(int j=0 ; j<_cols ; j++)
				addM._b[i][j] = add(addM._b[i][j], a[i][j]);
		
		return addM;
	}
	
	public TypedMatrix<T> addInPlace(TypedMatrix<T> m) {
		if(getRowSize() != m.getRowSize())
			throw new IllegalArgumentException("Row counts do not match.");
		
		if(getColumnSize() != m.getColumnSize())
			throw new IllegalArgumentException("Column counts do not match.");
		
		for(int i=0 ; i<_rows ; i++)
			for(int j=0 ; j<_cols ; j++)
				_b[i][j] = add(_b[i][j], m._b[i][j]);

		return this;
	}

	public boolean checkMultiplyability(int rows1, int cols1, int rows2, int cols2) {
		if(cols1 == rows2)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!obj.getClass().isInstance(this))
			return false;
		
		TypedMatrix<?> tmObj = (TypedMatrix<?>)obj;
		if(_rows != tmObj._rows || _cols != tmObj._cols)
			return false;
		
		for(int i=0 ; i<_rows ; i++)
			for(int j=0 ; j<_rows ; j++)
				if(!_b[i][j].equals(tmObj._b[i][j]))
					return false;
		
		return true;
	}
	
	public boolean isSquare() {
		return _rows == _cols;
	}
	
	public boolean isZero() {
		for(int i=0 ; i<_rows ; i++)
			for(int j=0 ; j<_cols ; j++)
				if(!isZero(_b[i][j]))
					return false;
		
		return true;
	}
	
	public boolean isIdentity() {
		if(!isSquare())
			return false;
		
		for(int i=0 ; i<_rows ; i++) {
			for(int j=0 ; j<_rows ; j++)
				if(i==j) {
					if(!isOne(_b[i][j]))
						return false;
				} else {
					if(!isZero(_b[i][j]))
						return false;
				}
		}
		
		return true;
	}
	
	public static String toStringMult(TypedMatrix<?> a, TypedMatrix<?> b, TypedMatrix<?> c) {
		TypedMatrix<?>[] ms = {a, b, c};
		String[] separators = {" x ", " = "};
		return toString(ms, separators);
	}
	
	public static String toStringAdd(TypedMatrix<?> a, TypedMatrix<?> b, TypedMatrix<?> c) {
		TypedMatrix<?>[] ms = {a, b, c};
		String[] separators = {" + ", " = "};
		return toString(ms, separators);
	}

	public static String toString(TypedMatrix<?>[] ms, String[] separator) {
		int rows = -1;
		for(TypedMatrix<?> m : ms) {
			if (m.getRowSize() > rows)
				rows = m.getRowSize();
		}
		
		if (ms.length != separator.length + 1)
			throw new IllegalArgumentException("Not enough separators: " + rows + " vs. " + separator.length);
		
		Writer ioWriter = new StringWriter(ms.length * rows * rows * 3);
		try{
			int[] lengths = new int[separator.length];
			for(int i=0 ; i<rows ; i++) {
				if(i!=0)
					ioWriter.append("\n");

				int len = 0;
				for(int j=0 ; j<ms.length ; j++) {
					if(i!=0 && j!=0)
						for(int k=len+1 ; k<lengths[j-1] ; k++)
							ioWriter.append('*');

					if(i==0 && j!=0)
						lengths[j-1] = len;

					if (j!=0)
						if(i==0)
							ioWriter.append(separator[j-1]);
						else
							for(int k=0 ; k<separator[j-1].length() ; k++)
								ioWriter.append(' ');
					
					len += ms[j].toStringRow(ioWriter, i, ",", _MAX_COLS_PRINT);
				}
			}
		}catch (IOException e) {
			return "ERROR";
		}
		
		return ioWriter.toString();
	}
	
	public int getInverseRowZero() {
		return _inverseRowZero;
	}
	
	public TypedMatrix<T> getInverseMatrix() {
		return _inverse;
	}
}
