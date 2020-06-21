package org.webpieces.webserver.test.sockets;

import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;
import org.webpieces.webserver.test.MockChannelManager;
import org.webpieces.webserver.test.OverridesForEmbeddedSvrWithParsing;

import com.google.inject.Module;

import io.micrometer.core.instrument.MeterRegistry;

public class AbstractWebpiecesTest {

	protected MockChannelManager mgr = new MockChannelManager();
	protected MockTime time = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();

	protected Module getOverrides(MeterRegistry metrics) {
		return new OverridesForEmbeddedSvrWithParsing(mgr, time, mockTimer, metrics);
	}
	
	
}
