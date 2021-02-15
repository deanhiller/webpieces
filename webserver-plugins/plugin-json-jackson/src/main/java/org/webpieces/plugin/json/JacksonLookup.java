package org.webpieces.plugin.json;

import java.lang.annotation.Annotation;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.BadClientRequestException;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.extensions.ParamMeta;

import com.fasterxml.jackson.databind.JsonNode;

public class JacksonLookup implements BodyContentBinder {

	private JacksonJsonConverter mapper;
	
	@Inject
	public JacksonLookup(JacksonJsonConverter mapper) {
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
	public <T> T unmarshal(RequestContext ctx, ParamMeta meta, byte[] data) {
		try {
        	Class<T> entityClass = (Class<T>) meta.getFieldClass();
        	
			ctx.getRequest().requestState.put(JacksonCatchAllFilter.JSON_REQUEST_KEY, data);
			if(data.length == 0)
				throw new BadClientRequestException("Client did not provide a json request in the body of the request");		
			else if(JsonNode.class.isAssignableFrom(entityClass))
				return (T) mapper.readTree(data);
			
			return mapper.readValue(data, entityClass);
		} catch(JsonReadException e) {
			throw new BadClientRequestException("invalid json in client request.  "+e.getMessage(), e);
		}
	}

	@Override
	public <T> RenderContent marshal(T bean) {
		byte[] content;
		if(bean == null)
			content = new byte[0];
		else
			content = mapper.writeValueAsBytes(bean);
		return new RenderContent(content, KnownStatusCode.HTTP_200_OK.getCode(), KnownStatusCode.HTTP_200_OK.getReason(), JacksonCatchAllFilter.MIME_TYPE);
	}

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return Jackson.class;
	}

}
