package com.webpieces.http2engine.api.client;

import org.webpieces.data.api.BufferCreationPool;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.util.time.Time;
import com.webpieces.util.time.TimeImpl;

public class InjectionConfig {

	private HpackParser lowLevelParser;
	private Time time;
	private Http2Config config;
	
	public InjectionConfig(HpackParser lowLevelParser, Time time, Http2Config config) {
		super();
		this.lowLevelParser = lowLevelParser;
		this.time = time;
		this.config = config;
	}

	public InjectionConfig(HpackParser lowLevelParser) {
		this(lowLevelParser, new TimeImpl(), new Http2Config());
	}
	
	public InjectionConfig(Time time, Http2Config config) {
		this(
			HpackParserFactory.createParser(new BufferCreationPool(), false),
			time,
			config
		);
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
