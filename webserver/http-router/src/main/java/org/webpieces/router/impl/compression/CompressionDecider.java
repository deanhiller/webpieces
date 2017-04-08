package org.webpieces.router.impl.compression;

import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class CompressionDecider {
	private static final Logger log = LoggerFactory.getLogger(CompressionDecider.class);

	public boolean isCompressableType(String extension, MimeTypeResult mimeType) {
		if(mimeType.mime.startsWith("text"))
			return true;
		else if(mimeType.mime.startsWith("application/json"))
			return true;
		else if(mimeType.mime.startsWith("application/javascript"))
			return true;
		
		log.info("skipping compression for file due to extension="+extension+" and mimetype="+mimeType);
		return false;
	}
	
}
