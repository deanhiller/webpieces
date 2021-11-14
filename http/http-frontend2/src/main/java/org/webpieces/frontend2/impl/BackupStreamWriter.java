package org.webpieces.frontend2.impl;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class BackupStreamWriter implements StreamWriter {

	@Override
	public XFuture<Void> processPiece(StreamMsg data) {
		//unresolved future on purpose so we stop using CPU on useless data coming in
		//since we either cancelled OR we responded with response.isEndOfStream=true
		return new XFuture<Void>();
	}

}
