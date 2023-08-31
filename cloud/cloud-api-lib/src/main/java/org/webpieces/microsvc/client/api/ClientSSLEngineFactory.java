package org.webpieces.microsvc.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;

public class ClientSSLEngineFactory {

    private static final Logger log = LoggerFactory.getLogger(ClientSSLEngineFactory.class);

    private HttpsConfig httpsConfig;

    @Inject
    public ClientSSLEngineFactory(HttpsConfig httpsConfig) {
        this.httpsConfig = httpsConfig;

        log.info("USING keyStoreLocation=" + httpsConfig.getKeyStoreLocation());
    }

    public SSLEngine createEngine(String host, int port) {

        try {
            String keyStoreType = "JKS";
            if(httpsConfig.getKeyStoreLocation().endsWith(".p12")) {
                keyStoreType = "PKCS12";
            }

            URL resource = this.getClass().getClassLoader().getResource(httpsConfig.getKeyStoreLocation());


            InputStream in = this.getClass().getResourceAsStream(httpsConfig.getKeyStoreLocation());

            if (in == null) {
                throw new IllegalStateException("keyStoreLocation=" + httpsConfig.getKeyStoreLocation() + " was not found on classpath");
            }

            //char[] passphrase = password.toCharArray();
            // First initialize the key and trust material.
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            ks.load(in, httpsConfig.getKeyStorePassword().toCharArray());

            //****************Client side specific*********************

            // TrustManager's decide whether to allow connections.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");

            tmf.init(ks);

            sslContext.init(null, tmf.getTrustManagers(), null);

            //****************Client side specific*********************

            SSLEngine engine = sslContext.createSSLEngine(host, port);

            engine.setUseClientMode(true);

            return engine;

        } catch (Exception ex) {
            throw new RuntimeException("Could not create SSLEngine", ex);
        }

    }
}
