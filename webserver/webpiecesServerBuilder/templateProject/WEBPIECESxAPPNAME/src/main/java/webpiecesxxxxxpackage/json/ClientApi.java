package webpiecesxxxxxpackage.json;

import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;

/**
 * In our microservices architecture using webpieces
 * 1. We make every method async as you can always use the methods synchronously by calling method(request).get() and it's synchronous
 * 2. You can put sync methods on top of async ones BUT you can't truly do the reverse since the sync method is blocking a thread, it is not truly async
 *    (ie. the idea being not to lock up threads when using async methods)
 * 3. BOTH client(typically generated with ClientCreator) and server implement this same API so changes  
 * 
 * @author dean
 *
 */
public interface ClientApi {

	/**
	 * Just like GRPC for forwards compatibility, ALL non-streaming methods shooold contain a single request and
	 * single response jackson object like this method.  All methods should be POST as well and you can
	 * dynamically create all routes by doing a ClientApi.class.getMethods() and using an @Path annotation
	 * on your apis to loop over the routes.  Clients can be dynamically generated as well
	 * 
	 */
	SearchResponse postJson(SearchRequest request);

	
	/**
	 * Clients and servers can have a streaming endpoint for uploading files or ndjson through a microservice
	 * architecture as well
	 */
	StreamRef myStream(ResponseStreamHandle handle);


}
