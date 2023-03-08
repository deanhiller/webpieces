package org.webpieces.microsvc.server.impl;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.util.UuidValidator;

import java.util.UUID;

public class UUID7 {

    public static UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();
    }

    public static boolean validate(byte[] uuid) {
        return UuidValidator.isValid(uuid);
    }

    public static boolean validate(String uuid) {
        return UuidValidator.isValid(uuid);
    }

}
