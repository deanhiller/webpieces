package org.webpieces.templatingdev.impl.source;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.templatingdev.api.GroovyGen;
import org.webpieces.templatingdev.impl.tags.ElseGen;
import org.webpieces.templatingdev.impl.tags.ElseIfGen;
import org.webpieces.templatingdev.impl.tags.IfGen;
import org.webpieces.templatingdev.impl.tags.ListGen;
import org.webpieces.templatingdev.impl.tags.VerbatimGen;

public class GenLookup {

	private Map<String, GroovyGen> generators = new HashMap<>();
	
	@Inject
	private ListGen listGen;
	
	protected void init() {
		put(new VerbatimGen());
		put(new IfGen());
		put(new ElseIfGen());
		put(new ElseGen());
		put(listGen);
	}

	protected void put(GroovyGen generator) {
		generators.put(generator.getName(), generator);
	}

	public GroovyGen lookup(String genName, TokenImpl token) {
		GroovyGen gen = generators.get(genName);
		return gen;
	}
}
