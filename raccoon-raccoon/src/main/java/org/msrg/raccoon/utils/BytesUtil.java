/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.utils;

import java.net.InetSocketAddress;

public class BytesUtil {
	
	protected static String[] dig = {"0","1","2","3","4","5","6","7",
            "8","9","a","b","c","d","e","f"};

	public static String hex(byte b) {
		return dig[(b & 0xff) >> 4] + dig[b & 0x0f];
	}
	
	public static int compareAddresses(InetSocketAddress addr1, InetSocketAddress addr2) {
		byte[] addr1Array = addr1.getAddress().getAddress();
		byte[] addr2Array = addr2.getAddress().getAddress();
		for(int i=0 ; i<addr1Array.length && i<addr2Array.length ; i++)
			if(addr1Array[i] < addr2Array[i])
				return -1;
		
		int port1 = addr1.getPort();
		int port2 = addr2.getPort();
		if(port1 < port2)
			return -1;
		else if(port1 > port2)
			return 1;
		else
			return 0;
	}

	public static byte[] combineInsertB2sizeAsByte(byte[] b1, byte[] b2) {
		if(b1 == null)
			throw new NullPointerException();
		if(b2 == null)
			throw new NullPointerException();
		
		byte[] ret = new byte[b1.length + b2.length + 1];
		for(int i=0 ; i<b1.length ; i++)
			ret[i] = b1[i];
		ret[b1.length] = (byte) b2.length;
		for(int i=0 ; i<b2.length ; i++)
			ret[b1.length + 1 + i] = b2[i];
		return ret;
	}

	public static boolean compareByteArray(byte[] arr1, byte[] arr2) {
		if(arr1.length != arr2.length)
			return false;
		for(int i=0 ; i<arr1.length ; i++)
			if(arr1[i] != arr2[i])
				return false;
		return true;
	}
	
}
