package org.webpieces.templating.impl.source;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlGen;
import org.webpieces.templating.impl.tags.ElseIfGen;
import org.webpieces.templating.impl.tags.ElseGen;
import org.webpieces.templating.impl.tags.IfGen;
import org.webpieces.templating.impl.tags.ListGen;
import org.webpieces.templating.impl.tags.VerbatimGen;

public class GenLookup {

	private Map<String, HtmlGen> generators = new HashMap<>();
	
	@Inject
	private ListGen listGen;
	
	protected void init() {
		put(new VerbatimGen());
		put(new IfGen());
		put(new ElseIfGen());
		put(new ElseGen());
		put(listGen);
	}

	protected void put(HtmlGen generator) {
		generators.put(generator.getName(), generator);
	}

	public HtmlGen lookup(String genName, TokenImpl token) {
		HtmlGen gen = generators.get(genName);
		return gen;
	}
}
