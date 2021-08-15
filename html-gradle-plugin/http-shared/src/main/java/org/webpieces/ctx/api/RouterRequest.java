package org.webpieces.ctx.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.MDC;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.dto.highlevel.Http2Request;

/**
 * The format of this class caters to the router so it has everything the router uses and no more.  It
 * also keeps the router independent of any http1.1 or http2 stack as well.  The most important piece is
 * it is really self documenting as to what the router needs and where in the router it uses that info
 * 
 * @author dhiller
 *
 */
public class RouterRequest {
/*

    <li>boolean isHttps - true if this request came in over an https socket to the load balancer (or webserver depending on setup)</li>
    <li>boolean isSendAheadNextResponses - In http2, you can send ahead responses to future requests before sending
     the first full response back.  This tells our router that we can pre-emptively send to make websites 
     faster.  (You usually don't need to read this yourself)</li>
    <li>String relativePath - The path for the router to match against excluding query params(you probably won't need this)</li>
    <li>String domain - The domain for the router to match against(you probably won't need this)</li>
    <li>int port - This is set to port 80 for http, 443 for https unless you configure the server for different ports</li>
    <li>HttpMethod method - The GET or POST, or PUT, basically the HttpMethod</li>
    <li>*[Map<String, RouterCookie> cookies]* - A map of cookie names to cookies</li>
    <li>*[Map<String, List<String>> queryParams]* - A map of query param names to query params like http://domain.com/page.html?query1=xxx&query1=yyy&query2=zzz
        <ol><li>Note that the spec allows a query param name to be listed multiple times</li></ol>
    </li>
    <li>*[Map<String, List<String>> multiPartFields]* - A map of key values that came from a POST from a form.  Like queryParams, it can contain a param name listed multiple times</li>
    <li></li>
    <li></li>
 */
	/**
	 * Not used by router, and not usually needed by Controllers
	 * 
	 * This is here only as a just in case the controllers want access to headers in the Http request.
	 * 
	 * The webpieces router is written so it can be part of any webserver and as such, the original request
	 * depends on the client webserver.  If using webpieces webserver, this will be the Http2Request object.
	 * 
	 * We did not want any dependencies on http1_1 parser nor http parser in the http router such that he
	 * is a re-usable webpiece for quickly creating a webserver if desired.This ensures the
	 * dependences of router are very light.  
	 * 
	 * see the picture of dependencies in the design at http://localhost:8080/@documentation in
	 * development mode. 
	 */
	public Http2Request originalRequest;

	public Http2Headers trailingHeaders;

	/**
	 * Request state you can throw into a map to use anywhere on request path
	 */
	public Map<String, Object> requestState = new HashMap<>();
	
	/**
	 * Used by router, not really needed by Controllers
	 * 
	 * This is true if the socket we received the request over was "secure" sort of.  If you terminate https
	 * into a firewall and then do http, this will be true as well even though you are serving it up as http
	 * internally.  You only need to configure that correctly in your webserver startup.
	 * 
	 * The router uses this in matching https routes.
	 */
	public boolean isHttps;
	
	/**
	 * Also, used by the router.  This is used in determining if it came in over the backend http server port
	 * which is typically only exposed on the companies internal network and not exposed externally.
	 */
	public boolean isBackendRequest;

	/**
	 * Use by router, not really needed by Controllers
	 * 
	 * In http2, when a webserver receives a request for say index.html, the webserver can automatically know that
	 * the browser will most likely request some *.css and *.js files and the webserver can pre-emptively send
	 * those files before sending the response to the index.html request.  IF the request was from an http2 client
	 * that can accept pre-emptive responses, this will be set to true.
	 */
	public boolean isSendAheadNextResponses;
	
	/**
	 * Used by the router(for matching a route).  Should not really be needed by the Controllers
	 *
	 * This is the url path after the domain of the request.  For example, for a request
	 * to http://domain.com:7777/mypath/to/file, this will be set to /mypath/to/file 
	 */
	public String relativePath;
	
