package org.webpieces.webserver.test;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

public interface IncomingDataListener {

	XFuture<Void> write(ByteBuffer b);

	XFuture<Void> close();

}
