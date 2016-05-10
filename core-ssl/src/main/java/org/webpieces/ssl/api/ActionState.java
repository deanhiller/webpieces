package org.webpieces.ssl.api;

public enum ActionState {

	SEND_TO_SOCKET,
	SEND_TO_CLIENT,
	
	/**
	 * just feed in another encrypted ByteBuffer from the socket as we don't 
	 * have the full ssl packet quite yet(or don't have all the necessary packets yet).  The 
	 * previous one you fed is now cached in the memento
	 */
	NOT_ENOUGH_ENCRYPTED_BYTES_YET, 
	
	/**
	 * In this case, a remote task or long running needs to be run.  Run this runnable
	 */
	RUN_RUNNABLE,
	
	CONNECTED,
	
	/**
	 * This end is connected now and to complete the connection on the other end
	 * ByteBuffers are returned that need to be fed to the socket
	 */
	CONNECTED_AND_SEND_TO_SOCKET, 
	
	WAITING_ON_RUNNABLE_COMPLETE_CALL,
	
}
