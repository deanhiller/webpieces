package org.webpieces.aws.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Before;
import org.webpieces.aws.storage.api.AWSStorage;

/**
 * This is for when we have to talk to google to find out what google returns so we can
 * simulate google better
 */
public class ProductionTest {

    private AWSStorage instance;

    @Before
    public void setup() {
        Module module = new TestProdModule();
        Injector injector = Guice.createInjector(module);
        instance = injector.getInstance(AWSStorage.class);
    }

    //@Test
    public void talkToGoogle() {
        //write simulation code here
    }

}
