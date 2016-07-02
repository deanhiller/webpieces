package org.webpieces.httpparser.api;

public enum ParsedStatus {

	/**
	 * All data given was parsed and message is returned
	 */
	ALL_DATA_PARSED,
	
	/**
	 * Only part of the http message was provided
	 */
	NEED_MORE_DATA,
	
	/**
	 * More than one http message was provided(perhaps 1.5 to be honest) and we
	 * parsed the first one and returned the leftover in a buffer
	 */
	MSG_PARSED_AND_LEFTOVER_DATA;
}
