package org.webpieces.googlecloud.cloudtasks.old.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.webpieces.api.*;
import org.webpieces.api.client.OrderlyLocalClient;
import org.webpieces.api.client.RemoteCallStateCheck;
import org.webpieces.api.util.HttpsConfig;
import org.webpieces.api.util.SingletonSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

class LocalTasksClient extends OrderlyLocalClient<CloudTaskQueue> {

    private static final Logger log = LoggerFactory.getLogger(LocalTasksClient.class);

    private final ObjectMapper mapper;

    private final Supplier<String> internalHeader;

    public LocalTasksClient(final ServerInfo serverInfo, final RequestContextAccessor contextAccessor, final OrderlyServiceAddress serviceAddresses,
                            final HttpsConfig httpsConfig, final ObjectMapper mapper, final RemoteCallStateCheck remoteCallStateCheck,
                            final ExecutorService executor, final SecretManager secrets) {

        super(CloudTaskQueue.class, serverInfo, contextAccessor, remoteCallStateCheck, serviceAddresses, httpsConfig, executor);

        this.mapper = mapper;
        this.internalHeader = new SingletonSupplier<>(() -> secrets.getSecret("x-orderly-internal"));
    }

    @Override
    public Object invoke(Object instance, Method method, Object[] args) throws Throwable {

        checkInvocation(method, args);

        String path = getPath(method);

        if ((path == null) || path.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        CloudTaskQueue annotation = method.getAnnotation(CloudTaskQueue.class);
        OrderlyService destination = annotation.destination();

        if (destination == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (destination == OrderlyService.__CURRENT) {
            destination = serverInfo.getService();
        }

        InetSocketAddress socketAddress = serviceAddresses.getSocketAddress(destination);
        String scheme = (socketAddress.getPort() < 9000) ? "http" : "https";
        URI uri = new URI(scheme, null, socketAddress.getHostName(), socketAddress.getPort(), path, null, null);
        String body = mapper.writeValueAsString(args[0]);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader(OrderlyHeaders.INTERNAL_SECURE_KEY.toString(), internalHeader.get())
                .setHeader("User-Agent", "Orderly Local CloudTasks Client")
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        for (Map.Entry<String, String> header : getRequestContextAttributes().entrySet()) {
            if (!Objects.equals(header.getKey(), OrderlyHeaders.INTERNAL_SECURE_KEY.getHeaderName())) {
                builder.header(header.getKey(), header.getValue());
            }
        }

        HttpRequest request = builder.build();

        log.info("Submitting " + args[0].getClass().getName() + " to " + request.uri());

        CompletableFuture<Object> future = CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignore) {
                    }
                }).thenApply((v) -> httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()))
                //return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(task -> {

                    StringBuilder sb = new StringBuilder();

                    sb.append("Task submitted: ").append(request.uri()).append('\n');
                    sb.append("Curl request: ").append(getCurlCommand(request.uri().toString(), request.method(), request.headers(), body));

                    log.debug(sb.toString());

                })
                .thenApply(r -> null);

        return future;

    }

}
