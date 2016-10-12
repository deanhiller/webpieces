package org.webpieces.httpcommon.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;

import java.util.concurrent.CompletableFuture;

public interface RequestListener {

	/**
	 * This is the main method that is invoked on every incoming http request on every channel giving
	 * you the channel it came in from.
     *
     * We encode if is https or httpv2 in the request itself.
     *
     * @param req
     * @param isComplete true if this request came in over an https socket
     *
     *
     */
	CompletableFuture<RequestId> incomingRequest(HttpRequest req, boolean isComplete, ResponseSender sender);

    CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender);
	
	/**
	 * In the event the client sends a bad unparseable request, OR your RequestListener
	 * throws an exception, we call this method to pass in the status you 'should' return to
	 * the client as well as the channel to feed that response into
	 *
     * @param exc
     * @param sender
     */
	void incomingError(HttpException exc, ResponseSender sender);

	/**
	 * client opened their channel(can start timeouts here)
     */
	void clientOpenChannel();
	
	/**
	 * The client closed their channel.
	 *
     */
	void clientClosedChannel();

	/**
	 * As you sendResponse back to the client, this is called if writes are backing up in which case
	 * you need to apply back pressure to whatever thing is causing so many writes to the channel
	 * which 'may' be the channel itself in which case you can call channel.unregisterForReads to
	 * stop reading from the socket or you could also close the socket as well.
	 *
     * @param sender
     */
	void applyWriteBackPressure(ResponseSender sender);

	void releaseBackPressure(ResponseSender sender);

}
