package org.webpieces.googlecloud.cloudtasks.old.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.webpieces.api.OrderlyServiceAddress;
import org.webpieces.api.RequestContextAccessor;
import org.webpieces.api.SecretManager;
import org.webpieces.api.ServerInfo;
import org.webpieces.api.client.RemoteCallStateCheck;
import org.webpieces.api.util.HttpsConfig;
import org.webpieces.googlecloud.cloudtasks.api.TasksClientFactory;

import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

public class LocalTasksClientFactory implements TasksClientFactory {

    private final ServerInfo serverInfo;
    private final RequestContextAccessor contextAccessor;
    private final HttpsConfig httpsConfig;
    private final ExecutorService executor;
    private final ObjectMapper mapper;
    private final RemoteCallStateCheck remoteCallStateCheck;
    private final OrderlyServiceAddress serviceAddresses;
    private final SecretManager secrets;

    @Inject
    LocalTasksClientFactory(final ServerInfo serverInfo, final RequestContextAccessor contextAccessor, final HttpsConfig httpsConfig,
                            final ObjectMapper mapper, final RemoteCallStateCheck remoteCallStateCheck,
                            final OrderlyServiceAddress serviceAddresses, final ExecutorService executor, final SecretManager secrets) {

        if(serverInfo.getEnvironment().isCloud()) {
            throw new IllegalStateException("LocalTasksClient can only be used for local development!");
        }

        this.serverInfo = serverInfo;
        this.contextAccessor = contextAccessor;
        this.httpsConfig = httpsConfig;
        this.executor = executor;
        this.mapper = mapper;
        this.remoteCallStateCheck = remoteCallStateCheck;
        this.serviceAddresses = serviceAddresses;
        this.secrets = secrets;
    }

    public <T> T createClient(final Class<T> apiInterface) {
        return (T)Proxy.newProxyInstance(
            apiInterface.getClassLoader(),
            new Class[] {apiInterface},
            new LocalTasksClient(serverInfo, contextAccessor, serviceAddresses, httpsConfig, mapper, remoteCallStateCheck, executor, secrets)
        );
    }

}
