package com.webpieces.http2engine.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HeaderSettings {

	public static final HeaderSettings DEFAULT = new HeaderSettings();
	
	//start with default per spec for all of these
	private String id = "headerSettingsId";
	private int headerTableSize = 4096;
	private volatile boolean isPushEnabled = true;
	private volatile Long maxConcurrentStreams = null; //no limit by default per spec
	private long initialWindowSize = 65_535;
	private AtomicInteger maxFrameSize = new AtomicInteger(16_384);
	private long maxHeaderListSize = 4096;
	private Map<Short, byte[]> unknownSettings = new HashMap<>();
	
	public int getHeaderTableSize() {
		return headerTableSize;
	}
	public void setHeaderTableSize(int maxHeaderTableSize) {
		this.headerTableSize = maxHeaderTableSize;
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
		return maxFrameSize.get();
	}
	public void setMaxFrameSize(int maxFrameSize) {
		this.maxFrameSize.set(maxFrameSize);
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
