package org.webpieces.googlecloud.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.googlecloud.storage.biz.BusinessLogic;
import org.webpieces.googlecloud.storage.biz.RestGuiceModule;
import org.webpieces.util.context.Context;

import java.util.HashMap;
import java.util.Map;

public class TestGeneratedClient {

    private BusinessLogic business;

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new RestGuiceModule());
        business = injector.getInstance(BusinessLogic.class);

    }

    //commented out for now...
    @Test
    public void testWithGoogle() {

        Map<String, String> headerMap = new HashMap<>();
        Context.put(Context.HEADERS, headerMap);

        business.runTest();
    }

}
