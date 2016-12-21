package net.alloyggp.matches.db;

import java.util.List;
import java.util.stream.Collectors;

import lass.RowResult;
import lass.SqlRunner;

//conn.execute("create table if not exists player (id integer primary key, name text)");
public class PlayerTable {
    private PlayerTable() {
        //Not instantiable
    }

    public static int getPlayerId(String playerName, SqlRunner sqlRunner) {
        return getPlayerId(playerName, sqlRunner, false);
    }
    private static int getPlayerId(String playerName, SqlRunner sqlRunner, boolean failIfNotFound) {
        return sqlRunner.call(conn -> {
            List<RowResult> rows = conn.getRowResults("select id from player where name = ?", playerName);
            if (rows.size() > 1) {
                throw new IllegalStateException("More than one row found for player " + playerName);
            } else if (rows.size() == 1) {
                return rows.get(0).getInt("id");
            } else {
                if (failIfNotFound) {
                    throw new IllegalStateException("We tried to insert player name " + playerName + " but couldn't find it afterwards!");
                }
                conn.execute("insert into player(name) values(?)", playerName);
                return getPlayerId(playerName, sqlRunner, true);
            }
        });
    }

    public static List<Player> getAllPlayers(SqlRunner sqlRunner) {
        return sqlRunner.call(conn -> {
            List<RowResult> rowResults = conn.getRowResults("select * from player");
            return rowResults.stream()
                    .map(row -> {
                        return new Player(row.getInt("id"), row.getString("name"));
                    })
                    .collect(Collectors.toList());
        });
    }

    public static final class Player {
        public final int id;
        public final String name;

        public Player(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
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
            Player other = (Player) obj;
            if (id != other.id)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Player [id=" + id + ", name=" + name + "]";
        }
    }
}
