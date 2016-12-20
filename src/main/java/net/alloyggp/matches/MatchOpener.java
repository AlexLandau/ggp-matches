package net.alloyggp.matches;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class MatchOpener {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule());

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("matchArchives/matchesFrom2009"))) {
            for (int i = 0; i < 10; i++) {
                String line = bufferedReader.readLine();

                System.out.println(line);

                MatchContainer container = objectMapper.readValue(line, ImmutableMatchContainer.Builder.class).build();
                System.out.println("Game URL: " + container.data().gameMetaURL());
                System.out.println("Player names: " + container.data().playerNamesFromHost());
                System.out.println("Goal values: " + container.data().goalValues());
            }
        }
    }

}
