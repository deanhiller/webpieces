package org.webpieces.nio.impl.cm.basic;

import org.webpieces.nio.api.exceptions.NioException;

public interface DelayedWritesCloses {

	public boolean runDelayedAction();

	public long getCreationTime();
	
	public void failWithReason(NioException e);
	
}
