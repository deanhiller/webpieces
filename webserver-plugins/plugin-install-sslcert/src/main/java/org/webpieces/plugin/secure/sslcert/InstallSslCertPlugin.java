package org.webpieces.plugin.secure.sslcert;

import java.util.List;

import org.webpieces.router.api.plugins.BackendPlugin;
import org.webpieces.router.api.routes.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class InstallSslCertPlugin implements BackendPlugin {

	public static final String PLUGIN_PROPERTIES_KEY = "org.webpieces.plugin.sslcert";
	public static final String CERT_CHAIN_PREFIX = "certChain";
	public static final String ACCOUNT_KEYPAIR_KEY = "accountKeyPair";
	public static final String CSR = "CSR";
	private InstallSslCertConfig config;

	public InstallSslCertPlugin(InstallSslCertConfig config) {
		super();
		this.config = config;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new InstallSslCertModule(config));
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new InstallSslCertRoutes());
	}

}
