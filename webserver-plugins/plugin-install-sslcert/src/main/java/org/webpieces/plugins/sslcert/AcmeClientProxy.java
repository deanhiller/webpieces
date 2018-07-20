package org.webpieces.plugins.sslcert;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
import org.webpieces.util.futures.CompletableFutureCollector;

public class AcmeClientProxy {

	//TODO: Put the remote request INTO a different pool to not hold up the webserver main
	//threadpool so only synchronous requests will hold up synchronous requests
	public CompletableFuture<AcmeInfo> fetchRemoteInfo() {
		try {
			Session session = new Session("acme://letsencrypt.org/staging");
			Metadata metadata = session.getMetadata();
			URI uri = metadata.getTermsOfService();
			URL website = metadata.getWebsite();
			
			return CompletableFuture.completedFuture(new AcmeInfo(metadata, uri, website));
		} catch(AcmeException e) {
			throw new RuntimeException(e);
		}
	}

	//TODO: Put the remote request INTO a different pool to not hold up the webserver main
	//threadpool so only synchronous requests will hold up synchronous requests
	public CompletableFuture<URL> openAccount(String email, KeyPair accountKeyPair) {
		try {
			Session session = new Session("acme://letsencrypt.org/staging");
	
			Account account = new AccountBuilder()
						.addContact("mailto:"+email)
						.agreeToTermsOfService()
						.useKeyPair(accountKeyPair)
						.create(session);
	
			URL location = account.getLocation();
			
			return CompletableFuture.completedFuture(location);
		} catch (AcmeException e) {
			throw new RuntimeException(e);
		}
	}
	//TODO: Put the remote request INTO a different pool to not hold up the webserver main
	//threadpool so only synchronous requests will hold up synchronous requests
	public CompletableFuture<CertAndSigningRequest> placeOrder(URL accountUrl, KeyPair accountKeyPair, String email, String domain, String organization) {
		try {
			Session session = new Session("acme://letsencrypt.org/staging");
			Login login = session.login(accountUrl, accountKeyPair);
			Account account = login.getAccount();
	
			String domainTemp = "something.com";
			Order order = account.newOrder()
				.domain(domainTemp)
				.create();
			
			List<AuthAndChallenge> challenges = processAuths(order);

			Stream<CompletableFuture<Void>> futures = challenges.stream().map( (challenge) -> processChallenge(challenge));
			CompletableFuture<List<Void>> results = futures.collect(CompletableFutureCollector.allOf());
			return results.thenCompose( (nothing) -> finalizeOrder(order, domain, organization, accountKeyPair) );
			
		} catch (AcmeException e) {
			throw new RuntimeException(e);
		}
	}
	
	private CompletableFuture<CertAndSigningRequest> finalizeOrder(Order order, String domain, String organization, KeyPair accountKeyPair) {
		try {
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
			
			Certificate cert = order.getCertificate();
			
			return CompletableFuture.completedFuture(new CertAndSigningRequest(csrb, cert));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (AcmeException e) {
			throw new RuntimeException(e);
		}
	}

	private CompletableFuture<Void> processChallenge(AuthAndChallenge challenge) {
		
		//TODO: We should DUMP EACH Challenge into it's own Runnable so we do challenges in parallel
		try {
			challenge.getChallenge().trigger();
			
			Authorization auth = challenge.getAuth();
			while (auth.getStatus() != Status.VALID) {
				//HACK for now....modify to catch RetryAfterException and schedule future
				Thread.sleep(3000L);
				auth.update();
			}
			
			return null;
		} catch (AcmeException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private List<AuthAndChallenge> processAuths(Order order) {
		List<AuthAndChallenge> challenges = new ArrayList<>();
		for(Authorization auth : order.getAuthorizations()) {
			Status status = auth.getStatus();
			if(status == Status.PENDING) {
				Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
				challenges.add(new AuthAndChallenge(auth, challenge));
			} else
				throw new ChallengeInBadStateException("challenge in bad state="+auth.getJSON());
		}
		return challenges;
	}
	
}
