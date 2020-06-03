package com.webpieces.http2engine.impl.shared;

import com.webpieces.http2.api.dto.lowlevel.SettingsFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Setting;
import com.webpieces.http2.api.dto.lowlevel.lib.SettingsParameter;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

public class RemoteSettingsManagement {

	private Level6RemoteFlowControl remoteFlow;
	private Level7MarshalAndPing notifyListener;
	private HeaderSettings remoteSettings;
	private Level3OutgoingSynchro outgoing;

	public RemoteSettingsManagement(
			Level3OutgoingSynchro outgoing,
			Level6RemoteFlowControl remoteFlow,
			Level7MarshalAndPing notifyListener,
			HeaderSettings remoteSettings) {
		this.outgoing = outgoing;
		this.remoteFlow = remoteFlow;
		this.notifyListener = notifyListener;
		this.remoteSettings = remoteSettings;
	}
	
	public void applyRemoteSettings(SettingsFrame settings) {
		for(Http2Setting setting : settings.getSettings()) {
			SettingsParameter key = setting.getKnownName();
			if(key == null)
				//TODO: forward unknown settings to clients
				continue; //unknown setting so skip it
			apply(key, setting.getValue());
		}
	}
	
	private void apply(SettingsParameter key, long value) {
		switch(key) {
			case SETTINGS_HEADER_TABLE_SIZE:
				notifyListener.setEncoderMaxTableSize( convertToInt(value));
				break;
			case SETTINGS_ENABLE_PUSH:
				applyPushEnabled(value);
				break;
			case SETTINGS_MAX_CONCURRENT_STREAMS:
				remoteSettings.setMaxConcurrentStreams(value);
				outgoing.modifyMaxConcurrentStreams(value);
				break;
			case SETTINGS_INITIAL_WINDOW_SIZE:
				remoteFlow.resetInitialWindowSize(value);
				break;
			case SETTINGS_MAX_FRAME_SIZE:
				applyMaxFrameSize(value);
				break;
			case SETTINGS_MAX_HEADER_LIST_SIZE:
				remoteSettings.setMaxHeaderListSize(value);
				break;
			default:
				throw new RuntimeException("bug, someone forgot to add some new setting="+key+" which had value="+value);
		}
	}
	
	private void applyPushEnabled(long value) {
		if(value == 0)
			remoteSettings.setPushEnabled(false);
		else if(value == 1)
			remoteSettings.setPushEnabled(true);
		else
			throw new RuntimeException("bug, this should not happen with other preconditions in place");
	}
	
	private void applyMaxFrameSize(long val) {
		int value = convertToInt(val);
		remoteSettings.setMaxFrameSize(value);
	}
	
	private int convertToInt(long value) {
		if(value > Integer.MAX_VALUE)
			throw new RuntimeException("Bug, hpack library only supports up to max integer.  need to modify that library");
		return (int)value;
	}
}
