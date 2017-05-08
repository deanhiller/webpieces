package com.webpieces.http2engine.api.client;

import com.webpieces.http2engine.impl.shared.HeaderSettings;

public class Http2Config {
	//will be logged.  only useful if you create many http2 clients which is not needed as one client can
	//talk to everyone
	private String id = "";

	//you may want to start off with 1 rather than 100.  some apis like Apple send a settings frame of
	//only 1 max concurrent request and the other 49 will blow up if you just start doing http2 off the bat
	private int initialRemoteMaxConcurrent = 100;
	private HeaderSettings localSettings = new HeaderSettings();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getInitialRemoteMaxConcurrent() {
		return initialRemoteMaxConcurrent;
	}
	public void setInitialRemoteMaxConcurrent(int initialMaxConcurrent) {
		this.initialRemoteMaxConcurrent = initialMaxConcurrent;
	}
	public HeaderSettings getLocalSettings() {
		return localSettings;
	}
	public void setLocalSettings(HeaderSettings localSettings) {
		this.localSettings = localSettings;
	}
}
