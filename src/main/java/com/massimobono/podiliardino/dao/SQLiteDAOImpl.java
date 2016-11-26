package com.massimobono.podiliardino.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.massimobono.podiliardino.model.Player;

public class SQLiteDAOImpl implements DAO {

	private static final int QUERY_TIMEOUT = 30;

	private File databaseFileName;
	private Connection sqliteConnection;

	private PreparedStatement insertPlayer;

	private PreparedStatement getAllPlayers;

	private PreparedStatement deletePlayer;

	public SQLiteDAOImpl(File databaseFileName) {
		this.databaseFileName = databaseFileName;
	}

	@Override
	public void addPlayer(Player p) throws DAOException {
		this.connectAndThenDo((c, s) -> {
			try {
				this.insertPlayer.setString(1, p.getName());
				this.insertPlayer.setString(2, p.getSurname());
				this.insertPlayer.setString(3, p.getBirthdayAsStandardString());
				this.insertPlayer.setString(4, p.getPhone());
				this.insertPlayer.addBatch();

				c.setAutoCommit(false);
				this.insertPlayer.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		}); 
	}

	@Override
	public void removePlayer(Player p) throws DAOException {
		this.connectAndThenDo((c,s) -> {
			try {
				this.deletePlayer.setLong(0, p.getId());
				this.deletePlayer.addBatch();
				
				c.setAutoCommit(false);
				this.deletePlayer.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}

	@Override
	public Collection<Player> getAllPlayersThat(Function<Player, Boolean> filter) throws DAOException {
		Collection<Player> retVal = new ArrayList<>();
		this.connectAndThenDo((c,s) -> {
			ResultSet rs;
			try {
				rs = this.getAllPlayers.executeQuery();
				while (rs.next()) {
			    	Player p = new Player(
			    			rs.getLong("id"),
			    			rs.getString("name"),
			    			rs.getString("surname"),
			    			Player.getBirthdayDateFromStandardString(rs.getString("birthday")),
			    			rs.getString("phone"));
			    	if (filter.apply(p)) {
				    	retVal.add(p);
				    }
			    }
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
		
		return retVal;
	}

	@Override
	public void clearAll() throws DAOException {
		this.connectAndThenDo((c, s) -> {
			try {
				s.executeUpdate("DROP TABLE IF EXISTS player;");
				return null;
			} catch (SQLException e) {
				return e;
			}
		}); 
	}

	@Override
	public void setup() throws DAOException {
		this.connectAndThenDo((connection, statement) -> {
			try {
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS player (id integer primary key autoincrement, firstname varchar(100), surname varchar(100), birthday varchar(20), phone varchar(20));");
				statement.executeUpdate("CREATE INDEX name ON player (firstname, surname);");
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}

	@Override
	public void tearDown() throws DAOException {
		try {
			this.sqliteConnection.close();
		} catch (SQLException e) {
			throw new DAOException(e);
		}
	}

	private void connectAndThenDo(BiFunction<Connection, Statement, Exception> queries) throws DAOException {
		try {
			this.createPrepareStatement();
			try (Statement statement = this.sqliteConnection.createStatement();) {
				statement.setQueryTimeout(QUERY_TIMEOUT);
				Exception e = queries.apply(this.sqliteConnection, statement);
				if (e != null) {
					throw e;
				}
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}

	private void createPrepareStatement() throws SQLException {
		if (this.sqliteConnection == null) {
			this.sqliteConnection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", databaseFileName.getAbsolutePath()));
			this.insertPlayer = this.sqliteConnection.prepareStatement("INSERT INTO player VALUES(?,?,?,?)");
			this.getAllPlayers = this.sqliteConnection.prepareStatement("SELECT id, firstname, surname, birthday, phone FROM player");
			this.deletePlayer = this.sqliteConnection.prepareStatement("DELETE FROM player WHERE id=?");
		}
	}

}
