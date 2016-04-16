package org.webpieces.nio.api.deprecated;

import java.io.IOException;

/**
 * @author Dean Hiller
 */
public interface ChannelService extends ChannelManagerOld {

	public void start() throws IOException;

	public void stop() throws IOException, InterruptedException;

}
