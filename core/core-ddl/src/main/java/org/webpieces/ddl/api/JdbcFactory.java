package org.webpieces.ddl.api;

import org.webpieces.ddl.impl.JdbcApiImpl;

public class JdbcFactory {

	public static JdbcApi create(String driver, String username, String password) {
		return new JdbcApiImpl(driver, username, password);
	}
}
