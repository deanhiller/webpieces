package org.webpieces.microsvc.client.impl;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

public class ClientCreator {

    public <T> T createClient(Class<T> apiInterface, com.orderlyhealth.json.client.util.HttpsClientHelper clientHelper, InetSocketAddress addr) {
        com.orderlyhealth.json.client.util.InternalAddHeaders addHeaders = new com.orderlyhealth.json.client.util.InternalAddHeaders();
        return createClient(apiInterface, clientHelper, addr, addHeaders);
    }

    public <T> T createClient(Class<T> apiInterface, com.orderlyhealth.json.client.util.HttpsClientHelper clientHelper, InetSocketAddress addr, com.orderlyhealth.json.client.util.InternalAddHeaders addHeaders) {

        return (T) Proxy.newProxyInstance(apiInterface.getClassLoader(),
                new Class[] { apiInterface },
                new com.orderlyhealth.json.client.util.WebpiecesClientImplementation(clientHelper, addr, addHeaders));

    }
}
