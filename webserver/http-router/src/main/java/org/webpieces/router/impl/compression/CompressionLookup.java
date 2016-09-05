package org.webpieces.router.impl.compression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

@Singleton
public class CompressionLookup {

	private CompressionDecider toCompressOrNotToCompress;
	private Map<String, Compression> compressions = new HashMap<>();

	@Inject
	public CompressionLookup(GzipCompression gzipCompression, CompressionDecider toCompressOrNotToCompress) {
		this.toCompressOrNotToCompress = toCompressOrNotToCompress;
		compressions.put(gzipCompression.getCompressionType(), gzipCompression);
	}
	
	public Compression createCompressionStream(List<String> encodings, String extension, MimeTypeResult mimeType) {
		if(!toCompressOrNotToCompress.shouldCompress(encodings, extension, mimeType)) {
			return null;
		}
		
		Compression compression = null;
		for(String type : encodings) {
			compression = compressions.get(type);
			if(compression != null)
				break;
		}
		return compression;
	}

}
