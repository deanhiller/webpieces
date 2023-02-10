package org.webpieces.microsvc.server.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.microsvc.api.MethodValidator;
import org.webpieces.router.api.routebldr.DefaultCorsProcessor;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.impl.routebldr.CurrentRoutes;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;

public class RESTApiRoutes implements Routes {

    private static final Pattern REGEX_SLASH_MERGE = Pattern.compile("/{2,}", Pattern.CASE_INSENSITIVE);

    private static final Logger log = LoggerFactory.getLogger(RESTApiRoutes.class);

    private final Class<?> api;
    private final Class<?> controller;
    private CorsConfig corsConfig;

    public RESTApiRoutes(Class<?> api, Class<?> controller) {
        this(api, controller, null);
    }

    /**
     * @param corsConfig - enable CORS by passing in CorsConfig
     */
    public RESTApiRoutes(Class<?> api, Class<?> controller, CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
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
        if(methods.length == 0)
            throw new UnsupportedOperationException("you must have at least 1 method in your api");

        if (corsConfig != null) {
            Set<String> domains = Set.of(corsConfig.getDomains());
            Set<String> allowedRequestHeaders = Set.of(corsConfig.getAllowedRequestHeaders());
            Set<String> exposedResponseHeaders = Set.of(corsConfig.getExposedResponseHeaders());
            CurrentRoutes.setProcessCorsHook(
                    new DefaultCorsProcessor(
                            domains,
                            allowedRequestHeaders,
                            exposedResponseHeaders,
                            corsConfig.isAllowCredentials(),
                            corsConfig.getExpiredTimeSeconds()
                    )
            );
        }

        boolean forceVoidForAll = MethodValidator.detectVoidOrElse(methods[0]);
        for (Method m : methods) {
            configurePath(bldr, m);
            MethodValidator.validateApiConvention(api, m, forceVoidForAll);
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
