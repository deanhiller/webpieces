package org.webpieces.webserver.basic.app.biz;

public class Address {

	private int number;
	private String street;
	private int zipCode;
	
	public int getNumber() {
		return number;
	}
	public String getStreet() {
		return street;
	}
	public int getZipCode() {
		return zipCode;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public void setZipCode(int zipCode) {
		this.zipCode = zipCode;
	}
}
