package org.webpieces.microsvc.client.api;

import org.webpieces.microsvc.api.MethodValidator;
import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.microsvc.client.impl.HttpsJsonClientInvokeHandler;
import org.webpieces.util.HostWithPort;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RESTClientCreator {

    private Provider<HttpsJsonClientInvokeHandler> wrapperProvider;

    @Inject
    public RESTClientCreator(Provider<HttpsJsonClientInvokeHandler> wrapperProvider) {
        this.wrapperProvider = wrapperProvider;
    }

    public <T> T createAuthClient(Class<T> apiInterface, HostWithPort addr, Authentication auth) {
        return createClient(apiInterface, addr, false, false, auth);
    }

    public <T> T createClient(Class<T> apiInterface, HostWithPort addr) {
        return createClient(apiInterface, addr, false, false, null);
    }

    public <T> T createClientPubsub(Class<T> apiInterface, HostWithPort addr) {
        return createClient(apiInterface, addr, true, false, null);
    }

    public <T> T createClientHttp(Class<T> apiInterface, HostWithPort addr) {
        return createClient(apiInterface, addr, false, true, null);
    }

    public <T> T createClient(
            Class<T> apiInterface,
            HostWithPort addr,
            boolean createForPubSub,
            boolean forHttp,
            Authentication authentication
    ) {

        //quick DNS check or fail
        try {
            InetAddress.getByName(addr.getHostOrIpAddress());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Host="+addr.getHostOrIpAddress()+" could not be found");
        }

        HttpsJsonClientInvokeHandler invokeHandler = wrapperProvider.get();
        boolean hasUrlParams = apiInterface.getAnnotation(NotEvolutionProof.class) != null;
        invokeHandler.initialize(addr, hasUrlParams, forHttp, authentication);

        boolean forceVoid = false;
        if(createForPubSub)
            forceVoid = true;

        Method[] methods = apiInterface.getMethods();
        for(Method method : methods) {
            MethodValidator.validateApiConvention(apiInterface, method, forceVoid);
        }

        return (T) Proxy.newProxyInstance(apiInterface.getClassLoader(),
                new Class[] { apiInterface },
                invokeHandler);
    }

}
