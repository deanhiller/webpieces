package org.webpieces.niostages.impl;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.StageManager;
import org.webpieces.nio.api.channels.FromSocket;
import org.webpieces.nio.api.channels.Stage;
import org.webpieces.nio.api.channels.StageFactory;
import org.webpieces.nio.api.channels.TCPChannel;

public class StageManagerImpl implements StageManager {

	public StageManagerImpl(ChannelManager mgr) {
	}

	@Override
	public void stop() {
	}

	@Override
	public void wrapTcpChannel(TCPChannel channel, Stage stage) {
		
	}
	
	public StageFactory addStage(StageFactory stage1, StageFactory stage2) {
		
		//stage1.createStageFactory(from, to);
		
		return null;
	}

}
