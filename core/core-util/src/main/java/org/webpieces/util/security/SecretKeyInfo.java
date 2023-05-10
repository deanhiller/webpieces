package org.webpieces.util.security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.webpieces.util.SneakyThrow;

public class SecretKeyInfo {

	private byte[] keyData;
	private String algorithm;
	private SecretKey key;

	public SecretKeyInfo(byte[] keyData, String algorithm) {
		this.keyData = keyData;
		this.algorithm = algorithm;
		this.key = new SecretKeySpec(keyData, algorithm);
	}

	public byte[] getKeyData() {
		return keyData;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public SecretKey getKey() {
		return key;
	}

	public static SecretKeyInfo generateNewKey() {
		try {
			String alg = "HmacSHA1";
			KeyGenerator keyGen = KeyGenerator.getInstance(alg);
			SecretKey key = keyGen.generateKey();
			byte[] encoded = key.getEncoded();
			return new SecretKeyInfo(encoded, alg);
		} catch(NoSuchAlgorithmException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public static SecretKeyInfo generateForTest() {
		String alg = "HmacSHA1";
		byte[] encoded = "xxx".getBytes();
		return new SecretKeyInfo(encoded, alg);
	}
}
