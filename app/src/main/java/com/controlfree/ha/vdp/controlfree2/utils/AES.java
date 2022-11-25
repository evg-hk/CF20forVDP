package com.controlfree.ha.vdp.controlfree2.utils;

import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	private static String IV = "d0129318f792a3fd";
	
	final private static char[] hexArray = "0123456789abcdef".toCharArray();
	
	public static String encrypt(String plainText, String encryptionKey) throws Exception {
		int paddingLen = 16-plainText.length()%16;
		for(int i=0;i<paddingLen;i++){
			plainText += "\0";
		}
	    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
	    SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
	    cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	    return bytesToHex(cipher.doFinal(plainText.getBytes("UTF-8")));
	}
	public static String decrypt(String cipherText, String encryptionKey) throws Exception{
		byte[] cipherBytes = hexToByte(cipherText);
	    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
	    SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
	    cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	    return new String(cipher.doFinal(cipherBytes),"UTF-8").trim();
	}
	
	//helper function
	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
		    int v = bytes[j] & 0xFF;
		    hexChars[j * 2] = hexArray[v >>> 4];
		    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	private static byte[] hexToByte(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	//function
	public static String getRandomKey(){
		Random rand = new Random();
		char[] hexChars = new char[16];
		for(int i=0;i<hexChars.length;i++){
			hexChars[i] = hexArray[rand.nextInt(hexArray.length)];
		}
		return new String(hexChars);
	}
}
