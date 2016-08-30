package org.webpieces.webserver.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class CompressionLookup {

	private Map<String, Compression> compressions = new HashMap<>();
	
	@Inject
	public CompressionLookup(GzipCompression compression) {
		compressions.put("gzip", compression);
	}
	
	public Compression lookup(String type) {
		return compressions.get(type);
	}
	
}
