package org.webpieces.templatingdev.impl.source;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.templatingdev.api.GroovyGen;
import org.webpieces.templatingdev.impl.tags.ElseGen;
import org.webpieces.templatingdev.impl.tags.ElseIfGen;
import org.webpieces.templatingdev.impl.tags.IfGen;
import org.webpieces.templatingdev.impl.tags.ListGen;

@Singleton
public class GenLookup {

	private final Map<String, GroovyGen> generators = new HashMap<>();
	
	private final ListGen listGen;

	@Inject
	public GenLookup(ListGen listGen) {
		super();
		this.listGen = listGen;
	}

	protected void init() {
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
