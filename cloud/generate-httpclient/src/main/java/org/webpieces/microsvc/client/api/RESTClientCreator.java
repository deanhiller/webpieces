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

    /**
     * @deprecated
     */
    @Deprecated
    public <T> T createClient(Class<T> apiInterface, InetSocketAddress addr) {
        return createClient(apiInterface, addr, false, false);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public <T> T createClientPubsub(Class<T> apiInterface, InetSocketAddress addr) {
        return createClient(apiInterface, addr, true, false);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public <T> T createClientHttp(Class<T> apiInterface, InetSocketAddress addr) {
        return createClient(apiInterface, addr, false, true);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public <T> T createClient(Class<T> apiInterface, InetSocketAddress addr, boolean createForPubSub, boolean forHttp) {
        //in testing getHostName returns host if they constructed with host and ip if they constructod with IP
        //ONLY if the DNS is remote.  If localhost, it will return localhost which is ok here.
        HostWithPort newAddr = new HostWithPort(addr.getAddress().getHostName(), addr.getPort());
        return createClient(apiInterface, newAddr, createForPubSub, forHttp);
    }

    public <T> T createClient(Class<T> apiInterface, HostWithPort addr) {
        return createClient(apiInterface, addr, false, false);
    }

    public <T> T createClientPubsub(Class<T> apiInterface, HostWithPort addr) {
        return createClient(apiInterface, addr, true, false);
    }

    public <T> T createClientHttp(Class<T> apiInterface, HostWithPort addr) {
        return createClient(apiInterface, addr, false, true);
    }

    public <T> T createClient(Class<T> apiInterface, HostWithPort addr, boolean createForPubSub, boolean forHttp) {

        //quick DNS check or fail
        try {
            InetAddress.getByName(addr.getHostOrIpAddress());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Host="+addr.getHostOrIpAddress()+" could not be found");
        }

        HttpsJsonClientInvokeHandler invokeHandler = wrapperProvider.get();
        boolean hasUrlParams = apiInterface.getAnnotation(NotEvolutionProof.class) != null;
        invokeHandler.initialize(addr, hasUrlParams, forHttp);

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
