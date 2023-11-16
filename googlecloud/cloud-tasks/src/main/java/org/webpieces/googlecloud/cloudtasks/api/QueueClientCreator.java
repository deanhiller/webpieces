package org.webpieces.googlecloud.cloudtasks.api;

import org.webpieces.googlecloud.cloudtasks.impl.QueueInvokeHandler;
import org.webpieces.microsvc.api.MethodValidator;
import org.webpieces.util.HostWithPort;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

public class QueueClientCreator {

    private Provider<QueueInvokeHandler> wrapperProvider;

    @Inject
    public QueueClientCreator(Provider<QueueInvokeHandler> wrapperProvider) {
        this.wrapperProvider = wrapperProvider;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public <T> T createClient(Class<T> apiInterface, InetSocketAddress addr) {
        HostWithPort newAddr = new HostWithPort(addr.getAddress().getHostName(), addr.getPort());
        return createClient(apiInterface, newAddr);
    }

    public <T> T createClient(Class<T> apiInterface, HostWithPort addr) {
        QueueInvokeHandler invokeHandler = wrapperProvider.get();
        invokeHandler.initialize(addr);

        Method[] methods = apiInterface.getMethods();
        for(Method method : methods) {
            MethodValidator.validateApiConvention(apiInterface, method, true);
        }

        return (T) Proxy.newProxyInstance(apiInterface.getClassLoader(),
                new Class[] { apiInterface },
                invokeHandler);
    }

}
