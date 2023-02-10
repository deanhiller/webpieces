package org.webpieces.microsvc.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsConfig {

    private static final Logger log = LoggerFactory.getLogger(HttpsConfig.class);

    private final String keyStoreLocation;
    private final String keyStorePassword;

    public HttpsConfig(String keyStoreLocation, String keyStorePassword) {
        this.keyStoreLocation = keyStoreLocation;
        this.keyStorePassword = keyStorePassword;
    }

    public HttpsConfig(boolean isLocal) {
        if(isLocal) {
            log.info("Using self-signed key store");
            keyStoreLocation = "/keystore.jks";
            keyStorePassword = "password";
        } else {
            log.info("Using cloud key store");
            keyStoreLocation = "/prodKeyStore.jks";
            keyStorePassword = "lP9Ow1uYXZr9zgt6";
        }
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

}

