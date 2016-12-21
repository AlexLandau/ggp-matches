package net.alloyggp.matches.db;

import lass.SqlRunner;
import lass.sqlite.SqliteRunner;

public class DatabaseShared {
    private static SqlRunner getSqlRunner(String filename) {
        try {
            Class.forName("org.sqlite.JDBC");
            return new SqliteRunner("jdbc:sqlite:" + filename);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static SqlRunner getSqlRunner() {
        return getSqlRunner("matchResults.db");
    }
}
