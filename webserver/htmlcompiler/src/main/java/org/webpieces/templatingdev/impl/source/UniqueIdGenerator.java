package org.webpieces.templatingdev.impl.source;

public class UniqueIdGenerator {

	private int id;

	public synchronized int generateId() {
		return id++;
	}

}
