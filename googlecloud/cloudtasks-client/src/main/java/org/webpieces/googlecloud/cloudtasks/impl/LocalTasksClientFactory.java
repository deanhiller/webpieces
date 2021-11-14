package com.orderlyhealth.googlecloud.cloudtasks.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderlyhealth.api.OrderlyServiceAddress;
import com.orderlyhealth.api.RequestContextAccessor;
import com.orderlyhealth.api.SecretManager;
import com.orderlyhealth.api.ServerInfo;
import com.orderlyhealth.api.client.RemoteCallStateCheck;
import com.orderlyhealth.api.util.HttpsConfig;
import com.orderlyhealth.googlecloud.cloudtasks.TasksClientFactory;

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
