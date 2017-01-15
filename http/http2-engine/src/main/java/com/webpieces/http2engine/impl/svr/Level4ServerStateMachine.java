package com.webpieces.http2engine.impl.svr;

import com.webpieces.http2engine.impl.shared.Level4AbstractStateMachine;
import com.webpieces.http2engine.impl.shared.Level5LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level5RemoteFlowControl;

public class Level4ServerStateMachine extends Level4AbstractStateMachine {

	public Level4ServerStateMachine(String id, Level5RemoteFlowControl remoteFlowControl,
			Level5LocalFlowControl localFlowControl) {
		super(id, remoteFlowControl, localFlowControl);
	}

}
