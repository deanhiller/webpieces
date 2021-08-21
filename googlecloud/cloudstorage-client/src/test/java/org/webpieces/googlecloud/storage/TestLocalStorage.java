package org.webpieces.googlecloud.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.googlecloud.storage.api.GCPStorage;

public class TestLocalStorage {

    private GCPStorage instance;

    @Before
    public void setup() {
        Module testModule = Modules.override().with(new LocalOverrideModule());

        Injector injector = Guice.createInjector(testModule);
        instance = injector.getInstance(GCPStorage.class);
    }

    @Test
    public void testReadFromClasspath() {
        //Perfect for Victor so do not implement yet :( 
    }

    @Test
    public void testWriteThenReadFromBuildDir() {
    }

    @Test
    public void testListFilesFromBothResourcesDirAndBuildDir() {
    }



}
