package org.webpieces.router.impl.compression;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class CompressionDecider {
	private static final Logger log = LoggerFactory.getLogger(CompressionDecider.class);

	private List<String> compressableTypes = new ArrayList<String>();
	
	public CompressionDecider() {
		compressableTypes.add("text");
		compressableTypes.add("application/json");
		compressableTypes.add("application/javascript");
	}
	
	public boolean isCompressableType(MimeTypeResult mimeType) {
		for(String compressableType : compressableTypes) {
			if(mimeType.mime.startsWith(compressableType))
				return true;
		}
		
		if(log.isTraceEnabled())
			log.trace("skipping compression for file of mimetype="+mimeType+" as does not START with ANY of these="+compressableTypes);
		return false;
	}
	
}
