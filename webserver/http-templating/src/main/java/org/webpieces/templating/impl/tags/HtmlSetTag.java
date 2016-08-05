package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;

import groovy.lang.Closure;

public class HtmlSetTag implements HtmlTag {

	@Override
	public void runTag(Map<?, ?> args, Closure body, PrintWriter out) {
		
	}

	@Override
	public String getName() {
		return null;
	}

}
