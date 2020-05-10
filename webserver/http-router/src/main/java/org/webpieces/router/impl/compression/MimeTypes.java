package org.webpieces.router.impl.compression;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.inject.Inject;

import org.webpieces.router.api.RouterConfig;

public class MimeTypes {

	private Properties mimetypes;
	private RouterConfig config;

	@Inject
	public MimeTypes(RouterConfig config) {
		this.config = config;
        InputStream is = MimeTypes.class.getClassLoader().getResourceAsStream("mime-types.properties");
        mimetypes = new Properties();
        try {
			mimetypes.load(is);
		} catch (IOException e) {
			throw new RuntimeException("mime types failed to load", e);
		}
	}
	
    public MimeTypeResult extensionToContentType(String extension, String defaultContentType){
    	if(defaultContentType == null)
    		throw new IllegalArgumentException("default content type must be provided");
    	
        String contentType = mimetypes.getProperty(extension);
        if (contentType == null){
            contentType = defaultContentType;
        }
        return createMimeType(contentType);
    }

	public MimeTypeResult createMimeType(String contentType) {
		if(contentType.contains(";")) {
        	String[] split = contentType.split(";");
        	String encoding = split[1].trim();
        	String[] pair = encoding.split("=");
        	String charSetStr = pair[1];
        	Charset charSet = Charset.forName(charSetStr);
        	return new MimeTypeResult(split[0], charSet);
        } else if(contentType.startsWith("text/")) {
            String mime = contentType + "; charset=" + config.getDefaultResponseBodyEncoding().name().toLowerCase();
            return new MimeTypeResult(mime, config.getDefaultResponseBodyEncoding());
        }
        
        //otherwise default the encoding...
        return new MimeTypeResult(contentType, config.getDefaultResponseBodyEncoding());
	}
    
    public static class MimeTypeResult {

		public String mime;
		public Charset htmlResponsePayloadEncoding;

		public MimeTypeResult(String mime, Charset htmlResponsePayloadEncoding) {
			this.mime = mime;
			this.htmlResponsePayloadEncoding = htmlResponsePayloadEncoding;
		}

		@Override
		public String toString() {
			return "MimeTypeResult [mime=" + mime + ", htmlResponsePayloadEncoding=" + htmlResponsePayloadEncoding
					+ "]";
		}
    }
}
