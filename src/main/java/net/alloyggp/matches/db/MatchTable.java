package net.alloyggp.matches.db;

import java.util.List;

import lass.RowResult;
import lass.SqlRunner;

//create table if not exists match (id integer primary key, tiltyard_id text, start_time integer, game integer)
//create index if not exists match_idx_time on match (start_time)
//create index if not exists match_idx_game on match (game, start_time)
public class MatchTable {
    public static int getMatchId(String tiltyardId, long startTime, int gameId, SqlRunner sqlRunner) {
        return getMatchId(tiltyardId, startTime, gameId, sqlRunner, false);
    }
    private static int getMatchId(String tiltyardId, long startTime, int gameId, SqlRunner sqlRunner, boolean failIfNotFound) {
        return sqlRunner.call(conn -> {
            List<RowResult> rows = conn.getRowResults("select id from match where tiltyard_id = ?", tiltyardId);
            if (rows.size() > 1) {
                throw new IllegalStateException("More than one row found for match " + tiltyardId);
            } else if (rows.size() == 1) {
                return rows.get(0).getInt("id");
            } else {
                if (failIfNotFound) {
                    throw new IllegalStateException("We tried to insert match " + tiltyardId + " but couldn't find it afterwards!");
                }
                conn.execute("insert into match(tiltyard_id, start_time, game) values(?,?,?)", tiltyardId, startTime, gameId);
                return getMatchId(tiltyardId, startTime, gameId, sqlRunner, true);
            }
        });
    }

}
