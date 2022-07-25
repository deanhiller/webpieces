package org.webpieces.plugin.secure.sslcert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jose4j.base64url.Base64;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.plugin.backend.menu.MenuCreator;
import org.webpieces.plugin.secure.sslcert.acme.AcmeClientProxy;
import org.webpieces.plugin.secure.sslcert.acme.AcmeInfo;
import org.webpieces.plugin.secure.sslcert.acme.ProxyAuthorization;
import org.webpieces.plugin.secure.sslcert.acme.ProxyOrder;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.digitalforge.sneakythrow.SneakyThrow;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

@Singleton
public class InstallSslCertController {

	private static final Logger log = LoggerFactory.getLogger(InstallSslCertController.class);

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
	
	public XFuture<Action> sslSetup() {
		XFuture<Map<String, String>> read = this.storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY);
		return read.thenCompose( (properties) -> decide(properties));
	}

	private XFuture<Action> decide(Map<String, String> properties) {
		String base64 = properties.get(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY);
		if(base64 == null) {
			log.info("accountKeyPair not found in database");

			//It really sucks that this is synchronous(could throw into future pool next time)
			XFuture<AcmeInfo> future = acmeClient.fetchRemoteInfo();

			return future.thenApply((info) -> {
				return Actions.renderThis(
						"menu", menuCreator.getMenu(), 
						"agreement", info.getTermsOfServiceUri()+"", 
						"website", info.getWebsite(),
						"email", null
						);
			});
		}

		log.info("accountKeyPair found in database.  redirecting to step 2");

		return XFuture.completedFuture(Actions.redirect(InstallSslCertRouteId.STEP2));
	}

	public XFuture<Redirect> postStartSslInstall(String email) {

		log.info("create key pair");
		KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);
		log.info("done creating key pair");
		
		try (StringWriter writer = new StringWriter()) {
			KeyPairUtils.writeKeyPair(accountKeyPair, writer);
			log.info("done marshalling keypair to string");

			Map<String, String> properties = new HashMap<>();
			properties.put(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY, writer.toString());
			properties.put(EMAIL, email);
			return this.storage.save(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY, properties)
				.thenApply( (v) -> {
					log.info("done saving, redirecting to step2");
					return Actions.redirect(InstallSslCertRouteId.STEP2);
				});
			
		} catch(IOException e) {
                        XFuture future = new XFuture();
                        future.completeExceptionally(e);
                        return future;
		}
	}

	public XFuture<Action> step2() {
		XFuture<Map<String, String>> read = this.storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY);
		return read.thenApply((props) -> decideStep2(props));
	}

	private Action decideStep2(Map<String, String> properties) {
		log.info("read in properties");
		String keyPair = properties.get(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY);
		if(keyPair == null) {
			log.info("keyPair not foudn, redirecting to first step");
			return Actions.redirect(InstallSslCertRouteId.INSTALL_SSL_SETUP);
		}
		
		log.info("rendering step");
		return Actions.renderThis(
				"menu", menuCreator.getMenu(), 
				"keyPair", keyPair,
				"organization", null);
	}

	public XFuture<Redirect> postStep2(String organization) {
		RouterRequest request = Current.request();
		return this.storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY)
				.thenCompose((props) -> process(props, request, organization));
	}
	
	private XFuture<Redirect> process(Map<String, String> props, RouterRequest request, String organization) {
		log.info("read in properties from database");
		String domain = request.domain;
		String accountKeyPairString = props.get(InstallSslCertPlugin.ACCOUNT_KEYPAIR_KEY);
		String email = props.get(EMAIL);

		try {
			KeyPair accountKeyPair = KeyPairUtils.readKeyPair(new StringReader(accountKeyPairString));
			log.info("deserialized keypair");
			return acmeClient.openAccount(email, accountKeyPair)
				.thenCompose((url) -> saveUrlAndProcessOrder(url, accountKeyPair, email, domain, organization));
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}	
	}
	
	private XFuture<Redirect> saveUrlAndProcessOrder(URL url, KeyPair accountKeyPair, String email, String domain, String organization) {
		return storage.save(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY, URL, url+"")
					.thenCompose((nothing) -> acmeClient.placeOrder(url, accountKeyPair))
					.thenCompose((order) -> createWebPages(order))
					.thenCompose((order) -> acmeClient.finalizeOrder(order, accountKeyPair, email, domain, organization))
					.thenCompose( (cert) -> installCertAllServers(cert))
					.thenApply((nothing) -> Actions.redirect(InstallSslCertRouteId.MAINTAIN_SSL));
	}

	/**
	 * WE ONLY use ONE webpage that renders ALL requests for these tokens and that web page just looks up the token in the database
	 * to see if the page exists and only renders an html page if that page exists 
	 */
	private XFuture<ProxyOrder> createWebPages(ProxyOrder order) {
		List<ProxyAuthorization> authorizations = order.getAuthorizations();
		Map<String, String> properties = new HashMap<>();
		for(ProxyAuthorization auth : authorizations) {
			log.info("process domain="+auth.getDomain()+" expires="+auth.getExpires()+" status="+auth.getStatus()+" else="+auth.getLocation());
			Instant expires = auth.getExpires();
			String dateTime = expires.toString();
			String domain = auth.getDomain();
			String authContent = auth.getAuthContent();
			String value = authContent+"---"+domain+"---"+dateTime;
			String token = auth.getToken();
			log.info("putting token in map="+token+" value="+value);
			properties.put(token, value);
		}
		
		return storage.save(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY, properties)
				.thenApply((nothing) -> order);
	}
	
	private XFuture<Void> installCertAllServers(CertAndSigningRequest certInfo) {
		List<X509Certificate> certChain = certInfo.getCertChain();

		Map<String, String> props = new HashMap<>();
		try {
			props.put(InstallSslCertPlugin.CSR, certInfo.getCsr());

			for(int i = 0; i < certChain.size(); i++) {
				X509Certificate cert = certChain.get(i);
				String certString = Base64.encode(cert.getEncoded());
				props.put(InstallSslCertPlugin.CERT_CHAIN_PREFIX, certString);
			}
		
			return storage.save(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY, props);
		} catch (CertificateEncodingException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	public XFuture<Render> renderToken(String token) {
		Current.getContext().addModifyResponse((http2Response) -> modifyResponse(http2Response));
		XFuture<Map<String, String>> future = storage.read(InstallSslCertPlugin.PLUGIN_PROPERTIES_KEY);
		return future.thenApply((props) -> {
			String result = props.get(token);
			log.info("token="+token+" value="+result);
			if(result == null)
				throw new NotFoundException();
			
			int index = result.indexOf("---");
			String authContent = result.substring(0, index);
			//check token exists in database
			return Actions.renderThis("authContent", authContent);
		});
	}
	
	private Object modifyResponse(Object http2Response) {
		Http2Response resp = (Http2Response) http2Response;
		Http2Header header = resp.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_TYPE);
		header.setValue("text/plain");
		return resp;
	}

	public Render maintainSsl() {
		return Actions.renderThis();
	}
}
