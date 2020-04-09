package org.webpieces.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.inject.Singleton;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.elasticsearch.actions.Action;
import org.webpieces.elasticsearch.actions.AliasChange;
import org.webpieces.elasticsearch.actions.AtomicActionList;
import org.webpieces.elasticsearch.mapping.ElasticIndex;

import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class ElasticClient {

    private static final Logger log = LoggerFactory.getLogger(ElasticClient.class);
    private RestClient client;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ElasticClient() {
        log.info("constructing singleton");
    }

    /**
     * Always connect in a method NOT in construction so that people don't accidentally connect to servers in
     * tests (ie. every line in guice Module.configure is hit for tests even if you swap it out in a test so to avoid that,
     * we do not create this in guice)
     */
    public void connect(String ipAddress, int port) {
        client = RestClient.builder(new HttpHost(ipAddress, port, "https")).build();
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException("Close failed");
        }
    }

    public CompletableFuture<Response> loadDocument(String index, long id, Object document) {
        Map<String, String> params = Collections.emptyMap();
        //url format is /{index}/{type}/{documentId}
        //type goes away in future releases as well so don't put different types in the same index!!!(use new index)
        return performRequest("PUT", "/"+index+"/_doc/"+id, params, document);
    }

    public CompletableFuture<Response> createAlias(String alias, String indexName) {
        Map<String, String> params = Collections.emptyMap();
        
        AliasChange addAlias = new AliasChange();
        addAlias.setIndex(indexName);
        addAlias.setAlias(alias);
        
        List<Action> actions = new ArrayList<Action>();
		actions.add(new Action(addAlias , true));
        
        AtomicActionList list = new AtomicActionList();
		list.setActions(actions);
        
    	return performRequest("POST", "/_aliases", params, list);
    }
    
	public CompletableFuture<Response> getAliases(String index) {
        Map<String, String> params = Collections.emptyMap();
    	return performRequest("GET", "/"+index+"/_alias/*", params, null);
	}
	
    public CompletableFuture<Response> renameAlias(String previousIndex, String newIndex, String alias) {
        Map<String, String> params = Collections.emptyMap();

        AliasChange removeAlias = new AliasChange();
        removeAlias.setIndex(previousIndex);
        removeAlias.setAlias(alias);
        
        AliasChange addAlias = new AliasChange();
        addAlias.setIndex(newIndex);
        addAlias.setAlias(alias);
        
        List<Action> actions = new ArrayList<Action>();
        actions.add(new Action(removeAlias, false));
		actions.add(new Action(addAlias , true));
        
        AtomicActionList list = new AtomicActionList();
		list.setActions(actions);
        
    	return performRequest("POST", "/_aliases", params, list);
    }
    
    public CompletableFuture<Response> deleteIndex(String name) {
        Map<String, String> params = Collections.emptyMap();
    	return performRequest("DELETE", "/"+name, params, null);
    }
    
	public CompletableFuture<Response> createIndex(String name, ElasticIndex index) {
        Map<String, String> params = Collections.emptyMap();
		return performRequest("PUT", "/"+name, params, index);
	}
	
	public CompletableFuture<Response> performRequest(
			String method, String endpoint, Map<String, String> params, Object jsonObj, Header... headers) {
		HttpEntity entity = null;
        String jsonString = null;
		if(jsonObj != null) {
	        try {
	            jsonString = objectMapper.writeValueAsString(jsonObj);
	        } catch (IOException e) {
	            throw new RuntimeException("failed to translate to json object to string: "+jsonObj, e);
	        }
	        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);			
		}
		
		String jsonStr = jsonString;
		CompletableFuture<Response> future = new CompletableFuture<Response>();
		ResponseListener responseListener = new ToFutureListener(future);

		RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
		Header[] allHeaders = new Header[headers.length + 1];
		for(Header header: headers) {
			builder.addHeader(header.getName(), header.getValue());
        }
		builder.addHeader("Authorization", "ApiKey V0xKeEdIRUIyMGdKdjF0QlZoWmc6X0NvaU9seGZSZHFxd283SjIyYXhvdw==");

		Request requst = new Request(method, endpoint);
		requst.setEntity(entity);
		requst.setOptions(builder);
		requst.addParameters(params);

		client.performRequestAsync(requst, responseListener);
		
		return future.handle( (r, e) -> {
			if(e != null) {
				CompletableFuture<Response> f = new CompletableFuture<Response>();
				f.completeExceptionally(new RuntimeException("json failed to be processed by elastic search="+jsonStr, e));
				return f;
			}
			return CompletableFuture.completedFuture(r);
		}).thenCompose(Function.identity());
	}
	
	private static class ToFutureListener implements ResponseListener {

		private CompletableFuture<Response> future;

		public ToFutureListener(CompletableFuture<Response> future) {
			this.future = future;
		}

		@Override
		public void onSuccess(Response response) {
            int status = response.getStatusLine().getStatusCode();
            if(status != 201 && status != 200) {
                throw new ElasticFailException("Invalid response, response="+response, response);
            }
			future.complete(response);
		}

		@Override
		public void onFailure(Exception exception) {
			future.completeExceptionally(exception);
		}
	}

}

