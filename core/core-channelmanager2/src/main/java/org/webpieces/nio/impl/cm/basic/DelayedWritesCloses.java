package org.webpieces.nio.impl.cm.basic;

import org.webpieces.util.exceptions.NioException;

public interface DelayedWritesCloses {

	public boolean runDelayedAction();

	public long getCreationTime();
	
	public void failWithReason(NioException e);
	
}
