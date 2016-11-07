package org.webpieces.httpfrontend.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

class MockResponseListener implements ResponseListener {

  private ConcurrentHashMap<ResponseId, List<Object>> responseLog = new ConcurrentHashMap<>();
  private ConcurrentHashMap<ResponseId, Boolean> completed = new ConcurrentHashMap<>();

  private List<Object> responseListForId(ResponseId id) {
    List<Object> list = responseLog.get(id);
    if(list == null) {
      list = new ArrayList<>();
      responseLog.put(id, list);
    }

    return list;
  }

  @Override
  public CompletableFuture<Void> incomingData(DataWrapper data, ResponseId id, boolean isLastData) {
    responseListForId(id).add(data);
    if(isLastData) {
      completed.put(id, true);
      synchronized (this) { this.notifyAll(); }
    }

    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void incomingTrailer(List<HasHeaderFragment.Header> headerList, ResponseId id, boolean isComplete) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void incomingResponse(HttpResponse resp, HttpRequest req, ResponseId id, boolean isComplete) {
    responseListForId(id).add(resp);
    if(isComplete) {
      completed.put(id, true);
      synchronized (this) { this.notifyAll(); }
    }
  }


  public synchronized ConcurrentHashMap<ResponseId, List<Object>> getResponseLog(long waitTimeMs, int count) {
    try {
      return getResponseLogImpl(waitTimeMs, count);
    } catch (InterruptedException e) {
      throw new RuntimeException("failed waiting", e);
    }
  }

  private synchronized ConcurrentHashMap<ResponseId, List<Object>> getResponseLogImpl(long waitTimeMs, int count) throws InterruptedException {
    long start = System.currentTimeMillis();
    while(completed.size() < count) {
      this.wait(waitTimeMs+500);
      if(completed.size() >= count)
        return responseLog;

      long time = System.currentTimeMillis() - start;
      if(time > waitTimeMs)
        throw new IllegalStateException("While waiting for "+count+" responses, some or all never came.  count that came="+completed.size());
    }

    return responseLog;
  }

  @Override
  public void failure(Throwable e) {

  }
}
