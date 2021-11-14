package com.orderlyhealth.googlecloud.cloudtasks.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.orderlyhealth.api.*;
import com.orderlyhealth.api.client.OrderlyRemoteClient;
import com.orderlyhealth.api.client.RemoteCallStateCheck;
import com.orderlyhealth.api.monitoring.OrderlyMonitoring;
import com.orderlyhealth.api.monitoring.metric.CloudTasksClientMetrics;
import com.orderlyhealth.api.util.SingletonSupplier;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

class GCPTasksClient extends OrderlyRemoteClient<CloudTaskQueue> {

    private static final Logger log = LoggerFactory.getLogger(GCPTasksClient.class);

    private final CloudTasksClient client;
    private final Environment environment;
    private final Executor executor;
    private final ObjectMapper mapper;
    private final OrderlyServiceAddress serviceAddresses;
    private final OrderlyMonitoring monitoring;

    private final Supplier<String> internalHeader;

    public GCPTasksClient(final ServerInfo serverInfo, final RequestContextAccessor contextAccessor,
                          final RemoteCallStateCheck remoteCallStateCheck,
                          final CloudTasksClient client,
                          final Environment environment,
                          final Executor executor,
                          final ObjectMapper mapper,
                          final OrderlyServiceAddress serviceAddresses,
                          final OrderlyMonitoring monitoring,
                          final SecretManager secrets) {

        super(CloudTaskQueue.class, serverInfo, contextAccessor, remoteCallStateCheck);

        this.client = client;
        this.environment = environment;
        this.executor = executor;
        this.mapper = mapper;
        this.serviceAddresses = serviceAddresses;
        this.monitoring = monitoring;

        internalHeader = new SingletonSupplier<>(() -> secrets.getSecret("x-orderly-internal"));

    }

