package org.webpieces.plugin.secure.sslcert.acme;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Metadata;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.plugin.secure.sslcert.CertAndSigningRequest;
import org.webpieces.plugin.secure.sslcert.ChallengeInBadStateException;
import org.webpieces.plugin.secure.sslcert.InstallSslCertConfig;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.futures.CompletableFutureCollector;

/**
 * Trying to create a new API since AcmeClientProxy's API is HORRIBLE for testing.  For a good API that someone can mock, it 
 * must meet these requirements
 * 
 * * All requests passed into a method must be constructable by the client
 * * All requests passed into a method should have no methods other than get/set (ie. no business logic)
 * * All responses returned from a method should be constructable by a test
 * * All responses returned from a method should have no methods other than get/set (ie. no business logic)
 * 
 * @author dhiller
 *
 */
public class AcmeClientProxy {

	private static final Logger log = LoggerFactory.getLogger(AcmeClientProxy.class);
	private InstallSslCertConfig config;

	@Inject
	public AcmeClientProxy(InstallSslCertConfig config) {
		this.config = config;
	}
	
	//TODO: Put the remote request INTO a different pool to not hold up the webserver main
	//threadpool so only synchronous requests will hold up synchronous requests
	public CompletableFuture<AcmeInfo> fetchRemoteInfo() {
		try {
			Session session = new Session(config.getProviderLocation());
			Metadata metadata = session.getMetadata();
			URI termsOfServiceUri = metadata.getTermsOfService();
			URL website = metadata.getWebsite();
			
			return CompletableFuture.completedFuture(new AcmeInfo(termsOfServiceUri, website));
		} catch(AcmeException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	//TODO: Put the remote request INTO a different pool to not hold up the webserver main
	//threadpool so only synchronous requests will hold up synchronous requests
	public CompletableFuture<URL> openAccount(String email, KeyPair accountKeyPair) {
		try {
			log.info("open account");
			Session session = new Session("acme://letsencrypt.org/staging");
	
			Account account = new AccountBuilder()
						.addContact("mailto:"+email)
						.agreeToTermsOfService()
						.useKeyPair(accountKeyPair)
						.create(session);
	
			URL location = account.getLocation();
			log.info("account location="+location);
			return CompletableFuture.completedFuture(location);
		} catch (AcmeException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	//TODO: Put the remote request INTO a different pool to not hold up the webserver main
	//threadpool so only synchronous requests will hold up synchronous requests
	/**
	 * @return The list of challenges with tokens to create webpages for that remote end will call to verify we own the domain
	 */
	public CompletableFuture<ProxyOrder> placeOrder(URL accountUrl, KeyPair accountKeyPair) {
		try {
			log.info("reestablish account from location="+accountUrl+" and keypair");
			Session session = new Session("acme://letsencrypt.org/staging");
			Login login = session.login(accountUrl, accountKeyPair);
			Account account = login.getAccount();
	
			log.info("create an order");
			String domainTemp = "something.com";
			Order order = account.newOrder()
				.domain(domainTemp)
				.create();
			
			checkAuthStatii(order);
			
			List<ProxyAuthorization> auths = new ArrayList<>();
			for(Authorization auth : order.getAuthorizations())
				auths.add(new ProxyAuthorization(auth));
			
			return CompletableFuture.completedFuture(new ProxyOrder(order, auths));
		} catch (AcmeException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public CompletableFuture<CertAndSigningRequest> finalizeOrder(ProxyOrder order, KeyPair accountKeyPair, String email, String domain, String organization) {
		Stream<Authorization> stream = order.getOrder().getAuthorizations().stream();
	
		Stream<CompletableFuture<Void>> futures = stream.map( (auth) -> processChallenge(auth));
		CompletableFuture<List<Void>> results = futures.collect(CompletableFutureCollector.allOf());
		return results.thenCompose( (nothing) -> finalizeOrder(order, domain, organization, accountKeyPair) );
	}
	
	private CompletableFuture<CertAndSigningRequest> finalizeOrder(ProxyOrder proxyOrder, String domain, String organization, KeyPair accountKeyPair) {
		try (StringWriter writer = new StringWriter()) {
			Order order = proxyOrder.getOrder();
			
			CSRBuilder csrb = new CSRBuilder();
			csrb.addDomain(domain);
			csrb.setOrganization(organization);
			csrb.sign(accountKeyPair);
	
			byte[] csr = csrb.getEncoded();
			
			//NEED to store the csr as base64 into the DB!!!
			
			order.execute(csr);
			
			while (order.getStatus() != Status.VALID) {
				  Thread.sleep(3000L);
				  order.update();
			}

			csrb.write(writer);
			
			Certificate cert = order.getCertificate();
			
			return CompletableFuture.completedFuture(new CertAndSigningRequest(writer.toString(), cert.getCertificateChain()));
		} catch (AcmeException | IOException | InterruptedException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	private CompletableFuture<Void> processChallenge(Authorization auth) {
		
		//TODO: We should DUMP EACH Challenge into it's own Runnable so we do challenges in parallel
		try {
			
			Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
			
			log.info("tell remote end to trigger a call now. status="+auth.getStatus()+" domain="+auth.getIdentifier().getDomain()+" expires="+auth.getExpires());
			challenge.trigger();
			log.info("status after="+auth.getStatus());
			
			while (auth.getStatus() != Status.VALID) {
				//HACK for now....modify to catch RetryAfterException and schedule future
				Thread.sleep(3000L);
				log.info("reupdate status");
				auth.update();
				log.info("updated to status="+auth.getStatus());
			}
			
			return null;
		} catch (AcmeException | InterruptedException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	private void checkAuthStatii(Order order) {
		for(Authorization auth : order.getAuthorizations()) {
			Status status = auth.getStatus();
			log.info("checking auth="+auth.getIdentifier().getDomain()+" status="+status+" location="+auth.getLocation()+" expires="+auth.getExpires());
			if(status != Status.PENDING)
				throw new ChallengeInBadStateException("challenge in bad state="+auth.getJSON());
		}
	}
	
}
