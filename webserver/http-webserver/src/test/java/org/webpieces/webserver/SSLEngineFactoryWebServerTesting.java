package org.webpieces.webserver;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.SSLEngineFactory;

public class SSLEngineFactoryWebServerTesting implements SSLEngineFactory {

	private String serverKeystore = "/keystore.jks";
	private	String password = "password";
	
	@Override
	public SSLEngine createSslEngine() {
		// Create/initialize the SSLContext with key material
		try(InputStream keySt = SSLEngineFactoryWebServerTesting.class.getResourceAsStream(serverKeystore)) {
			char[] passphrase = password.toCharArray();
			// First initialize the key and trust material.
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(keySt, passphrase);
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			
			//****************Server side specific*********************
			// KeyManager's decide which key material to use.
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, passphrase);
			sslContext.init(kmf.getKeyManagers(), null, null);		
			//****************Server side specific*********************
			
			SSLEngine engine = sslContext.createSSLEngine();
			engine.setUseClientMode(false);
			
			return engine;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
