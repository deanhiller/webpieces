package org.webpieces.nio.api.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.webpieces.util.file.FileFactory;


public class SSLEngineFactoryForChanMgrTest {

	//private static final Logger log = Logger.getLogger(MockSSLEngineFactory.class.getName());
	
	private File clientKeystore;
	private File serverKeystore;
	private	String password = "123456";
	
	public SSLEngineFactoryForChanMgrTest() {
		File baseWorkingDir = FileFactory.getBaseWorkingDir();
		clientKeystore = FileFactory.newFile(baseWorkingDir, "src/test/resources/client2.keystore");
		serverKeystore = FileFactory.newFile(baseWorkingDir, "src/test/resources/server2.keystore");
	}
	
	public SSLEngine createEngineForServerSocket()  throws GeneralSecurityException, IOException {
		// Create/initialize the SSLContext with key material

		char[] passphrase = password.toCharArray();
		// First initialize the key and trust material.
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(serverKeystore), passphrase);
		SSLContext sslContext = SSLContext.getInstance("TLS");

		//make the test deterministic here
		FixedSecureRandom notRandom = new FixedSecureRandom(new byte[0], "server");

		//****************Server side specific*********************
		// KeyManager's decide which key material to use.
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, passphrase);
		sslContext.init(kmf.getKeyManagers(), null, notRandom);
		//****************Server side specific*********************
		
		SSLEngine engine = sslContext.createSSLEngine();
		engine.setUseClientMode(false);
		
		return engine;
	}

	public SSLEngine createEngineForSocket() throws GeneralSecurityException, IOException {
		// Create/initialize the SSLContext with key material
		char[] passphrase = password.toCharArray();
		// First initialize the key and trust material.
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(clientKeystore), passphrase);
		SSLContext sslContext = SSLContext.getInstance("TLS");

		FixedSecureRandom notRandom = new FixedSecureRandom(new byte[0], "client");

		//****************Client side specific*********************
		// TrustManager's decide whether to allow connections.
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);
		sslContext.init(null, tmf.getTrustManagers(), notRandom);
		//****************Client side specific*********************
		
		SSLEngine engine = sslContext.createSSLEngine();
		engine.setUseClientMode(true);
		
		return engine;
	}

}
