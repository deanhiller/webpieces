package org.webpieces.googlecloud.cloudtasks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.util.context.Context;

import java.util.HashMap;
import java.util.Map;

public class TestLocalCloudTasks {

    private BusinessLogicForTest businessLogic;

    @Before
    public void setup() {
        Module testModule = Modules.override(new FakeProdModule()).with(new LocalOverrideModule());
        Injector injector = Guice.createInjector(testModule);
        businessLogic = injector.getInstance(BusinessLogicForTest.class);
    }

    @After
    public void tearDown() {

    }

    //@Test
    public void testReadFromClasspath() {

        Map<String, String> headerMap = new HashMap<>();
        Context.put(Context.HEADERS, headerMap);

        businessLogic.runDeveloperExperience();

    }

}
