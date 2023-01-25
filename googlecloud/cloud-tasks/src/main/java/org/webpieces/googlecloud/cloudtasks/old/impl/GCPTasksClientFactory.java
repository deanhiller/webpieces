package org.webpieces.googlecloud.cloudtasks.old.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.ExecutorProvider;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.CloudTasksSettings;
import org.webpieces.api.*;
import org.webpieces.api.client.RemoteCallStateCheck;
import org.webpieces.api.monitoring.OrderlyMonitoring;
import org.webpieces.googlecloud.cloudtasks.api.TasksClientFactory;
import org.digitalforge.sneakythrow.SneakyThrow;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.ScheduledExecutorService;

public class GCPTasksClientFactory implements TasksClientFactory {

    private final ServerInfo serverInfo;
    private final RequestContextAccessor contextAccessor;
    private final Environment environment;
    private final ScheduledExecutorService executor;
    private final ObjectMapper mapper;
    private final RemoteCallStateCheck remoteCallStateCheck;
    private final OrderlyServiceAddress serviceAddresses;
    private final OrderlyMonitoring monitoring;
    private final SecretManager secrets;

    private final CloudTasksClient client;

    @Inject
    GCPTasksClientFactory(final ServerInfo serverInfo,
                          final RequestContextAccessor contextAccessor,
                          final Environment environment,
                          final ScheduledExecutorService executor,
                          final ObjectMapper mapper,
                          final RemoteCallStateCheck remoteCallStateCheck,
                          final OrderlyServiceAddress serviceAddresses,
                          final OrderlyMonitoring monitoring,
                          final SecretManager secrets) {

        this.serverInfo = serverInfo;
        this.contextAccessor = contextAccessor;
        this.environment = environment;
        this.executor = executor;
        this.mapper = mapper;
        this.remoteCallStateCheck = remoteCallStateCheck;
        this.serviceAddresses = serviceAddresses;
        this.monitoring = monitoring;
        this.secrets = secrets;

        this.client = createCloudTasksClient();

    }

    public <T> T createClient(final Class<T> apiInterface) {
        return (T) Proxy.newProxyInstance(
                apiInterface.getClassLoader(),
                new Class[]{apiInterface},
                new GCPTasksClient(serverInfo, contextAccessor, remoteCallStateCheck, client, environment, executor, mapper, serviceAddresses, monitoring, secrets)
        );
    }

    private CloudTasksClient createCloudTasksClient() {

        try {
            return CloudTasksClient.create(CloudTasksSettings.newBuilder()
                    .build()
            );
        } catch (IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    private static class ExecutorProviderImpl implements ExecutorProvider {

        private final ScheduledExecutorService executor;

        public ExecutorProviderImpl(final ScheduledExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public boolean shouldAutoClose() {
            return false;
        }

        @Override
        public ScheduledExecutorService getExecutor() {
            return executor;
        }

    }

}
