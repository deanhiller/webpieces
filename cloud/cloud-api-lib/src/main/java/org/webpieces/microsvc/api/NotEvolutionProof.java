package org.webpieces.microsvc.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation exists to allow developers to do REST API with urls like /company/{company}/user/{userId}
 * which is not evolution proof.  It is also locking yourself into HTTP semantics preventing you from exposing
 * 1 to 1 with GRPC.   As one example, if you have two APIs
 *
 *     @POST
 *     @Path("/search/item")
 *     public CompletableFuture<SearchResponse> search(SearchRequest request);
 *
 *     @GET
 *     @Path("/users/{id}/account/{number}")
 *     public CompletableFuture<MethodResponse> method(String id, int number);
 *
 *  and in the future, you want to add a new parameter for 1 new customer.  With the first mehod, you simply add the
 *  new parameter to the 'SearchRequest' object.  In the other case, you have to create a whole other method.  On
 *  top of this, the first method is 1 to 1 with gRPC and completely compatible.  The second method can lead to a
 *  proliferation of methods
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NotEvolutionProof {

}
