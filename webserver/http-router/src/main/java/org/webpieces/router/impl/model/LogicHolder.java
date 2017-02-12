package org.webpieces.router.impl.model;

import java.nio.charset.Charset;

import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.ControllerLoader;

public class LogicHolder {

	private ReverseRoutes reverseRoutes;
	private ControllerLoader finder;
	private Charset urlEncoding;

	public LogicHolder(ReverseRoutes reverseRoutes, 
			ControllerLoader finder, Charset urlEncoding) {
				this.reverseRoutes = reverseRoutes;
				this.finder = finder;
				this.urlEncoding = urlEncoding;
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
	
}
