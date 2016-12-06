package com.massimobono.podiliardino.extensibles.dao;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Indexable;
import com.massimobono.podiliardino.model.Match;
import com.massimobono.podiliardino.model.MatchStatus;
import com.massimobono.podiliardino.model.Partecipation;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.TableFriendlyObservableMap;
import com.massimobono.podiliardino.util.TerConsumer;
import com.massimobono.podiliardino.util.TerFunction;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.util.Pair;

public class SQLiteDAOImpl implements DAO {

	private static final Logger LOG = LogManager.getLogger(SQLiteDAOImpl.class.getName());

	private static final int QUERY_TIMEOUT = 30;

	private File databaseFileName;

	private class PreparedStatements implements Closeable {
		private Map<String, PreparedStatement> preparedStatements;

		public PreparedStatements(Connection connection) throws SQLException {
			this.preparedStatements = new HashMap<>();

			this.preparedStatements.put("insertPlayer",connection.prepareStatement("INSERT INTO player(name, surname, birthday, phone) VALUES(?,?,?,?)"));
			this.preparedStatements.put("getAllPlayers",connection.prepareStatement("SELECT id, name, surname, birthday, phone FROM player"));
			this.preparedStatements.put("updatePlayer",connection.prepareStatement("UPDATE OR ROLLBACK player SET name=?, surname=?, birthday=?,phone=? WHERE id=?"));
			this.preparedStatements.put("deletePlayer",connection.prepareStatement("DELETE FROM player WHERE id=?"));

			this.preparedStatements.put("insertTeam",connection.prepareStatement("INSERT INTO team(name, date) VALUES(?,?)"));
			this.preparedStatements.put("getAllTeams",connection.prepareStatement("SELECT id, name, date FROM team"));
			this.preparedStatements.put("updateTeam",connection.prepareStatement("UPDATE OR ROLLBACK team SET name=?, date=? WHERE id=?"));
			this.preparedStatements.put("deleteTeam",connection.prepareStatement("DELETE FROM team WHERE id=?"));

			this.preparedStatements.put("getPlayersInTeam",connection.prepareStatement("SELECT pct.player_id FROM player_compose_team as pct WHERE pct.team_id=?"));
			this.preparedStatements.put("getTeamWithPlayer",connection.prepareStatement("SELECT pct.team_id FROM player_compose_team as pct WHERE pct.player_id=?"));

			this.preparedStatements.put("addPlayerComposeTeam",connection.prepareStatement("INSERT OR IGNORE INTO player_compose_team(player_id, team_id) VALUES(?,?)"));
			this.preparedStatements.put("removePlayerComposeTeam",connection.prepareStatement("DELETE FROM player_compose_team WHERE player_id=? AND team_id=?"));

			this.preparedStatements.put("insertTournament",connection.prepareStatement("INSERT INTO tournament(name,start_date,end_date) VALUES(?,?,?);"));
			this.preparedStatements.put("getAllTournaments",connection.prepareStatement("SELECT id,name,start_date,end_date FROM tournament"));
			this.preparedStatements.put("updateTournament",connection.prepareStatement("UPDATE OR ROLLBACK tournament SET name=?, start_date=?, end_date=? WHERE id=?"));
			this.preparedStatements.put("deleteTournament",connection.prepareStatement("DELETE FROM tournament WHERE id=?"));

			this.preparedStatements.put("insertOrIgnorePartecipation", connection.prepareStatement("INSERT OR IGNORE INTO partecipation(team_id, tournament_id) VALUES (?,?)"));
			this.preparedStatements.put("getAllPartecipations", connection.prepareStatement("SELECT p.team_id, p.tournament_id FROM partecipation AS p"));
			this.preparedStatements.put("deletePartecipation", connection.prepareStatement("DELETE FROM partecipation WHERE team_id=? and tournament_id=?"));

			this.preparedStatements.put("getPartecipationsOfTeam",connection.prepareStatement("SELECT p.team_id, p.tournament_id FROM partecipation AS p WHERE p.team_id=?"));
			this.preparedStatements.put("getPartecipationsInTournament",connection.prepareStatement("SELECT p.team_id, p.tournament_id FROM partecipation AS p WHERE p.tournament_id=?"));

			this.preparedStatements.put("insertDay", connection.prepareStatement("INSERT INTO day(number,date,tournament_id) VALUES(?,?,NULL);"));
			this.preparedStatements.put("getAllDays", connection.prepareStatement("SELECT id,number,date,tournament_id FROM day"));
			this.preparedStatements.put("updateDay", connection.prepareStatement("UPDATE OR ROLLBACK day SET number=?,date=? WHERE id=?"));
			this.preparedStatements.put("deleteDay", connection.prepareStatement("DELETE FROM day WHERE id=?"));

			this.preparedStatements.put("insertDivision", connection.prepareStatement("UPDATE day SET tournament_id=? WHERE id=?"));
			this.preparedStatements.put("deleteDivision", connection.prepareStatement("UPDATE day SET tournament_id=NULL WHERE id=?"));

			this.preparedStatements.put("getTournamentFromDay", connection.prepareStatement("SELECT tournament_id FROM day WHERE id=?"));
			this.preparedStatements.put("getDaysInTournament", connection.prepareStatement("SELECT d.id FROM day AS d WHERE tournament_id=?"));

			// MATCHES

			this.preparedStatements.put("insertMatch", connection.prepareStatement("INSERT OR IGNORE INTO match(team1_id, team2_id, day_id, points_earned_from_winning, points_earned_from_losing, team1_goals, team2_goals, status) VALUES (?,?,?,?,?,?,?,?)"));
			this.preparedStatements.put("deleteMatch", connection.prepareStatement("DELETE FROM match WHERE team1_id=? AND team2_id=? AND day_id=?"));
			this.preparedStatements.put("setMatchStatus", connection.prepareStatement("UPDATE OR ROLLBACK match set team1_goals=?, team2_goals=?, status=? WHERE team1_id=? AND team2_id=? and day_id=?"));

			this.preparedStatements.put("getMatchesFromTeam", connection.prepareStatement("SELECT team1_id, team2_id, day_id, points_earned_from_winning, points_earned_from_losing, team1_goals, team2_goals, status FROM match WHERE team1_id=? OR team2_id=?"));
			this.preparedStatements.put("getMatchesFromDay", connection.prepareStatement("SELECT team1_id, team2_id, day_id, points_earned_from_winning, points_earned_from_losing, team1_goals, team2_goals, status FROM match WHERE day_id=?"));

			//GENERAL

			this.preparedStatements.put("lastInsertedRow",connection.prepareStatement("SELECT seq as last_inserted_id FROM sqlite_sequence WHERE name=?;"));
		}

		public PreparedStatement getGetMatchesFromDay() {
			return this.get("getMatchesFromDay");
		}

		public PreparedStatement getGetMatchesFromTeam() {
			return this.get("getMatchesFromTeam");
		}

		public PreparedStatement getDeleteMatch() {
			return this.get("deleteMatch");
		}

		public PreparedStatement getInsertMatch() {
			return this.get("insertMatch");
		}

		public PreparedStatement getSetMatchStatus() {
			return this.get("setMatchStatus");
		}

		public PreparedStatement getDeleteDivision() {
			return this.get("deleteDivision");
		}
		
		public PreparedStatement getInsertDivision() {
			return this.get("insertDivision");
		}

		public PreparedStatement getGetDaysInTournament() {
			return this.get("getDaysInTournament");
		}

		public PreparedStatement getGetTournamentFromDay() {
			return this.get("getTournamentFromDay");
		}

		public PreparedStatement getDeleteDay() {
			return this.get("deleteDay");
		}

		public PreparedStatement getUpdateDay() {
			return this.get("updateDay");
		}

