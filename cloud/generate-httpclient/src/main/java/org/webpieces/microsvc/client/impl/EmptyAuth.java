package org.webpieces.microsvc.client.impl;

import org.webpieces.microsvc.client.api.Authentication;

public class EmptyAuth implements Authentication {
    @Override
    public Object setupMagic() {
        return null;
    }

    @Override
    public void resetMagic(Object state) {
    }
}
