package org.webpieces.ssl.api;

public enum ActionState {

	SEND_TO_SOCKET,
	
	/**
	 * This means send to the client java code (not remote client) as opposed to sending over socket
	 */
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
	
	/**
	 * This end is connected now, feel free to notify client code so that it can now write
	 */
	CONNECTED,
	
	/**
	 * This end is connected now and to complete the connection on the other end
	 * ByteBuffers are returned that need to be fed to the socket
	 */
	CONNECTED_AND_SEND_TO_SOCKET, 
	
	WAITING_ON_RUNNABLE_COMPLETE_CALL,
	
	CLOSED,
	
	/**
	 * We are now officially closed but must send final handshake message to prevent truncation attack
	 */
	CLOSED_AND_SEND_TO_SOCKET,
	
}
