package org.webpieces.nio.api;

import org.webpieces.nio.api.channels.Stage;
import org.webpieces.nio.api.channels.TCPChannel;

/**
 * @author Dean Hiller
 */
public interface StageManager {

	public void wrapTcpChannel(TCPChannel channel, Stage stage);

	public void stop();

}