		public PreparedStatement getGetAllDays() {
			return this.get("getAllDays");
		}

		public PreparedStatement getInsertDay() {
			return this.get("insertDay");
		}

		public PreparedStatement getInsertOrIgnorePartecipation() {
			return this.preparedStatements.get("insertOrIgnorePartecipation");
		}

		public PreparedStatement getGetAllPartecipations() {
			return this.preparedStatements.get("getAllPartecipations");
		}

		public PreparedStatement getUpdatePartecipation() {
			return this.preparedStatements.get("updatePartecipation");
		}

		public PreparedStatement getDeletePartecipation() {
			return this.preparedStatements.get("deletePartecipation");
		}

		public PreparedStatement getInsertPlayer() {
			return this.preparedStatements.get("insertPlayer");
		}

		public PreparedStatement getGetAllPlayers() {
			return this.preparedStatements.get("getAllPlayers");
		}

		public PreparedStatement getUpdatePlayer() {
			return this.preparedStatements.get("updatePlayer");
		}

		public PreparedStatement getDeletePlayer() {
			return this.preparedStatements.get("deletePlayer");
		}

		public PreparedStatement getInsertTeam() {
			return this.preparedStatements.get("insertTeam");
		}

		public PreparedStatement getGetAllTeams() {
			return this.preparedStatements.get("getAllTeams");
		}

		public PreparedStatement getUpdateTeam() {
			return this.preparedStatements.get("updateTeam");
		}

		public PreparedStatement getDeleteTeam() {
			return this.preparedStatements.get("deleteTeam");
		}

		public PreparedStatement getGetPlayersInTeam() {
			return this.preparedStatements.get("getPlayersInTeam");
		}

		public PreparedStatement getGetTeamWithPlayer() {
			return this.preparedStatements.get("getTeamWithPlayer");
		}

		public PreparedStatement getAddPlayerComposeTeam() {
			return this.preparedStatements.get("addPlayerComposeTeam");
		}

		public PreparedStatement getRemovePlayerComposeTeam() {
			return this.preparedStatements.get("removePlayerComposeTeam");
		}

		public PreparedStatement getGetPartecipationsOfTeam() {
			return this.preparedStatements.get("getPartecipationsOfTeam");
		}

		public PreparedStatement getInsertTournament() {
			return this.preparedStatements.get("insertTournament");
		}

		public PreparedStatement getGetAllTournaments() {
			return this.preparedStatements.get("getAllTournaments");
		}

		public PreparedStatement getUpdateTournament() {
			return this.preparedStatements.get("updateTournament");
		}

		public PreparedStatement getDeleteTournament() {
			return this.preparedStatements.get("deleteTournament");
		}

		public PreparedStatement getGetPartecipationsInTournament() {
			return this.preparedStatements.get("getPartecipationsInTournament");
		}

		public PreparedStatement getLastInsertedRow() {
			return this.preparedStatements.get("lastInsertedRow");
		}

		private PreparedStatement get(String key) {
			return this.preparedStatements.get(key);
		}

		@Override
		public void close() throws IOException {
			try {
				for (PreparedStatement p : this.preparedStatements.values()) {
					p.close();
				}
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}
	}

	/**
	 * True if the connection is used by another level of the java Stack, False otherwise.
	 * The field is useful to ensure no stack uses twice {@link #connection} in 2 different places.
	 * 
	 */
	private boolean connectionUsed;
	private Connection singleConnection;
	private PreparedStatements preparedStatements;

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
	 * A map containing all the {@link Day} computed by the DAO.
	 * 
	 * Every time we add a new {@link Day}, this map stores the reference. Every time the DAO
	 * removes a  {@link Day}, this map destroy a reference. Everytime we get  {@link Day} from the DAO,
	 * the map stores a new reference if it doesn't have already. Otherwise the DAO does not create a new  {@link Day} instance.
	 */
	private TableFriendlyObservableMap<Long, Day> days;

	/**
	 * 
	 * @param databaseFileName the file sqlite will use to store data.
	 * @param performSetup true if you want to perform a setup immediately
	 * @throws DAOException if something bad happens
	 */
	public SQLiteDAOImpl(File databaseFileName, boolean performSetup) throws DAOException {
		this.databaseFileName = databaseFileName;
		this.preparedStatements = null;

		//main entities list
		this.players = new TableFriendlyObservableMap<>();
		this.teams = new TableFriendlyObservableMap<>();
		this.tournaments = new TableFriendlyObservableMap<>();
		this.days = new TableFriendlyObservableMap<>();

		this.connectionUsed = false;
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
				this.tournaments.clear();
				s.executeUpdate("DELETE FROM tournament;");
				this.days.clear();
				s.executeUpdate("DELETE FROM day;");

				s.executeUpdate("DELETE FROM partecipation;");
				s.executeUpdate("DELETE FROM player_compose_team;");
				s.executeUpdate("DELETE FROM match;");

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

				s.executeUpdate("CREATE TABLE IF NOT EXISTS player_compose_team (player_id INTEGER REFERENCES player(id) ON UPDATE CASCADE, team_id INTEGER REFERENCES team(id) ON UPDATE CASCADE, UNIQUE(player_id, team_id));");

				s.executeUpdate("CREATE TABLE IF NOT EXISTS tournament (id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(100), start_date varchar(20), end_date varchar(20));");

				s.executeUpdate("CREATE TABLE IF NOT EXISTS day (id INTEGER PRIMARY KEY AUTOINCREMENT, number INTEGER, date varchar(20), tournament_id INTEGER REFERENCES tournament(id) ON UPDATE CASCADE);");

				s.executeUpdate("CREATE TABLE IF NOT EXISTS match (team1_id INTEGER REFERENCES team(id) ON UPDATE CASCADE, team2_id INTEGER REFERENCES team(id) ON UPDATE CASCADE, day_id INTEGER REFERENCES day(id) ON UPDATE CASCADE, points_earned_from_winning INTEGER, points_earned_from_losing INTEGER, team1_goals INTEGER, team2_goals INTEGER, status INTEGER, UNIQUE(team1_id, team2_id, day_id));");

				s.executeUpdate("CREATE TABLE IF NOT EXISTS partecipation (team_id INTEGER REFERENCES team(id) ON UPDATE CASCADE, tournament_id INTEGER REFERENCES tournament(id) ON UPDATE CASCADE, UNIQUE(team_id, tournament_id));");

				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}

