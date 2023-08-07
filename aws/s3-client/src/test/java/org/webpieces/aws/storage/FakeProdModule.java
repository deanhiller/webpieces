package org.webpieces.googlecloud.storage;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.webpieces.util.context.ClientAssertions;

public class FakeProdModule implements Module{
    @Override
    public void configure(Binder binder) {
        binder.bind(ClientAssertions.class).toInstance(new ClientAssertionsForTest());
    }
}
