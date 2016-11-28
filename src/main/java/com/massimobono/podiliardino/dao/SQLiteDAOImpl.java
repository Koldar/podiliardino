package com.massimobono.podiliardino.dao;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.massimobono.podiliardino.model.Indexable;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.TableFriendlyObservableMap;
import com.massimobono.podiliardino.util.TerFunction;
import com.massimobono.podiliardino.util.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SQLiteDAOImpl implements DAO {

	private static final int QUERY_TIMEOUT = 30;

	private File databaseFileName;
	
	private class PreparedStatements implements Closeable {
		
		private Connection connection;
		private Map<Connection, PreparedStatements> map;
		
		public PreparedStatement insertPlayer;
		public PreparedStatement getAllPlayers;
		public PreparedStatement updatePlayer;
		public PreparedStatement deletePlayer;
		
		public PreparedStatement insertTeam;
		public PreparedStatement getAllTeams;
		public PreparedStatement updateTeam;
		public PreparedStatement deleteTeam;
		public PreparedStatement getPlayersInTeam;
		public PreparedStatement getTeamWithPlayer;
		public PreparedStatement addPlayerComposeTeam;
		public PreparedStatement removePlayerComposeTeam;
		public PreparedStatement getTeamPartecipations;
		
		public PreparedStatement insertTournament;
		public PreparedStatement getAllTournaments;
		public PreparedStatement updateTournament;
		public PreparedStatement deleteTournament;
		public PreparedStatement getPartecipationsInTournament;
		
		
		public PreparedStatement lastInsertedRow;
		
		public PreparedStatements(Connection connection, Map<Connection, PreparedStatements> map) throws SQLException {
			this.map = map;
			this.connection = connection;
			
			this.insertPlayer = connection.prepareStatement("INSERT INTO player(name, surname, birthday, phone) VALUES(?,?,?,?)");
			this.getAllPlayers = connection.prepareStatement("SELECT id, name, surname, birthday, phone FROM player");
			this.updatePlayer = connection.prepareStatement("UPDATE OR ROLLBACK player SET name=?, surname=?, birthday=?,phone=? WHERE id=?");
			this.deletePlayer = connection.prepareStatement("DELETE FROM player WHERE id=?");
			
			this.insertTeam = connection.prepareStatement("INSERT INTO team(name, date) VALUES(?,?)");
			this.getAllTeams = connection.prepareStatement("SELECT id, name, date FROM team");
			this.updateTeam = connection.prepareStatement("UPDATE OR ROLLBACK team SET name=?, date=? WHERE id=?");
			this.deleteTeam = connection.prepareStatement("DELETE FROM team WHERE id=?");
			
			this.getPlayersInTeam = connection.prepareStatement("SELECT pct.player_id FROM player_compose_team as pct WHERE pct.team_id=?");
			this.getTeamWithPlayer = connection.prepareStatement("SELECT pct.team_id FROM player_compose_team as pct WHERE pct.player_id=?");
			this.getTeamPartecipations = connection.prepareStatement("SELECT p.team_id, p.tournament_id, p.bye FROM partecipation AS p WHERE p.team_id=?");
			
			this.addPlayerComposeTeam = connection.prepareStatement("INSERT INTO player_compose_team(player_id, team_id) VALUES(?,?)");
			this.removePlayerComposeTeam = connection.prepareStatement("DELETE FROM player_compose_team WHERE team_id=?");
			
			this.insertTournament = connection.prepareStatement("INSERT INTO tournament(name,start_date,end_date) VALUES(?,?,?);");
			this.getAllTournaments = connection.prepareStatement("SELECT id,name,start_date,end_date FROM tournament");
			this.updateTournament = connection.prepareStatement("UPDATE OR ROLLBACK tournament SET name=?, start_date=?, end_date=?");
			this.deleteTournament = connection.prepareStatement("DELETE FROM tournament WHERE id=?");
			this.getPartecipationsInTournament = connection.prepareStatement("SELECT p.team_id, p.tournament_id, p.bye FROM partecipation AS p WHERE p.tournament_id=?"); 
			
			this.lastInsertedRow = connection.prepareStatement("SELECT seq as last_inserted_id FROM sqlite_sequence WHERE name=?;");
		}

		@Override
		public void close() throws IOException {
			try {
				this.insertPlayer.close();
				this.getAllPlayers.close();
				this.updatePlayer.close();
				this.deletePlayer.close();
				this.deletePlayer.close();
				this.insertTeam.close();
				this.getAllTeams.close();
				this.updateTeam.close();
				this.deleteTeam.close();
				this.getPlayersInTeam.close();
				this.getTeamWithPlayer.close();
				this.addPlayerComposeTeam.close();
				this.removePlayerComposeTeam.close();
				this.lastInsertedRow.close();
			} catch (SQLException e) {
				throw new IOException(e);
			} finally {
				this.map.remove(this.connection);
			}
		}
	}
	
	private Map<Connection, PreparedStatements> preparedStatements;
	
	/**
	 * A map containing all the players computed by the DAO.
	 * 
	 * Every tme we add a new player, this map sotres the reference. Every time the DAO
	 * removes a player, this map destroy a reference. Everytime we get players from the DAO,
	 * the map stores a new reference if it doesn't have already. Otherwise the DAO does not create a new {@link Player} instance.
	 */
	private TableFriendlyObservableMap<Long, Player> players;
	/**
	 * A map containing all the teams computed by the DAO.
	 * 
	 * Every tme we add a new team, this map sotres the reference. Every time the DAO
	 * removes a team, this map destroy a reference. Everytime we get teams from the DAO,
	 * the map stores a new reference if it doesn't have already. Otherwise the DAO does not create a new {@link Team} instance.
	 */
	private TableFriendlyObservableMap<Long, Team> teams;
	/**
	 * A map containing all the tournaments computed by the DAO.
	 * 
	 * Every time we add a new tournament, this map stores the reference. Every time the DAO
	 * removes a tournament, this map destroy a reference. Everytime we get tournaments from the DAO,
	 * the map stores a new reference if it doesn't have already. Otherwise the DAO does not create a new {@link Tournament} instance.
	 */
	private TableFriendlyObservableMap<Long, Tournament> tournaments;

	/**
	 * 
	 * @param databaseFileName the file sqlite will use to store data.
	 * @param performSetup true if you want to perform a setup immediately
	 * @throws DAOException if something bad happens
	 */
	public SQLiteDAOImpl(File databaseFileName, boolean performSetup) throws DAOException {
		this.databaseFileName = databaseFileName;
		this.preparedStatements = new HashMap<>();
		this.players = new TableFriendlyObservableMap<>();
		this.teams = new TableFriendlyObservableMap<>();
		this.tournaments = new TableFriendlyObservableMap<>();
		if (performSetup) {
			this.setup();
		}
	}
	
	@Override
	public void clearAll() throws DAOException {
		this.connectAndThenDo(false, (c, s,ps) -> {
			try {
				s.executeUpdate("DELETE FROM player;");
				this.players.clear();
				s.executeUpdate("DELETE FROM team;");
				this.teams.clear();
				s.executeUpdate("DELETE FROM tournament;");
				this.tournaments.clear();
				s.executeUpdate("DELETE FROM partecipation;");
				return null;
			} catch (SQLException e) {
				return e;
			}
		}); 
	}

	@Override
	public void setup() throws DAOException {
		this.connectAndThenDo(false, (c, s,ps) -> {
			try {
				s.executeUpdate("PRAGMA foreign_keys = \"1\";");
				s.executeUpdate("CREATE TABLE IF NOT EXISTS player (id integer primary key autoincrement, name varchar(100), surname varchar(100), birthday varchar(20), phone varchar(20));");
				s.executeUpdate("CREATE INDEX IF NOT EXISTS name ON player (name, surname);");
				
				s.executeUpdate("CREATE TABLE IF NOT EXISTS team (id integer primary key autoincrement, name varchar(100), date varchar(20));");
				s.executeUpdate("CREATE INDEX IF NOT EXISTS team_name ON team (name);");
				
				s.executeUpdate("CREATE TABLE IF NOT EXISTS player_compose_team (player_id INTEGER REFERENCES player(id) ON UPDATE CASCADE, team_id INTEGER REFERENCES team(id) ON UPDATE CASCADE);");

				s.executeUpdate("CREATE TABLE IF NOT EXISTS tournament (id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(100), start_date varchar(20), end_date varchar(20));");
				
				s.executeUpdate("CREATE TABLE IF NOT EXISTS partecipation (team_id INTEGER REFERENCES team(id) ON UPDATE CASCADE, tournament_id INTEGER REFERENCES tournament(id) ON UPDATE CASCADE, bye integer);"); 
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}

	@Override
	public void tearDown() throws DAOException {
		for (Connection c : this.preparedStatements.keySet()) {
			try {
				this.preparedStatements.get(c).close();
			} catch (IOException e) {
				e.printStackTrace();
				//TODO complete here
			}
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
	private void connectAndThenDo(boolean tryPreparedStatement, TerFunction<Connection, Statement, PreparedStatements, Exception> queries) throws DAOException {
		try {
			try (	Connection conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s", databaseFileName.getAbsolutePath()));
					Statement statement = conn.createStatement();
					PreparedStatements preparedStatements = this.createPrepareStatement(conn, tryPreparedStatement); 
				) {
				statement.setQueryTimeout(QUERY_TIMEOUT);
				Exception e = queries.apply(conn, statement, preparedStatements);
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
	private void connectAndThenDo(TerFunction<Connection, Statement, PreparedStatements, Exception> queries) throws DAOException {
		this.connectAndThenDo(true, queries);
	}

	/**
	 * 
	 * @param setupPreparedStatement if se tto true, we will initialize prerpared stsatement (if they have already initialized, we do nothing)
	 * @return null if setupPreparedStatement is false, an instance of PreparedStatements otherwise
	 * @throws SQLException if something bad goes wrong
	 */
	private PreparedStatements createPrepareStatement(Connection connection, boolean setupPreparedStatement) throws SQLException {
		if (setupPreparedStatement && !this.preparedStatements.containsKey(connection)) {
			PreparedStatements retVal = new PreparedStatements(connection, this.preparedStatements);
			this.preparedStatements.put(connection, retVal);
		}
		return this.preparedStatements.get(connection);
	}
	
	/**
	 * Represents an abstract way yo add a single row inside a table
	 * 
	 * This function aims tyo provide a generic way to add a single row in a single table. Nothing more, nothing less
	 * 
	 * @param toAdd an instance to add in the database whose id has not been set yet
	 * @param tableInvolved the table where the insertion happens
	 * @param insertQuery a function that actually perform the insertion of <tt>toAdd</tt>
	 * @param afterInsertQuery a function you can use to perform addtional insertion in other tables. You can use this to add dependency rows inside the DB
	 * @param afterSuccessfulDBInsertion an action to perform if all the insertions are completely successful
	 * @return the same instance provided by <tt>toAdd</tt> but with the id set as it shows up in the db
	 * @throws DAOException if something bad happens
	 */
	private <TOADD extends Indexable> TOADD abstractAdd(final TOADD toAdd, String tableInvolved, TerFunction<Connection, Statement, PreparedStatements, Exception> insertQuery, TerFunction<Connection, Statement, PreparedStatements, Exception> afterInsertQuery, Runnable afterSuccessfulDBInsertion) throws DAOException {
		this.connectAndThenDo((c, s, ps) -> {
			try {
				Exception e = insertQuery.apply(c, s, ps);
				if (e != null) {
					throw e;
				}
				
				ps.lastInsertedRow.setString(1, tableInvolved);
				ResultSet rs = ps.lastInsertedRow.executeQuery();
				while (rs.next()) {
					toAdd.setId(rs.getLong("last_inserted_id"));
				}
				e = afterInsertQuery.apply(c, s, ps);
				if (e != null) {
					throw e;
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		afterSuccessfulDBInsertion.run();
		return toAdd;
	}
	
	private <TOEDIT> abstractUpdate(TOEDIT toEdit) {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.updateTeam.setString(1, team.getName().get());
				ps.updateTeam.setString(2, Utils.getStandardDateFrom(team.getDate().get()));
				ps.updateTeam.setLong(3, team.getId());
				ps.updateTeam.addBatch();
				
				ps.removePlayerComposeTeam.setLong(1, team.getId());
				ps.removePlayerComposeTeam.addBatch();
				
				ps.addPlayerComposeTeam.setLong(1, team.getPlayers().get(0).getId());
				ps.addPlayerComposeTeam.setLong(2, team.getId());
				ps.addPlayerComposeTeam.addBatch();
				ps.addPlayerComposeTeam.setLong(1, team.getPlayers().get(1).getId());
				ps.addPlayerComposeTeam.setLong(2, team.getId());
				ps.addPlayerComposeTeam.addBatch();
				
				
				c.setAutoCommit(false);
				ps.updateTeam.executeBatch();
				ps.removePlayerComposeTeam.executeBatch();
				ps.addPlayerComposeTeam.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
		return team;
	}

	@Override
	public void close() throws IOException {
		try {
			this.tearDown();
		} catch (DAOException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Player addPlayer(final Player p) throws DAOException {
		return this.abstractAdd(
				p, 
				"player",
				(c,s,ps) -> {
					try {
						Optional<String> birthday = p.getBirthdayAsStandardString();
						ps.insertPlayer.setString(1, p.getName().get());
						ps.insertPlayer.setString(2, p.getSurname().get());
						ps.insertPlayer.setString(3, birthday.isPresent() ? birthday.get() : Utils.EMPTY_DATE);
						ps.insertPlayer.setString(4, p.getPhone().get().isPresent() ? p.getPhone().get().get() : Utils.EMPTY_PHONE);
						ps.insertPlayer.addBatch();
						c.setAutoCommit(false);
						ps.insertPlayer.executeBatch();
						c.setAutoCommit(true);
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(c,s,ps) -> null,
				() -> {this.players.put(p.getId(), p);}
			);
	}
	
	@Override
	public Player updatePlayer(final Player player) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				Optional<String> birthday = player.getBirthdayAsStandardString();
				ps.updatePlayer.setString(1, player.getName().get());
				ps.updatePlayer.setString(2, player.getSurname().get());
				ps.updatePlayer.setString(3, birthday.isPresent() ? birthday.get() : Utils.EMPTY_DATE);
				ps.updatePlayer.setString(4, player.getPhone().get().isPresent() ? player.getPhone().get().get() : Utils.EMPTY_PHONE);
				ps.updatePlayer.setLong(5, player.getId());
				
				ps.updatePlayer.addBatch();
				c.setAutoCommit(false);
				ps.updatePlayer.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
		return player;
	}

	@Override
	public void removePlayer(Player p) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.deletePlayer.setLong(1, p.getId());
				ps.deletePlayer.addBatch();
				
				c.setAutoCommit(false);
				ps.deletePlayer.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
		this.players.remove(p.getId());
	}

	@Override
	public Collection<Player> getAllPlayersThat(Function<Player, Boolean> filter) throws DAOException {
		Collection<Player> retVal = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			ResultSet rs;
			try {
				rs = ps.getAllPlayers.executeQuery();
				Player p = null;
				while (rs.next()) {
					if (this.players.containsKey(rs.getLong("id"))) {
						p = this.players.get(rs.getLong("id"));
					} else {
						p = new Player(
			    			rs.getLong("id"),
			    			rs.getString("name"),
			    			rs.getString("surname"),
			    			!rs.getString("birthday").equalsIgnoreCase(Utils.EMPTY_DATE) ? Utils.getDateFrom(rs.getString("birthday")) : null,
			    			!rs.getString("phone").equalsIgnoreCase(Utils.EMPTY_PHONE) ? rs.getString("phone") : null,
			    			new ArrayList<>());
						this.players.putIfAbsent(p.getId(), p);
						p.getTeamWithPlayer().addAll(this.getTeamsWith(rs.getLong("id")));
					}
					System.out.println(p.getName().get()+ " analyzing "+ filter);
			    	if (filter.apply(p)) {
			    		System.out.println(p.getName().get() + " passed!");
			    		retVal.add(p);
				    }
			    }
				return null;
			} catch (SQLException | DAOException e) {
				return e;
			}
		});
		return retVal;
	}
	
	public ObservableList<Player> getPlayerList() throws DAOException {
		return this.players.observableValueList();
	}
	
	public ObservableList<Team> getTeamList() throws DAOException {
		return this.teams.observableValueList();
	}

	@Override
	public Team addTeam(final Team team) throws DAOException {
		return this.abstractAdd(
				team,
				"team",
				(c,s,ps) -> {
					try {
						ps.insertTeam.setString(1, team.getName().get());
						ps.insertTeam.setString(2, Utils.getStandardDateFrom(team.getDate().get()));
						ps.insertTeam.addBatch();
						ps.insertTeam.executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(c,s,ps) -> {
					try {
						ps.addPlayerComposeTeam.setLong(1, team.getPlayers().get(0).getId());
						ps.addPlayerComposeTeam.setLong(2, team.getId());
						ps.addPlayerComposeTeam.addBatch();
						ps.addPlayerComposeTeam.setLong(1, team.getPlayers().get(1).getId());
						ps.addPlayerComposeTeam.setLong(2, team.getId());
						ps.addPlayerComposeTeam.addBatch();
						ps.addPlayerComposeTeam.executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				() -> {this.teams.put(team.getId(), team);}
			);
	}

	@Override
	public Team update(final Team team) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.updateTeam.setString(1, team.getName().get());
				ps.updateTeam.setString(2, Utils.getStandardDateFrom(team.getDate().get()));
				ps.updateTeam.setLong(3, team.getId());
				ps.updateTeam.addBatch();
				
				ps.removePlayerComposeTeam.setLong(1, team.getId());
				ps.removePlayerComposeTeam.addBatch();
				
				ps.addPlayerComposeTeam.setLong(1, team.getPlayers().get(0).getId());
				ps.addPlayerComposeTeam.setLong(2, team.getId());
				ps.addPlayerComposeTeam.addBatch();
				ps.addPlayerComposeTeam.setLong(1, team.getPlayers().get(1).getId());
				ps.addPlayerComposeTeam.setLong(2, team.getId());
				ps.addPlayerComposeTeam.addBatch();
				
				
				c.setAutoCommit(false);
				ps.updateTeam.executeBatch();
				ps.removePlayerComposeTeam.executeBatch();
				ps.addPlayerComposeTeam.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
		return team;
	}

	@Override
	public Collection<Team> getAllTeamsThat(Function<Team, Boolean> filter) throws DAOException {
		Collection<Team> retVal = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ResultSet rs = ps.getAllTeams.executeQuery();
				while (rs.next()) {
					Team team = null;
					if (this.teams.containsKey(rs.getLong("id"))) {
						team = this.teams.get(rs.getLong("id"));
					} else {
						team = new Team(
			    			rs.getLong("id"),
			    			rs.getString("name"),
			    			Utils.getDateFrom(rs.getString("date")),
			    			new ArrayList<>(),
			    			new ArrayList<>()
			    		);
						team.getPlayers().addAll(this.getPlayersInTeam(rs.getLong("id")));
						this.teams.putIfAbsent(team.getId(), team);
					}
			    	if (filter.apply(team)) {
				    	retVal.add(team);
				    }
			    }
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		return retVal;
	}

	@Override
	public void removeTeam(final Team team) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.deleteTeam.setLong(1, team.getId());
				ps.deleteTeam.addBatch();
				
				c.setAutoCommit(false);
				ps.deleteTeam.executeBatch();
				c.setAutoCommit(true);
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
		this.teams.remove(team.getId());
	}
	
	/**
	 * 
	 * @param teamId
	 * @return the players inside a particular team
	 * @throws SQLException if something bad happens
	 * @throws DAOException if something bad happens
	 */
	private Collection<Player> getPlayersInTeam(long teamId) throws DAOException {
		Collection<Player> retVal = new HashSet<>();
		
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getPlayersInTeam.setLong(1, teamId);
				ResultSet rs = ps.getPlayersInTeam.executeQuery();
				while (rs.next()) {
					final long player_id = rs.getLong("player_id");
					retVal.addAll(this.getAllPlayersThat(p -> p.getId() == player_id));
				}
				return null;
			} catch (SQLException | DAOException e) {
				return e;
			}
		});
		return retVal;
	}
	
	private Collection<Team> getTeamsWith(long player_id) throws DAOException {
		Collection<Team> retVal = new HashSet<>();
		
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getTeamWithPlayer.setLong(1, player_id);
				ResultSet rs = ps.getTeamWithPlayer.executeQuery();
				while (rs.next()) {
					final long team_id = rs.getLong("team_id");
					retVal.addAll(this.getAllTeamsThat(t -> t.getId() == team_id));
				}
				return null;
			} catch (SQLException | DAOException e) {
				return e;
			}
		}); 
		return retVal;
	}

	@Override
	public Tournament add(Tournament tournament) throws DAOException {
		return this.abstractAdd(
				tournament, 
				"tournament", 
				(c,s,ps) -> {
					try {
						Optional<LocalDate> endDate = tournament.getEndDate().get();
						ps.insertTournament.setString(1, tournament.getName().get());
						ps.insertTournament.setString(2, Utils.getStandardDateFrom(tournament.getStartDate().get()));
						ps.insertTournament.setString(3, endDate.isPresent() ? Utils.getStandardDateFrom(endDate.get()) : Utils.EMPTY_DATE);
						ps.insertTournament.addBatch();
						ps.insertTournament.executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(c,s,ps) -> null,
				() -> {this.tournaments.put(tournament.getId(), tournament);}
			);
	}

	@Override
	public Tournament update(Tournament tournament) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(Tournament tournament) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObservableList<Tournament> getTournamentList() throws DAOException {
		return this.tournaments.observableValueList();
	}

	@Override
	public Collection<Team> getTournamentsThat(Function<Tournament, Boolean> filter) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
