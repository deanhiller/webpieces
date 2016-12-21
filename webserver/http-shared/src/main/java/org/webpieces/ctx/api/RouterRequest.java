package org.webpieces.ctx.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RouterRequest {

	/**
	 * Request in it's original form, but should not be relied upon really
	 */
	public Object orginalRequest;
	
	public boolean isHttps;
	//true if http2 so we respond with not just the response but all other responses that the client
	//will request next as well....
	public boolean isSendAheadNextResponses;
	
	public String relativePath;
	/**
	 * This comes from sniServerName in the case of https or Host header in case of http but even
	 * Host in https should match
	 */
	public String domain;
	public HttpMethod method;

	public Map<String, RouterCookie> cookies = new HashMap<>();
	
	/**
	 * Let's not let this contain stuff from the path such as /user/{id}/account/{account}.  The
	 * library will parse the path in the request to get that information and NOT put it in this Map so
	 * the app developer can grab all 3 different cases uniquely if they need to
	 */
	public Map<String, List<String>> queryParams = new HashMap<>();
	
	/**
	 * this will be the multi-part form
	 * upload with fields such as user.id, user.name, user.email, user.address, etc. etc.
	 */
	public Map<String, String> multiPartFields = new HashMap<>();

	public List<Locale> preferredLocales = new ArrayList<>();
	
	public List<AcceptMediaType> acceptedTypes = new ArrayList<>();

	public String referrer;

	public List<String> encodings = new ArrayList<>();
	
	@Override
	public String toString() {
		return "RouterRequest [isHttps=" + isHttps + ", \nisSendAheadNextResponses=" + isSendAheadNextResponses
				+ ", \nrelativePath=" + relativePath + ", \ndomain=" + domain + ", \nmethod=" + method + ", \nqueryParams=\n"
				+ queryParams + ", \nmultiPartFields=\n" + multiPartFields + "\n"
				+ "cookies="+cookies+"\n]";
	}
	
}
