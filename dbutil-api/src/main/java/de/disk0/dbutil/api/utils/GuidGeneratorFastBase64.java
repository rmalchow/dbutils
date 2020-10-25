package de.disk0.dbutil.api.utils;

import java.util.SplittableRandom;

import de.disk0.dbutil.api.GuidGenerator;

public class GuidGeneratorFastBase64 implements GuidGenerator {
	
	private static SplittableRandom sr = new SplittableRandom();
	private static char[] alphabet = new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','_','-' };
	
	@Override
	public String generateUuid() {
		char[] x = new char[16];
		int l=0;
		l=sr.nextInt();
		x[0] = (char)alphabet[l>>0 &0x3F];
		x[1] = (char)alphabet[l>>6 &0x3F];
		x[2] = (char)alphabet[l>>12 &0x3F];
		x[3] = (char)alphabet[l>>18 &0x3F];
		l=sr.nextInt();
		x[4] = (char)alphabet[l>>0 &0x3F];
		x[5] = (char)alphabet[l>>6 &0x3F];
		x[6] = (char)alphabet[l>>12 &0x3F];
		x[7] = (char)alphabet[l>>18 &0x3F];
		l=sr.nextInt();
		x[8] = (char)alphabet[l>>0 &0x3F];
		x[9] = (char)alphabet[l>>6 &0x3F];
		x[10] = (char)alphabet[l>>12 &0x3F];
		x[11] = (char)alphabet[l>>18 &0x3F];
		l=sr.nextInt();
		x[12] = (char)alphabet[l>>0 &0x3F];
		x[13] = (char)alphabet[l>>6 &0x3F];
		x[14] = (char)alphabet[l>>12 &0x3F];
		x[15] = (char)alphabet[l>>18 &0x3F];
		return new String(x);
	}

}
