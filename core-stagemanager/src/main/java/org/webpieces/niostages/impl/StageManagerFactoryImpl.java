package org.webpieces.niostages.impl;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.StageManager;
import org.webpieces.nio.api.StageManagerFactory;

public class StageManagerFactoryImpl extends StageManagerFactory {

	@Override
	public StageManager createStageManager(ChannelManager mgr) {
		return new StageManagerImpl(mgr);
	}

}
