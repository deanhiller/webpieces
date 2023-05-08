package org.webpieces.microsvc.server.impl;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.recorder.impl.EndpointInfo;
import org.webpieces.recorder.impl.TestCaseRecorder;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.util.futures.XFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestCaseRecorderImpl implements TestCaseRecorder {

    private static final Logger log = LoggerFactory.getLogger(TestCaseRecorderImpl.class);

    private Map<String, Object> fullRequestContext;
    private List<EndpointInfo> endpointInfo = new ArrayList<>();

    public TestCaseRecorderImpl(Map<String, Object> fullRequestContext) {
        this.fullRequestContext = fullRequestContext;
    }

    public void addEndpointInfo(EndpointInfo info) {
        endpointInfo.add(info);
    }

    public void spitOutTestCase(EndpointInfo microSvcEndpoint) {

        List<MetaVarInfo> metaInfos = new ArrayList<>();
        for(EndpointInfo info : endpointInfo) {
            MetaVarInfo meta = convert(info, true);
            metaInfos.add(meta);
        }

        MetaVarInfo svcMeta = convert(microSvcEndpoint, false);

        //1. print out the bootstrap of test case that extends base FeatureTest.java(all FeatureTest.java are the same)
        String testCase = writeTestCaseToString(microSvcEndpoint, metaInfos, svcMeta);
        testCase += "\n\n";
        testCase += addResponseCreators(metaInfos);
        testCase += "\n\n";
        testCase += addRequestCreators(svcMeta);
        testCase += "\n\n";

        log.info("Logging test case below\n***********************************************\n\n"+testCase);
    }

    private String addRequestCreators(MetaVarInfo microSvcEndpoint) {
        String testCase = "public class Requests {\n";

        MetaVarInfo info = microSvcEndpoint;
        String varName = "request";
        testCase += "\tpublic "+info.getRequestClassName()+" create"+info.getSvcName()+"Request() {\n";
        testCase += "\t\t"+info.getRequestClassName()+" "+varName+" = new "+info.getRequestClassName()+"()\n";
        testCase += "\t\t //TODO: generate here to fill in this instance of the bean="+info.getInfo().getArgs()[0]+"\n";
        testCase += "\t\treturn "+varName+";\n";
        testCase += "\t}\n";

        testCase += "}";
        return testCase;
    }

    private String writeTestCaseToString(EndpointInfo microSvcEndpoint, List<MetaVarInfo> metaInfo, MetaVarInfo svcMeta) {
        String testCase = "public class TestSomething extends FeatureTest {\n\n";
        testCase += "\t@Test\n";
        testCase += "\tpublic void testSomething() {\n";

        testCase += fillInTestCase(microSvcEndpoint, metaInfo, svcMeta);

        EndpointInfo info = svcMeta.getInfo();
        Object successResponse = info.getSuccessResponse();

        testCase += "\t}\n\n";
        testCase += "\tpublic void validateResponse("+svcMeta.getResponseBeanClassName()+" response) {\n";
        testCase += "\t\t   //next generate validation based on response="+ successResponse +"\n";
        testCase += "\t}\n\n";

        for(int i = 0; i < metaInfo.size(); i++) {
            MetaVarInfo info1 = metaInfo.get(i);
            String requestClassName = info1.getRequestClassName();
            testCase += "\tpublic void validate"+requestClassName+"("+info1.getRequestClassName()+" request) {\n";
            testCase += "\t\t   //next generate validation based on request="+ info1.getInfo().getArgs()[0]+"\n";
            testCase += "\t}\n\n";
        }

        testCase += "}";
        return testCase;
    }

    private MetaVarInfo convert(EndpointInfo info, boolean isMock) {
        Class api = findApi(info.getMethod().getDeclaringClass());

        String svcName = api.getSimpleName();
        String varName = "mock"+api.getSimpleName();
        if(!isMock) {
            String clazzName = api.getSimpleName();
            varName = Character.toLowerCase(clazzName.charAt(0)) + clazzName.substring(1);
        }

        if(info.getArgs().length != 1)
            throw new IllegalStateException("We are KISS and start with supporting only 1 argument at this time");

        Object arg1 = info.getArgs()[0];
        String requestClassName = arg1.getClass().getSimpleName();

        Class<?> returnType = info.getMethod().getReturnType();
        if(XFuture.class != returnType) {
            throw new IllegalStateException("Only XFuture responses supported right now");
        }

        String fullTypeName = info.getMethod().getGenericReturnType().getTypeName();
        int indexDot = fullTypeName.lastIndexOf(".");
        int indexGreaterThan = fullTypeName.lastIndexOf(">");
        String responseBeanClassName = fullTypeName.substring(indexDot+1, indexGreaterThan);

        return new MetaVarInfo(varName, svcName, requestClassName, responseBeanClassName, info);
    }

    private String fillInTestCase(EndpointInfo microSvcEndpoint, List<MetaVarInfo> metaInfos, MetaVarInfo svcMeta) {
        String testCase = "";

        //2. fill in mock success or failure responses
        for(int i = 0; i < metaInfos.size(); i++) {
            EndpointInfo info = endpointInfo.get(i);
            MetaVarInfo metaVarInfo = metaInfos.get(i);
            String varName = metaVarInfo.getSvcVarName();
            String svcName = metaVarInfo.getSvcName();
            Throwable failureResponse = info.getFailureResponse();
            if(failureResponse != null) {
                String simpleName = failureResponse.getClass().getSimpleName();
                testCase += "\t\t"+ varName + ".addValueToReturn(XFuture.failedFuture(new "+simpleName+"()));\n";
            } else {
                Object successResponse = info.getSuccessResponse();
                testCase = printSuccess(successResponse, metaVarInfo.getResponseBeanClassName(), varName, i);
           }
        }


        String svcVarName = svcMeta.getSvcVarName();
        String requestClassName = svcMeta.getRequestClassName();

        testCase += "\t\t"+requestClassName+" request = Requests.createSomethingRequest();\n";
        testCase += "\n";
        testCase += "\t\tXFuture<"+svcMeta.getResponseBeanClassName()+"> future = "+svcVarName+"."+ microSvcEndpoint.getMethod().getName()+"(request);\n";
        testCase += "\t\t"+svcMeta.getResponseBeanClassName()+" respObj = future.get(5, TimeUnit.SECONDS);\n";
        testCase += "\n";

        //validate response object
        testCase += "\t\tvalidateResponse(respObj)\n";
        testCase += "\n";

        //validate requests to mock objects...
        for(int i = 0; i < metaInfos.size(); i++) {
            MetaVarInfo info = metaInfos.get(i);
            String variableName = "req"+i;
            testCase += "\t\t"+info.getRequestClassName()+" "+variableName+" = ("+info.getRequestClassName()+")"+info.getSvcVarName()+".getCalledMethodList().get(0);\n";
            testCase += "\t\tvalidate"+info.getRequestClassName()+"("+variableName+");\n";
        }


        return testCase;
    }

    private String addResponseCreators(List<MetaVarInfo> metaInfos) {
        String testCase = "public class Responses {\n";

        for(int i = 0; i < metaInfos.size(); i++) {
            MetaVarInfo info = metaInfos.get(i);
            String varName = "resp";
            testCase += "\tpublic "+info.getResponseBeanClassName()+" create"+info.getResponseBeanClassName()+"() {\n";
            testCase += "\t\t"+info.getResponseBeanClassName()+" "+varName+" = new "+info.getResponseBeanClassName()+"()\n";
            testCase += "\t\t //TODO: generate here to fill in this instance of the bean="+info.getInfo().getSuccessResponse()+"\n";
            testCase += "\t\treturn "+varName+";\n";
            testCase += "\t}\n";
        }

        testCase += "}";
        return testCase;
    }

    private String printSuccess(Object successResponse, String respBeanName, String apiVarName, int i) {
        String testCase = "";
        if(successResponse == null) {
            testCase += "\t\t" + apiVarName + ".addValueToReturn(XFuture.completedFuture(null));\n";
            return testCase;
        }

        Class<?> aClass = successResponse.getClass();
        String variableName = "var"+i;
        testCase += "\t\t"+aClass.getSimpleName()+" "+variableName+" = Responses.create" + respBeanName + "();\n";
        testCase += "\t\t" + apiVarName + ".addValueToReturn(XFuture.completedFuture("+variableName+"));\n";

        return testCase;
    }

    private Class findApi(Class<?> svc) {
        //Force all 'recordable' APIs to tell us they are recordable by ending in Api or fail.
        //legacy apis can be wrapped in recordable APIs to allow generation

        if(svc.isInterface()) {
            if(!svc.getSimpleName().endsWith("Api"))
                throw new IllegalStateException("Your interface must end in Api to be recordable.  interface="+svc);
            return svc;
        }
        Class<?>[] interfaces = svc.getInterfaces();
        for(Class c : interfaces) {
            if(c.getSimpleName().endsWith("Api"))
                return c;
        }
        throw new IllegalStateException("Could not find api so you cannot record.  disable recording or fix svc to implement xxxxApi.java="+svc);
    }
}
