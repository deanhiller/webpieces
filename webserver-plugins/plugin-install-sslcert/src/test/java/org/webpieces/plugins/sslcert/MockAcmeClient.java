package org.webpieces.plugins.sslcert;

import java.net.URL;
import java.security.KeyPair;
import java.util.concurrent.CompletableFuture;

import org.webpieces.plugins.sslcert.acme.AcmeClientProxy;
import org.webpieces.plugins.sslcert.acme.AcmeInfo;
import org.webpieces.plugins.sslcert.acme.ProxyOrder;

public class MockAcmeClient extends AcmeClientProxy {

	private CompletableFuture<AcmeInfo> remoteInfo;
	private CompletableFuture<URL> url;
	private CompletableFuture<ProxyOrder> order;
	private CompletableFuture<CertAndSigningRequest> certAndSigningRequest;

	public MockAcmeClient() {
		super(null);
	}

	@Override
	public CompletableFuture<AcmeInfo> fetchRemoteInfo() {
		return remoteInfo;
	}

	@Override
	public CompletableFuture<URL> openAccount(String email, KeyPair accountKeyPair) {
		return url;
	}

	@Override
	public CompletableFuture<ProxyOrder> placeOrder(URL accountUrl, KeyPair accountKeyPair) {
		return order;
	}

	@Override
	public CompletableFuture<CertAndSigningRequest> finalizeOrder(ProxyOrder order, KeyPair accountKeyPair,
			String email, String domain, String organization) {
		return certAndSigningRequest;
	}
	
	public void setRemoteInfo(CompletableFuture<AcmeInfo> remoteInfo) {
		this.remoteInfo = remoteInfo;
	}
	
	public void setOpenAccount(CompletableFuture<URL> url) {
		this.url = url;
	}
	
	public void setProxyOrder(CompletableFuture<ProxyOrder> order) {
		this.order = order;
	}
	
	public void setCertAndSigningRequest(CompletableFuture<CertAndSigningRequest> certAndSigningRequest) {
		this.certAndSigningRequest = certAndSigningRequest;
	}
}
