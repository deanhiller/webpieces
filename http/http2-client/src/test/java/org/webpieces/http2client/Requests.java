package org.webpieces.http2client;

import com.webpieces.http2engine.impl.shared.HeaderSettings;

public class Requests {

	public static HeaderSettings createSomeSettings() {
		HeaderSettings settings = new HeaderSettings();
		settings.setHeaderTableSize(4099);
		settings.setInitialWindowSize(5009);
		settings.setMaxConcurrentStreams(1L);
		settings.setMaxFrameSize(16385);
		settings.setMaxHeaderListSize(5222);
		settings.setPushEnabled(true);
		return settings;
	}

}
