package org.webpieces.util.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.inject.Singleton;

import org.digitalforge.sneakythrow.SneakyThrow;

@Singleton
public class Security {

	public enum Hash {
		MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256"), SHA512("SHA-512");
		private String algorithm;

		Hash(String algorithm) {
			this.algorithm = algorithm;
		}

		@Override
		public String toString() {
			return this.algorithm;
		}
	}

	private static final Hash DEFAULT_HASH_TYPE = Hash.MD5;

	public String sign(SecretKeyInfo keyInfo, String message) {
		if(keyInfo == null || keyInfo.getAlgorithm() == null || keyInfo.getKeyData() == null)
			throw new IllegalArgumentException("key must be fully specified");

		try {
			Mac mac = Mac.getInstance(keyInfo.getAlgorithm());
			mac.init(keyInfo.getKey());
			byte[] messageBytes = message.getBytes("utf-8");
			byte[] result = mac.doFinal(messageBytes);

			return Base64.getEncoder().encodeToString(result);
		} catch (InvalidKeyException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw SneakyThrow.sneak(e);
		}

	}

	public static String passwordHash(String input) {
		return passwordHash(input, DEFAULT_HASH_TYPE);
	}

	public static String passwordHash(String input, Hash hashType) {
		byte[] bytes = input.getBytes();
		return hash(hashType, bytes);
	}

	public static String hash(byte[] data) {
		return hash(DEFAULT_HASH_TYPE, data);
	}
	
	private static String hash(Hash hashType, byte[] bytes) {
		try {
			MessageDigest m = MessageDigest.getInstance(hashType.toString());
			byte[] out = m.digest(bytes);
			return Base64.getEncoder().encodeToString(out);
		} catch (NoSuchAlgorithmException e) {
			throw SneakyThrow.sneak(e);
		}
	}

}
