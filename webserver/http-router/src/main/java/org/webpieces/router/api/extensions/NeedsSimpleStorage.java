package org.webpieces.router.api.extensions;

import org.webpieces.util.futures.XFuture;

public interface NeedsSimpleStorage {

	public XFuture<Void> init(SimpleStorage storage);
	
}
