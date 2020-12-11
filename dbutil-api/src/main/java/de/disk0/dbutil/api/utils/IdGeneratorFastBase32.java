package de.disk0.dbutil.api.utils;

import java.util.SplittableRandom;

import de.disk0.dbutil.api.IdGenerator;

public class IdGeneratorFastBase32 implements IdGenerator {
	
	private static SplittableRandom sr = new SplittableRandom();
	private static char[] alphabet = new char[] { '2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','J','K','L','M','N','P','Q','R','S','T','U','V','W','X','Y','Z' };
	
	@Override
	public String generateId() {
		char[] x = new char[20];
		int l=0;
		l=sr.nextInt();
		x[0] = (char)alphabet[l>>0 &0x1F];
		x[1] = (char)alphabet[l>>5 &0x1F];
		x[2] = (char)alphabet[l>>10 &0x1F];
		x[3] = (char)alphabet[l>>15 &0x1F];
		l=sr.nextInt();
		x[4] = (char)alphabet[l>>0 &0x1F];
		x[5] = (char)alphabet[l>>5 &0x1F];
		x[6] = (char)alphabet[l>>10 &0x1F];
		x[7] = (char)alphabet[l>>15 &0x1F];
		l=sr.nextInt();
		x[8] = (char)alphabet[l>>0 &0x1F];
		x[9] = (char)alphabet[l>>5 &0x1F];
		x[10] = (char)alphabet[l>>10 &0x1F];
		x[11] = (char)alphabet[l>>15 &0x1F];
		l=sr.nextInt();
		x[12] = (char)alphabet[l>>0 &0x1F];
		x[13] = (char)alphabet[l>>5 &0x1F];
		x[14] = (char)alphabet[l>>10 &0x1F];
		x[15] = (char)alphabet[l>>15 &0x1F];
		l=sr.nextInt();
		x[16] = (char)alphabet[l>>0 &0x1F];
		x[17] = (char)alphabet[l>>5 &0x1F];
		x[18] = (char)alphabet[l>>10 &0x1F];
		x[19] = (char)alphabet[l>>15 &0x1F];
		return new String(x);
	}
	
	public static void main(String[] args) {
		System.err.println(alphabet.length);
	}

}
