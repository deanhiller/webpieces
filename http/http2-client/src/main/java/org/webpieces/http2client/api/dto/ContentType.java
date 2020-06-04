package org.webpieces.http2client.api.dto;

import java.util.HashMap;
import java.util.Map;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

public class ContentType {

	private String type;
	private String subtype;
	private Map<String, String> params = new HashMap<>();

	public ContentType(String type, String subtype) {
		this.type = type;
		this.subtype = subtype;
	}

	public ContentType(String type, String subtype, Map<String, String> params) {
		this.type = type;
		this.subtype = subtype;
		this.params = params;
	}

	public String getType() {
		return type;
	}

	public String getSubtype() {
		return subtype;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getParams(String name) {
		return params.get(name);
	}
	
	public String getCharSet() {
		return params.get("charset");
	}
	
	public static ContentType parse(Http2Header header) {
		if(header.getKnownName() != Http2HeaderName.CONTENT_TYPE)
			throw new IllegalArgumentException("Can only parse content type headers");
		
		String value = header.getValue();
		int firstSemiColon = value.indexOf(";");

		int index = value.indexOf("/");
		String type = value.substring(0, index).trim();
		if(firstSemiColon < 0) {
			String subtype = value.substring(index+1).trim();
			return new ContentType(type, subtype);
		}

		//cheating here as a quoted string could have a ; in it, but that is very unlikely but we can fix later
		//if we need
		String theRest = value.substring(firstSemiColon+1);
		String subtype = value.substring(index+1, firstSemiColon).trim();
		String[] split = theRest.split(";");
		
		Map<String, String> params = new HashMap<>();
		for(String s : split) {
			String[] nameValue = s.split("=");
			params.put(nameValue[0].trim(), nameValue[1].trim());
		}
		
		ContentType ct = new ContentType(type, subtype, params);
		return ct;
	}
	
	
}
