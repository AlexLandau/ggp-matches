package net.alloyggp.matches.db;

import lass.SqlRunner;

public class SchemaCreator {
    public static void main(String[] args) throws InterruptedException {
        SqlRunner sqlRunner = DatabaseShared.getSqlRunner();
        createSchemaIfNotExists(sqlRunner);
        Thread.sleep(5000);
    }

    private static void createSchemaIfNotExists(SqlRunner sqlRunner) {
        sqlRunner.run(conn -> {
            conn.execute("create table if not exists player (id integer primary key, name text)");
            conn.execute("create table if not exists game (id integer primary key, url text)");
            //TODO: Add whether there were errors in the match, so we can filter those (or not).
            conn.execute("create table if not exists match (id integer primary key, tiltyard_id text, start_time integer, game integer)");
            conn.execute("create index if not exists match_idx_time on match (start_time)");
            conn.execute("create index if not exists match_idx_game on match (game, start_time)");
            conn.execute("create table if not exists played_in (match_id integer, role_number integer, player_id integer, score integer, UNIQUE (match_id, role_number))");
        });
    }
}
