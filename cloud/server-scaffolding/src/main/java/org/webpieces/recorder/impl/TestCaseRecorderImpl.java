package org.webpieces.recorder.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.microsvc.server.impl.MetaVarInfo;
import org.webpieces.recorder.api.DoNotRecord;
import org.webpieces.util.futures.XFuture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

public class TestCaseRecorderImpl implements TestCaseRecorder {

    private static final Logger log = LoggerFactory.getLogger(TestCaseRecorderImpl.class);

    private static Set<Class<?>> wrapperTypes = getWrapperTypes();

    private Map<String, Object> fullRequestContext;
    private List<EndpointInfo> endpointInfo = new ArrayList<>();

    public TestCaseRecorderImpl(Map<String, Object> fullRequestContext) {
        this.fullRequestContext = fullRequestContext;
    }

    private static Set<Class<?>> getWrapperTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    public void addEndpointInfo(EndpointInfo info) {
        endpointInfo.add(info);
    }

    @Override
    public EndpointInfo getLastEndpointInfo() {
        return endpointInfo.get(endpointInfo.size()-1);
    }

    public void spitOutTestCase(EndpointInfo microSvcEndpoint) {

        TestCaseHolder test = new TestCaseHolder();

        try {
            List<MetaVarInfo> metaInfos = new ArrayList<>();
            for (EndpointInfo info : endpointInfo) {
                MetaVarInfo meta = convert(info, true);
                metaInfos.add(meta);
            }

            MetaVarInfo svcMeta = convert(microSvcEndpoint, false);

            //1. print out the bootstrap of test case that extends base FeatureTest.java(all FeatureTest.java are the same)
            writeTestCaseToString(microSvcEndpoint, metaInfos, svcMeta, test);
            test.add("\n\n");
            addResponseCreators(metaInfos, test);
            test.add("\n\n");
            addRequestCreators(svcMeta, test);
            test.add("\n\n");

            log.info("Logging test case below\n***********************************************\n\n" + test);
        } catch (Throwable t) {
            //silently fail but log the issue so we do not impact production with a bug from generating test
            log.error("Failed to generate test case.  partial testcase=\n\n"+test, t);
        }
    }

    private void addRequestCreators(MetaVarInfo microSvcEndpoint, TestCaseHolder test) {
        test.add("public class Requests {\n");

        MetaVarInfo info = microSvcEndpoint;
        String requestClass = info.getRequestClassName();
        String varName = "request";
        test.add("\tpublic static "+requestClass+" create"+requestClass+"() {\n");
        test.add("\t\t"+requestClass+" "+varName+" = new "+requestClass+"();\n");
        writeFillInBeanCode(info.getInfo().getArgs()[0], varName, test, 0);
        test.add("\t\treturn "+varName+";\n");
        test.add("\t}\n");

        test.add("}");
        
    }

