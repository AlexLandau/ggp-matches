package lass;

import java.util.Collection;
import java.util.List;

public interface SqlConnection {

	//TODO: Add methods to run SQL stuff
	void execute(String sql);

	//TODO: Rename getRows?
	List<RowResult> getRowResults(String sql, Object... arguments);

	List<RowResult> selectUsingTempIds(String sql, Collection<Integer> tempIds,
			Object... arguments);

	List<RowResult> selectUsingTempStrings(String string, Collection<String> strings,
			Object... arguments);

	//TODO: Rename to execute? Same thing?
	void execute(String sql, Object... arguments);

	void executeBatch(String sql, List<Object[]> argBatches);

	boolean tableExists(String tableName);

	/**
	 * Arguments should alternate between column names and values.
	 */
	boolean insert(String tableName, Object... arguments);


}
