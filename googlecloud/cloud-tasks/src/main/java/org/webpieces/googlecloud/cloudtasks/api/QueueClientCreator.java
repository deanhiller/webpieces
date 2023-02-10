package org.webpieces.googlecloud.cloudtasks.api;

import org.webpieces.googlecloud.cloudtasks.impl.QueueInvokeHandler;
import org.webpieces.microsvc.api.MethodValidator;

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

    public <T> T createClient(Class<T> apiInterface, InetSocketAddress addr) {
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
