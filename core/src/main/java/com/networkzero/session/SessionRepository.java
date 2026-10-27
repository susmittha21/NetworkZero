package com.networkzero.session;

import java.nio.file.*;
import java.sql.*;
import com.networkzero.engine.ProfilePaths;

/** Persists hint audit records in the user's SQLite database. */
public final class SessionRepository implements AutoCloseable {
    private Connection connection;
    public SessionRepository(){try{Path db=ProfilePaths.root().resolve("game.db");Files.createDirectories(db.getParent());connection=DriverManager.getConnection("jdbc:sqlite:"+db);try(Statement s=connection.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS hint_usage(session_id TEXT NOT NULL, puzzle_id TEXT NOT NULL, tier INTEGER NOT NULL, used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");}}catch(Exception e){System.err.println("Could not initialize optional hint history database: "+e.getMessage());}}
    public void record(String session,String puzzle,int tier){if(connection==null)return;try(PreparedStatement p=connection.prepareStatement("INSERT INTO hint_usage(session_id,puzzle_id,tier) VALUES(?,?,?)")){p.setString(1,session);p.setString(2,puzzle);p.setInt(3,tier);p.executeUpdate();}catch(SQLException e){System.err.println("Could not record hint use: "+e.getMessage());}}
    @Override public void close(){try{if(connection!=null)connection.close();}catch(SQLException ignored){}}
}
