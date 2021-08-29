package org.webpieces.microsvc.client.api;

import org.webpieces.microsvc.client.impl.HttpsJsonClientInvokeHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

public class RESTClientCreator {

    private Provider<HttpsJsonClientInvokeHandler> wrapperProvider;

    @Inject
    public RESTClientCreator(Provider<HttpsJsonClientInvokeHandler> wrapperProvider) {
        this.wrapperProvider = wrapperProvider;
    }

    public <T> T createClient(Class<T> apiInterface, InetSocketAddress addr) {
        HttpsJsonClientInvokeHandler invokeHandler = wrapperProvider.get();
        invokeHandler.setTargetAddress(addr);

        return (T) Proxy.newProxyInstance(apiInterface.getClassLoader(),
                new Class[] { apiInterface },
                invokeHandler);
    }
}
