package org.webpieces.plugins.sslcert;

import java.util.List;

import org.webpieces.router.api.routing.BackendPlugin;
import org.webpieces.router.api.routing.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class InstallSslCertPlugin implements BackendPlugin {

	public static final String PLUGIN_PROPERTIES_KEY = "org.webpieces.plugins.sslcert";
	public static final String CERT_CHAIN_PREFIX = "certChain";
	public static final String ACCOUNT_KEYPAIR_KEY = "accountKeyPair";
	public static final String CSR = "CSR";

	public InstallSslCertPlugin() {
		super();
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new InstallSslCertModule());
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new InstallSslCertRoutes());
	}

}
