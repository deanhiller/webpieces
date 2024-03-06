package org.webpieces.microsvc.client.api;

import org.webpieces.util.context.Context;

public interface Authentication {
    public Object setupMagic();

    public void resetMagic(Object state);

}
