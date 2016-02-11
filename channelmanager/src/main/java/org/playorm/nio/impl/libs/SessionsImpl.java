package org.playorm.nio.impl.libs;

import org.playorm.nio.api.libs.ChannelSession;
import org.playorm.nio.api.libs.SessionThread;
import org.playorm.nio.api.libs.Sessions;

public final class SessionsImpl extends Sessions {

	private SessionsImpl() {
	}

	public static void init() {
		SessionsImpl impl = new SessionsImpl();
		Sessions.setCurrentInstance(impl);
	}
	
	@Override
	protected ChannelSession getSessionImpl() {
		SessionThread thread = (SessionThread)Thread.currentThread();
		return thread.getSession();
	}

}
