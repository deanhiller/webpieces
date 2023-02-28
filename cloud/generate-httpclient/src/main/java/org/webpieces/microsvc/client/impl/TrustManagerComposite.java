package org.webpieces.microsvc.client.impl;

import org.digitalforge.sneakythrow.SneakyThrow;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Taken from stackoverflow post
 * https://stackoverflow.com/questions/48790981/load-java-trust-store-at-runtime-after-jvm-have-been-launched
 */
public class TrustManagerComposite implements X509TrustManager {

    private static boolean wasRun = false;
    private final List<X509TrustManager> compositeTrustmanager = new ArrayList<>();

    public TrustManagerComposite(String input) {
        try (InputStream truststoreInput = TrustManagerComposite.class.getResourceAsStream("/prodKeyStore.jks")) {
            compositeTrustmanager.add(getCustomTrustmanager(truststoreInput));
            compositeTrustmanager.add(getDefaultTrustmanager());
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }

    public synchronized static void setupTruststoreForJdk() {
        if(wasRun)
            return;

        try {
            TrustManagerComposite composite = new TrustManagerComposite(null);
            TrustManager[] trustMgr = new TrustManager[1];
            trustMgr[0] = composite;
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustMgr, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }

    private X509TrustManager getCustomTrustmanager(InputStream trustStream) throws Exception {
        return createTrustManager(trustStream);
    }

    private X509TrustManager getDefaultTrustmanager() throws Exception {
        return createTrustManager(null);
    }

    private X509TrustManager createTrustManager(InputStream trustStream) throws Exception {
        // Now get trustStore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        // load the stream to your store
        trustStore.load(trustStream, null);

        // initialize a trust manager factory with the trusted store
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);

        // get the trust managers from the factory
        TrustManager[] trustManagers = trustFactory.getTrustManagers();
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (X509TrustManager trustManager : compositeTrustmanager) {
            boolean isTrusted = isClientTrusted(chain, authType, trustManager);
            if(isTrusted)
                return;
        }
        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    private boolean isClientTrusted(X509Certificate[] chain, String authType, X509TrustManager trustManager) {
        try {
            trustManager.checkClientTrusted(chain, authType);
            return true;
        } catch (CertificateException e) {
            // maybe the next trust manager will trust it, don't break the loop
            return false;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (X509TrustManager trustManager : compositeTrustmanager) {
            if (isServerTrusted(chain, authType, trustManager)) return;
        }
        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    private boolean isServerTrusted(X509Certificate[] chain, String authType, X509TrustManager trustManager) {
        try {
            trustManager.checkServerTrusted(chain, authType);
            return true;
        } catch (CertificateException e) {
            // maybe the next trust manager will trust it, don't break the loop
            return false;
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> certs = new ArrayList<>();
        for (X509TrustManager trustManager : compositeTrustmanager) {
            for (X509Certificate cert : trustManager.getAcceptedIssuers()) {
                certs.add(cert);
            }
        }
        return certs.toArray(new X509Certificate[0]);
    }
}

