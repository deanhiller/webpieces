package com.webpieces.http2.api.streaming;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;

public interface StreamWriter {

	XFuture<Void> processPiece(StreamMsg data);

}