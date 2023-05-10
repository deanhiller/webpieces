package org.webpieces.ddl.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.util.SneakyThrow;

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
			Class.forName("org.digitalforge.log4jdbc.LoggingDriver");
			return DriverManager.getConnection(driver, username, password);
		} catch (ClassNotFoundException | SQLException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	@Override
	public void dropAllTablesFromDatabase() {
		try (Connection connection = createConnection(); Statement statement = connection.createStatement()) {
			statement.executeUpdate("DROP ALL OBJECTS");
		} catch (SQLException e) {
			throw SneakyThrow.sneak(e);
		}
	}
}
