package com.webpieces.http2parser.api.dto;

public class Http2Headers {

	private Boolean streamDependencyIsExclusive;
	private int streamDependency;
	private short weight;
	private boolean endStream;
	private boolean endHeaders;
	private boolean priorityWeightAndStreamDepSet;
	private Http2HeaderBlock headerBlock;
	
	
}
