package org.webpieces.nio.api.handlers;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.util.futures.Future;

public interface FutureResult extends Future<Channel, FailureInfo> {

}
