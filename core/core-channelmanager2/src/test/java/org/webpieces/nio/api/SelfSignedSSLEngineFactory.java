package org.webpieces.nio.api;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.file.FileFactory;


public class SelfSignedSSLEngineFactory implements SSLEngineFactoryWithHost {

	//private static final Logger log = Logger.getLogger(MockSSLEngineFactory.class.getName());
	
	private File clientKeystore;
	private File serverKeystore;
	private	String password = "password";

	private String cachedHost = "";
	
	public SelfSignedSSLEngineFactory() {
		File baseWorkingDir = FileFactory.getBaseWorkingDir();
		clientKeystore = FileFactory.newFile(baseWorkingDir, "src/test/resources/exampletrust.jks");
		serverKeystore = FileFactory.newFile(baseWorkingDir, "src/test/resources/exampleca.jks");
	}
	
	@Override
	public SSLEngine createSslEngine(String host) {
		try {
			this.cachedHost = host;
			
			// Create/initialize the SSLContext with key material
	
			char[] passphrase = password.toCharArray();
			// First initialize the key and trust material.
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(serverKeystore), passphrase);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			
			//****************Server side specific*********************
			// KeyManager's decide which key material to use.
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, passphrase);
			sslContext.init(kmf.getKeyManagers(), null, null);		
			//****************Server side specific*********************
			
			SSLEngine engine = sslContext.createSSLEngine();
			engine.setUseClientMode(false);
			
			return engine;
		} catch(Exception e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public SSLEngine createSslEngine() {
		throw new IllegalStateException("Do not return here to make test fail if this no longer works");
	}

	public SSLEngine createEngineForClient(String host, int port) {
		try {
			// Create/initialize the SSLContext with key material
			char[] passphrase = password.toCharArray();
			// First initialize the key and trust material.
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(clientKeystore), passphrase);
			SSLContext sslContext = SSLContext.getInstance("TLS");
	
			//****************Client side specific*********************
			// TrustManager's decide whether to allow connections.
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
			sslContext.init(null, tmf.getTrustManagers(), null);		
			//****************Client side specific*********************

			SSLEngine engine = sslContext.createSSLEngine(host, port);
			engine.setUseClientMode(true);
			
			return engine;
		} catch(Exception e) {
			throw SneakyThrow.sneak(e);
		}
	}

	public String getCachedHost() {
		return cachedHost;
	}

}