	@Override
	public void tearDown() throws DAOException {
		try {
			if (preparedStatements != null) {
				this.preparedStatements.close();
			}
		} catch (IOException e) {
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
	 * @param queries a lambda where you can execute all the statement you want. Remeber to return the exception you find, or "null" if you encounter no exception.The function signature is the following:
	 * 	<ul>
	 * 		<li>the first parameter is the connection the software is using to query the database</li>
	 * 		<li>the second parameter is a {@link Statement} instance you can use to perform generics queries</li>
	 * 		<li>the third parameter is a {@link PreparedStatements} instance containing all the {@link PreparedStatement} available for you to use to quickly query the system</li>
	 * 		<li>the output is the first exception you encounter inside the lamda or <tt>null<//tt> if you encounter none of them</li>
	 * 	</ul>  
	 * @throws DAOException if something bad happen
	 */
	private void connectAndThenDo(boolean tryPreparedStatement, TerFunction<Connection, Statement, PreparedStatements, Exception> queries) throws DAOException {
		if (connectionUsed) {
			throw new DAOException("you're trying to access connectAndThenDo Mehotd in 2 different parts of the stack! This can't happen because SQLite3 doesn't support multiple connections!");
		}
		try {
			if (this.singleConnection == null) {
				this.singleConnection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", databaseFileName.getAbsolutePath()));
			}
			if (tryPreparedStatement && this.preparedStatements == null) {
				this.preparedStatements = new PreparedStatements(this.singleConnection);
			}
			try (Statement statement = this.singleConnection.createStatement()) {
				this.connectionUsed = true;
				statement.setQueryTimeout(QUERY_TIMEOUT);
				Exception e = queries.apply(this.singleConnection, statement, this.preparedStatements);
				if (e != null) {
					throw e;
				}
			} finally {
				connectionUsed = false;
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
	 * Represents an abstract way yo add a single row inside a table
	 * 
	 * This function aims tyo provide a generic way to add a single row in a single table. Nothing more, nothing less
	 * 
	 * @param toAdd an instance to add in the database whose id has not been set yet
	 * @param tableInvolved the table where the insertion happens
	 * @param insertQuery a function that actually perform the insertion of <tt>toAdd</tt>.The function signature is the following:
	 * 	<ul>
	 * 		<li>the first parameter is the connection the software is using to query the database</li>
	 * 		<li>the second parameter is a {@link Statement} instance you can use to perform generics queries</li>
	 * 		<li>the third parameter is a {@link PreparedStatements} instance containing all the {@link PreparedStatement} available for you to use to quickly query the system</li>
	 * 		<li>the output is the first exception you encounter inside the lamda or <tt>null<//tt> if you encounter none of them</li>
	 * 	</ul>  
	 * @param afterInsertQuery a function you can use to perform addtional insertion in other tables. You can use this to add dependency rows inside the DB.The function signature is the following:
	 * 	<ul>
	 * 		<li>the first parameter is the connection the software is using to query the database</li>
	 * 		<li>the second parameter is a {@link Statement} instance you can use to perform generics queries</li>
	 * 		<li>the third parameter is a {@link PreparedStatements} instance containing all the {@link PreparedStatement} available for you to use to quickly query the system</li>
	 * 		<li>the output is the first exception you encounter inside the lamda or <tt>null<//tt> if you encounter none of them</li>
	 * 	</ul>  
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

				ps.getLastInsertedRow().setString(1, tableInvolved);
				ResultSet rs = ps.getLastInsertedRow().executeQuery();
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

	/**
	 * Represents an abstract procedure used to update a set of tables involving the particular element <tt>toEdit</tt>
	 * 
	 * This function let you synchronize the instance already updated by the code into the database.
	 * 
	 * @param toEdit the instance already updated by the code
	 * @param updateQueries the list of queries to do in order to synchronize the db. The function signature is the following:
	 * 	<ul>
	 * 		<li>the first parameter is the connection the software is using to query the database</li>
	 * 		<li>the second parameter is a {@link Statement} instance you can use to perform generics queries</li>
	 * 		<li>the third parameter is a {@link PreparedStatements} instance containing all the {@link PreparedStatement} available for you to use to quickly query the system</li>
	 * 		<li>the output is the first exception you encounter inside the lamda or <tt>null<//tt> if you encounter none of them</li>
	 * 	</ul>  
	 * @return the same instance passed as <tt>toEdit</tt>
	 * @throws DAOException if something goes wrong
	 */
	private <TOEDIT> TOEDIT abstractUpdate(final TOEDIT toEdit, TerFunction<Connection, Statement, PreparedStatements, Exception> updateQueries) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				Exception e = updateQueries.apply(c, s, ps);
				if (e != null) {
					throw e;
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		return toEdit;
	}

	/**
	 * Function used to abstract the deletion of a row inside the database of a concept <tt>toRemove</tt>.
	 * 
	 * You can use this function whenever you need to remove something from the database
	 * @param toRemove the instance to remove from the database
	 * @param deleteQueries the queries the program needs to perform in order to succesfully delete the concept. 
	 * 	The function signature is the following:
	 * 	<ul>
	 * 		<li>the first parameter is the connection the software is using to query the database</li>
	 * 		<li>the second parameter is a {@link Statement} instance you can use to perform generics queries</li>
	 * 		<li>the third parameter is a {@link PreparedStatements} instance containing all the {@link PreparedStatement} available for you to use to quickly query the system</li>
	 * 		<li>the output is the first exception you encounter inside the lamda or <tt>null<//tt> if you encounter none of them</li>
	 * 	</ul>  
	 * @param actionBeforeDeleting a series of instruction to perform before the deletion process actually starts. Returns an exception if the process fails, null otherwise
	 * @param actionWhenDeleteSucceed a series of instruction to perform if the deletion succeeds. Returns an exception if the process fails, null otherwise
	 * @throws DAOException if something bad happens. <tt>actionWhenDeleteSucceed</tt> won't be called at all in this scenario
	 */
	private <TOREMOVE> void abstractRemove(final TOREMOVE toRemove, TerFunction<Connection, Statement, PreparedStatements, Exception> deleteQueries, Supplier<Exception> actionBeforeDeleting, Supplier<Exception> actionWhenDeleteSucceed) throws DAOException {

		Exception e1 = actionBeforeDeleting.get();
		if (e1 != null) {
			throw new DAOException(e1);
		}
		this.connectAndThenDo((c,s,ps) -> {
			try {
				Exception e = deleteQueries.apply(c, s, ps);
				if (e != null) {
					throw e;
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		e1 = actionWhenDeleteSucceed.get();
		if (e1 != null) {
			throw new DAOException(e1);
		}
	}

	/**
	 * A method that generalize the operation of getting object from db according to a particular assertion
	 * 
	 * The algorithm is the following:
	 * <ol>
	 * 	</li>we scan the database according to <tt>selectQuery</tt>: that represents the basis of our search</li>
	 * 	<li>if an "id" of a row computed by <tt>selectQuery</tt> is already inside <tt>mapContainingAlreadyVisitedObjectSupplier</tt>, we fetch the one inside that map;</li>
	 * 	<li>otherwise we create a new <tt>TOGET</tt> instance using <tt>setupToGetWithResultSet</tt> and then we add it inside <tt>mapContainingAlreadyVisitedObjectSupplier</tt>;
	 * 		After that we can setup it correctly by using additional action inside <tt>setupJustCreatedObjectAction</tt>. We do that <b>after</b> having stored it inside the db because here you might
	 * 		want to setup the relationships between different concepts: in order to avoid infinite loops the whole DAO relies on the fact that already explored concepts are already inside the
	 * 		<tt>mapContainingAlreadyVisitedObjectSupplier</tt>; if you were to setup before storing the newly created object inside the <tt>mapContainingAlreadyVisitedObjectSupplier</tt> a cyclic relationship
	 * 		(i.e. a user buys several products and several products are buyed by several user) would generate an infinite loop;
	 * 	<li>Regardless of the presence of the item inside <tt>mapContainingAlreadyVisitedObjectSupplier</tt> we check if the concept <tt>TOGET</tt>
	 * 		satisfies the <tt>filter</tt>. If so we add it inside the return value</li>
	 * </ol>
	 * 
	 * @param filter the function used to check whether or not a concept of type <tt>TOGET</tt> from the database should be included in the output
	 * @param selectQuery a prepared statement used to actually fetch data from the database. this query should be a "select" query, returns a <b>single concept</b> from the database and have a "id" column in the returned value
	 * @param mapContainingAlreadyVisitedObjectSupplier a map containing all the instances of type <tt>TOGET</tt> fetched from the database up until now. A instance won't be refetched if already present
	 * 	in this structure
	 * @param emptyConstructor the empty constructor to optionally use if we need to create a new model concept;
	 * @param setupToGetWithResultSet a function setupping the just created <tt>TOGET</tt> instance with the data received from the database using <tt>selectQuery</tt> query. This function will be called
	 * 	only if <tt>mapContainingAlreadyVisitedObjectSupplier</tt> does not contain already an element <tt>TOGET</tt>; the function is structured as follows:
	 * <ul>
	 * 	<li>the function has to return the first exception encoutered in the lambda (null if everything goes fine)</li>
	 * 	<li>the function has to change the instance provided by the input parameter: that parameter has been genereated automatically</li>
	 * </ul>
	 * @param setupJustCreatedObjectAction additional operation to perform after <tt>setupToGetWithResultSet</tt> construction function has been called and its outpu has been stored inside <tt>mapContainingAlreadyVisitedObjectSupplier</tt> in order to setup the newly created instance.
	 * The long represents the id of the row obtained from the database 
	 * @return a collection of <tt>TOGET</tt> instances satisfying the criterion expressed in <tt>filter</tt>
	 * @throws DAOException if something bad happens
	 */
	private <TOGET extends Indexable> Collection<TOGET> abstractGetObjectThat(Function<TOGET, Boolean> filter, TerFunction<Connection, Statement, PreparedStatements, PreparedStatement> selectQuery, Supplier<ObservableMap<Long, TOGET>> mapContainingAlreadyVisitedObjectSupplier, Supplier<TOGET> emptyConstructor, BiFunction<TOGET, ResultSet, Exception> setupToGetWithResultSet, BiFunction<TOGET, Long, Exception> setupJustCreatedObjectAction) throws DAOException {
		Collection<Pair<Long, TOGET>> objectToSetup = new ArrayList<>();
		Collection<TOGET> objectAlreadySetup = new ArrayList<>();
		Collection<TOGET> retVal = new ArrayList<>(); 
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ResultSet rs = selectQuery.apply(c, s, ps).executeQuery();
				while (rs.next()) {
					TOGET obj = null;
					if (mapContainingAlreadyVisitedObjectSupplier.get().containsKey(rs.getLong("id"))) {
						obj = mapContainingAlreadyVisitedObjectSupplier.get().get(rs.getLong("id"));
						objectAlreadySetup.add(obj);
					} else {
						obj = emptyConstructor.get();
						Exception e = setupToGetWithResultSet.apply(obj, rs);
						if (e != null) {
							throw e;
						}
						mapContainingAlreadyVisitedObjectSupplier.get().putIfAbsent(obj.getId(), obj);
						objectToSetup.add(new Pair<>(rs.getLong("id"), obj));
					}
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});

		for (Pair<Long, TOGET> pair : objectToSetup) {
			Exception e = setupJustCreatedObjectAction.apply(pair.getValue(), pair.getKey());
			if (e != null) {
				throw new DAOException(e);
			}
		}
		objectAlreadySetup.addAll(objectToSetup.stream().map(p -> p.getValue()).collect(Collectors.toList()));
		for (TOGET obj : objectAlreadySetup) {
			if (filter.apply(obj)) {
				retVal.add(obj);
			}
		}
		return retVal;
	}
	
	/**
	 * Often there are several list that the DAO needs to listen to in order to add/remove sutff from the database.
	 * This method allows you to generate a compliant listener that will add/remove rows from the db
	 * @return
	 */
	private <SOURCE, CHANGED> ListChangeListener<CHANGED> getDefaultListListener(final SOURCE source, BiFunction<SOURCE, CHANGED, Exception> adder, BiFunction<SOURCE, CHANGED, Exception> remover) {
		return new ListChangeListener<CHANGED>() {

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends CHANGED> e) {
				Exception ex;
				while (e.next()) {
					try {
						for (CHANGED obj : e.getAddedSubList()) {
							ex = adder.apply(source, obj);
							if (ex != null) {
								throw ex;
							}
						}
						for (CHANGED obj : e.getRemoved()) {
							ex = remover.apply(source, obj);
							if (ex != null) {
								throw ex;
							}
						}
					} catch (Exception ex2) {
						ex2.printStackTrace();
						//TODO complete
					}
				}
			}
		};
	}

	@Override
	public void close() throws IOException {
		try {
			this.tearDown();
		} catch (DAOException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Setup, for every relationship of the entity, a listener that will notify to the DAo if new relationships have been created
	 * @param p
	 */
	private void setupPlayerListeners(Player p) {
		//we have added the player. Now we need to listens forrelationships changes
		p.getTeams().addListener(this.getDefaultListListener(p, 
			(s,c) -> {
				try {
					addDBCompose(p, c);
					return null;
				} catch (DAOException e) {
					return e;
				}
			},
			(s,c) -> {
				try {
					removeDBCompose(p, c);
					return null;
				} catch (DAOException e) {
					return e;
				}
			}));
	}

	@Override
	public Player add(final Player p) throws DAOException {
		return this.abstractAdd(
				p, 
				"player",
				(c,s,ps) -> {
					try {
						Optional<String> birthday = p.getBirthdayAsStandardString();
						ps.getInsertPlayer().setString(1, p.getName().get());
						ps.getInsertPlayer().setString(2, p.getSurname().get());
						ps.getInsertPlayer().setString(3, birthday.isPresent() ? birthday.get() : Utils.EMPTY_DATE);
						ps.getInsertPlayer().setString(4, p.getPhone().get().isPresent() ? p.getPhone().get().get() : Utils.EMPTY_PHONE);
						ps.getInsertPlayer().addBatch();
						
						ps.getInsertPlayer().executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(c,s,ps) -> null,
				() -> {
					this.setupPlayerListeners(p);
					this.players.put(p.getId(), p);
				}
				);
	}

	@Override
	public Player updatePlayer(final Player player) throws DAOException {
		return this.abstractUpdate(
				player,
				(c,s,ps) -> {
					try {
						Optional<String> birthday = player.getBirthdayAsStandardString();
						ps.getUpdatePlayer().setString(1, player.getName().get());
						ps.getUpdatePlayer().setString(2, player.getSurname().get());
						ps.getUpdatePlayer().setString(3, birthday.isPresent() ? birthday.get() : Utils.EMPTY_DATE);
						ps.getUpdatePlayer().setString(4, player.getPhone().get().isPresent() ? player.getPhone().get().get() : Utils.EMPTY_PHONE);
						ps.getUpdatePlayer().setLong(5, player.getId());
						ps.getUpdatePlayer().addBatch();

						c.setAutoCommit(false);
						ps.getUpdatePlayer().executeBatch();
						c.setAutoCommit(true);
						return null;
					} catch (SQLException e) {
						return e;
					}
				});
	}

	@Override
	public void removePlayer(final Player p) throws DAOException {
		this.abstractRemove(
				p,
				(c,s,ps) -> {
					try {
						ps.getDeletePlayer().setLong(1, p.getId());
						ps.getDeletePlayer().addBatch();
						ps.getDeletePlayer().executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				() -> null,
				() -> {this.players.remove(p.getId()); return null;}
				);
	}

	@Override
	public Collection<Player> getAllPlayersThat(Function<Player, Boolean> filter) throws DAOException {
		return this.abstractGetObjectThat(
				filter, 
				(c,s,ps) -> ps.getGetAllPlayers(),
				() -> players,
				Player::new,
				(p, rs) -> {
					try {
						p.setId(rs.getLong("id"));
						p.getName().set(rs.getString("name"));
						p.getSurname().set(rs.getString("surname"));
						p.getBirthday().set(Optional.ofNullable(!rs.getString("birthday").equalsIgnoreCase(Utils.EMPTY_DATE) ? Utils.getDateFrom(rs.getString("birthday")) : null));
						p.getPhone().set(Optional.ofNullable(!rs.getString("phone").equalsIgnoreCase(Utils.EMPTY_PHONE) ? rs.getString("phone") : null));
						return null;
					} catch (SQLException e) {
						return e;
					}
				}, 
				(p, id) -> {
					try {
						this.setupPlayerListeners(p);
						this.computeComposeFromPlayer(p);
						return null;
					} catch (Exception e) {
						return e;
					}
				});
	}

	public ObservableList<Player> getPlayerList() throws DAOException {
		return this.players.observableValueList();
	}

	public ObservableList<Team> getTeamList() throws DAOException {
		return this.teams.observableValueList();
	}
	
	/**
	 * Add the relation "compose" inside the DB between p and t
	 * 
	 * <b>This function deals only with the db, not with the model. We assume the model is fine</b>
	 * 
	 * @param p
	 * @param t
	 * @throws DAOException 
	 */
	private void addDBCompose(Player p, Team t) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getAddPlayerComposeTeam().setLong(1, p.getId());
				ps.getAddPlayerComposeTeam().setLong(2, t.getId());
				
				ps.getAddPlayerComposeTeam().executeUpdate();
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}
	
	/**
	 * Remove the relation "compose" inside the DB between p and t
	 * 
	 * <b>This function deals only with the db, not with the model. We assume the model is fine</b>
	 * 
	 * @param p
	 * @param t
	 * @throws DAOException 
	 */
	private void removeDBCompose(Player p, Team t) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getRemovePlayerComposeTeam().setLong(1, p.getId());
				ps.getRemovePlayerComposeTeam().setLong(2, t.getId());
				
				ps.getRemovePlayerComposeTeam().executeUpdate();
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}
	
	/**
	 * Connects the model classes {@link Player} and {@link Team} with the relationships
	 * declared inside the database
	 * 
	 * @param p the player whose relationship "compose"  needed to be created inside the model 
	 * @throws DAOException 
	 */
	private void computeComposeFromPlayer(Player p) throws DAOException {
		List<Long> teamIds = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetTeamWithPlayer().setLong(1, p.getId());
				ResultSet rs = ps.getGetTeamWithPlayer().executeQuery();
				while (rs.next()) {
					teamIds.add(rs.getLong("team_id"));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});

		for (int j = 0; j<teamIds.size(); j++) {
			final int i = j;
			this.getTeamThat(t -> t.getId() == teamIds.get(i)).ifPresent(t -> {
				t.getPlayers().add(p);
				p.getTeams().add(t);
			});
		}
	}
	
	/**
	 * Connects the model classes {@link Player} and {@link Team} with the relationships
	 * declared inside the database
	 * 
	 * @param t the team whose relationship "compose"  needed to be created inside the model 
	 * @throws DAOException 
	 */
	private void computeComposeFromTeam(Team t) throws DAOException {
		List<Long> playerIds = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetPlayersInTeam().setLong(1, t.getId());
				ResultSet rs = ps.getGetPlayersInTeam().executeQuery();
				while (rs.next()) {
					playerIds.add(rs.getLong("player_id"));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});

		for (int j = 0; j<playerIds.size(); j++) {
			final int i = j;
			this.getPlayerThat(p -> p.getId() == playerIds.get(i)).ifPresent(p -> {
				t.getPlayers().add(p);
				p.getTeams().add(t);
			});
		}
	}
	
	/**
	 * Setup, for every relationship of the entity, a listener that will notify to the DAO if new relationships have been created
	 * @param team
	 */
	private void setupTeamListeners(Team team) {
		team.getPlayers().addListener(this.getDefaultListListener(
				team, 
				(t,p) -> {
					try {
						addDBCompose(p, t);
					} catch (DAOException e) {
						return e;
					}
					return null;
				},
				(t,p) -> {
					try {
						removeDBCompose(p, t);
						return null;
					} catch (DAOException e) {
						return e;
					}
					
				}));
		team.getPartecipations().addListener(this.getDefaultListListener(
				team, 
				(t,p) -> {
					try {
						addDBPartecipation(p);
						return null;
					} catch (DAOException e) {
						return e;
					}
					
				},
				(t,p) -> {
					try {
						removeDBPartecipation(p);
						return null;
					} catch (DAOException e) {
						return e;
					}
				}
				));
		team.getMatches().addListener(this.getDefaultListListener(
				team,
				(t,m) -> {
					try {
						addDBMatch(m);
						return null;
					} catch (DAOException e) {
						return e;
					}
				},
				(t,m) -> {
					try {
						removeDBMatch(m);
						return null;
					} catch (DAOException e) {
						return e;
					}
				}));	
	}

	@Override
	public Team addTeam(final Team team) throws DAOException {
		return this.abstractAdd(
				team,
				"team",
				(c,s,ps) -> {
					try {
						ps.getInsertTeam().setString(1, team.getName().get());
						ps.getInsertTeam().setString(2, Utils.getStandardDateFrom(team.getDate().get()));
						ps.getInsertTeam().addBatch();
						ps.getInsertTeam().executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(c,s,ps) -> {
						this.setupTeamListeners(team);
						return null;
				},
				() -> {this.teams.put(team.getId(), team);}
				);
	}

	@Override
	public Team update(final Team team) throws DAOException {
		return this.abstractUpdate(
				team,
				(c,s,ps) -> {
					try {
						ps.getUpdateTeam().setString(1, team.getName().get());
						ps.getUpdateTeam().setString(2, Utils.getStandardDateFrom(team.getDate().get()));
						ps.getUpdateTeam().setLong(3, team.getId());
						ps.getUpdateTeam().addBatch();

						ps.getUpdateTeam().executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				});
	}

	@Override
	public Collection<Team> getAllTeamsThat(Function<Team, Boolean> filter) throws DAOException {
		return this.abstractGetObjectThat(
				filter, 
				(c,s,ps) -> ps.getGetAllTeams(),
				() -> this.teams, 
				Team::new,
				(t, rs) -> {
					try {
						t.setId(rs.getLong("id"));
						t.getName().set(rs.getString("name"));
						t.getDate().set(Utils.getDateFrom(rs.getString("date")));
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(t, id) -> {
					try {
						this.setupTeamListeners(t);
						
						this.computeComposeFromTeam(t);
						this.computePartecipationsFromTeam(t);
						this.computeMatchesFromTeam(t);
						return null;
					} catch (DAOException e) {
						return e;
					}
				});
	}

	@Override
	public void removeTeam(final Team team) throws DAOException {
		this.abstractRemove(
				team, 
				(c,s,ps) -> {
					try {
						ps.getDeleteTeam().setLong(1, team.getId());
						ps.getDeleteTeam().addBatch();
						ps.getDeleteTeam().executeBatch();
						//TODO check team removal
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				() -> null,
				() -> {this.teams.remove(team.getId()); return null;}
				);
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
		Collection<Long> ids = new ArrayList<>();

		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetPlayersInTeam().setLong(1, teamId);
				ResultSet rs = ps.getGetPlayersInTeam().executeQuery();
				while (rs.next()) {
					ids.add(rs.getLong("player_id"));
				}
				return null;
			} catch (SQLException e) {
				return e;
			}
		});

		for (Long id : ids) {
			retVal.addAll(this.getAllPlayersThat(p -> p.getId() == id));
		}

		return retVal;
	}

	private Collection<Team> getTeamsFromPlayer(long player_id) throws DAOException {
		Collection<Team> retVal = new HashSet<>();
		Collection<Long> ids = new ArrayList<>();

		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetTeamWithPlayer().setLong(1, player_id);
				ResultSet rs = ps.getGetTeamWithPlayer().executeQuery();
				while (rs.next()) {
					ids.add(rs.getLong("team_id"));
				}
				return null;
			} catch (SQLException e) {
				return e;
			}
		}); 

		for (Long id : ids) {
			retVal.addAll(this.getAllTeamsThat(t -> t.getId() == id));
		}

		return retVal;
	}
	
	/**
	 * Setup, for every relationship of the entity, a listener that will notify to the DAO if new relationships have been created
	 * @param tournament
	 */
	private void setupTournamentListeners(Tournament tournament) {
		//the model has changed the divide list
		tournament.getPartecipations().addListener(this.getDefaultListListener(
				tournament,
				(t,p) -> {
					try {
						addDBPartecipation(p);
						return null;
					} catch (DAOException e) {
						return e;
					}
				},
				(t,p) -> {
					try {
						removeDBPartecipation(p);
						return null;
					} catch (DAOException e) {
						return e;
					}
				}));
		tournament.getDays().addListener(this.getDefaultListListener(
				tournament,
				(t,d) -> {
					//TODO this function should wor with references
					try {
						addDBDivide(d.getId(), t.getId());
						return null;
					} catch (DAOException e) {
						return e;
					}
				},
				(t,d) -> {
					//TODO this function should wor with references
					try {
						removeDBDivide(d.getId(), t.getId());
						return null;
					} catch (DAOException e) {
						return e;
					}
				}));
	}

	@Override
	public Tournament add(Tournament tournament) throws DAOException {
		return this.abstractAdd(
				tournament, 
				"tournament", 
				(c,s,ps) -> {
					try {
						Optional<LocalDate> endDate = tournament.getEndDate().get();
						ps.getInsertTournament().setString(1, tournament.getName().get());
						ps.getInsertTournament().setString(2, Utils.getStandardDateFrom(tournament.getStartDate().get()));
						ps.getInsertTournament().setString(3, endDate.isPresent() ? Utils.getStandardDateFrom(endDate.get()) : Utils.EMPTY_DATE);
						ps.getInsertTournament().addBatch();
						ps.getInsertTournament().executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(c,s,ps) -> {
					this.setupTournamentListeners(tournament);
					return null;
				},
				() -> {
					this.tournaments.put(tournament.getId(), tournament);
				}
				);
	}

	@Override
	public Tournament update(final Tournament tournament) throws DAOException {
		return this.abstractUpdate(
				tournament,
				(c,s,ps) -> {
					try {
						Optional<LocalDate> endDate = tournament.getEndDate().get();
						ps.getUpdateTournament().setString(1, tournament.getName().get());
						ps.getUpdateTournament().setString(2, Utils.getStandardDateFrom(tournament.getStartDate().get()));
						ps.getUpdateTournament().setString(3, endDate.isPresent() ? Utils.getStandardDateFrom(endDate.get()) : Utils.EMPTY_DATE);
						ps.getUpdateTournament().setLong(4, tournament.getId());
						ps.getUpdateTournament().addBatch();
						ps.getUpdateTournament().executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				}
				);
	}

	@Override
	public void remove(final Tournament tournament) throws DAOException {
		this.abstractRemove(
				tournament, 
				(c,s,ps) -> {
					try {
						//TODO remove days
						ps.getDeleteTournament().setLong(1, tournament.getId());
						ps.getDeleteTournament().executeUpdate();
						return null;
					}catch (SQLException e) {
						return e;
					}
				},
				() -> null,
				() -> {this.tournaments.remove(tournament.getId()); return null;});
	}

	@Override
	public ObservableList<Tournament> getTournamentList() throws DAOException {
		return this.tournaments.observableValueList();
	}

	@Override
	public Collection<Tournament> getAllTournamentsThat(Function<Tournament, Boolean> filter) throws DAOException {
		return this.abstractGetObjectThat(
				filter, 
				(c,s,ps) -> ps.getGetAllTournaments(),
				() -> this.tournaments,
				Tournament::new,
				(t,rs) -> {
					try {
						String endDate = rs.getString("end_date");
						t.setId(rs.getLong("id"));
						t.getName().set(rs.getString("name"));
						t.getStartDate().set(Utils.getDateFrom(rs.getString("start_date")));
						t.getEndDate().set(Optional.ofNullable(!endDate.equalsIgnoreCase(Utils.EMPTY_DATE) ? Utils.getDateFrom(endDate) : null));
						return null;
					} catch (Exception e) {
						return e;
					}
				}, 
				(t,rs) -> {
					try {
						this.setupTournamentListeners(t);
						//ok, we created a tournament, we need to create the relationships of the model as well
						this.computePartecipationsFromTournament(t);
						this.computeDivideFromTournament(t);
						return null;
					} catch (Exception e ){
						return e;
					}
				});
	}

	private <TABLE1 extends Indexable, TABLE2 extends Indexable, NNTABLE>void abstractCompute(long primaryID, TerFunction<Connection, Statement, PreparedStatements, PreparedStatement> startQuery, Function<PreparedStatement, Exception> setupQuery, String secondaryIDName, Supplier<Collection<TABLE1>> firstTableSupplier, Supplier<Collection<TABLE2>> secondTableSuppplier, Supplier<NNTABLE> emptyConstructor, BiFunction<NNTABLE, ResultSet, Exception> nntableSetupper, TerConsumer<TABLE1, TABLE2, NNTABLE> TablesUpdater) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				PreparedStatement query = startQuery.apply(c, s, ps);
				Exception e = setupQuery.apply(query);
				if (e != null) {
					throw e;
				}
				ResultSet rs = query.executeQuery();
				while (rs.next()) {
					final long secondaryID = rs.getLong(secondaryIDName);
					Optional<TABLE1> oTable1 = firstTableSupplier.get().parallelStream().filter(t1 -> t1.getId() == primaryID).findFirst();
					Optional<TABLE2> oTable2 = secondTableSuppplier.get().parallelStream().filter(t2 -> t2.getId() == secondaryID).findFirst();

					if (oTable1.isPresent() && oTable2.isPresent()) {
						NNTABLE nntable = emptyConstructor.get();
						e = nntableSetupper.apply(nntable, rs);
						if (e != null) {
							throw e;
						}
						TablesUpdater.consume(oTable1.get(), oTable2.get(), nntable);
					}
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}

	/**
	 * Computes every {@link Partecipation} of the tournament <tt>tournament_id</tt>
	 * 
	 * It automatically adds the partecipations inside the model, creating a new {@link Partecipation} instance
	 * linking the {@link Team} model class and {@link Tournament} model class
	 * 
	 * @param tournament_id the id of the tournament involved
	 * @throws DAOException if something bad happens
	 */
	private void computePartecipationsFromTournament(Tournament tournament) throws DAOException {
		List<Long> team_ids = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetPartecipationsInTournament().setLong(1, tournament.getId());
				ResultSet rs = ps.getGetPartecipationsInTournament().executeQuery();
				while (rs.next()) {
					team_ids.add(rs.getLong("team_id"));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});

		for (int j = 0; j<team_ids.size(); j++) {
			final int i = j;
			this.getTeamThat(p -> p.getId() == team_ids.get(i)).ifPresent(team -> {
				Partecipation p = new Partecipation(tournament, team);
				p.getTournament().get().getPartecipations().add(p);
				p.getTeam().get().getPartecipations().add(p);
			});
		}
	}

	/**
	 * Computes every {@link Partecipation} of the team <tt>team_id</tt>
	 * 
	 * It automatically adds the partecipations inside the model, creating a new {@link Partecipation} instance
	 * linking the {@link Team} model class and {@link Tournament} model class
	 * 
	 * @param team_id the id of the team involved
	 * @throws DAOException if something bad happens
	 */
	private void computePartecipationsFromTeam(Team team) throws DAOException {
		List<Long> tournament_ids = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetPartecipationsOfTeam().setLong(1, team.getId());
				ResultSet rs = ps.getGetPartecipationsOfTeam().executeQuery();
				while (rs.next()) {
					tournament_ids.add(rs.getLong("tournament_id"));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});

		for (int j = 0; j<tournament_ids.size(); j++) {
			final int i = j;
			this.getTournamentThat(p -> p.getId() == tournament_ids.get(i)).ifPresent(tournament -> {
				Partecipation p = new Partecipation(tournament, team);
				p.getTournament().get().getPartecipations().add(p);
				p.getTeam().get().getPartecipations().add(p);
			});
		}
	}

	/**
	 * Add a {@link Partecipation} inside the db
	 * @param partecipation the partecipation to add in the database
	 * @return the partecipation given in input
	 * @throws DAOException if something bad happens
	 */
	private Partecipation addDBPartecipation(Partecipation partecipation) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				LOG.debug("adding partecipation {} team_id={} tournament_id={}...", partecipation, partecipation.getTeam().get().getId(), partecipation.getTournament().get().getId());
				ps.getInsertOrIgnorePartecipation().setLong(1, partecipation.getTeam().get().getId());
				ps.getInsertOrIgnorePartecipation().setLong(2, partecipation.getTournament().get().getId());
				ps.getInsertOrIgnorePartecipation().executeUpdate();
				LOG.debug("DONE");
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		return partecipation;
	}

	/**
	 * Removed a {@link Partecipation} from the db
	 * @param partecipation the partecipation to remove from the database
	 * @return the partecipation given in input
	 * @throws DAOException if something bad happens
	 */
	private void removeDBPartecipation(Partecipation partecipation) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				LOG.debug("removing partecipation {} team_id={} tournament_id={}...", partecipation, partecipation.getTeam().get().getId(), partecipation.getTournament().get().getId());
				ps.getDeletePartecipation().setLong(1, partecipation.getTeam().get().getId());
				ps.getDeletePartecipation().setLong(2, partecipation.getTournament().get().getId());
				ps.getDeletePartecipation().executeUpdate();
				LOG.debug("DONE");
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}
	
	private void setupDayListeners(Day day) {
		day.getTournament().addListener((ov, oldValue, newValue) -> {
			try {
				if (newValue == null) {
					//it's safe because that value is not used at all
					removeDBDivide(day.getId(), newValue.getId());
				} else {
					addDBDivide(day.getId(), newValue.getId());
				}
			} catch (DAOException e) {
				//TODO handle exception
				e.printStackTrace();
			}
		});
		day.getMatches().addListener(this.getDefaultListListener(
				day, 
				(d,m) -> {
					try {
						addDBMatch(m);
						return null;
					} catch (DAOException e) {
						return e;
					}
				},
				(d,m) -> {
					try {
						removeDBMatch(m);
						return null;
					} catch (DAOException e) {
						return e;
					}
				}));
	}

	@Override
	public Day add(Day day) throws DAOException {
		return this.abstractAdd(
				day, 
				"day", 
				(c,s,ps) -> {
					try {
						ps.getInsertDay().setInt(1, day.getNumber().get());
						ps.getInsertDay().setString(2, Utils.getStandardDateFrom(day.getDate().get()));
						ps.getInsertDay().addBatch();

						ps.getInsertDay().executeBatch();
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(c,s,ps) -> {
					this.setupDayListeners(day);
					return null;
				},
				() -> {
					this.days.put(day.getId(), day);
				});
	}

	@Override
	public Day update(Day day) throws DAOException {
		return this.abstractUpdate(
				day, 
				(c,s,ps) -> {
					try {
						ps.getUpdateDay().setInt(1,day.getNumber().get());
						ps.getUpdateDay().setString(2, Utils.getStandardDateFrom(day.getDate().get()));
						ps.getUpdateDay().setLong(3, day.getId());
						ps.getUpdateDay().executeUpdate();
						return null;
					} catch (SQLException e) {
						return e;
					}
				});
	}

	@Override
	public Collection<Day> getAllDaysThat(Function<Day, Boolean> filter) throws DAOException {
		return this.abstractGetObjectThat(
				filter, 
				(c,s,ps) -> ps.getGetAllDays(),
				() -> this.days,
				Day::new,
				(d,rs) -> {
					try {
						d.setId(rs.getLong("id"));
						d.getNumber().set(rs.getInt("number"));
						d.getDate().set(Utils.getDateFrom(rs.getString("date")));
						return null;
					} catch (SQLException e) {
						return e;
					}
				}, 
				(d,id) -> {
					//we crete the day, now we have to create inside the memory an instance of Tournament and its relationship
					try {
						this.setupDayListeners(d);
						this.computeDivideTournamentFromDay(d);
						this.computeMatchesFromDay(d);
						return null;
					} catch (DAOException ex) {
						return ex;
					}

				});
	}

	@Override
	public void remove(Day day) throws DAOException {
		this.abstractRemove(
				day, 
				(c,s,ps) -> {
					try {
						ps.getDeleteDay().setLong(1, day.getId());
						ps.getDeleteDay().executeUpdate();
						return null;
					}catch (SQLException e) {
						return e;
					}
				}, 
				() -> {
					//before we remove a day, we need to remove every match performed in that day adn the relationship with the tournament
					day.removeAllMatches();
					day.remove(day.getTournament().get());
					return null;
				},
				() -> {
					this.days.remove(day);
					return null;
				}
				);
	}

	@Override
	public ObservableList<Day> getDaysList() throws DAOException {
		return this.days.observableValueList();
	}

	/**
	 * Computes the list of days in relationship "divide" with the given tournament
	 * 
	 * the function will fetch the data of the days in relationship with the given tournament and then
	 * it will add them in the {@link Tournament#getDays()} list. Since such list is observed by the {@link DAO} itself,
	 * the {@link DAO} will be able to update the db properly
	 * 
	 * @param tournament the tournament involved
	 * @throws DAOException if something bad happens
	 */
	private void computeDivideFromTournament(Tournament tournament) throws DAOException {
		List<Long> ids = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetDaysInTournament().setLong(1, tournament.getId());
				ResultSet rs = ps.getGetDaysInTournament().executeQuery();
				while (rs.next()) {
					ids.add(rs.getLong("id"));
				}
				return null;
			} catch (SQLException e) {
				return e;
			}
		});

		for (Long dayId : ids) {
			this.getAllDaysThat(d -> d.getId() == dayId)
			.stream()
			.findFirst()
			.ifPresent(d -> {
				d.getTournament().set(tournament);
				tournament.getDays().add(d);
			});

		}
	}

	/**
	 * Computes the {@link Tournament} of the specific day in relationship "divide"
	 * 
	 * the function will fetch the data of the tournaments in relationship with the given day and then
	 * it will add them in the {@link Day#getTournament()}. Since such element is observed by the {@link DAO} itself,
	 * the {@link DAO} will be able to update the db properly
	 * 
	 * @param day the day involved
	 * @throws DAOException if something bad happens
	 */
	private void computeDivideTournamentFromDay(Day day) throws DAOException {
		List<Long> tournamentIds = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetTournamentFromDay().setLong(1, day.getId());
				ResultSet rs = ps.getGetTournamentFromDay().executeQuery();
				while (rs.next()) {
					tournamentIds.add(rs.getLong("tournament_id"));
				}
				return null;
			}catch (SQLException e) {
				return e;
			}
		});
		if (tournamentIds.size() != 1) {
			throw new DAOException(String.format("got multiple tournaments referenced by day %d", day.getId()));
		}
		long tournamentId = tournamentIds.get(0);

		this.getAllTournamentsThat(tournament -> tournament.getId() == tournamentId).stream().findFirst().ifPresent(tournament -> {
			//we need to set the tournament before the other because the second insturction relies on the first one
			day.getTournament().set(tournament);
			tournament.getDays().add(day);
		});
	}

	/**
	 * Add the relationship between day and tournament inside the db
	 * 
	 * The function deals only with <b>database</b> entry, not with models one!
	 * 
	 * @param dayId the the day that belong to the tournament
	 * @param tournamentId the tournament that will aggregates the day referred
	 * @throws DAOException if something bad happens
	 */
	private void addDBDivide(long dayId, long tournamentId) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getInsertDivision().setLong(1, tournamentId);
				ps.getInsertDivision().setLong(2, dayId);
				ps.getInsertDivision().executeUpdate();
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}

	/**
	 * remove the relationship between day and tournament inside the db
	 * 
	 * The function deals only with <b>database</b> entry, not with models one!
	 * 
	 * @param dayId the the day that belong to the tournament
	 * @param tournamentId the tournament that will aggregates the day referred
	 * @throws DAOException if something bad happens
	 */
	private void removeDBDivide(long dayId, long tournamentId) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getDeleteDivision().setLong(1, dayId);
				ps.getDeleteDivision().executeUpdate();
				return null;
			} catch (SQLException e) {
				return e;
			}
		});
	}