	/**
	 * Used by the router(for matching a domain route).  Should not really be needed by the Controllers
	 * 
	 * This comes from sniServerName in the case of https or Host header in case of http but even
	 * Host header in https should match sniServerName but sniServerName is more secure.  For example,
	 * if you send a request to http://domain.com:7777/mypath/to/file, the Host header or the sniServerName
	 * is generally set to domain.com so this will be 'domain.com'
	 */
	public String domain;
	
	/**
	 * Used by router in some cases to redirect to same port as before that request came from.  Not needed by Controllers
	 * 
	 * This is the port the socket was opened on, OR the port in the host header.  ONLY use isHttps for security as this
	 * port, the client can muck with by changing his host header but generally they don't.  The issue here is a request
	 * goint to port 80 load balancer may then go to a webserver port 66534 and in that case, the Host Header is 
	 * used which the load balancer should set port 80 for that use case(though it is usually already set correctly from
	 * the client).
	 */
	public int port;

	/**
	 * Used by router(for matching POST, GET, etc).  Not needed by Controllers generally
	 * 
	 * This is the HttpMethod as in GET, POST, PUT, DELETE, etc. etc.
	 */
	public HttpMethod method;

	/**
	 * Used by router(to create the Flash, Validation, and Session scopes in RequestContext).  Not generally needed by the Controllers
	 * 
	 * This is a Map of names to cookies for the request.
	 */
	public Map<String, RouterCookie> cookies = new HashMap<>();
	
	/**
	 * Used by the router to bind queryParams to controller method variables/beans.  Not generally needed by Controller, but can be used by Controller.
	 * 
	 * Query params are the variables after the ? in a url like http://domain.com/index.html?queryParam1=xxx&queryParam1=yyy
	 * 
	 * This DOES not contain stuff from the path such as /user/{id}/account/{account}.  The
	 * library will parse the path in the request to get that information and NOT put it in this Map so
	 * the app developer can grab all 3 different cases uniquely if they need to.
	 * 
	 * In the specification, it allows a single variable to be used multiple times so for example
	 * http://domain.com/index.html?queryParam1=xxx&queryParam1=yyy&queryParam2=zzz
	 * 
	 * In this case, you will end up with queryParam1 = List("xxx", "yyy") in the Map
	 * as well as queryParam2=List("zzz")
	 * 
	 * Of course, if you have a method in your controller like so
	 * 
	 * public Action someControllerMethod(List<String> queryParam1, String queryParam2)
	 * 
	 * we graciously detect queryParam1 as a list and fill it in as such for you and graciously bind queryParam2 for
	 * you or blow up if queryParam2 end up being too many in the url.  You can thank us later!!!!  Anytime, we
	 * always appreciate a simple thank you note!!!
	 * 
	 * A big NOTE is that queryParams, multiPartFields and pathParams(as in /account/{id}/user/{name}) can all
	 * conflict in naming.  In that case, queryParams is applied first, multiPartFields is applied after that
	 * overwriting any of the same name, and finally pathParams is then applied overwriting any of the same
	 * name
	 */
	public Map<String, List<String>> queryParams = new HashMap<>();
	
	/**
	 * Used by the router to bind form fields to controller method variables/beans.  Not generally needed by Controller, but can be used by Controller.
	 * 
	 * this will be the multi-part form on a POST request
	 * upload with fields such as user.id, user.name, user.email, user.address, etc. etc.
	 * 
	 * In the case of <select multiple>, we need to support selectedRoles=j&selectedRoles=f meaning
	 * we need to support String to array lookup just like queryParams(FUCKING ANNOYING, right???).  Again, like queryParams
	 * we can detect List<String> vs. String for you in the beans and method variables.
	 * 
	 * A big NOTE is that queryParams, multiPartFields and pathParams(as in /account/{id}/user/{name}) can all
	 * conflict in naming.  In that case, queryParams is applied first, multiPartFields is applied after that
	 * overwriting any of the same name, and finally pathParams is then applied overwriting any of the same
	 * name
	 */
	public Map<String, List<String>> multiPartFields = new HashMap<>();

