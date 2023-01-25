package org.webpieces.googlecloud.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Before;
import org.webpieces.googlecloud.storage.api.GCPStorage;

/**
 * This is for when we have to talk to google to find out what google returns so we can
 * simulate google better
 */
public class IntegGoogleTest {

    private GCPStorage instance;

    @Before
    public void setup() {
        Module module = new FakeProdModule();
        Injector injector = Guice.createInjector(module);
        instance = injector.getInstance(GCPStorage.class);
    }

    //@Test
    public void talkToGoogle() {
        //write simulation code here
    }
}
