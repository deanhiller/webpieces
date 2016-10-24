package org.webpieces.webserver.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class ChunkedStream extends OutputStream {

	private static final Logger log = LoggerFactory.getLogger(ChunkedStream.class);
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private ByteArrayOutputStream str = new ByteArrayOutputStream();

	private ResponseSender responseSender;
	private int size;
	private String type;
	private ResponseId responseId;

	public ChunkedStream(ResponseSender responseSender, int size, boolean compressed, ResponseId responseId) {
		this.responseSender = responseSender;
		this.size = size;
		this.str = new ByteArrayOutputStream(size);
		this.responseId = responseId;
		if(compressed)
			this.type = "compressed";
		else
			this.type = "not compressed";
	}

	@Override
	public void write(int b) throws IOException {
		str.write(b);
		
		if(str.size() >= size) {
			writeDataOut();
		}
	}

	@Override
	public void flush() {
		if(str.size() > 0) {
			writeDataOut();
		}
	}
	
	@Override
	public void close() {
		if(str.size() > 0) {
			writeDataOut();
		}
		
		responseSender.sendData(wrapperFactory.emptyWrapper(), responseId, true);
	}
	
	private void writeDataOut() {
		byte[] data = str.toByteArray();
		str = new ByteArrayOutputStream();
		DataWrapper body = wrapperFactory.wrapByteArray(data);
		log.info("writing "+type+" data="+body.getReadableSize()+" to socket="+responseSender);
		responseSender.sendData(body, responseId, false);
	}

}