	/**
	 * Browsers will send a list of preferred languages based typically on the US on installation.  In Chrome for instance,
	 * I can add Chinese and put it above English to say Chinese preferred and if not available, add english.  That list
	 * is parsed and set here for you.
	 */
	public List<Locale> preferredLocales = new ArrayList<>();
	
	/**
	 * Used somewhat by the router.  you may want to use this in the controller but ideally, we should keep adding features.
	 * 
	 * This is essentially crappy naming on my part and is the Accept headers value which is the Mime Types supported
	 * in order of preference.
	 */
	public List<AcceptMediaType> acceptedTypes = new ArrayList<>();

	/**
	 * Currently used by the LoginFilter(not technically part of the router but client code we wrote for you).  Could be used by your filters.
	 * 
	 * This is the referred in a redirect scenario.  ie. it's the page you came from such that you can go back to it after the
	 * user logs in.  ie. user hit secure page, login filter redirects to login, user logs in and then uses referrer to redirect
	 * back to the page the user wanted to begin with.
	 */
	public String referrer;

	/**
	 * Used by router to see what compressed files can be sent back or not. 
	 * 
	 * what the client will accept for encodings(typically compression encodings)
	 */
	public List<String> encodings = new ArrayList<>();
	
	/**
	 * Whether or not this was an ajax request.  LoginFilter uses as 303 redirect does not work as a response
	 * to an ajax request so the login filter responds with an ajax redirect( http code 287 as a hack!!!).  
	 * Damn ajax but it fixed our post ajax, redirect to login issue cleanly instead of other webservers that
	 * pop-up error with a login link(that is kind of dumb when you can just render the login page)
	 */
	public boolean isAjaxRequest;
	
	/**
	 * Used by router to determine if we parse the whole body or stream the body in and to know how many bytes
	 * are coming in. 
	 * 
	 * This is the value of the CONTENT_LENGTH header if it exists.  If it doesn't exist, this is null.
	 * 
	 * Only set if there is a body.
	 */
	public Integer contentLengthHeaderValue;

	/**
	 * Set to the CONTENT_TYPE header
	 */
	public ContentType contentTypeHeaderValue;

	/**
	 * This is the body that is set ONLY IF this is not a Streaming path.
	 * ie. If you added a Content-Type route using ContentTypeRouteBuilder which currently only does streaming routes
	 * OR called an addStreamingRoute() instead of addRoute()
	 *
	 * This happens to be filled in later then all the other data (I kind of hate that but it's working well anyways)
	 */
	public DataWrapper body;

	public UriInfo requestUri;

    public void putMultipart(String key, String value) {
		List<String> values = new ArrayList<>();
		values.add(value);
		multiPartFields.put(key, values);
	}
	
	public String getSingleMultipart(String key) {
		List<String> list = multiPartFields.get(key);
		if(list.size() > 1)
			throw new IllegalStateException("too many values");
		else if(list.size() == 1)
			return list.get(0);
		return null;
	}

	public <T> T getRequestState(final Object key) {
		return (T)requestState.get(key.toString());
	}

	public void setRequestState(final Object key, final Object value) {
    	setRequestState(key, value, false);
	}

	public void setRequestState(final Object key, final Object value, final boolean addToMDC) {
		requestState.put(key.toString(), value);
		if(addToMDC) {
			MDC.put(key.toString(), String.valueOf(value));
		}
	}

	
	@Override
	public String toString() {
		return "\nRouterRequest [\nisHttps=" + isHttps + ", \nisBackendRequest=" + isBackendRequest
				+ ", \nisSendAheadNextResponses=" + isSendAheadNextResponses
				+ ", \nrelativePath=" + relativePath + ", \ndomain=" + domain + ", \nmethod=" + method + ", isAjaxRequest="+isAjaxRequest+"\nqueryParams=\n"
				+ queryParams + ", \nmultiPartFields=\n" + multiPartFields + "\n"
				+ "cookies="+cookies+"\n]";
	}
	
}
