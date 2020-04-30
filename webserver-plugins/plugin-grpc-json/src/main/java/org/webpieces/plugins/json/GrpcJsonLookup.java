package org.webpieces.plugins.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.extensions.BodyContentBinder;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

public class GrpcJsonLookup implements BodyContentBinder {
    @Override
    public <T> boolean isManaged(Class<T> entityClass, Class<? extends Annotation> paramAnnotation) {
    	//we could detect if the class is a grpc class BUT instead just use annotations for now...
        if(paramAnnotation == GrpcJson.class)
            return true;

        return false;
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T unmarshal(Class<T> paramTypeToCreate, byte[] data) {
        try {

            String json = new String(data, Charset.forName("UTF-8"));
            JsonFormat.Parser parser = JsonFormat.parser();

            Method m = paramTypeToCreate.getMethod("newBuilder");
            Message.Builder builder = (Message.Builder) m.invoke(null);
            parser.merge(json, builder);

            return (T) builder.build();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("failed", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("failed", e);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("failed", e);
        }
    }

    @Override
    public <T> RenderContent marshal(T bean) {
        try {
            JsonFormat.Printer jsonPrinter = JsonFormat.printer();
            String json = jsonPrinter.print((MessageOrBuilder) bean);
            byte[] reqAsBytes = json.getBytes(Charset.forName("UTF-8"));

            return new RenderContent(reqAsBytes, KnownStatusCode.HTTP_200_OK.getCode(), KnownStatusCode.HTTP_200_OK.getReason(), GrpcJsonCatchAllFilter.MIME_TYPE);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Could not marshal bean="+bean.getClass(), e);
        }
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return GrpcJson.class;
    }
}
