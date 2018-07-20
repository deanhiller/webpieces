package org.webpieces.plugins.sslcert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jose4j.base64url.Base64;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.plugins.backend.MenuCreator;
import org.webpieces.router.api.SimpleStorage;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;

@Singleton
public class InstallSslCertController {

	private static final String EMAIL = "email";
	private static final String URL = "urlLocation";
	private SimpleStorage storage;
	private MenuCreator menuCreator;
	private AcmeClientProxy acmeClient;

	@Inject
	public InstallSslCertController(MenuCreator menuCreator, SimpleStorage storage, AcmeClientProxy acmeClient) {
		this.menuCreator = menuCreator;
		this.storage = storage;
		this.acmeClient = acmeClient;
	}
	
	public CompletableFuture<Action> sslSetup() {
		CompletableFuture<Map<String, String>> read = this.storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY);
		return read.thenCompose( (properties) -> decide(properties));
	}

	private CompletableFuture<Action> decide(Map<String, String> properties) {
		String base64 = properties.get(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY);
		if(base64 == null) {
			//It really sucks that this is synchronous(could throw into future pool next time)
			CompletableFuture<AcmeInfo> future = acmeClient.fetchRemoteInfo();

			return future.thenApply((info) -> {
				return Actions.renderThis(
						"menus", menuCreator.getMenu(), 
						"agreement", info.getUri()+"", 
						"website", info.getWebsite()
						);
			});
		}

		return CompletableFuture.completedFuture(Actions.redirect(InstallSslCertRouteId.STEP2));
	}

	public CompletableFuture<Redirect> postStartSslInstall(String email) {

		KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

		try (StringWriter writer = new StringWriter()) {
			KeyPairUtils.writeKeyPair(accountKeyPair, writer);

			Map<String, String> properties = new HashMap<>();
			properties.put(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY, writer.toString());
			properties.put(EMAIL, email);
			return this.storage.save(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY, properties)
				.thenApply( (v) -> Actions.redirect(InstallSslCertRouteId.STEP2));
			
		} catch(IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public CompletableFuture<Action> step2() {
		CompletableFuture<Map<String, String>> read = this.storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY);
		return read.thenApply((props) -> decideStep2(props));
	}

	private Action decideStep2(Map<String, String> properties) {
		String keyPair = properties.get(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY);
		if(keyPair == null)
			return Actions.redirect(InstallSslCertRouteId.INSTALL_SSL_SETUP);
		
		return Actions.renderThis("keyPair", keyPair);
	}

	public CompletableFuture<Redirect> postStep2(String organization) {
		RouterRequest request = Current.request();
		return this.storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY)
				.thenCompose((props) -> process(props, request, organization));
	}
	
	private CompletableFuture<Redirect> process(Map<String, String> props, RouterRequest request, String organization) {
		String domain = request.domain;
		String accountKeyPairString = props.get(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY);
		String email = props.get(EMAIL);

		try {
			KeyPair accountKeyPair = KeyPairUtils.readKeyPair(new StringReader(accountKeyPairString));
			return acmeClient.openAccount(email, accountKeyPair)
				.thenCompose((url) -> saveUrlAndProcessOrder(url, accountKeyPair, email, domain, organization));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}
	
	private CompletableFuture<Redirect> saveUrlAndProcessOrder(URL url, KeyPair accountKeyPair, String email, String domain, String organization) {
		return storage.save(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY, URL, url+"")
					.thenCompose((nothing) -> acmeClient.placeOrder(url, accountKeyPair, email, domain, organization))
					.thenCompose( (cert) -> saveCert(cert))
					.thenApply((nothing) -> Actions.redirect(InstallSslCertRouteId.MAINTAIN_SSL));
	}

	private CompletableFuture<Void> saveCert(CertAndSigningRequest certInfo) {
		List<X509Certificate> certChain = certInfo.getFinalCertificate().getCertificateChain();
		CSRBuilder signingRequest = certInfo.getSigningRequest();

		Map<String, String> props = new HashMap<>();
		try (StringWriter writer = new StringWriter()) {
			signingRequest.write(writer);
			props.put(InstallSslCertPlugin.CSR, writer.toString());

			for(int i = 0; i < certChain.size(); i++) {
				X509Certificate cert = certChain.get(i);
				String certString = Base64.encode(cert.getEncoded());
				props.put(InstallSslCertPlugin.CERT_CHAIN_PREFIX, certString);
			}
		
			return storage.save(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY, props);
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch (CertificateEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public Render renderToken(String token) {
		//check token exists in database AND delete that token afterwards
		
		return Actions.renderThis("token", token);
	}
	
	public Render maintainSsl() {
		return Actions.renderThis();
	}
}
