package org.webpieces.httpfrontend.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.mock.MockSuperclass;

public class MockResponseListener extends MockSuperclass implements ResponseListener {
  @Override
  public void incomingResponse(HttpResponse resp, HttpRequest req, ResponseId id, boolean isComplete) {

  }

  @Override
  public CompletableFuture<Void> incomingData(DataWrapper data, ResponseId id, boolean isComplete) {
    return null;
  }

  @Override
  public void incomingTrailer(List<HasHeaderFragment.Header> headers, ResponseId id, boolean isComplete) {

  }

  @Override
  public void failure(Throwable e) {

  }
}
