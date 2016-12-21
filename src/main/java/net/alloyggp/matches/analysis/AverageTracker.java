package net.alloyggp.matches.analysis;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;

import lass.SqlRunner;
import net.alloyggp.matches.db.DatabaseShared;
import net.alloyggp.matches.db.PlayedInTable;
import net.alloyggp.matches.db.PlayerTable.Player;

public class AverageTracker {

    public static void main(String[] args) {
        SqlRunner sqlRunner = DatabaseShared.getSqlRunner();

//        List<Player> players = PlayerTable.getAllPlayers(sqlRunner);

        for (int year = 2009; year < 2010; year++) {
            for (Month month : Month.values()) {
                System.out.println(month + " " + year + ":");
                LocalDateTime startDateTime = LocalDateTime.of(year, month, 1, 0, 0);
                LocalDateTime endDateTime = startDateTime.plusMonths(1);
                System.out.println(startDateTime.toString() + " to " + endDateTime);
                System.out.println(startDateTime.toEpochSecond(ZoneOffset.UTC)*1000 + " to " + endDateTime.toEpochSecond(ZoneOffset.UTC)*1000);
                long startTime = startDateTime.toEpochSecond(ZoneOffset.UTC)*1000;
                long endTime = endDateTime.toEpochSecond(ZoneOffset.UTC)*1000;

                Map<Player, IntSummaryStatistics> matches = PlayedInTable.getScoreStatsInTimeRange(startTime, endTime, sqlRunner);
                //TODO: Have these sorted by score
                for (Entry<Player, IntSummaryStatistics> entry : matches.entrySet()) {
                    if (entry.getValue().getCount() >= 10) {
                        System.out.println(entry.getKey().name + ": " + entry.getValue().getAverage() + " (" + entry.getValue().getCount() + ")");
                    }
                }
            }
        }
    }

}
