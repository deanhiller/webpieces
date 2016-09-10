package org.webpieces.webserver.tags.app;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.webserver.basic.app.biz.Address;

public class Account {

	private String name;
	private int value;
	private String color;
	private List<Address> addresses = new ArrayList<>();

	public Account() {
	}
	
	public Account(String name, int value, String color) {
		this.name = name;
		this.value = value;
		this.color = color;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setColor(String color) {
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

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}
}
