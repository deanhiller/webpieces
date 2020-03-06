package org.webpieces.router.impl.compression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class CompressionDecider {
	private static final Logger log = LoggerFactory.getLogger(CompressionDecider.class);

	public boolean isCompressableType(String extension, MimeTypeResult mimeType) {
		if(mimeType.mime.startsWith("text"))
			return true;
		else if(mimeType.mime.startsWith("application/json"))
			return true;
		else if(mimeType.mime.startsWith("application/javascript"))
			return true;
		
		if(log.isTraceEnabled())
			log.trace("skipping compression for file due to extension="+extension+" and mimetype="+mimeType);
		return false;
	}
	
}
