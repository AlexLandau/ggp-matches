package net.alloyggp.matches.ingest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;

import lass.SqlRunner;
import net.alloyggp.matches.ImmutableMatchContainer;
import net.alloyggp.matches.MatchContainer;
import net.alloyggp.matches.db.DatabaseShared;
import net.alloyggp.matches.db.GameTable;
import net.alloyggp.matches.db.MatchTable;
import net.alloyggp.matches.db.PlayedInTable;
import net.alloyggp.matches.db.PlayerTable;

public class ArchiveIngestor {
    private static final List<String> ARCHIVES_TO_INGEST = ImmutableList.of(
            "matchArchives/matchesFrom2009",
            "matchArchives/matchesFrom2010",
            "matchArchives/matchesFrom2011",
            "matchArchives/matchesFrom2012",
            "matchArchives/matchesFrom2013",
            "matchArchives/matchesFrom2014");

    public static void main(String[] args) throws FileNotFoundException, IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule());

        SqlRunner sqlRunner = DatabaseShared.getSqlRunner();

        long startTime = System.currentTimeMillis();
        for (String archiveFilename : ARCHIVES_TO_INGEST) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(archiveFilename))) {
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }

                    MatchContainer container = objectMapper.readValue(line, ImmutableMatchContainer.Builder.class).build();

                    if (!container.data().isCompleted()
                            || container.data().goalValues() == null
                            || container.data().goalValues().isEmpty()
                            || container.data().playerNamesFromHost() == null
                            || container.data().playerNamesFromHost().isEmpty()
                            || container.data().gameMetaURL() == null
                            || container.data().gameMetaURL().isEmpty()
                            || container.data().goalValues().size() != container.data().playerNamesFromHost().size()) {
                        continue;
                    }

                    System.out.println("Importing match from " + archiveFilename);
                    sqlRunner.run(conn -> {
                        System.out.println("Game URL: " + container.data().gameMetaURL());
                        int gameId = GameTable.getGameId(container.data().gameMetaURL(), sqlRunner);
                        System.out.println("Game ID: " + gameId);
                        int matchId = MatchTable.getMatchId(container.data().matchId(), container.data().startTime(), gameId, sqlRunner);
                        System.out.println("Match ID: " + matchId);
                        for (int r = 0; r < container.data().playerNamesFromHost().size(); r++) {
                            String playerName = container.data().playerNamesFromHost().get(r);
                            int score = container.data().goalValues().get(r);
                            int playerId = PlayerTable.getPlayerId(playerName, sqlRunner);
                            System.out.println("ID for player " + playerName + " is " + playerId);
                            PlayedInTable.record(matchId, r, playerId, score, sqlRunner);
                        }
                    });
                }
            }
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("Completed ingest in " + timeTaken + " ms.");
    }

}
