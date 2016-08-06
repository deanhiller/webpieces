package org.webpieces.templating.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class ClosureUtil {

    public static String toString(Closure<?> closure) {
        PrintWriter oldWriter = (PrintWriter) closure.getProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME);
        StringWriter newWriter = new StringWriter();
        closure.setProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, new PrintWriter(newWriter));
        closure.call();
        closure.setProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, oldWriter);
        return newWriter.toString();
    }
    
}
