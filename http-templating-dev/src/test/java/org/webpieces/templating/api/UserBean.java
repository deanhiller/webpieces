package org.webpieces.templating.api;

public class UserBean {

	private String name;
	private int numSiblings;
	
	public UserBean(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNumSiblings() {
		return numSiblings;
	}
	public void setNumSiblings(int numSiblings) {
		this.numSiblings = numSiblings;
	}
}
