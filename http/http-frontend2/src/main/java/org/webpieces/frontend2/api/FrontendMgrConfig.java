package org.webpieces.frontend2.api;

import org.webpieces.nio.api.BackpressureConfig;

import com.webpieces.http2engine.api.client.Http2Config;

public class FrontendMgrConfig {
	private int threadPoolSize = 20;
	private BackpressureConfig backpressureConfig = new BackpressureConfig();
	private Http2Config http2Config = new Http2Config();
	
	public int getThreadPoolSize() {
		return threadPoolSize;
	}
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}
	public BackpressureConfig getBackpressureConfig() {
		return backpressureConfig;
	}
	public void setBackpressureConfig(BackpressureConfig config) {
		this.backpressureConfig = config;
	}
	public Http2Config getHttp2Config() {
		return http2Config;
	}
	public void setHttp2Config(Http2Config http2Config) {
		this.http2Config = http2Config;
	}
	
	
}
