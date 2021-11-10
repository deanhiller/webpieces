package org.webpieces.httpclientx.impl;

import org.webpieces.util.futures.XFuture;

import org.webpieces.http2translations.api.Http2ToHttp11;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;

import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class StreamWriterImpl implements StreamWriter {

	private HttpDataWriter dataWriter;
	private HttpRequest req;

	public StreamWriterImpl(HttpDataWriter dataWriter, HttpRequest req) {
		this.dataWriter = dataWriter;
		this.req = req;
	}

	@Override
	public XFuture<Void> processPiece(StreamMsg data) {
		if(!(data instanceof DataFrame))
			throw new IllegalArgumentException("You must feed in http1_1 compatible http2 payloads like DataFrame.  this is not http1_1 compatible="+data.getClass());
		HttpData chunk = Http2ToHttp11.translate((DataFrame)data, req);
		return dataWriter.send(chunk);
	}

}
