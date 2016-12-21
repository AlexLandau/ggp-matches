package net.alloyggp.matches.db;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import lass.RowResult;
import lass.SqlRunner;
import net.alloyggp.matches.db.PlayerTable.Player;

//create table if not exists played_in (match_id integer, role_number integer, player_id integer, score integer, UNIQUE (match_id, role_number))
public class PlayedInTable {
    public static void record(int matchId, int roleNumber, int playerId, int score, SqlRunner sqlRunner) {
        sqlRunner.run(conn -> {
            conn.execute("insert or ignore into played_in(match_id, role_number, player_id, score) values(?,?,?,?)",
                    matchId, roleNumber, playerId, score);
        });
    }

    public static IntSummaryStatistics getScoreStatsForPlayer(int playerId, SqlRunner sqlRunner) {
        return sqlRunner.call(conn -> {
            List<RowResult> rows = conn.getRowResults("select score from played_in where player_id = ?", playerId);
            return rows.stream()
                .mapToInt(row -> row.getInt("score"))
                .summaryStatistics();
        });
    }

    public static Map<Player, IntSummaryStatistics> getScoreStatsInTimeRange(long startTime, long endTime,
            SqlRunner sqlRunner) {
        List<Player> players = PlayerTable.getAllPlayers(sqlRunner);
        Map<Integer, Player> playerMap = Maps.uniqueIndex(players, Player::getId);
        return sqlRunner.call(conn -> {
            List<RowResult> rows = conn.getRowResults("select player_id, score from match, played_in "
                    + " where match.start_time >= ? and match.start_time < ?"
                    + " and match.id = played_in.match_id", startTime, endTime);
            Map<Player, IntSummaryStatistics> map = Maps.newHashMap();
            for (Player player : players) {
                map.put(player, new IntSummaryStatistics());
            }
            for (RowResult row : rows) {
                int playerId = row.getInt("player_id");
                Player player = playerMap.get(playerId);
                map.get(player).accept(row.getInt("score"));
            }
            return map;
        });
    }

    public static Map<Player, IntSummaryStatistics> getScoreStatsForGame(int gameId, SqlRunner sqlRunner) {
        List<Player> players = PlayerTable.getAllPlayers(sqlRunner);
        Map<Integer, Player> playerMap = Maps.uniqueIndex(players, Player::getId);
        return sqlRunner.call(conn -> {
            List<RowResult> rows = conn.getRowResults("select player_id, score from match, played_in "
                    + " where match.game = ?"
                    + " and match.id = played_in.match_id", gameId);
            Map<Player, IntSummaryStatistics> map = Maps.newHashMap();
            for (Player player : players) {
                map.put(player, new IntSummaryStatistics());
            }
            for (RowResult row : rows) {
                int playerId = row.getInt("player_id");
                Player player = playerMap.get(playerId);
                map.get(player).accept(row.getInt("score"));
            }
            return map;
        });
    }
}
