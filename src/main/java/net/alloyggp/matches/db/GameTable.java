package net.alloyggp.matches.db;

import java.util.List;
import java.util.stream.Collectors;

import lass.RowResult;
import lass.SqlRunner;

// create table if not exists game (id integer primary key, url text)
public class GameTable {
    private GameTable() {
        // Not instantiable
    }

    public static int getGameId(String gameUrl, SqlRunner sqlRunner) {
        return getGameId(gameUrl, sqlRunner, false);
    }

    private static int getGameId(String gameUrl, SqlRunner sqlRunner, boolean failIfNotFound) {
        return sqlRunner.call(conn -> {
            List<RowResult> rows = conn.getRowResults("select id from game where url = ?", gameUrl);
            if (rows.size() > 1) {
                throw new IllegalStateException("More than one row found for game " + gameUrl);
            } else if (rows.size() == 1) {
                return rows.get(0).getInt("id");
            } else {
                if (failIfNotFound) {
                    throw new IllegalStateException("We tried to insert game URL " + gameUrl + " but couldn't find it afterwards!");
                }
                conn.execute("insert into game(url) values(?)", gameUrl);
                return getGameId(gameUrl, sqlRunner, true);
            }
        });
    }

    public static List<Game> getAllGames(SqlRunner sqlRunner) {
        return sqlRunner.call(conn -> {
            List<RowResult> rowResults = conn.getRowResults("select * from game");
            return rowResults.stream()
                    .map(row -> {
                        return new Game(row.getInt("id"), row.getString("url"));
                    })
                    .collect(Collectors.toList());
        });
    }

    public static final class Game {
        public final int id;
        public final String url;

        public Game(int id, String url) {
            this.id = id;
            this.url = url;
        }

        public int getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((url == null) ? 0 : url.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Game other = (Game) obj;
            if (id != other.id)
                return false;
            if (url == null) {
                if (other.url != null)
                    return false;
            } else if (!url.equals(other.url))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Game [id=" + id + ", url=" + url + "]";
        }
    }
}
