package com.j256.ormlite.h2;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * H2 connection source.
 * 
 * @author graywatson
 */
public class H2ConnectionSource implements ConnectionSource {

	public DatabaseType databaseType = new H2DatabaseType();
	private DatabaseConnection connection = null;
	private final String databaseUrl;

	public H2ConnectionSource() throws SQLException {
		this(H2DatabaseType.DATABASE_URL);
	}

	public H2ConnectionSource(String databaseUrl) throws SQLException {
		databaseType.loadDriver();
		this.databaseUrl = databaseUrl;
	}

	public void close() throws SQLException {
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	public void closeQuietly() {
		try {
			close();
		} catch (SQLException e) {
			// ignored
		}
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		return getReadWriteConnection();
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		if (connection == null) {
			connection = new H2DatabaseConnection(DriverManager.getConnection(databaseUrl));
		}
		return connection;
	}

	public void releaseConnection(DatabaseConnection connection) {
		// noop right now
	}

	public boolean saveSpecialConnection(DatabaseConnection connection) {
		// noop since this is a single connection source
		return true;
	}

	public void clearSpecialConnection(DatabaseConnection connection) {
		// noop since this is a single connection source
	}

	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	public DatabaseConnection getSpecialConnection() {
		return null;
	}

	public boolean isOpen() {
		return connection != null;
	}
}
