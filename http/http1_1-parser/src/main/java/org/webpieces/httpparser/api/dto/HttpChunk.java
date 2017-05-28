package org.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

public class HttpChunk extends HttpData {
	
	public static final String TRAILER_STR = "\r\n";
	protected List<HttpChunkExtension> extensions = new ArrayList<>();
	
	public HttpChunk() {
	}
	public HttpChunk(DataWrapper data) {
		super(data, false);
	}

	@Override
	public boolean isEndOfData() {
		return false;
	}

	@Override
	public HttpMessageType getMessageType() {
		return HttpMessageType.CHUNK;
	}

	public void addExtension(HttpChunkExtension extension) {
		extensions.add(extension);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HttpChunk other = (HttpChunk) obj;
		if (extensions == null) {
			if (other.extensions != null)
				return false;
		} else if (!extensions.equals(other.extensions))
			return false;
		return true;
	}

	public List<HttpChunkExtension> getExtensions() {
		return extensions;
	}

	public String createMetaLine() {
		String metaLine = Integer.toHexString(getBody().getReadableSize());
		for(HttpChunkExtension extension : getExtensions()) {
			metaLine += ";"+extension.getName();
			if(extension.getValue() != null)
				metaLine += "="+extension.getValue();
		}		
		return metaLine+TRAILER_STR;
	}

	public String createTrailer() {
		return TRAILER_STR;
	}

	@Override
	public boolean isHasChunkedTransferHeader() {
		return false;
	}

	@Override
	public String toString() {
		String metaLine = createMetaLine();
		String trailer = createTrailer();
		return metaLine+trailer;
	}
}
