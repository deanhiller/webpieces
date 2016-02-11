package org.webpieces.nio.impl.cm.secure;

import java.io.IOException;

interface SecChannelListener {

	String getId();

	void close() throws IOException;

}
