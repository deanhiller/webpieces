package org.webpieces.throughput;

import org.webpieces.nio.api.BackpressureConfig;

public class AsyncConfig {

	private Integer clientThreadCount;
	private Integer serverThreadCount;
	private boolean isHttps;
	private int clientMaxConcurrentRequests;
	private BackpressureConfig backpressureConfig;
	private int numSockets = 1;

	public boolean isHttps() {
		return isHttps;
	}

	public void setHttps(boolean isHttps) {
		this.isHttps = isHttps;
	}

	public int getClientMaxConcurrentRequests() {
		return clientMaxConcurrentRequests;
	}

	public void setHttp2ClientMaxConcurrentRequests(int clientMaxConcurrentRequests) {
		this.clientMaxConcurrentRequests = clientMaxConcurrentRequests;
	}

	public void setBackPressureConfig(BackpressureConfig backpressureConfig) {
		this.backpressureConfig = backpressureConfig;
	}

	public BackpressureConfig getBackpressureConfig() {
		return backpressureConfig;
	}

	public Integer getClientThreadCount() {
		return clientThreadCount;
	}

	public void setClientThreadCount(Integer clientThreadCount) {
		this.clientThreadCount = clientThreadCount;
	}

	public Integer getServerThreadCount() {
		return serverThreadCount;
	}

	public void setServerThreadCount(Integer serverThreadCount) {
		this.serverThreadCount = serverThreadCount;
	}

	public int getNumSockets() {
		return numSockets;
	}

	public void setNumSockets(int numSockets) {
		this.numSockets = numSockets;
	}

}
