package org.webpieces.router.api.streams;

import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;

public abstract class StreamFilter {

	public abstract RouterStreamRef openStream(ProxyStreamHandle handle, StreamService svc);

	public StreamFilter chain(StreamFilter nextFilter) {
		return new StreamFilter() {
			@Override
			public RouterStreamRef openStream(ProxyStreamHandle handle, StreamService svc) {
				try {
					return StreamFilter.this.openStream(handle, svc);
				} catch(Exception e) {
					return new RouterStreamRef("filterChains", e);
				}
			}
		};
	}
	
	public StreamService chain(StreamService svc) {
		return new StreamService() {
			@Override
			public RouterStreamRef openStream(MethodMeta meta, ProxyStreamHandle handle) {
				try {
					return StreamFilter.this.openStream(handle, svc);
				} catch(Exception e) {
					return new RouterStreamRef("streamFilterSvc", e);
				}
			}
		};
	}
	
}
