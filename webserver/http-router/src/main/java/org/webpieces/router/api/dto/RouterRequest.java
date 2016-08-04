package org.webpieces.router.api.dto;

import java.util.HashMap;
import java.util.Map;

public class RouterRequest {

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
	
	//Fill this in with the query parameters in the url (ie. the <pre> ?var1=xxx&var2=yyyy&var1=www&var3=999</pre>
	/**
	 * 
	 * 
	 * Let's not let this contain stuff from the path such as /user/{id}/account/{account}.  The
	 * library will parse the path in the request to get that information and NOT put it in this Map so
	 * the app developer can grab all 3 different cases uniquely if they need to
	 */
	public Map<String, String[]> queryParams = new HashMap<>();
	
	/**
	 * this will be the multi-part form
	 * upload with fields such as user.id, user.name, user.email, user.address, etc. etc.
	 */
	public Map<String, String[]> multiPartFields = new HashMap<>();
	
	/**
	 * 
	 */
	public Map<String, String[]> urlPathParams = new HashMap<>();

	@Override
	public String toString() {
		return "RouterRequest [isHttps=" + isHttps + ", \nisSendAheadNextResponses=" + isSendAheadNextResponses
				+ ", \nrelativePath=" + relativePath + ", \ndomain=" + domain + ", \nmethod=" + method + ", \nqueryParams=\n"
				+ queryParams + ", \nmultiPartFields=\n" + multiPartFields + ", \nurlPathParams=\n" + urlPathParams + "]";
	}
	
}
