package com.orderlyhealth.googlecloud.cloudtasks.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;

import com.orderlyhealth.api.Endpoint;
import com.orderlyhealth.api.OrderlyHeaders;
import com.orderlyhealth.googlecloud.cloudtasks.GCPTasks;

import com.orderlyhealth.json.client.util.HttpsClientHelper;

@Singleton
public class LocalTasks implements GCPTasks {

    private static final Logger log = LoggerFactory.getLogger(LocalTasks.class);
    private static final int MAX_PARALLEL_FOR_LOCAL = 3;

    private final Executor exec;
    private final HttpsClientHelper clientHelper;

    @Inject
    public LocalTasks(final HttpsClientHelper clientHelper) {
        this.clientHelper = clientHelper;
        this.exec = Executors.newFixedThreadPool(MAX_PARALLEL_FOR_LOCAL, new ThreadFactoryBuilder().setNameFormat("LocalTasksSubmitter-%d").build());
    }

    @Override
    public String getPrefixedQueueId(String queueId) {
        return queueId;
    }

    @Override
    public void createTask(Endpoint endpoint, String queueId, Map<String, String> headers, Object obj) {
        List<Http2Header> headerList = new ArrayList<>();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            Http2Header header = new Http2Header(entry.getKey(), entry.getValue());
            headerList.add(header);
        }

        createTaskImpl(obj, endpoint, headerList);

    }

    private void createTaskImpl(Object o, Endpoint endpoint, List<Http2Header> headers) {

        exec.execute(() -> {

            try {

                setContext();

                clientHelper.sendHttpRequest(null, o, endpoint, Map.class, headers).get();

            }
            catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException("Failed to send http request for local task.", ex);
            }

        });

    }

    private void setContext() {

        RouterRequest rtrRq = new RouterRequest();

        rtrRq.requestState.put(OrderlyHeaders.CLIENT_ID.toString(), "orderly");
        rtrRq.requestState.put(OrderlyHeaders.TRANSACTION_ID.toString(), "e1084e12-1d47-11eb-ba33-822636a07d93"); //Setting this so it can be tracked in logs

        RequestContext ctx = new RequestContext(null, null, null, rtrRq, null);

        Current.setContext(ctx);

    }

}
