package org.webpieces.util.filters;

import java.util.concurrent.CompletableFuture;

/**
 * Yes, we could do Service<REQIN, RESPIN, REQOUT, RESPOUT> and allow stacking of filters in 
 * different combinations of req/resp pairs but this becomes limiting very fast in that only certain
 * filters wire to certain filters and then rewiring the stack becomes very very difficult and is
 * not worth it. Service<REQ, RESP> is enough to meet anyones needs
 * 
 * @author dhiller
 *
 * @param <REQ>
 * @param <RESP>
 */
public abstract class Service<REQ, RESP> {

	public abstract CompletableFuture<RESP> invoke(REQ meta);

	public Service<REQ, RESP> addOnTop(Filter<REQ, RESP> filter) {
		return new Service<REQ, RESP>() {
			@Override
			public CompletableFuture<RESP> invoke(REQ meta) {
				return filter.filter(meta, Service.this);
			}
		};
	}
	
}
