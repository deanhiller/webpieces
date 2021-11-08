package org.webpieces.microsvc.client.api;

import org.webpieces.microsvc.api.MethodValidator;
import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.microsvc.client.impl.HttpsJsonClientInvokeHandler;
import org.webpieces.util.urlparse.RegExResult;
import org.webpieces.util.urlparse.RegExUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class RESTClientCreator {

    private Provider<HttpsJsonClientInvokeHandler> wrapperProvider;

    @Inject
    public RESTClientCreator(Provider<HttpsJsonClientInvokeHandler> wrapperProvider) {
        this.wrapperProvider = wrapperProvider;
    }

    public <T> T createClient(Class<T> apiInterface, InetSocketAddress addr) {
        HttpsJsonClientInvokeHandler invokeHandler = wrapperProvider.get();
        invokeHandler.setTargetAddress(addr);

        Method[] methods = apiInterface.getMethods();
        for(Method method : methods) {
            MethodValidator.validateApiConvention(apiInterface, method);
        }

        return (T) Proxy.newProxyInstance(apiInterface.getClassLoader(),
                new Class[] { apiInterface },
                invokeHandler);
    }

}
