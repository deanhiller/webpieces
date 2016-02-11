package org.playorm.nio.api.libs;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface SSLListener {

	/**
	 * This is basically the SSL connected even in that your channel is now secure so go ahead and start writing to
	 * to the AsynchSSLEngine interface.
	 * @throws IOException
	 */
	void encryptedLinkEstablished() throws IOException;

	//real channel operations...
	void packetEncrypted(ByteBuffer engineToSocketData, Object passThrough) throws IOException;

	void packetUnencrypted(ByteBuffer out, Object passthrough) throws IOException;
	
	/**
	 * Java's SSL Engine gives us a Runnable so you can run it on a different thread.  In reality, most clients
	 * are have a thread pool and just run it on the same thread so you can just call r.run() when you implement
	 * this method.
	 * 
	 * ONE BIG WARNING: If you run the Runnable on another thread, you cannot feed any more encrypted packets in until the handshake is
	 * complete.  You can feed unencrypted packets in though.  
     *
	 * @param r
	 */
	void runTask(Runnable r);

	/**
	 * Called when the engine is closed after initiating a close
	 *
	 * @param clientInitiated true if client called close or initateClose.  false
	 * if closed was caused by far end sending close handshake message.
	 */
	void closed(boolean clientInitiated);
}