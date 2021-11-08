package org.webpieces.microsvc.api;

import org.webpieces.ctx.api.HttpMethod;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.StringJoiner;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

public class MethodValidator {

    // Similar to validateApiConvention in PubSubServiceRoutes
    public static void validateApiConvention(Class<?> api, Method method) {

        String methodName = method.getName();

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

        // If it's not POST, don't worry about it for now
        HttpMethod httpMethod = getHttpMethod(method);
        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
            validatePostPutMethods(api, method, methodName, pascalMethodName, errorResponse);
        }

        NotEvolutionProof annotation = api.getAnnotation(NotEvolutionProof.class);
        if(annotation == null) {
            Class<?>[] paramTypes = method.getParameterTypes();
            String requestName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1)+"Request";
            if(paramTypes.length != 1) {
                errorResponse.add(api.getName() + " must add @NotEvolutionProof annotation " +
                        "or "+methodName+" must only have 1 parameter of '"+requestName+"' to be prepared for clean evolution");
            } else if(!paramTypes[0].getSimpleName().equals(requestName)) {
                errorResponse.add(api.getName() + "::" + methodName + " must take 1 parameter called '"+requestName+"' and does not" +
                        " or you can add @NotEvolutionProof to create an http type REST api that is not very evolution proof");
            }
        }

        if (errorResponse.length() != 0) {
            throw new IllegalArgumentException(errorResponse.toString());
        }
    }

    private static void validatePostPutMethods(Class<?> api, Method method, String methodName, String pascalMethodName, StringJoiner errorResponse) {
        Class<?>[] paramTypes = method.getParameterTypes();
        boolean foundParam = false;
        for(Class clazz : paramTypes) {
            String parameterType = clazz.getSimpleName();
            String requestName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1)+"Request";
            if(parameterType.equals(requestName)) {
                foundParam = true;
            }
        }

        if(!foundParam) {
            String requestName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1)+"Request";
            errorResponse.add(api.getName()+"::"+methodName+" is missing a parameter named '"+requestName+"' which is required");
        }
    }

    public static HttpMethod getHttpMethod(Method method) {

        Path path = method.getAnnotation(Path.class);

        if(path == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method);
        }

        HttpMethod httpMethod;

        if(method.getAnnotation(DELETE.class) != null) {
            httpMethod = HttpMethod.DELETE;
        }
        else if(method.getAnnotation(GET.class) != null) {
            httpMethod = HttpMethod.GET;
        }
        else if(method.getAnnotation(OPTIONS.class) != null) {
            httpMethod = HttpMethod.OPTIONS;
        }
        else if(method.getAnnotation(PATCH.class) != null) {
            httpMethod = HttpMethod.PATCH;
        }
        else if(method.getAnnotation(POST.class) != null) {
            httpMethod = HttpMethod.POST;
        }
        else if(method.getAnnotation(PUT.class) != null) {
            httpMethod = HttpMethod.PUT;
        }
        else {
            throw new IllegalStateException("Missing or unsupported HTTP method annotation on " + method.getName() + ": Must be @DELETE,@GET,@OPTIONS,@PATCH,@POST,@PUT");
        }

        return httpMethod;

    }
}
