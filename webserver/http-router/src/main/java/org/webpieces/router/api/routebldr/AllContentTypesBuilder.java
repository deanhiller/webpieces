package org.webpieces.router.api.routebldr;

/**
 * MOST of the time, Developers really don't care about the requests ContentType header.  In those cases where you do not care,
 * just call getBldrForAllOtherContentTypes() and any fixed paths will be hit
 * 
 * @author dean
 */
public interface AllContentTypesBuilder {

	/**
	 * DO NOT USE this unless you know what you are doing.  This is for advanced users.  It is mainly for plugins
	 * that want to add a full set of routes for incoming application/grpc-json or application/grpc-protobuf content
	 * types in the request.  It can be used for full json with Content-Type="application/json" if you want to force
	 * clients to use that header(but honestly, that's quite annoying as then you can't hit the url with chrome
	 * and what not.......I am more make stuff practical instead of follow the theory which tends to break down and
	 * make things harder when you go toooo hard in that area).  ie. usability first or you lose customers and 
	 * customers are more important than following theory.
	 * 
	 * This takes precendence of routes such that if there is a contentType match, we will use only the routes in
	 * this builder or return a not found.  We will not use the routes defined by any other route builder
	 * 
	 * @param ctType
	 */
	ContentTypeRouteBuilder getContentTypeRtBldr(String requestContentType);
	
	/**
	 * Use this router to add routes for all other content types.  You can build json routes with this builder or the 
	 * above builder as well to require the request content type from requests
	 */
	RouteBuilder getBldrForAllOtherContentTypes();
	
}