	/**
	 * After generating a team inside {@link #teams}, we need to create the model instances
	 * of {@link Match}. This function does exactly that
	 * 
	 * @param team the team just created
	 * @throws DAOException if something bad happens 
	 */
	private void computeMatchesFromTeam(Team team) throws DAOException {
		List<Long> team1_ids = new ArrayList<>();
		List<Long> team2_ids = new ArrayList<>();
		List<Long> day_ids = new ArrayList<>();
		List<Integer> pointsEarnedFromWinnings = new ArrayList<>();
		List<Integer> pointsEarnedFromLosings = new ArrayList<>();
		List<Integer> team1Goals = new ArrayList<>();
		List<Integer> team2Goals = new ArrayList<>();
		List<MatchStatus> statuses = new ArrayList<>();

		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetMatchesFromTeam().setLong(1, team.getId());
				ps.getGetMatchesFromTeam().setLong(2, team.getId());
				ResultSet rs = ps.getGetMatchesFromTeam().executeQuery();
				while (rs.next()) {
					team1_ids.add(rs.getLong("team1_id"));
					team2_ids.add(rs.getLong("team2_id"));
					day_ids.add(rs.getLong("day_id"));
					pointsEarnedFromWinnings.add(rs.getInt("points_earned_from_winning"));
					pointsEarnedFromLosings.add(rs.getInt("points_earned_from_losing"));
					team1Goals.add(rs.getInt("team1_goals"));
					team2Goals.add(rs.getInt("team2_goals"));
					statuses.add(MatchStatus.from(rs.getInt("status")));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		
		this.addMatchesInEntities(team1_ids, team2_ids, day_ids, pointsEarnedFromWinnings, pointsEarnedFromLosings, team1Goals, team2Goals, statuses);
	}
	
	/**
	 * After generating a day inside {@link #days}, we need to create the model instances
	 * of {@link Match}. This function does exactly that
	 * 
	 * @param team the team just created
	 * @throws DAOException if something bad happens 
	 */
	private void computeMatchesFromDay(Day day) throws DAOException {
		List<Long> team1_ids = new ArrayList<>();
		List<Long> team2_ids = new ArrayList<>();
		List<Long> day_ids = new ArrayList<>();
		List<Integer> pointsEarnedFromWinnings = new ArrayList<>();
		List<Integer> pointsEarnedFromLosings = new ArrayList<>();
		List<Integer> team1Goals = new ArrayList<>();
		List<Integer> team2Goals = new ArrayList<>();
		List<MatchStatus> statuses = new ArrayList<>();

		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetMatchesFromDay().setLong(1, day.getId());
				ResultSet rs = ps.getGetMatchesFromDay().executeQuery();
				while (rs.next()) {
					team1_ids.add(rs.getLong("team1_id"));
					team2_ids.add(rs.getLong("team2_id"));
					day_ids.add(rs.getLong("day_id"));
					pointsEarnedFromWinnings.add(rs.getInt("points_earned_from_winning"));
					pointsEarnedFromLosings.add(rs.getInt("points_earned_from_losing"));
					team1Goals.add(rs.getInt("team1_goals"));
					team2Goals.add(rs.getInt("team2_goals"));
					statuses.add(MatchStatus.from(rs.getInt("status")));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		
		this.addMatchesInEntities(team1_ids, team2_ids, day_ids, pointsEarnedFromWinnings, pointsEarnedFromLosings, team1Goals, team2Goals, statuses);
	}

	/**
	 * Given lists containing all the matches data we need to create, create such {@link Match} and adds them in the references model
	 * If such entities instances are not available yet in the memory, the function will create them
	 * 
	 * <b>All the lists must have the same length</b>
	 * 
	 * @param team1_ids 
	 * @param team2_ids
	 * @param day_ids
	 * @param pointsEarnedFromWinnings
	 * @param pointsEarnedFromLosings
	 * @param team1Goals
	 * @param team2Goals
	 * @param statuses
	 * @throws DAOException if something bad has happened
	 */
	private void addMatchesInEntities(List<Long> team1_ids,
			List<Long> team2_ids,
			List<Long> day_ids,
			List<Integer> pointsEarnedFromWinnings,
			List<Integer> pointsEarnedFromLosings,
			List<Integer> team1Goals,
			List<Integer> team2Goals,
			List<MatchStatus> statuses) throws DAOException {
		for (int j=0; j<team1_ids.size(); j++) {
			final int i = j;
			Optional<Team> oteam1 = this.getTeamThat(t -> t.getId() == team1_ids.get(i));
			Optional<Team> oteam2 = this.getTeamThat(t -> t.getId() == team2_ids.get(i));
			Optional<Day> oday = this.getDayThat(d -> d.getId() == day_ids.get(i));

			oteam1.ifPresent(team1 -> {
				oteam2.ifPresent(team2 -> {
					oday.ifPresent(day -> {
						Match m = new Match(team1, team2, day, pointsEarnedFromWinnings.get(i), pointsEarnedFromLosings.get(i), team1Goals.get(i), team2Goals.get(i), statuses.get(i));
						//we don't care if the match is already inside the list, because they are ObservableDistinctList
						team1.getMatches().add(m);
						team2.getMatches().add(m);
						day.getMatches().add(m);
					});
				});
			});
		}
	}

	/**
	 * insert in the database the given match
	 * 
	 * 
	 * 
	 * @param match the match to add
	 * @throws DAOException if something bad happens
	 */
	private void addDBMatch(final Match match) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				LOG.debug("team1Id: {} team2Id: {} dayId: {}", match.getTeam1().get().getId(), match.getTeam2().get().getId(), match.getDay().get().getId());
				ps.getInsertMatch().setLong(1, match.getTeam1().get().getId());
				ps.getInsertMatch().setLong(2, match.getTeam2().get().getId());
				ps.getInsertMatch().setLong(3, match.getDay().get().getId());
				ps.getInsertMatch().setLong(4, match.getPointsEarnedByWinning().get());
				ps.getInsertMatch().setLong(5, match.getPointsEarnedByLosing().get());
				ps.getInsertMatch().setLong(6, match.getTeam1Goals().get());
				ps.getInsertMatch().setLong(7, match.getTeam2Goals().get());
				ps.getInsertMatch().setLong(8, match.getStatus().get().getId());
				ps.getInsertMatch().executeUpdate();
				LOG.debug("DONE");
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}

	private void removeDBMatch(Match match) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				LOG.debug("delete match {}...", match);
				ps.getDeleteMatch().setLong(1, match.getTeam1().get().getId());
				ps.getDeleteMatch().setLong(2, match.getTeam2().get().getId());
				ps.getDeleteMatch().setLong(3, match.getDay().get().getId());
				ps.getDeleteMatch().executeUpdate();
				LOG.debug("DONE");
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}

}
