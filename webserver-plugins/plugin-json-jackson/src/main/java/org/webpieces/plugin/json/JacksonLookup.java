package org.webpieces.plugin.json;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.http.exception.BadRequestException;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.extensions.ParamMeta;

import com.fasterxml.jackson.databind.JsonNode;

public class JacksonLookup implements BodyContentBinder {

	private JacksonJsonConverter mapper;
	private Set<Class> primitives = new HashSet<>();

	@Inject
	public JacksonLookup(JacksonJsonConverter mapper) {
		this.mapper = mapper;

		primitives.add(Boolean.class);
		primitives.add(Byte.class);
		primitives.add(Short.class);
		primitives.add(Integer.class);
		primitives.add(Long.class);
		primitives.add(Float.class);
		primitives.add(Double.class);
		primitives.add(String.class);
		primitives.add(Boolean.TYPE);
		primitives.add(Byte.TYPE);
		primitives.add(Short.TYPE);
		primitives.add(Integer.TYPE);
		primitives.add(Long.TYPE);
		primitives.add(Float.TYPE);
		primitives.add(Double.TYPE);
	}

	@Override
	public boolean canTransform(Class<?> paramClass) {
		if(primitives.contains(paramClass))
			return false;

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unmarshal(RequestContext ctx, ParamMeta meta, byte[] data) {
		try {
        	Class<T> entityClass = (Class<T>) meta.getFieldClass();

			if(data.length == 0)
				return null;
			else if(JsonNode.class.isAssignableFrom(entityClass))
				return (T) mapper.readTree(data);
			
			return mapper.readValue(data, entityClass);
		} catch(JsonReadException e) {
			throw new BadRequestException("invalid json in client request.  "+e.getMessage(), e);
		}
	}

	@Override
	public <T> RenderContent marshal(T bean) {
		byte[] content;
		if(bean == null) {
			content = "{}".getBytes(StandardCharsets.UTF_8);
		} else {
			content = mapper.writeValueAsBytes(bean);
		}
		return new RenderContent(content, KnownStatusCode.HTTP_200_OK.getCode(), KnownStatusCode.HTTP_200_OK.getReason(), JacksonCatchAllFilter.MIME_TYPE);
	}

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return Jackson.class;
	}

}
