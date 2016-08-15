package org.webpieces.webserver.tags.app;

public class Account {

	private String name;
	private int value;
	private String color;

	public Account(String name, int value, String color) {
		this.name = name;
		this.value = value;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public String getColor() {
		return color;
	}
	
}
