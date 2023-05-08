package org.webpieces.recorder.api;

import org.webpieces.recorder.impl.ApiRecorder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Proxy;

public class ApiRecorderCreator {

    private Provider<ApiRecorder> provider;

    @Inject
    public ApiRecorderCreator(Provider<ApiRecorder> provider) {
        this.provider = provider;
    }

    public <T> T createClient(Class<T> apiInterface, T apiImplementation) {
        ApiRecorder apiRecorder = provider.get();
        apiRecorder.init(apiImplementation);

        return (T) Proxy.newProxyInstance(apiInterface.getClassLoader(),
                new Class[] { apiInterface },
                apiRecorder);
    }
}
