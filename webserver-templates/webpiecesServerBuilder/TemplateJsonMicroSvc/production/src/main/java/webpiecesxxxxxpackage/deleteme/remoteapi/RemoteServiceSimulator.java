package webpiecesxxxxxpackage.deleteme.remoteapi;

import org.webpieces.util.futures.XFuture;

public class RemoteServiceSimulator implements RemoteService {
    @Override
    public XFuture<FetchValueResponse> fetchValue(FetchValueRequest request) {
        FetchValueResponse resp = new FetchValueResponse();
        return XFuture.completedFuture(resp);
    }
}