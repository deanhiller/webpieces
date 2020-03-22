package org.webpieces.elasticsearch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.elasticsearch.client.Response;
import org.junit.Test;
import org.webpieces.elasticsearch.mapping.BooleanMapping;
import org.webpieces.elasticsearch.mapping.DateMapping;
import org.webpieces.elasticsearch.mapping.ElasticIndex;
import org.webpieces.elasticsearch.mapping.Mappings;
import org.webpieces.elasticsearch.mapping.NestedMapping;
import org.webpieces.elasticsearch.mapping.PropertyMapping;
import org.webpieces.elasticsearch.mapping.SingleType;
import org.webpieces.elasticsearch.mapping.TextMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestElasticStuff {

    private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testMappings() throws InterruptedException, ExecutionException, IOException, TimeoutException {
		TextMapping name = new TextMapping();
		
		Map<String, PropertyMapping> dataSourceProps = new HashMap<String, PropertyMapping>();
		dataSourceProps.put("name", name);
		
		NestedMapping dataSource = new NestedMapping();
		dataSource.setProperties(dataSourceProps);
		
		
        Map<String, PropertyMapping> properties = new HashMap<String, PropertyMapping>();
        properties.put("dataSource", dataSource);
        properties.put("dbCreatedAt", new DateMapping());
        properties.put("dbUpdatedAt", new DateMapping());

        SingleType doc = new SingleType();
        doc.setProperties(properties);

        Mappings mappings = new Mappings();
		mappings.set_doc(doc);
        
        ElasticIndex index = new ElasticIndex();
		index.setMappings(mappings);

        String s = mapper.writeValueAsString(index);
        System.out.println("json="+s);
	}
}
