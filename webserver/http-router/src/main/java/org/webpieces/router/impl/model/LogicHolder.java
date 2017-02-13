package org.webpieces.router.impl.model;

import java.nio.charset.Charset;

import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.ControllerLoader;

import com.google.inject.Injector;

public class LogicHolder {

	private ReverseRoutes reverseRoutes;
	private ControllerLoader finder;
	private Charset urlEncoding;
	private Injector injector;

	public LogicHolder(ReverseRoutes reverseRoutes, 
			ControllerLoader finder, Charset urlEncoding, Injector injector) {
				this.reverseRoutes = reverseRoutes;
				this.finder = finder;
				this.urlEncoding = urlEncoding;
				this.injector = injector;
	}

	public ReverseRoutes getReverseRoutes() {
		return reverseRoutes;
	}

	public ControllerLoader getFinder() {
		return finder;
	}

	public Charset getUrlEncoding() {
		return urlEncoding;
	}

	public Injector getInjector() {
		return injector;
	}

}
