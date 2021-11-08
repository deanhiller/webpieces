package org.webpieces.microsvc.server.api;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.microsvc.api.MethodValidator;
import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;

public class RESTApiRoutes implements Routes {

    private static final Pattern REGEX_SLASH_MERGE = Pattern.compile("/{2,}", Pattern.CASE_INSENSITIVE);

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
            MethodValidator.validateApiConvention(api, m);
        }
    }

    private void configurePath(RouteBuilder bldr, Method method) {

        String name = method.getName();
        String path = getPath(method);
        HttpMethod httpMethod = MethodValidator.getHttpMethod(method);

//NEED to put a JsonStreamHandle on top of these instead of ResponseStreamHandle(though could allow both?)
//        if (method.getParameterTypes().length > 0 && ResponseStreamHandle.class.equals(method.getParameterTypes()[0])) {
//            bldr.addStreamRoute(Port.HTTPS, httpMethod, path, controller.getName() + "." + name);
//        } else {
            //We do not want to deploy to production exposing over HTTP
            bldr.addContentRoute(Port.HTTPS, httpMethod, path, controller.getName() + "." + name);
//        }
    }





    protected String getPath(Method method) {

        Path path = method.getAnnotation(Path.class);

        if(path == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method);
        }

        String pathValue = getFullPath(method);

        if((pathValue == null) || pathValue.isBlank()) {
            throw new IllegalStateException("Invalid value for @Path annotation on " + method.getName() + ": " + pathValue);
        }

        return pathValue;

    }

    private String getFullPath(Method method) {

        Path cPath = method.getDeclaringClass().getAnnotation(Path.class);
        Path mPath = method.getAnnotation(Path.class);

        String cPathValue = null;
        String mPathValue = null;

        if(cPath != null) {
            cPathValue = cPath.value();
        }

        if(mPath != null) {
            mPathValue = mPath.value();
        }

        StringBuilder sb = new StringBuilder();

        if(cPathValue != null) {
            sb.append(cPathValue);
        }

        if(mPathValue != null) {
            sb.append(mPathValue);
        }

        String path = REGEX_SLASH_MERGE.matcher(sb.toString().trim()).replaceAll("/");

        return path;

    }



}
