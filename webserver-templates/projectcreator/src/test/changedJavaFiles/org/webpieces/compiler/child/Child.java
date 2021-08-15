package org.webpieces.compiler.child;

public class Child {

	private Grandchild child = new Grandchild();
	
	public int someValue() {
		return child.newFetchMethod();
	}
}
