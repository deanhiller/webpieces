package webpiecesxxxxxpackage.json;

import org.webpieces.plugin.json.Jackson;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.webpieces.util.futures.XFuture;

@Singleton
public class JsonRESTController implements ExampleRestAPI {

    @Inject
    public JsonRESTController() {
    }

    @Override
    @Jackson
    public XFuture<MethodResponse> method(String id, int number) {
        return XFuture.completedFuture(new MethodResponse(number, id));
    }

    @Override
    public XFuture<PostTestResponse> postTest(String id, int number, @Jackson PostTestRequest request) {
        return XFuture.completedFuture(new PostTestResponse(id, number, request.getSomething()));
    }

}
