package lass.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lass.SqlCallable;
import lass.SqlRunnable;
import lass.SqlRunner;

public class SqliteRunner implements SqlRunner {
	private final String jdbcString;

	public SqliteRunner(String jdbcString) {
		this.jdbcString = jdbcString;
	}

	@Override
	public void run(SqlRunnable runnable) {
		try (Connection c = DriverManager.getConnection(jdbcString)) {
			runnable.run(new SqliteConnection(c));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T call(SqlCallable<T> callable) {
		try (Connection c = DriverManager.getConnection(jdbcString)) {
			return callable.run(new SqliteConnection(c));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
