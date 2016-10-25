package WEBPIECESxPACKAGE;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.SSLEngineFactory;

public class WEBPIECESxCLASSSSLFactory implements SSLEngineFactory {
	
	private String serverKeystore = "/keystore.jks";
	private	String password = "password";
	
	public WEBPIECESxCLASSSSLFactory() {
		//since this bites a lot of people, let's read in the keystor early
		try(InputStream keySt = WEBPIECESxCLASSSSLFactory.class.getResourceAsStream(serverKeystore)) {
			if(keySt == null)
				throw new IllegalStateException("keystore was not found");
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public SSLEngine createSslEngine() {
		// Create/startPing the SSLContext with key material
		try(InputStream keySt = WEBPIECESxCLASSSSLFactory.class.getResourceAsStream(serverKeystore)) {
			char[] passphrase = password.toCharArray();
			// First startPing the key and trust material.
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