    @Override
    public Object invoke(Object instance, Method method, Object[] args) throws Throwable {
        try {
            return invokeImpl(instance, method, args);
        } catch (Throwable e) {
            log.error("EXCEPTION::GCPTasksClient::",e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public Object invokeImpl(Object instance, Method method, Object[] args) throws Throwable {

        if (!environment.isCloud()) {
            throw new IllegalStateException("GCPTasksClient can only be used in the cloud!");
        }

        checkInvocation(method, args);

        Path pathAnnotation = method.getAnnotation(Path.class);
        CloudTaskQueue annotation = method.getAnnotation(CloudTaskQueue.class);

        if (pathAnnotation == null) {
            throw new IllegalArgumentException("Method must be annotated with @" + Path.class.getSimpleName() + " method=" + method + " clazz=" + method.getDeclaringClass());
        } else if (annotation == null) {
            throw new IllegalArgumentException("Method must be annotated with @" + CloudTaskQueue.class.getSimpleName() + " method=" + method + " clazz=" + method.getDeclaringClass());
        }
        String path = pathAnnotation.value();
        if ((path == null) || path.isBlank()) {
            throw new IllegalStateException("Missing path or path is blank. annotation=" + Path.class.getSimpleName() + " method=" + method + " clazz=" + method.getDeclaringClass());
        }

        String queue = annotation.name();
        OrderlyService destination = annotation.destination();

        if (destination == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (destination == OrderlyService.__CURRENT) {
            destination = serverInfo.getService();
        }

        InetSocketAddress socketAddress = serviceAddresses.getSocketAddress(destination);
        String scheme = (socketAddress.getPort() != 443) ? "http" : "https";
        URI uri = new URI(scheme, null, socketAddress.getHostName(), socketAddress.getPort(), path, null, null);
        String body = mapper.writeValueAsString(args[0]);
        QueueName queueName = getQueue(queue);
        Map<String, String> attributes = getRequestContextAttributes();

        log.info("Sending attributes: " + attributes);

        HttpRequest request = HttpRequest.newBuilder()
                .setBody(ByteString.copyFrom(body, Charset.defaultCharset()))
                .setUrl(uri.toString())
                .setHttpMethod(HttpMethod.POST)
                .putAllHeaders(getRequestContextAttributes())
                .putHeaders(OrderlyHeaders.INTERNAL_SECURE_KEY.toString(), internalHeader.get())
                .build();

        Task.Builder taskBuilder = Task.newBuilder()
                .setHttpRequest(request)
                .setDispatchDeadline(Duration.newBuilder().setSeconds(1020).build()); // 17 minutes (longer than Cloud Run timeout)

        log.info("Submitting " + args[0].getClass().getName() + " to " + request.getUrl() + " via " + queueName);

        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> createTask(queueName, taskBuilder), executor)
                .thenAccept(task -> {

                    StringBuilder sb = new StringBuilder();

                    sb.append("Task created: ").append(task.getName()).append('\n');
                    sb.append("Curl request: ").append(getCurlCommand(request.getUrl(), request.getHttpMethod().name(), request.getHeadersMap(), body));

                    log.debug(sb.toString());

                }).thenApply(r -> null);

        // Track metrics with future.handle()
        future = future.handle((r, e) -> {

            log.error("You will see a full stack trace as long as you are calling .get() on the future return from API");
            long start = System.currentTimeMillis();
            String clientId = Optional.of(attributes.get(OrderlyHeaders.CLIENT_ID.getHeaderName())).orElse("unknown");

            Map<String, String> tags = Map.of(
                    CloudTasksClientMetrics.CloudTasksClientMetricTags.API_NAME, method.getDeclaringClass().getSimpleName(),
                    CloudTasksClientMetrics.CloudTasksClientMetricTags.METHOD_NAME, method.getName(),
                    CloudTasksClientMetrics.CloudTasksClientMetricTags.CLIENT_ID, clientId,
                    CloudTasksClientMetrics.CloudTasksClientMetricTags.QUEUE_NAME, queue
            );
            monitoring.endTimer(CloudTasksClientMetrics.CLOUD_TASKS_CLIENT_ENQUEUE_TIME, tags, start);

            if (e != null) {
                Map<String, String> errorTags = Map.of(
                        CloudTasksClientMetrics.CloudTasksClientExceptionMetricTags.API_NAME, method.getDeclaringClass().getSimpleName(),
                        CloudTasksClientMetrics.CloudTasksClientExceptionMetricTags.METHOD_NAME, method.getName(),
                        CloudTasksClientMetrics.CloudTasksClientExceptionMetricTags.CLIENT_ID, clientId,
                        CloudTasksClientMetrics.CloudTasksClientExceptionMetricTags.QUEUE_NAME, queue,
                        CloudTasksClientMetrics.CloudTasksClientExceptionMetricTags.EXCEPTION, unwrapEx(e).getClass().getSimpleName()
                );
                monitoring.incrementMetric(CloudTasksClientMetrics.CLOUD_TASKS_CLIENT_ENQUEUE_FAILURE, errorTags);
                return CompletableFuture.failedFuture(e);
            } else {
                monitoring.incrementMetric(CloudTasksClientMetrics.CLOUD_TASKS_CLIENT_ENQUEUE_SUCCESS, tags);
                return CompletableFuture.completedFuture(r);
            }

        }).thenCompose(Function.identity());

        return future;

    }

    // Copy of SneakyThrow.sneak() (return instead of throw)
    private Throwable unwrapEx(Throwable t) {
        while((t instanceof ExecutionException) || (t instanceof CompletionException) || (t instanceof InvocationTargetException)) {
            if (t.getCause() == null) {
                break;
            }
            t = t.getCause();
        }
        return t;
    }

    private Task createTask(QueueName queueName, Task.Builder taskBuilder) {
        return client.createTask(queueName, taskBuilder.build());
    }


    private QueueName getQueue(final String queueId) {

        switch (environment) {

            case DEMO:
                return QueueName.of("demoenv-orderly-gcp", "us-west2", "new-" + queueId);
            case PRODUCTION:
                return QueueName.of("orderly-gcp", "us-central1", "new-" + queueId);
            case STAGING:
                return QueueName.of("staging-orderly-gcp", "us-west2", "new-" + queueId);

        }

        throw new IllegalStateException("GCPTasksClient can't be run from Environment." + environment + "!");

    }

}
