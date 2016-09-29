package org.webpieces.frontend.api;

import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpRequestListener {

	/**
	 * This is the main method that is invoked on every incoming http request on every channel giving
	 * you the channel it came in from
	 * 
	 * @param channel
	 * @param req
	 * @param isHttps true if this request came in over an https socket
	 */
	void processHttpRequests(FrontendSocket channel, HttpRequest req, boolean isHttps); //, boolean isHttp2);
	
	/**
	 * In the event the client sends a bad unparseable request, OR your HttpRequestListener 
	 * throws an exception, we call this method to pass in the status you 'should' return to
	 * the client as well as the channel to feed that response into
	 * 
	 * @param channel
	 * @param exc
	 */
	void sendServerResponse(FrontendSocket channel, HttpException exc);

	/**
	 * client opened their channel(can start timeouts here)
	 * @param channel
	 */
	void clientOpenChannel(FrontendSocket channel);
	
	/**
	 * The client closed their channel.
	 * 
	 * @param channel
	 */
	void clientClosedChannel(FrontendSocket channel);

	/**
	 * As you write back to the client, this is called if writes are backing up in which case 
	 * you need to apply back pressure to whatever thing is causing so many writes to the channel
	 * which 'may' be the channel itself in which case you can call channel.unregisterForReads to
	 * stop reading from the socket or you could also close the socket as well.
	 * 
	 * @param channel
	 */
	void applyWriteBackPressure(FrontendSocket channel);

	void releaseBackPressure(FrontendSocket channel);

}
