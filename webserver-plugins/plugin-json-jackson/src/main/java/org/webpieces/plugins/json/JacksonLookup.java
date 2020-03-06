package org.webpieces.plugins.json;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.ClientDataError;
import org.webpieces.router.api.extensions.BodyContentBinder;

public class JacksonLookup implements BodyContentBinder {

	private ObjectMapper mapper;
	
	@Inject
	public JacksonLookup(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public <T> boolean isManaged(Class<T> entityClass, Class<? extends Annotation> paramAnnotation) {
		boolean isJnode = false;
		if(entityClass != null) //if this is null, next line will NullPointer instead of return false
			isJnode = JsonNode.class.isAssignableFrom(entityClass);

		if(paramAnnotation == Jackson.class || isJnode)
			return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unmarshal(Class<T> entityClass, byte[] data) {
		try {
			if(data.length == 0)
				throw new ClientDataError("Client did not provide a json request in the body of the request");
			
			if(JsonNode.class.isAssignableFrom(entityClass))
				return (T) mapper.readTree(data);
			
			return mapper.readValue(data, entityClass);
		} catch(JsonProcessingException e) {
			throw new ClientDataError("invalid json in client request.  "+e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException("should not occur", e);
		}
	}

	@Override
	public <T> RenderContent marshal(T bean) {
		try {
			byte[] content;
			if(bean == null)
				content = new byte[0];
			else
				content = mapper.writeValueAsBytes(bean);
			return new RenderContent(content, KnownStatusCode.HTTP_200_OK.getCode(), KnownStatusCode.HTTP_200_OK.getReason(), JacksonCatchAllFilter.MIME_TYPE);
		} catch (IOException e) {
			throw new RuntimeException("should not occur", e);
		}
	}

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return Jackson.class;
	}

}