    private void writeFillInBeanCode(Object bean, String varName, TestCaseHolder test, int recurseLevel) {
        if(recurseLevel > 10)
            throw new IllegalStateException("Recursion greater than 10, probably a bug as beans are not that big(they shouldn't be)");

        //excludes inherited fields for now...(KISS until we need it)
        Field[] fields = bean.getClass().getDeclaredFields();
        for(Field f : fields) {
            DoNotRecord annotation = f.getAnnotation(DoNotRecord.class);
            if(annotation != null) {
                continue; //skip this one
            }
            Class<?> type = f.getType();
            try {
                Object value = getValue(bean, f);
                String setMethodName = fixName("set", f.getName());
                if(value == null) {
                    //do nothing but skip for deep beans so we don't nullpointer
                } else if(type == String.class) {
                    test.add("\t\t" + varName + "." + setMethodName + "(\"" +value+"\");\n");
                } else if(type.isEnum()) {
                    test.add("\t\t" + varName + "." + setMethodName + "(" +type.getSimpleName()+"."+value+");\n");
                } else if (type.isPrimitive() || wrapperTypes.contains(type)) {
                    test.add("\t\t" + varName + "." + setMethodName + "(" + value + ");\n");
                } else if (Map.class.isAssignableFrom(type)) {
                    test.add("\t\t//We need to implement the one for Map. field=\"+f.getName()+\"\n");
                } else if (List.class.isAssignableFrom(type)) {
                    List<Object> list = (List<Object>) value;
                    if(list.size() == 0) {
                        test.add("\t\t" + varName + "." + setMethodName + "(new ArrayList());\n");
                    } else {
                        Class<?> beanClazz = list.get(0).getClass();
                        String beanType = beanClazz.getSimpleName();
                        String listVarName = f.getName()+recurseLevel+"List";
                        test.add("\t\tList<"+beanType+"> "+listVarName+" = new ArrayList<>();\n");
                        test.add("\t\t"+varName+"."+setMethodName+"("+listVarName+");\n");
                        for(int i = 0; i < list.size(); i++) {
                            String itemInListVarName = f.getName()+recurseLevel+"_"+i;
                            Object listBean = list.get(i);
                            if(beanClazz.isEnum()) {
                                test.add("\t\t" + listVarName + ".add(" + beanType + "." + listBean + ");\n");
                            } else if(wrapperTypes.contains(beanClazz)) {
                                test.add("\t\t" + listVarName + ".add(" + listBean + ");\n");
                            } else {
                                test.add("\t\t" + beanType + " " + itemInListVarName + " = new " + beanType + "();\n");
                                test.add("\t\t" + listVarName + ".add(" + itemInListVarName + ");\n");
                                writeFillInBeanCode(listBean, itemInListVarName, test, recurseLevel+1);
                            }
                        }
                    }
                } else if (Set.class.isAssignableFrom(type)) {
                    test.add("\t\t//We need to implement the one for Set. field=\"+f.getName()+\"\n");
                } else if(UUID.class.isAssignableFrom(type)) {
                    test.add("\t\t UUID uuid = UUID.fromString(\"" +  value+"\");\n");
                    test.add("\t\t" + varName + "." + setMethodName + "(uuid);\n");
                } else if(LocalDateTime.class.isAssignableFrom(type)) {
                    test.add("\t\tLocalDateTime time = FIX THIS-> '" +  value+"';\n");
                    test.add("\t\t" + varName + "." + setMethodName + "(time);\n");
                } else {
                    String fieldVarName = f.getName() + recurseLevel;
                    //assume another bean and recurse
                    test.add("\t\t" + type.getSimpleName() + " " + fieldVarName + " = new " + type.getSimpleName() + "();\n");
                    test.add("\t\t" + varName + "." + setMethodName + "(" +fieldVarName+");\n");
                    writeFillInBeanCode(value, fieldVarName, test, recurseLevel+1);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed on field="+bean.getClass().getName()+"."+f.getName()+" and field type="+type.getSimpleName(), t);
            }
        }
        
    }

    private void writeValidateCode(Object bean, String varName, TestCaseHolder test, int recurseLevel) {
        if(recurseLevel > 10)
            throw new IllegalStateException("Recursion greater than 10, probably a bug as beans are not that big(they shouldn't be)");

        if(bean.getClass().isEnum()) {
            String value = bean.getClass().getSimpleName()+"."+bean;
            test.add("\t\tAssertions.assertEquals(" + value + ", " + varName +");\n");
            return;
        } else if(wrapperTypes.contains(bean.getClass())) {
            test.add("\t\tAssertions.assertEquals(" + bean + ", " + varName +");\n");
            return;
        }

        //excludes inherited fields for now...(KISS until we need it)
        Field[] fields = bean.getClass().getDeclaredFields();
        for(Field f : fields) {
            DoNotRecord annotation = f.getAnnotation(DoNotRecord.class);
            if(annotation != null) {
                continue; //skip this one
            }
            Class<?> type = f.getType();
            try {
                Object value = getValue(bean, f);
                String getMethodName = fixName("get", f.getName());
                if(value == null) {
                    test.add("\t\tAssertions.assertNull(" + varName + "." + getMethodName + "());\n");
                } else if(type.isEnum()) {
                    test.add("\t\tAssertions.assertEquals(" + type.getSimpleName()+"."+value + ", " + varName + "." +getMethodName+"());\n");
                } else if(type == String.class) {
                    test.add("\t\tAssertions.assertEquals(\"" + value + "\", " + varName + "." +getMethodName+"());\n");
                } else if (type.isPrimitive() || wrapperTypes.contains(type)) {
                    test.add("\t\tAssertions.assertEquals(" + value + ", " + varName + "." +getMethodName+"());\n");
                } else if (Map.class.isAssignableFrom(type)) {
                    test.add("\t\t//We need to implement the one for Map. field="+f.getName()+"\n");
                } else if (List.class.isAssignableFrom(type)) {
                    List<Object> list = (List<Object>) value;
                    if(list.size() == 0) {
                        test.add("\t\tAssertions.assertEquals(0, " + varName + "." + getMethodName + "().size());\n");
                    } else {
                        String beanType = list.get(0).getClass().getSimpleName();
                        String listVarName = f.getName()+recurseLevel+"List";
                        test.add("\t\tList<"+beanType+"> "+listVarName+" = "+varName + "." + getMethodName + "();\n");
                        for(int i = 0; i < list.size(); i++) {
                            Object listBean = list.get(i);
                            writeValidateCode(listBean, listVarName+".get("+i+")", test, recurseLevel+1);
                        }
                    }

                } else if (Set.class.isAssignableFrom(type)) {
                    test.add("\t\t//We need to implement the one for Set. field=" + f.getName() + "\n");
                } else if(UUID.class.isAssignableFrom(type)) {
                    test.add("\t\tAssertions.assertEquals(\"" + value + "\", " + varName + "." +getMethodName+"().toString());\n");
                } else if(LocalDateTime.class.isAssignableFrom(type)) {
                    test.add("\t\tAssertions.assertEquals(\"" + value + "\", " + varName + "." +getMethodName+"().toString());\n");
                } else {
                    String fieldVarName = f.getName() + recurseLevel;
                    //assume another bean and recurse
                    test.add("\t\t" + type.getSimpleName() + " " + fieldVarName + " = " + varName +"."+getMethodName + "();\n");
                    writeValidateCode(value, fieldVarName, test,  recurseLevel+1);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed on field="+bean.getClass().getName()+"."+f.getName()+" and field type="+type.getSimpleName(), t);
            }
        }
        
    }

    private String fixName(String prefix, String name) {
        return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private Object getValue(Object bean, Field f) throws IllegalAccessException {
        f.setAccessible(true);
        return f.get(bean);
    }

    private void writeTestCaseToString(EndpointInfo microSvcEndpoint, List<MetaVarInfo> metaInfo, MetaVarInfo svcMeta, TestCaseHolder test) {
        test.add("public class TestSomething extends FeatureTest {\n\n");
        test.add("\t@Test\n");
        test.add("\tpublic void testSomething() throws Exception {\n");
        fillInTestCase(microSvcEndpoint, metaInfo, svcMeta, test);
        test.add("\t}\n\n");

        EndpointInfo info = svcMeta.getInfo();
        if(!svcMeta.isReturnVoid()) {
            Object successResponse = info.getSuccessResponse();

            test.add("\tpublic void validateResponse(" + svcMeta.getResponseBeanClassName() + " response) {\n");
            //test.add("\t\t   //next generate validation based on response="+ successResponse +"\n");
            writeValidateCode(successResponse, "response", test, 0);
            test.add("\t}\n\n");
        }

        for(int i = 0; i < metaInfo.size(); i++) {
            MetaVarInfo info1 = metaInfo.get(i);
            String requestClassName = info1.getRequestClassName();
            test.add("\tpublic void validate"+requestClassName+"("+info1.getRequestClassName()+" request) {\n");
            writeValidateCode(info1.getInfo().getArgs()[0], "request", test, 0);
            test.add("\t}\n\n");
        }

        test.add("}");
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

    private void fillInTestCase(EndpointInfo microSvcEndpoint, List<MetaVarInfo> metaInfos, MetaVarInfo svcMeta, TestCaseHolder test) {


        //2. fill in mock success or failure responses
        for(int i = 0; i < metaInfos.size(); i++) {
            EndpointInfo info = endpointInfo.get(i);
            MetaVarInfo metaVarInfo = metaInfos.get(i);
            String varName = metaVarInfo.getSvcVarName();
            String svcName = metaVarInfo.getSvcName();
            Throwable failureResponse = info.getFailureResponse();
            if(failureResponse != null) {
                String simpleName = failureResponse.getClass().getSimpleName();
                String method = info.getMethod().getName().toUpperCase();
                String enumName = metaVarInfo.getSvcName()+"Method."+method;
                test.add("\t\t"+ varName + ".addValueToReturn("+enumName+", XFuture.failedFuture(new "+simpleName+"()));\n");
            } else {
                printSuccess(metaVarInfo, varName, test, i);
           }
        }


        String svcVarName = svcMeta.getSvcVarName();
        String requestClassName = svcMeta.getRequestClassName();

        test.add("\t\t"+requestClassName+" request = Requests.create"+requestClassName+"();\n");
        test.add("\n");
        test.add("\t\tXFuture<"+svcMeta.getResponseBeanClassName()+"> future = "+svcVarName+"."+ microSvcEndpoint.getMethod().getName()+"(request);\n");
        test.add("\t\t"+svcMeta.getResponseBeanClassName()+" respObj = future.get(5, TimeUnit.SECONDS);\n");
        test.add("\n");

        //validate response object
        if(!svcMeta.isReturnVoid()) {
            test.add("\t\tvalidateResponse(respObj);\n");
            test.add("\n");
        }

        //validate requests to mock objects...
        for(int i = 0; i < metaInfos.size(); i++) {
            MetaVarInfo info = metaInfos.get(i);
            String variableName = "req"+i;
            String enumName = getEnumStr(info);
            test.add("\t\t"+info.getRequestClassName()+" "+variableName+" = ("+info.getRequestClassName()+")"+info.getSvcVarName()+".getSingleRequestList("+enumName+").get(0);\n");
            test.add("\t\tvalidate"+info.getRequestClassName()+"("+variableName+");\n");
        }


        
    }

    private void addResponseCreators(List<MetaVarInfo> metaInfos, TestCaseHolder test) {
        test.add("public class Responses {\n");

        for(int i = 0; i < metaInfos.size(); i++) {
            MetaVarInfo info = metaInfos.get(i);
            if(info.isReturnVoid())
                continue; //skip generating method

            String varName = "resp";
            test.add("\tpublic static XFuture<"+info.getResponseBeanClassName()+"> create"+info.getResponseBeanClassName()+"() {\n");
            test.add("\t\t"+info.getResponseBeanClassName()+" "+varName+" = new "+info.getResponseBeanClassName()+"();\n");
            writeFillInBeanCode(info.getInfo().getSuccessResponse(), varName, test, 0);
            test.add("\t\treturn XFuture.completedFuture("+varName+");\n");
            test.add("\t}\n");
        }

        test.add("}");
    }

    private void printSuccess(MetaVarInfo info, String apiVarName, TestCaseHolder test, int i) {
        String respBeanName = info.getResponseBeanClassName();
        String enumName = getEnumStr(info);

        if(info.getInfo().isQueueApi()) {
            String jobRefId = info.getInfo().getJobRefId();
            if(jobRefId == null)
                throw new IllegalStateException("jobRefId should not be null on queue api method");

            test.add("\t\tSupplier<Object> "  + apiVarName + "Sup = () -> {\n");
            test.add("\t\t\tContext.put(Constants.WEBPIECES_SCHEDULE_RESPONSE, new JobReference(\""+jobRefId+"\"));\n");
            test.add("\t\t\treturn XFuture.completedFuture(null);\n");
            test.add("\t\t};\n");
            test.add("\t\t" + apiVarName + ".addCalculateRetValue("+enumName+", "+apiVarName+"Sup);\n");
            return;
        }

        test.add("\t\t" + apiVarName + ".addValueToReturn("+enumName+", Responses.create"+respBeanName+"());\n");
    }

    private String getEnumStr(MetaVarInfo info) {
        String method = info.getInfo().getMethod().getName().toUpperCase();
        String enumName = info.getSvcName()+"Method."+method;
        return enumName;
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
