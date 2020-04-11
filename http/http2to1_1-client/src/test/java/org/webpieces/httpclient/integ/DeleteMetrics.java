package org.webpieces.httpclient.integ;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.dto.FullResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteMetrics {
    public static void main(String[] args) throws InterruptedException, TimeoutException, ExecutionException, IOException {
        String homeDir = System.getProperty("user.home");
        String accessToken = getAccessTokenFromOAuth2(homeDir+"/workspace/keys/cloudMonitoringKey.json");
        String key = "&key=AIzaSyBZj3GzcP-8wLeJW75Pf_aSXIue15CmjqE";
        HttpHelper httpHelper = new HttpHelper(key, accessToken);


        //send GET https://monitoring.googleapis.com/v3/projects/orderly-gcp/metricDescriptors?filter=metric.type%3Dhas_substring(%22custom%22)&key=[YOUR_API_KEY] HTTP/1.1
        //from response, get list of all descriptors' types
        String metricListPath = "/v3/projects/orderly-gcp/metricDescriptors" +
                "?filter=metric.type%3Dhas_substring(%22custom%22)";

        CompletableFuture<FullResponse> response = httpHelper.sendHttpRequest("GET", metricListPath);

        response.thenApply(httpResp -> {
            DataWrapper payload = httpResp.getPayload();
            String contents = payload.createStringFromUtf8(0, payload.getReadableSize());
            System.out.println(contents);
            MetricDescriptors descriptors = httpHelper.unmarshal(httpResp, contents, MetricDescriptors.class);
            return null;
        });

        //String metricDeletePrefix = "/v3/projects/orderly-gcp/metricDescriptors/custom.googleapis.com/";
        //String contents = payload.createStringFromUtf8(0, payload.getReadableSize());
        //List<String> descriptors = httpHelper.unmarshal(response).getTypes();

//        for (String descriptor:descriptors) {
//            //send DELETE https://monitoring.googleapis.com/v3/projects/orderly-gcp/metricDescriptors/custom.googleapis.com/<metric name>?key=[YOUR_API_KEY] HTTP/1.1
//            CompletableFuture<FullResponse> deleteResponse = httpHelper.sendHttpRequest(request, "DELETE", metricDeletePrefix + descriptor);
//            deleteResponse.wait(10);
//            System.out.println("Delete metric: " + descriptor);
//        }
        response.get(100000, TimeUnit.SECONDS);
        System.out.println(response.getClass());
        System.out.println(response.toString());
        System.out.println("Deleted all metrics.");
        System.exit(0);
    }


    private static String getAccessTokenFromOAuth2(String pathToSvcAcctJson) throws IOException {
        GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(pathToSvcAcctJson));
        List<String> scopes = new ArrayList<>();
        scopes.add("https://www.googleapis.com/auth/cloud-platform");
        scopes.add("https://www.googleapis.com/auth/monitoring");
        scopes.add("https://www.googleapis.com/auth/monitoring.read");
        scopes.add("https://www.googleapis.com/auth/monitoring.write");
        GoogleCredentials scoped = credential.createScoped(scopes);
        scoped.refreshIfExpired();
        AccessToken accessToken = scoped.getAccessToken();
        return accessToken.getTokenValue();
    }
}
