package net.alloyggp.matches.analysis;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lass.SqlRunner;
import net.alloyggp.matches.db.DatabaseShared;
import net.alloyggp.matches.db.GameTable;
import net.alloyggp.matches.db.GameTable.Game;
import net.alloyggp.matches.db.PlayedInTable;
import net.alloyggp.matches.db.PlayerTable.Player;

public class AverageByGame {

    public static void main(String[] args) {
        SqlRunner sqlRunner = DatabaseShared.getSqlRunner();

        List<Game> games = GameTable.getAllGames(sqlRunner);

        for (Game game : games) {
            System.out.println();
            System.out.println(game.url + ":");

            Map<Player, IntSummaryStatistics> matches = PlayedInTable.getScoreStatsForGame(game.id, sqlRunner);
            //TODO: Have these sorted by score
            for (Entry<Player, IntSummaryStatistics> entry : matches.entrySet()) {
                if (entry.getValue().getCount() >= 10) {
                    System.out.println("  " + entry.getKey().name + ": " + entry.getValue().getAverage() + " (" + entry.getValue().getCount() + ")");
                }
            }
        }
    }

}
