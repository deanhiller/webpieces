package org.webpieces.microsvc.client.api;

import org.webpieces.microsvc.api.MethodValidator;
import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.microsvc.client.impl.APIRecorderInvokeHandler;
import org.webpieces.microsvc.client.impl.HttpsJsonClientInvokeHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

public class APIRecorderFactory {

    private Provider<APIRecorderInvokeHandler> wrapperProvider;

    @Inject
    public APIRecorderFactory(Provider<APIRecorderInvokeHandler> wrapperProvider) {
        this.wrapperProvider = wrapperProvider;
    }

    public <T> T createClient(Class<T> apiInterface, T apiImplementation) {
        APIRecorderInvokeHandler invokeHandler = wrapperProvider.get();

        invokeHandler.init(apiImplementation);

        return (T) Proxy.newProxyInstance(apiInterface.getClassLoader(),
                new Class[] { apiInterface },
                invokeHandler);
    }

}
