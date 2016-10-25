package org.webpieces.httpcommon.api;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RequestListener {

	/**
	 * This is the main method that is invoked on every incoming http request on every channel giving
	 * you the channel it came in from.
     *
	 * The RequestId is only used in HTTP/2 -- in HTTP/1.1 all incomingData requests that come in are for the
	 * incomingRequest that came in immediately preceding, because multiplexing is not permitted.
	 *
	 * @param req
     * @param requestId
	 * @param isComplete true if this request contains the entire payload as well or false if just headers.
	 *
     */
	void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender);

	/**
	 * When additional data comes in for a request that isn't complete, this is called. The RequestId is used
	 * to map this additional data to the request that came in in the first place.
	 *
	 * The RequestId is only used in HTTP/2 -- in HTTP/1.1 all incomingData requests that come in are for the
	 * incomingRequest that came in immediately preceding, because multiplexing is not permitted.
	 *
	 * The final bit of data has 'isComplete' set to true.
	 *
	 * @param data
	 * @param id
	 * @param isComplete
	 * @param sender
	 * @return
	 */
    CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender);

	/**
	 * It's possible to send headers after all the data has been sent. If so, then the last incomingData has
	 * isComplete set to false, and incomingTrailer has isComplete set to true.
	 *
	 * @param headers
	 * @param id
	 * @param isComplete
	 * @param sender
	 */
	void incomingTrailer(List<HasHeaderFragment.Header> headers, RequestId id, boolean isComplete, ResponseSender sender);

	/**
	 * In the event the client sends a bad unparseable request, OR your RequestListener
	 * throws an exception, we call this method to pass in the status you 'should' return to
	 * the client as well as the channel to feed that response into
	 *  @param exc
     * @param httpSocket
	 */
	void incomingError(HttpException exc, HttpSocket httpSocket);

	/**
	 * client opened their channel(can start timeouts here)
	 * @param HttpSocket
	 */
	void clientOpenChannel(HttpSocket HttpSocket);
	
	/**
	 * The client closed their channel.
	 *
	 * @param httpSocket
	 */
	void clientClosedChannel(HttpSocket httpSocket);

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
