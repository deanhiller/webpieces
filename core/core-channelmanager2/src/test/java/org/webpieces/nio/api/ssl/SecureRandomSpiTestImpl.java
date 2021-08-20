package org.webpieces.nio.api.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.bytes.Hex;

import java.security.SecureRandom;
import java.security.SecureRandomSpi;

public class SecureRandomSpiTestImpl extends SecureRandomSpi {
    private static final Logger log = LoggerFactory.getLogger(SecureRandomSpiTestImpl.class);

    private SecureRandom r = new SecureRandom();


    @Override
    protected void engineSetSeed(byte[] seed) {
        //no-op
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        r.nextBytes(bytes);
        String bytesAsHex = Hex.printHexBinary(bytes);
        log.info("bytesAsHex="+bytesAsHex);
        //throw new RuntimeException("need to get this from a REAL SecureRandom")
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        throw new RuntimeException("is this called at all");
    }
}
