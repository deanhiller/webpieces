package org.webpieces.httpclient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForTestSslClientEngineFactory {

	private static final Logger log = LoggerFactory.getLogger(ForTestSslClientEngineFactory.class);

	public SSLEngine createSslEngine(String host, int port) {
		try {
			return createSslEngineImpl(host, port);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SSLEngine createSslEngineImpl(String host, int port) throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String authType) throws CertificateException {
				log.info("authType(client)="+authType);
			}
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String authType) throws CertificateException {
				log.info("authType(server)="+authType);
			}
		} };

		// Ignore differences between given hostname and certificate hostname
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

		final SSLEngine sslEngine = sc.createSSLEngine(host, port);
		sslEngine.setUseClientMode(true);
		sslEngine.setNeedClientAuth(false);
		return sslEngine;
	}

}
