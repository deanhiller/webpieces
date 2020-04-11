package com.webpieces.http2engine.api.client;

import org.webpieces.data.api.TwoPools;
import org.webpieces.util.time.Time;
import org.webpieces.util.time.TimeImpl;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;

import io.micrometer.core.instrument.MeterRegistry;

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
	
	public InjectionConfig(Time time, Http2Config config, MeterRegistry metrics) {
		this(
			HpackParserFactory.createParser(new TwoPools(config.getId()+".bufpool", metrics), false),
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
