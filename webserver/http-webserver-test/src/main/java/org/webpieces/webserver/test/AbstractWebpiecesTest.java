package org.webpieces.webserver.test;

import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;

public class AbstractWebpiecesTest {

	protected MockChannelManager mgr = new MockChannelManager();
	protected MockTime time = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();
	protected PlatformOverridesForTest platformOverrides = new PlatformOverridesForTest(mgr, time, mockTimer);
	
	protected Http11ClientSimulator http11Simulator = new Http11ClientSimulator(mgr);
	
}
