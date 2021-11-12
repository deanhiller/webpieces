package org.webpieces.httpclient11.api;

import org.webpieces.util.futures.XFuture;

import org.webpieces.httpparser.api.dto.HttpData;

public interface HttpDataWriter {

	XFuture<Void> send(HttpData chunk);
}
