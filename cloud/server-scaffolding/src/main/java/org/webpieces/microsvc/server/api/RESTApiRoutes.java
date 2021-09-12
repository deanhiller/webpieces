package org.webpieces.microsvc.server.api;

import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.microsvc.api.Path;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.StringJoiner;

public class RESTApiRoutes implements Routes {

    private static final Logger log = LoggerFactory.getLogger(RESTApiRoutes.class);

    private final Class<?> api;
    private final Class<?> controller;

    public RESTApiRoutes(Class<?> api, Class<?> controller) {

        if (!api.isInterface()) {
            throw new IllegalArgumentException("api must be an interface and was not");
        }

        this.api = api;
        this.controller = controller;

    }

    @Override
    public void configure(DomainRouteBuilder domainRouteBldr) {
        RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
        Method[] methods = api.getMethods();

        for (Method m : methods) {

            configurePath(bldr, m);
            validateApiConvention(m);

        }
    }

    private void configurePath(RouteBuilder bldr, Method method) {
        String name = method.getName();
        Path annotation = method.getAnnotation(Path.class);

        if (annotation == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method);
        }

        String path = annotation.value();
        HttpMethod httpMethod = HttpMethod.valueOf(annotation.method().getMethod());

        if (method.getParameterCount() != 1 && httpMethod.equals(HttpMethod.POST)) {
            throw new IllegalArgumentException("The method on this API is invalid as it takes " + method.getParameterCount() + " and only 1 param is allowed.  method=" + method);
        }

        if (method.getParameterTypes().length > 0 && ResponseStreamHandle.class.equals(method.getParameterTypes()[0])) {
            bldr.addStreamRoute(Port.HTTPS, httpMethod, path, controller.getName() + "." + name);
        } else {
            //We do not want to deploy to production exposing over HTTP
            bldr.addContentRoute(Port.HTTPS, httpMethod, path, controller.getName() + "." + name);
        }
    }

    // Similar to validateApiConvention in PubSubServiceRoutes
    private void validateApiConvention(Method method) {

        String methodName = method.getName();

        // If it's not POST, don't worry about it for now
        if (!HttpMethod.valueOf(method.getAnnotation(Path.class).method().getMethod()).equals(HttpMethod.POST)) {
            return;
        }

        String pascalMethodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

        StringJoiner errorResponse = new StringJoiner("\n\n");

        String returnType = "";
        if (method.getGenericReturnType() instanceof ParameterizedType) {
            returnType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0].getTypeName();
            returnType = returnType.substring(returnType.lastIndexOf(".") + 1);
        }
        if (!returnType.endsWith("Response") || !returnType.toLowerCase().contains(methodName.toLowerCase())) {
            errorResponse.add(api.getName() + "::" + methodName + " return type does not follow Orderly REST API convention.\n" +
                    "\tThe return type must have the format \"CompletableFuture<" + pascalMethodName + "Response>\".\n" +
                    "\tUse the new convention, or add @Legacy or @Deprecated on this method and add a ticket to change your API");
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 1) {
            String parameterType = paramTypes[0].getSimpleName();
            if (!parameterType.endsWith("Request") || !parameterType.toLowerCase().contains(methodName.toLowerCase())) {
                errorResponse.add(api.getName() + "::" + methodName + " parameter type does not follow future-proof compatibility REST API convention.\n" +
                        "\tThe parameter type must have the format \"" + pascalMethodName + "Request\".\n" +
                        "\tUse the new convention, or add @Legacy or @Deprecated on this method and add a ticket to change your API");
            }
        } else {
            errorResponse.add(api.getName() + "::" + methodName + " must have exactly 1 parameter for POST requests");
        }

        if (errorResponse.length() != 0) {
            throw new IllegalArgumentException(errorResponse.toString());
        }

    }

}
