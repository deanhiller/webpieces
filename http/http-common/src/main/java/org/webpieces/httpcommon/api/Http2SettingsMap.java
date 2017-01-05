package org.webpieces.httpcommon.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

@Deprecated
public class Http2SettingsMap extends HashMap<SettingsParameter, Long> {

	private static final long serialVersionUID = 1L;

	public Http2SettingsMap(List<Http2Setting> settings) {
		for(Http2Setting s: settings) {
			if(s.getKnownName() != null)
				put(s.getKnownName(), s.getValue());
		}
	}

	public Http2SettingsMap() {
	}

	public void fillFrame(SettingsFrame settingsFrame) {
		for(Entry<SettingsParameter, Long> entry : this.entrySet()) {
			settingsFrame.addSetting(new Http2Setting(entry.getKey(), entry.getValue()));
		}
	}

	public List<Http2Setting> toNewer() {
		List<Http2Setting> set = new ArrayList<>();
		for(Entry<SettingsParameter, Long> entry : this.entrySet()) {
			set.add(new Http2Setting(entry.getKey(), entry.getValue()));
		}
		return set;
	}
	
	
}
