package org.webpieces.frontend2.impl;

import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class NullWriter implements StreamWriter {

	private static final Logger log = LoggerFactory.getLogger(NullWriter.class);
	
	@Override
	public XFuture<Void> processPiece(StreamMsg data) {
		DataFrame f = (DataFrame)data;
		DataWrapper body = f.getData();
		String str = body.createStringFromUtf8(0, body.getReadableSize());
		log.error("Should not be receiving data(len="+body.getReadableSize()
			+").  data Received="+str, new RuntimeException("Received data here and should not").fillInStackTrace());
		return XFuture.completedFuture(null);
	}

}
