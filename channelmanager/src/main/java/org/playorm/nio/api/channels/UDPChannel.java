package org.playorm.nio.api.channels;

import java.io.IOException;

/**
 * @author Dean Hiller
 */
public interface UDPChannel extends Channel {

    /**
     * @throws IOException 
     * 
     */
    public void disconnect();

}
