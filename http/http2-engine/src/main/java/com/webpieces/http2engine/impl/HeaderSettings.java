package com.webpieces.http2engine.impl;

import java.util.HashMap;
import java.util.Map;

public class HeaderSettings {

	public static final HeaderSettings DEFAULT = new HeaderSettings();
	
	//start with default per spec for all of these
	
	private int maxHeaderTableSize = 4096;
	private boolean isPushEnabled = true;
	private Long maxConcurrentStreams = null; //no limit by default per spec
	private long initialWindowSize = 65_535;
	private int maxFrameSize = 16_384; 
	private long maxHeaderListSize = 4096;
	private Map<Short, byte[]> unknownSettings = new HashMap<>();
	
	public int getMaxHeaderTableSize() {
		return maxHeaderTableSize;
	}
	public void setMaxHeaderTableSize(int maxHeaderTableSize) {
		this.maxHeaderTableSize = maxHeaderTableSize;
	}
	public boolean isPushEnabled() {
		return isPushEnabled;
	}
	public void setPushEnabled(boolean isPushEnabled) {
		this.isPushEnabled = isPushEnabled;
	}
	public Long getMaxConcurrentStreams() {
		return maxConcurrentStreams;
	}
	public void setMaxConcurrentStreams(Long maxConcurrentStreams) {
		this.maxConcurrentStreams = maxConcurrentStreams;
	}
	public long getInitialWindowSize() {
		return initialWindowSize;
	}
	public void setInitialWindowSize(long initialWindowSize) {
		this.initialWindowSize = initialWindowSize;
	}
	public int getMaxFrameSize() {
		return maxFrameSize;
	}
	public void setMaxFrameSize(int maxFrameSize) {
		this.maxFrameSize = maxFrameSize;
	}
	public long getMaxHeaderListSize() {
		return maxHeaderListSize;
	}
	public void setMaxHeaderListSize(long maxHeaderListSize) {
		this.maxHeaderListSize = maxHeaderListSize;
	}
	public Map<Short, byte[]> getUnknownSettings() {
		return unknownSettings;
	}
	public void setUnknownSettings(Map<Short, byte[]> unknownSettings) {
		this.unknownSettings = unknownSettings;
	}
	
}
