package com.webpieces.http2engine.api.client;

import java.util.concurrent.Executor;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.util.threading.SessionExecutor;
import org.webpieces.util.threading.SessionExecutorImpl;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.util.time.Time;
import com.webpieces.util.time.TimeImpl;

public class InjectionConfig {

	private SessionExecutor executor;
	private HpackParser lowLevelParser;
	private Time time;
	private Http2Config config;
	
	public InjectionConfig(Executor executor, HpackParser lowLevelParser, Time time, Http2Config config) {
		super();
		this.executor = new SessionExecutorImpl(executor);
		this.lowLevelParser = lowLevelParser;
		this.time = time;
		this.config = config;
	}

	public InjectionConfig(Executor executor, HpackParser lowLevelParser) {
		this(executor, lowLevelParser, new TimeImpl(), new Http2Config());
	}
	
	public InjectionConfig(Executor executor, Time time, Http2Config config) {
		this(
			executor, 
			HpackParserFactory.createParser(new BufferCreationPool(), false),
			time,
			config
		);
	}
	
	public SessionExecutor getExecutor() {
		return executor;
	}
	public void setExecutor(SessionExecutor executor) {
		this.executor = executor;
	}
	public HpackParser getLowLevelParser() {
		return lowLevelParser;
	}
	public void setLowLevelParser(HpackParser lowLevelParser) {
		this.lowLevelParser = lowLevelParser;
	}
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	public Http2Config getConfig() {
		return config;
	}
	public void setConfig(Http2Config config) {
		this.config = config;
	}
	
}
