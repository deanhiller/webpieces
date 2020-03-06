package org.webpieces.router.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.util.cmdline2.Arguments;

import com.google.inject.Injector;

public interface RouterService {

	/**
	 * Needs to be called before start() so that all Arguments are formed from any application modules and plugins.  
	 * The command line help is then dynamic spitting out each read of the command line and whether it's optional or
	 * not.  In this way, adding a plugin or module can change the required command line arguments
	 * 
	 * If you are re-using this router jar, you could pass in a no-op Arguments object BUT dynamic help is very
	 * nice for users
	 */
	void configure(Arguments arguments);
	
	Injector start();

	void stop();

	CompletableFuture<Void> incomingCompleteRequest(RouterRequest req, ResponseStreamer streamer);

	/**
	 * This is exposed as the webserver wires router and templating engine and the templating engine needs a callback to
	 * reverse all routeIds in the html file to actual urls which only the router has knowledge of.  The templating
	 * engine therefor can pass the routeId as well as the arguments into convertToUrl and a url comes back.
	 * 
	 * On top of that, isValidating is for a special test case that makes sure all route ids in all templates actually
	 * exist so we don't deploy with broken links.
	 */
	String convertToUrl(String routeId, Map<String, Object> notUrlEncodedArgs, boolean isValidating);
	
	FileMeta relativeUrlToHash(String urlPath);

	/**
	 * As you run a template, it was passed argument and needs to now how to convert those beans into a 
	 * String.  getting the correct ObjectStringConverter for say a DateTime will convert it to a String
	 */
	<T> ObjectStringConverter<T> getConverterFor(T bean);
	
}
