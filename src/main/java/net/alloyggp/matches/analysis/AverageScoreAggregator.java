package net.alloyggp.matches.analysis;

import java.util.IntSummaryStatistics;
import java.util.List;

import lass.SqlRunner;
import net.alloyggp.matches.db.DatabaseShared;
import net.alloyggp.matches.db.PlayedInTable;
import net.alloyggp.matches.db.PlayerTable;
import net.alloyggp.matches.db.PlayerTable.Player;

public class AverageScoreAggregator {
    public static void main(String[] args) {
        SqlRunner sqlRunner = DatabaseShared.getSqlRunner();

        List<Player> players = PlayerTable.getAllPlayers(sqlRunner);
        for (Player player : players) {
            IntSummaryStatistics playerStats = PlayedInTable.getScoreStatsForPlayer(player.id, sqlRunner);
            double average = playerStats.getAverage();
            long count = playerStats.getCount();
            if (count > 100) {
                System.out.println("Average score for player " + player.name + " is " + average + " over " + count + " games");
            }
        }
    }
}
