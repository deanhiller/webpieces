package org.webpieces.templating.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class ClosureUtil {

//	private final static Logger log = LoggerFactory.getLogger(ClosureUtil.class);
//	private static CountThreadLocal closureDepth = new CountThreadLocal();
	
    public static String toString(String name, Closure<?> closure, Map<String, Object> groovyVariablesBound) {
//    	boolean enabled = log.isInfoEnabled();
//    	if(enabled) {
//    		int count = closureDepth.get().incrementAndGet();
//    		String tabs = generate(count);
//    		if(groovyVariablesBound == null)
//    			groovyVariablesBound = new HashMap<>();
//    		log.info(tabs+"closure '"+name+"' nestLevel="+count+" {");
//    		for(Entry<String, Object> entry : groovyVariablesBound.entrySet()) {
//    			log.info(tabs+"   "+entry.getKey()+"="+entry.getValue());
//    		}
//    	}
    	
        PrintWriter oldWriter = (PrintWriter) closure.getProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME);
        StringWriter newWriter = new StringWriter();
        closure.setProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, new PrintWriter(newWriter));
        closure.call();
        closure.setProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, oldWriter);
//
//        if(enabled) {
//        	int count = closureDepth.get().getAndDecrement();
//    		String tabs = generate(count);
//    		log.info(tabs+"} // end "+name);
//
//        }
        
        return newWriter.toString();
    }
    
//    private static String generate(int count) {
//    	String tabs = "";
//    	for(int i = 0; i < count; i++) {
//    		tabs += "   ";
//    	}
//		return tabs;
//	}
//
//	private static class CountThreadLocal extends ThreadLocal<AtomicInteger> {
//		@Override
//		protected AtomicInteger initialValue() {
//			return new AtomicInteger(0);
//		}
//    }
	
	
}
