package org.webpieces.router.impl.compression;

import java.util.List;

import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class CompressionDecider {

	public boolean shouldCompress(List<String> encodings, String extension, MimeTypeResult mimeType) {
		if(mimeType.mime.startsWith("text"))
			return true;
		else if(mimeType.mime.startsWith("application/json"))
			return true;
		else if(mimeType.mime.startsWith("application/javascript"))
			return true;
		
		return false;
	}
	
}
