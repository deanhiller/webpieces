package com.webpieces.hpack.api;

public class HpackConfig {
	private String logId;
	private boolean ignoreUnknownFrames = false; //drop unknown frames or pass them through to client
	private int maxHeaderSize = 1_000;
	private int maxHeaderTableSize = 6_000;
	private long localMaxFrameSize = 32_768;
	private long remoteMaxFrameSize = 32_768;
	
	public HpackConfig(String logId) {
		this.logId = logId;
	}
	
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
	public boolean isIgnoreUnknownFrames() {
		return ignoreUnknownFrames;
	}
	public void setIgnoreUnknownFrames(boolean ignoreUnknownFrames) {
		this.ignoreUnknownFrames = ignoreUnknownFrames;
	}
	public int getMaxHeaderSize() {
		return maxHeaderSize;
	}
	public void setMaxHeaderSize(int maxHeaderSize) {
		this.maxHeaderSize = maxHeaderSize;
	}
	public int getMaxHeaderTableSize() {
		return maxHeaderTableSize;
	}
	public void setMaxHeaderTableSize(int maxHeaderTableSize) {
		this.maxHeaderTableSize = maxHeaderTableSize;
	}
	public long getLocalMaxFrameSize() {
		return localMaxFrameSize;
	}
	public void setLocalMaxFrameSize(long localMaxFrameSize) {
		this.localMaxFrameSize = localMaxFrameSize;
	}
	public long getRemoteMaxFrameSize() {
		return remoteMaxFrameSize;
	}
	public void setRemoteMaxFrameSize(long remoteMaxFrameSize) {
		this.remoteMaxFrameSize = remoteMaxFrameSize;
	}
	
	
}
