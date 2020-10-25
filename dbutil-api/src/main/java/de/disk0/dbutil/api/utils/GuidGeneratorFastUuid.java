package de.disk0.dbutil.api.utils;

import java.util.SplittableRandom;

import de.disk0.dbutil.api.GuidGenerator;

public class GuidGeneratorFastUuid implements GuidGenerator {
	
	private static char[] hex      = new char[] { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'}; 
	private static SplittableRandom sr = new SplittableRandom();

	public String generateUuid() {
		/**
		return UUID.randomUUID().toString();
		 **/
		char[] x = new char[36];
		int l=0;
		l=sr.nextInt();
		// 8-block
		x[0] = (char)hex[l>>0  &0xF];
		x[1] = (char)hex[l>>4  &0xF];
		x[2] = (char)hex[l>>8  &0xF];
		x[3] = (char)hex[l>>12 &0xF];
		x[4] = (char)hex[l>>16 &0xF];
		x[5] = (char)hex[l>>20 &0xF];
		x[6] = (char)hex[l>>24 &0xF];
		x[7] = (char)hex[l>>28 &0xF];
		x[8] = '-';
		l=sr.nextInt();
		// 4-block
		x[9] = (char)hex[l>>0  &0xF];
		x[10]= (char)hex[l>>4  &0xF];
		x[11]= (char)hex[l>>8  &0xF];
		x[12]= (char)hex[l>>12 &0xF];
		x[13] = '-';
		// 4-block
		x[14]= (char)hex[l>>16 &0xF];
		x[15]= (char)hex[l>>20 &0xF];
		x[16]= (char)hex[l>>24 &0xF];
		x[17]= (char)hex[l>>28 &0xF];
		x[18] = '-';
		l=sr.nextInt();
		// 4-block
		x[19]= (char)hex[l>>0  &0xF];
		x[20]= (char)hex[l>>4  &0xF];
		x[21]= (char)hex[l>>8  &0xF];
		x[22]= (char)hex[l>>12 &0xF];
		x[23] = '-';
		// 10-block
		x[24]= (char)hex[l>>16 &0xF];
		x[25]= (char)hex[l>>20 &0xF];
		x[26]= (char)hex[l>>24 &0xF];
		x[27]= (char)hex[l>>28 &0xF];
		l=sr.nextInt();
		x[28]= (char)hex[l>>0  &0xF];
		x[29]= (char)hex[l>>4  &0xF];
		x[30]= (char)hex[l>>8  &0xF];
		x[31]= (char)hex[l>>12 &0xF];
		x[32]= (char)hex[l>>16 &0xF];
		x[33]= (char)hex[l>>20 &0xF];
		x[34]= (char)hex[l>>24 &0xF];
		x[35]= (char)hex[l>>28 &0xF];
		return new String(x);
	}

}
