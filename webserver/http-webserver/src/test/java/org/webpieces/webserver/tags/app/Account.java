package org.webpieces.webserver.tags.app;

import org.webpieces.webserver.basic.app.biz.Address;

import java.util.ArrayList;
import java.util.List;

public class Account {

	private String name;
	private int value;
	private String color;
	private List<Address> addresses = new ArrayList<>();
	private int id;

	public Account() {
	}
	
	public Account(int id, String name, int value, String color) {
		this.setId(id);
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
