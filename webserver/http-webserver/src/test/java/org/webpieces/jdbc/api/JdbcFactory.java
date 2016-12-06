package org.webpieces.jdbc.api;

import org.webpieces.jdbc.impl.JdbcApiImpl;

public class JdbcFactory {

	public static JdbcApi create(String driver, String username, String password) {
		return new JdbcApiImpl(driver, username, password);
	}
}
