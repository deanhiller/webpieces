package org.webpieces.router.api.streams;

import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;

public interface StreamService {

	RouterStreamRef openStream(MethodMeta meta, ProxyStreamHandle handle);
	
}
