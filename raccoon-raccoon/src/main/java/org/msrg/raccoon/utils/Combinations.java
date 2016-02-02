/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.BitSet;
import java.util.Random;
import java.util.Vector;

public class Combinations {

	static final int MAX_SUPPORTED_TOTAL = 1000;
	private static final Random _RANDOM = new Random();
	private final Vector<BitSet> _allCombinations = new Vector<BitSet>();
	private final int _count;
	private final int _total;
	
	protected Combinations(int count, int total) {
		if(total > MAX_SUPPORTED_TOTAL)
			throw new IllegalArgumentException("Total is too big: " + total);
//		else if (count > total)
//			throw new IllegalArgumentException("Count is larger than total");
		
		_count = count;
		_total = total;
		
		BitSet bv = new BitSet(total);
		if(_count <= _total)
			generateBVRecursively(_allCombinations, bv, 0, 0, _count, _total);
	}
	
	public BitSet getRandomCombinations() {
		int size = _allCombinations.size();
		if(size==0)
			return null;
		
		int random = _RANDOM.nextInt(size);
		return _allCombinations.get(random);
	}
	
	public BitSet[] getAllCombinations() {
		return _allCombinations.toArray(new BitSet[0]);
	}
	
	protected void generateBVRecursively(Vector<BitSet> allCombinations, BitSet bv, int bitIndex, int bitsSet, int bitsRequired, int total) {
		if (bitIndex > total)
			return;
		
		if (total - bitIndex < bitsRequired)
			return;
			
		if(bitsRequired == 0) {
			allCombinations.add((BitSet)bv.clone());
			return;
		}
		
		bv.set(bitIndex);
		generateBVRecursively(allCombinations, bv, bitIndex + 1, bitsSet + 1, bitsRequired - 1, total);
		
		bv.clear(bitIndex);
		generateBVRecursively(allCombinations, bv, bitIndex + 1, bitsSet, bitsRequired, total);
	}
	
	protected int getCount() {
		return _count;
	}
	
	protected int getTotal() {
		return _total;
	}
	
	public static Combinations makeAllCombinations(int count, int total) {
		return new Combinations(count, total);
	}
	
	@Override
	public String toString() {
		return toString("\n");
	}
	
	public String toString(String delim) {
		int initialSize = _allCombinations.size() * (10 + _count * 3);
		System.out.println("Making room for: " + initialSize);
		Writer ioWriter = new StringWriter(initialSize);
		
		try {
			toString(ioWriter, delim);
		} catch (IOException e) {
			return "ERROR";
		}
		return ioWriter.toString();
	}
	
	public void toString(Writer ioWriter, String delim) throws IOException {
		int i = 0;
		for(BitSet bv : _allCombinations) {
			ioWriter.append((i==0?(i+":"):(delim+i+":")) + bv);
			i++;
		}
	}
	
	public static void main(String[] argv) {
		long starttime = System.currentTimeMillis();
		Combinations com = makeAllCombinations(MAX_SUPPORTED_TOTAL - 1, MAX_SUPPORTED_TOTAL);
		long endtime = System.currentTimeMillis();
		
		System.out.println("TOTAL_TIME: " + (endtime - starttime) + " (ms).");
		System.out.println(com.toString().length());
	}
	
}
