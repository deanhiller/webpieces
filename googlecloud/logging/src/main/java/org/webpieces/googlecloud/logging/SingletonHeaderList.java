package org.webpieces.googlecloud.logging;

import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.util.context.PlatformHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SingletonHeaderList implements HeaderCtxList {

    private static SingletonHeaderList singleton;
    private AtomicReference<List<PlatformHeaders>> headersList = new AtomicReference<>(new ArrayList<>());

    @Override
    public List<PlatformHeaders> listHeaderCtxPairs() {
        return headersList.get();
    }

    private void setHeaderCtxList(List<PlatformHeaders> headers) {
        headersList.set(headers);
    }

    public synchronized static void initialize(List<PlatformHeaders> headers) {
        List<PlatformHeaders> result = new ArrayList<>();
        for(PlatformHeaders h : headers) {
            if(h.isWantLogged()) {
                result.add(h);
            }
        }
        SingletonHeaderList singleton1 = getSingleton();
        singleton1.setHeaderCtxList(result);
    }

    public synchronized static SingletonHeaderList getSingleton() {
        if(singleton == null)
            singleton = new SingletonHeaderList();
        return singleton;
    }

}
