package webpiecesxxxxxpackage.json;

import org.webpieces.plugin.json.Jackson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class JsonRESTController implements ExampleRestAPI {

    @Inject
    public JsonRESTController() {
    }

    @Override
    @Jackson
    public CompletableFuture<MethodResponse> method(String id, int number) {
        return CompletableFuture.completedFuture(new MethodResponse(number, id));
    }

    @Override
    public CompletableFuture<PostTestResponse> postTest(String id, int number, @Jackson PostTestRequest request) {
        return CompletableFuture.completedFuture(new PostTestResponse(id, number, request.getSomething()));
    }

}
