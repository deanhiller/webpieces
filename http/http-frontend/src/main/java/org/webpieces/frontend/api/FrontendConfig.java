package org.webpieces.frontend.api;

import java.net.SocketAddress;
import java.util.Optional;

import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.Http2Settings;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.data.api.BufferCreationPool;

public class FrontendConfig {

	public AsyncConfig asyncServerConfig = new AsyncConfig();
	
	/**
	 * When a client connects, they must send a request in this amount of time.  null means disabled.
	 * telnet google.com 443 yeilds an 11 second timeout while telnet google.com 80 yeilds a 
	 * 60 second timeout so wait until 65 seconds 
	 */
	public Integer maxConnectToRequestTimeoutMs = 65000;
	
	/**
	 * null means keep alive will be disabled
	 */
	public Integer keepAliveTimeoutMs = 15000;
	
	/**
	 * The max size a client may send.  I advise not too large a limit here or DOS attacks become easier in that
	 * you can send very large request to eat up memory of the server.
	 * 
	 * This is the max size of any http request(headers that is) or any chunk being uploaded.
	 *
	 */
	public int maxHeaderSize = 4096;
	
	public int maxBodyOrChunkSize = BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE;

	/**
	 * Various optional HTTP/2 settings. If empty use the default.
	 *
	 */
	public Optional<Long> maxConcurrentStreams = Optional.empty();
	public Optional<Long> initialWindowSize = Optional.empty();

	/**
	 * This turns on HTTP/2 by default, so HTTP/1.1 won't work. Only needed for testing with
	 * 'h2spec' over cleartext. Will be made obsolete once we have ALPN.
	 *
	 */
	public boolean alwaysHttp2 = false;

	public FrontendConfig(String id, SocketAddress bindAddr) {
		asyncServerConfig.id = id;
		asyncServerConfig.bindAddr = bindAddr;
	}
	
	public FrontendConfig(String id, SocketAddress bindAddr, Integer connectToRequestTimeout) {
		asyncServerConfig.id = id;
		asyncServerConfig.bindAddr = bindAddr;
		maxConnectToRequestTimeoutMs = connectToRequestTimeout;
	}

	public Http2SettingsMap getHttp2Settings() {
		Http2SettingsMap settings = new Http2SettingsMap();
		settings.put(Http2Settings.Parameter.SETTINGS_MAX_HEADER_LIST_SIZE, (long) maxHeaderSize);
		settings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, (long) maxBodyOrChunkSize);
		maxConcurrentStreams.ifPresent(v -> settings.put(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS, v));
		initialWindowSize.ifPresent(v -> settings.put(Http2Settings.Parameter.SETTINGS_INITIAL_WINDOW_SIZE, v));
		return settings;
	}

}
