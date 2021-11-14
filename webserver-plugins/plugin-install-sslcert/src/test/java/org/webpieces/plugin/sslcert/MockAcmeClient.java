package org.webpieces.plugin.sslcert;

import java.net.URL;
import java.security.KeyPair;
import org.webpieces.util.futures.XFuture;

import org.webpieces.plugin.secure.sslcert.CertAndSigningRequest;
import org.webpieces.plugin.secure.sslcert.acme.AcmeClientProxy;
import org.webpieces.plugin.secure.sslcert.acme.AcmeInfo;
import org.webpieces.plugin.secure.sslcert.acme.ProxyOrder;

public class MockAcmeClient extends AcmeClientProxy {

	private XFuture<AcmeInfo> remoteInfo;
	private XFuture<URL> url;
	private XFuture<ProxyOrder> order;
	private XFuture<CertAndSigningRequest> certAndSigningRequest;

	public MockAcmeClient() {
		super(null);
	}

	@Override
	public XFuture<AcmeInfo> fetchRemoteInfo() {
		return remoteInfo;
	}

	@Override
	public XFuture<URL> openAccount(String email, KeyPair accountKeyPair) {
		return url;
	}

	@Override
	public XFuture<ProxyOrder> placeOrder(URL accountUrl, KeyPair accountKeyPair) {
		return order;
	}

	@Override
	public XFuture<CertAndSigningRequest> finalizeOrder(ProxyOrder order, KeyPair accountKeyPair,
			String email, String domain, String organization) {
		return certAndSigningRequest;
	}
	
	public void setRemoteInfo(XFuture<AcmeInfo> remoteInfo) {
		this.remoteInfo = remoteInfo;
	}
	
	public void setOpenAccount(XFuture<URL> url) {
		this.url = url;
	}
	
	public void setProxyOrder(XFuture<ProxyOrder> order) {
		this.order = order;
	}
	
	public void setCertAndSigningRequest(XFuture<CertAndSigningRequest> certAndSigningRequest) {
		this.certAndSigningRequest = certAndSigningRequest;
	}
}
