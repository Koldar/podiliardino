package com.massimobono.podiliardino.dao;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.massimobono.podiliardino.model.Player;

public class SQLiteDAOImpl implements DAO {

	private static final int QUERY_TIMEOUT = 30;

	private File databaseFileName;
	private Connection sqliteConnection;

	private PreparedStatement insertPlayer;
	private PreparedStatement getAllPlayers;
	private PreparedStatement updatePlayer;
	private PreparedStatement deletePlayer;
	private PreparedStatement lastInsertedRow;
	private boolean preparedStatementCreated;

	/**
	 * 
	 * @param databaseFileName the file sqlite will use to store data.
	 * @param performSetup true if you want to perform a setup immediately
	 * @throws DAOException if something bad happens
	 */
	public SQLiteDAOImpl(File databaseFileName, boolean performSetup) throws DAOException {
		this.databaseFileName = databaseFileName;
		this.insertPlayer = null;
		this.getAllPlayers = null;
		this.deletePlayer = null;
		this.preparedStatementCreated = false;
		
		if (performSetup) {
			this.setup();
		}
	}

	@Override
	public Player addPlayer(Player p) throws DAOException {
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
				
				this.lastInsertedRow.setString(1, "player");
				c.setAutoCommit(false);
				ResultSet rs = this.lastInsertedRow.executeQuery();
				while (rs.next()) {
					p.setId(rs.getLong("last_inserted_id"));
				}
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		}); 
		return null;
	}
	
	@Override
	public Player updatePlayer(Player player) throws DAOException {
		this.connectAndThenDo((c,s) -> {
			try {
				this.updatePlayer.setString(0, player.getName());
				this.updatePlayer.setString(1, player.getSurname());
				this.updatePlayer.setString(2, player.getBirthdayAsStandardString());
				this.updatePlayer.setString(3, player.getPhone());
				this.updatePlayer.setLong(4, player.getId());
				
				this.updatePlayer.addBatch();
				c.setAutoCommit(false);
				this.updatePlayer.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
		return null;
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
		this.connectAndThenDo(false, (c, s) -> {
			try {
				s.executeUpdate("DELETE * FROM player;");
				return null;
			} catch (SQLException e) {
				return e;
			}
		}); 
	}

	@Override
	public void setup() throws DAOException {
		this.connectAndThenDo(false, (connection, statement) -> {
			try {
				statement.executeUpdate("PRAGMA foreign_keys = \"1\";");
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
			this.insertPlayer.close();
			this.getAllPlayers.close();
			this.deletePlayer.close();
			this.lastInsertedRow.close();
			this.updatePlayer.close();
			this.sqliteConnection.close();
		} catch (SQLException e) {
			throw new DAOException(e);
		}
	}

	/**
	 * Allows you to execute queries on the system. This is the main function of the class
	 * 
	 * Remember that in "queries", you need to return the exception you have encountered (null if you stumbled in no exception)
	 * 
	 * @param tryPreparedStatement set this to false if you need to setup the database (you still have tables to create): otherwise the SQl engine will complain about
	 * 	no tables involved in the prepared statements. Otheriwse set this to true
	 * @param queries a lambda where you can execute all the statement you want. Remeber to return the exception you find, or "null" if you encounter no exception
	 * @throws DAOException if something bad happen
	 */
	private void connectAndThenDo(boolean tryPreparedStatement, BiFunction<Connection, Statement, Exception> queries) throws DAOException {
		try {
			this.createPrepareStatement(tryPreparedStatement);
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
	
	/**
	 * Like {@link #connectAndThenDo(boolean, BiFunction)}, but we will try to setup prepared statement by default
	 * 
	 * @param queries
	 * @throws DAOException
	 */
	private void connectAndThenDo(BiFunction<Connection, Statement, Exception> queries) throws DAOException {
		this.connectAndThenDo(true, queries);
	}

	/**
	 * 
	 * @param setupPreparedStatement if se tto true, we will initialize prerpared stsatement (if they have already initialized, we do nothing)
	 * @throws SQLException if something bad goes wrong
	 */
	private void createPrepareStatement(boolean setupPreparedStatement) throws SQLException {
		if (this.sqliteConnection == null) {
			this.sqliteConnection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", databaseFileName.getAbsolutePath()));
		}
		if (setupPreparedStatement && !this.preparedStatementCreated) {
			this.insertPlayer = this.sqliteConnection.prepareStatement("INSERT INTO player(firstname, surname, birthday, phone) VALUES(?,?,?,?)");
			this.getAllPlayers = this.sqliteConnection.prepareStatement("SELECT id, firstname, surname, birthday, phone FROM player");
			this.updatePlayer = this.sqliteConnection.prepareStatement("UPDATE OR ROLLBACK player SET firstname=?, surname=?, birthday=?,phone=? WHERE id=?");
			this.deletePlayer = this.sqliteConnection.prepareStatement("DELETE FROM player WHERE id=?");
			this.lastInsertedRow = this.sqliteConnection.prepareStatement("SELECT seq as last_inserted_id FROM sqlite_sequence WHERE name=?;");
			
			this.preparedStatementCreated = true;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.tearDown();
		} catch (DAOException e) {
			throw new IOException(e);
		}
	}

}
