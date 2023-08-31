package org.webpieces.auth0.api;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class GenerateAESKey {

    public static String generateBase64Key() throws NoSuchAlgorithmException {
        KeyGenerator aes1 = KeyGenerator.getInstance("AES");
        aes1.init(256);
        SecretKey aes = aes1.generateKey();
        byte[] data = aes.getEncoded();
        String myBase64Str = Base64.getEncoder().encodeToString(data);
        return myBase64Str;
    }
}
