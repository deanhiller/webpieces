package com.webpieces.http2engine.impl.shared.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.data.api.TwoPools;

import com.webpieces.http2.api.dto.lowlevel.SettingsFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Setting;
import com.webpieces.http2.api.dto.lowlevel.lib.SettingsParameter;

public class HeaderSettings {

	public static final HeaderSettings DEFAULT = new HeaderSettings();
	
	//start with default per spec for all of these
	private int headerTableSize = 4096;
	private volatile boolean isPushEnabled = true;
	private volatile Long maxConcurrentStreams = null; //no limit by default per spec
	private long initialWindowSize = 65_535;
	
	//NOTE: The spec default is 16_384 which if you have a server with many connections can waste quite a bit of memory 
	//per channel in streaming AND it's BETTER to match DEFAULT ByteBuffer size in the buffer pool
	//16_384 is the minimum!! grrrr...that's a little big to be honest.  5k would be better to save on memory costs.
	private AtomicInteger maxFrameSize = new AtomicInteger(16_384);
	
	private long maxHeaderListSize = 4096;
	private Map<Short, byte[]> unknownSettings = new HashMap<>();
	
	public HeaderSettings() {
	}
	
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

	public static SettingsFrame createSettingsFrame(HeaderSettings localSettings) {
		SettingsFrame f = new SettingsFrame();
		
		if(localSettings.getHeaderTableSize() != DEFAULT.getHeaderTableSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_HEADER_TABLE_SIZE, localSettings.getHeaderTableSize()));
		if(localSettings.isPushEnabled() != DEFAULT.isPushEnabled()) {
			long enabled = 1;
			if(!localSettings.isPushEnabled())
				enabled = 0;
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_ENABLE_PUSH, enabled));
		}
		if(localSettings.getMaxConcurrentStreams() != DEFAULT.getMaxConcurrentStreams())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, localSettings.getMaxConcurrentStreams()));
		if(localSettings.getInitialWindowSize() != DEFAULT.getInitialWindowSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_INITIAL_WINDOW_SIZE, localSettings.getInitialWindowSize()));
		if(localSettings.getMaxFrameSize() != DEFAULT.getMaxFrameSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_FRAME_SIZE, localSettings.getMaxFrameSize()));		
		if(localSettings.getMaxHeaderListSize() != DEFAULT.getMaxHeaderListSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_HEADER_LIST_SIZE, localSettings.getMaxHeaderListSize()));			
		
		return f;		
	}
	
	
}
