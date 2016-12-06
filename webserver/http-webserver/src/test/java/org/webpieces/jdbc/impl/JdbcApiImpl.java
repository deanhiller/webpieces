package org.webpieces.jdbc.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.webpieces.jdbc.api.JdbcApi;
import org.webpieces.jdbc.api.SqlRuntimeException;

public class JdbcApiImpl implements JdbcApi {

	private String driver;
	private String username;
	private String password;

	public JdbcApiImpl(String driver, String username, String password) {
		this.driver = driver;
		this.username = username;
		this.password = password;
	}

	private Connection createConnection() {
		try {
			Class.forName("net.sf.log4jdbc.DriverSpy");
			return DriverManager.getConnection(driver, username, password);
		} catch (SQLException e) {
			throw new SqlRuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("missing class.  check chained exception", e);
		}
	}
	
	@Override
	public void dropAllTablesFromDatabase() {
		try (Connection connection = createConnection()) {
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP ALL OBJECTS");
		} catch (SQLException e) {
			throw new SqlRuntimeException("could not complete", e);
		}
	}
}
