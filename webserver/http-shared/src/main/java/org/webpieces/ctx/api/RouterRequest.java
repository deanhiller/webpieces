package org.webpieces.ctx.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.webpieces.data.api.DataWrapper;

/**
 * The format of this class caters to the router so it has everything the router uses and no more.  It
 * also keeps the router independent of any http1.1 or http2 stack as well.  The most important piece is
 * it is really self documenting as to what the router needs and where in the router it uses that info
 * 
 * @author dhiller
 *
 */
public class RouterRequest {

	/**
	 * Request in it's original form, but should not be relied upon really at all.  It is here 
	 * for clients just in case they need more detail about the request
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
	
	/**
	 * The port to redirect to when doing a redirect
	 */
	public int port;

	public HttpMethod method;

	public Map<String, RouterCookie> cookies = new HashMap<>();
	
	/**
	 * Let's not let this contain stuff from the path such as /user/{id}/account/{account}.  The
	 * library will parse the path in the request to get that information and NOT put it in this Map so
	 * the app developer can grab all 3 different cases uniquely if they need to
	 */
	public Map<String, List<String>> queryParams = new HashMap<>();
	
	/**
	 * this will be the multi-part form on a POST request
	 * upload with fields such as user.id, user.name, user.email, user.address, etc. etc.
	 * 
	 * In the case of <select multiple>, we need to support selectedRoles=j&selectedRoles=f meaning
	 * we need to support String to array lookup
	 */
	public Map<String, List<String>> multiPartFields = new HashMap<>();

	public List<Locale> preferredLocales = new ArrayList<>();
	
	public List<AcceptMediaType> acceptedTypes = new ArrayList<>();

	public String referrer;

	/**
	 * what the client will accept for encodings(typically compression encodings)
	 */
	public List<String> encodings = new ArrayList<>();
	
	public boolean isAjaxRequest;
	
	public Integer contentLengthHeaderValue;

	public String contentTypeHeaderValue;

	public DataWrapper body;
	
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
	
	@Override
	public String toString() {
		return "RouterRequest [isHttps=" + isHttps + ", \nisSendAheadNextResponses=" + isSendAheadNextResponses
				+ ", \nrelativePath=" + relativePath + ", \ndomain=" + domain + ", \nmethod=" + method + ", isAjaxRequest="+isAjaxRequest+"\nqueryParams=\n"
				+ queryParams + ", \nmultiPartFields=\n" + multiPartFields + "\n"
				+ "cookies="+cookies+"\n]";
	}
	
}
