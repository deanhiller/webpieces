package org.webpieces.templating.api;

import java.io.PrintWriter;
import java.util.Map;

import groovy.lang.Closure;

public interface HtmlTag {

	public void runTag(Map<?, ?> args, Closure body, PrintWriter out);

	public String getName();
		
}
