package lass.sqlite;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lass.RowResult;
import lass.SqlConnection;

public class SqliteConnection implements SqlConnection {
	private final Connection conn;

	public SqliteConnection(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void execute(String sql) {
		try (Statement statement = conn.createStatement()) {
			statement.execute(sql);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(String sql, Object... arguments) {
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setPreparedStatementArgs(statement, arguments);
			statement.execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void executeBatch(String sql, List<Object[]> argBatches) {
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			for (Object[] argBatch : argBatches) {
				setPreparedStatementArgs(statement, argBatch);
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	//TODO: Use a better kind of RowResult return value
	//TODO: Stream the results
	@Override
	public List<RowResult> getRowResults(String sql, Object... arguments) {
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setPreparedStatementArgs(statement, arguments);
			try (ResultSet resultSet = statement.executeQuery()) {
				return convertResultSet(resultSet);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static enum TempTable {
		IDS("temp_ids", "id INTEGER"),
		STRINGS("temp_strings", "string TEXT");
		private final String name;
		private final String columns;

		private TempTable(String name, String columns) {
			this.name = name;
			this.columns = columns;
		}

		public String getName() {
			return name;
		}

		public String getColumns() {
			return columns;
		}
	}

	//TODO: Doesn't work like this, apparently...
	//Need to figure out temp tables?
	@Override
	public List<RowResult> selectUsingTempIds(String sql, Collection<Integer> tempIds, Object... arguments) {
		createTempIdsTableIfNeeded(TempTable.IDS);
		insertTempIds(tempIds);
		List<RowResult> rowResults = getRowResults(sql, arguments);
		clearTempIds(TempTable.IDS);
		return rowResults;
	}

	@Override
	public List<RowResult> selectUsingTempStrings(String sql, Collection<String> tempStrings, Object... arguments) {
		createTempIdsTableIfNeeded(TempTable.STRINGS);
		insertTempStrings(tempStrings);
		List<RowResult> rowResults = getRowResults(sql, arguments);
		clearTempIds(TempTable.STRINGS);
		return rowResults;
	}

	private void clearTempIds(TempTable table) {
		execute("DELETE FROM " + table.getName());
	}

	private void insertTempIds(Collection<Integer> tempIds) {
		List<Object[]> argBatches = tempIds.stream()
					.map(id -> new Object[] {id})
					.collect(Collectors.toList());
		executeBatch("INSERT INTO temp_ids (id) VALUES (?)", argBatches);
	}

	private void insertTempStrings(Collection<String> tempStrings) {
		List<Object[]> argBatches = tempStrings.stream()
				.map(string -> new Object[] {string})
				.collect(Collectors.toList());
		executeBatch("INSERT INTO temp_strings (string) VALUES (?)", argBatches);
	}

	private void createTempIdsTableIfNeeded(TempTable table) {
		execute("CREATE TEMP TABLE IF NOT EXISTS "
				+ table.getName() + " (" + table.getColumns() + ") WITHOUT ROWID");
	}

	private List<RowResult> convertResultSet(ResultSet resultSet)
			throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		List<String> columnNames = getColumnNames(metaData);
		List<RowResult> results = Lists.newArrayList();
		while (resultSet.next()) {
			Map<String, Object> rowContents = Maps.newHashMap();
			for (int izi = 0; izi < columnNames.size(); izi++) {
				String columnName = columnNames.get(izi);
				int sqlIndex = izi + 1;
				Object columnContents = resultSet.getObject(sqlIndex);
				rowContents.put(columnName, columnContents);
			}
			results.add(new RowResult(rowContents));
		}
		return results;
	}

	private List<String> getColumnNames(ResultSetMetaData metaData) throws SQLException {
		List<String> colNames = Lists.newArrayList();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			colNames.add(metaData.getColumnName(i));
		}
		return colNames;
	}

	public void setPreparedStatementArgs(PreparedStatement statement,
			Object... arguments) throws SQLException {
		int i = 1; //1-indexed
		for (Object arg : arguments) {
			setPreparedStatementArgument(statement, arg, i);
			i++;
		}
	}

	private void setPreparedStatementArgument(PreparedStatement statement,
			Object arg, int index) throws SQLException {
		if (arg == null) {
			statement.setString(index, null);
		} else if (arg instanceof Long) {
			statement.setLong(index, (Long) arg);
		} else if (arg instanceof Integer) {
			statement.setInt(index, (Integer) arg);
		} else if (arg instanceof String) {
			statement.setString(index, (String) arg);
		} else if (arg instanceof byte[]) {
			byte[] bytes = (byte[]) arg;
			statement.setBlob(index, new ByteArrayInputStream(bytes), bytes.length);
		} else {
			throw new RuntimeException("No handling in place for arg type " + arg.getClass());
		}
	}

	@Override
	public boolean tableExists(String tableName) {
		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
		List<RowResult> results = getRowResults(sql, tableName);

		Preconditions.checkState(results.size() <= 1);
		return results.size() == 1;
	}

	//Returns strings of the form "?,?,?"
	private static final LoadingCache<Integer, String> N_QUESTION_MARKS = CacheBuilder.newBuilder()
			.build(new CacheLoader<Integer, String>() {
				@Override
				public String load(Integer count) throws Exception {
					Preconditions.checkArgument(count >= 0);
					if (count == 0) {
						return "";
					}
					String string = Strings.repeat("?,", count);
					return string.substring(0, string.length() - 1);
				}
			});

	@Override
	public boolean insert(String tableName, Object... arguments) {
		Preconditions.checkArgument(arguments.length % 2 == 0,
				"Arguments must come in pairs of column name and value");
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("INSERT INTO ")
		.append(tableName)
		.append("(");
		for (int i = 0; i < arguments.length; i += 2) {
			String columnName = (String) arguments[i];
			sqlBuilder.append(columnName);
			sqlBuilder.append(",");
		}
		sqlBuilder.deleteCharAt(sqlBuilder.length() - 1)
		.append(") VALUES(")
		.append(N_QUESTION_MARKS.getUnchecked(arguments.length / 2))
		.append(")");
		String sql = sqlBuilder.toString();

		Object[] argObjects = new Object[arguments.length / 2];
		for (int i = 0; i < argObjects.length; i++) {
			argObjects[i] = arguments[i*2 + 1];
		}

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setPreparedStatementArgs(statement, argObjects);
			int rowCount = statement.executeUpdate();
			return rowCount > 0;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
