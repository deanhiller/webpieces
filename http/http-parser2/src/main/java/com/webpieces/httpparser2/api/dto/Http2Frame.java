package com.webpieces.httpparser2.api.dto;

public abstract class Http2Frame {

	private int length; //24 bits unsigned
	private int streamId; //31 bits unsigned
	
	public abstract Http2FrameType getFrameType();
	
}
