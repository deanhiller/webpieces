package org.webpieces.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
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
    public void connect(String ipAddress, String transport) {
        client = RestClient.builder(
                new HttpHost(ipAddress, 9200, transport),
                new HttpHost(ipAddress, 9201, transport)).build();
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
        return performRequest("PUT", "/"+index+"/doc/"+id, params, document);    
    }

    public CompletableFuture<Response> createAlias(String indexName, String alias) {
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
		if(jsonObj != null) {
	        String jsonString = null;
	        try {
	            jsonString = objectMapper.writeValueAsString(jsonObj);
	        } catch (IOException e) {
	            throw new RuntimeException("failed to translate to json object to string: "+jsonObj, e);
	        }
	        entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);			
		}
		
		CompletableFuture<Response> future = new CompletableFuture<Response>();
		ResponseListener responseListener = new ToFutureListener(future); 
		client.performRequestAsync(method, endpoint, params, entity, responseListener, headers);
		return future;
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

