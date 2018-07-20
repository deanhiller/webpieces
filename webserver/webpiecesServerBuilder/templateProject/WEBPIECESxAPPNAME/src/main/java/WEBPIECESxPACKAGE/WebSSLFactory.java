package WEBPIECESxPACKAGE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jose4j.base64url.Base64;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.plugins.sslcert.InstallSslCertPlugin;
import org.webpieces.router.api.NeedsSimpleStorage;
import org.webpieces.router.api.SimpleStorage;

public class WebSSLFactory implements SSLEngineFactory, NeedsSimpleStorage {
	
	private static final Logger log = LoggerFactory.getLogger(WebSSLFactory.class);

	private String serverKeystore = "/keystore.jks";
	private	String password = "password";
	private char[] passphrase = password.toCharArray();

	private SimpleStorage storage;
	
	private KeyPair accountKeyPair;
	private X509Certificate[] certChain;
	
	public WebSSLFactory() {
		//since this bites a lot of people, let's read in the keystor early
		try(InputStream keySt = WebSSLFactory.class.getResourceAsStream(serverKeystore)) {
			if(keySt == null)
				throw new IllegalStateException("keystore was not found");
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	//NOTE: In development MODE, this may be called a few times to pass in the newly compiled one each time
	//in production, this is called ONCE.  This was a definite workaround due to class loading and dependencies since
	//the main system in this case wants to read the certs from storage (so the cert is in ONE place instead of on
	//N machines in your cluster, it will be read from storage that you lock down).
	@Override
	public CompletableFuture<Void> init(SimpleStorage storage) {
		this.storage = storage;
		CompletableFuture<Map<String, String>> future = storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY);
		return future.thenApply((props) -> setupCert(props));
	}
	
	private Void setupCert(Map<String, String> properties) {
		//lookup 1st cert installed from plugin
		String cert = properties.get(InstallSslCertPlugin.CERT_CHAIN_PREFIX+0);
		if(cert == null) {
			log.warn("No cert for SSL installed yet, using self signed cert");
			return null;
		}
		
		String accountKeyPairString = properties.get(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY);

		try (StringReader reader = new StringReader(accountKeyPairString)) {
			accountKeyPair = KeyPairUtils.readKeyPair(reader);


		List<X509Certificate> certs = new ArrayList<>();
		int i = 0; 
		while(true) {
			String base64Cert = properties.get(InstallSslCertPlugin.CERT_CHAIN_PREFIX+i);
			i++;
			if(base64Cert == null)
				break;
			
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(Base64.decode(base64Cert)));
			certs.add(certificate);
		}

		this.certChain = certs.toArray(new X509Certificate[0]);
		
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	@Override
	public SSLEngine createSslEngine() {
		try {
			if(certChain != null)
				return createSslEngineFromCert();

			//otherwise, each request, try to kick off the loading
			CompletableFuture<Map<String, String>> future = storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY);
			future.thenApply( (props) -> setupCert(props));
			
			return createFromSelfSignedCert();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private SSLEngine createFromSelfSignedCert() throws 
			NoSuchAlgorithmException, CertificateException, IOException, 
			KeyStoreException, UnrecoverableKeyException, KeyManagementException {
		
		// Create/startPing the SSLContext with key material
		try(InputStream keySt = WebSSLFactory.class.getResourceAsStream(serverKeystore)) {
			// First startPing the key and trust material.
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(keySt, passphrase);
			
			return createFromKeystore(ks);
		}
	}

	private SSLEngine createSslEngineFromCert() 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, 
			IOException, UnrecoverableKeyException, KeyManagementException {
		
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    ks.load(null, passphrase);
	    ks.setKeyEntry("alias", accountKeyPair.getPrivate(), passphrase, certChain);
	    
		return createFromKeystore(ks);
	}

	private SSLEngine createFromKeystore(KeyStore ks)
			throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
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
	}

}
