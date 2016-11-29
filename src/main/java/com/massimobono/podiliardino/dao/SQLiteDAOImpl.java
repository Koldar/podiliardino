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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.massimobono.podiliardino.model.Indexable;
import com.massimobono.podiliardino.model.Partecipation;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.TableFriendlyObservableMap;
import com.massimobono.podiliardino.util.TerConsumer;
import com.massimobono.podiliardino.util.TerFunction;
import com.massimobono.podiliardino.util.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.util.Pair;

public class SQLiteDAOImpl implements DAO {

	private static final int QUERY_TIMEOUT = 30;

	private File databaseFileName;

	private class PreparedStatements implements Closeable {

		private Connection connection;
		private Map<Connection, PreparedStatements> map;		
		private Map<String, PreparedStatement> preparedStatements;

		public PreparedStatements(Connection connection, Map<Connection, PreparedStatements> map) throws SQLException {
			this.map = map;
			this.preparedStatements = new HashMap<>();
			this.connection = connection;

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
			this.preparedStatements.put("removePlayerComposeTeam",connection.prepareStatement("DELETE FROM player_compose_team WHERE team_id=?"));

			this.preparedStatements.put("insertTournament",connection.prepareStatement("INSERT INTO tournament(name,start_date,end_date) VALUES(?,?,?);"));
			this.preparedStatements.put("getAllTournaments",connection.prepareStatement("SELECT id,name,start_date,end_date FROM tournament"));
			this.preparedStatements.put("updateTournament",connection.prepareStatement("UPDATE OR ROLLBACK tournament SET name=?, start_date=?, end_date=?"));
			this.preparedStatements.put("deleteTournament",connection.prepareStatement("DELETE FROM tournament WHERE id=?"));

			this.preparedStatements.put("insertOrIgnorePartecipation", connection.prepareStatement("INSERT OR IGNORE INTO partecipation(team_id, tournament_id, bye) VALUES (?,?,?)"));
			this.preparedStatements.put("getAllPartecipations", connection.prepareStatement("SELECT p.team_id, p.tournament_id, p.bye FROM partecipation AS p"));
			this.preparedStatements.put("updatePartecipation", connection.prepareStatement("UPDATE OR ROLLBACK partecipation SET bye=? WHERE team_id=? AND tournament_id=?"));
			this.preparedStatements.put("deletePartecipation", connection.prepareStatement("DELETE FROM partecipation WHERE team_id=? and tournament_id=?"));

			this.preparedStatements.put("getPartecipationsOfTeam",connection.prepareStatement("SELECT p.team_id, p.tournament_id, p.bye FROM partecipation AS p WHERE p.team_id=?"));
			this.preparedStatements.put("getPartecipationsInTournament",connection.prepareStatement("SELECT p.team_id, p.tournament_id, p.bye FROM partecipation AS p WHERE p.tournament_id=?")); 

			this.preparedStatements.put("lastInsertedRow",connection.prepareStatement("SELECT seq as last_inserted_id FROM sqlite_sequence WHERE name=?;"));
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

		public PreparedStatement getDeleteOrIgnorePartecipation() {
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

		@Override
		public void close() throws IOException {
			try {
				for (PreparedStatement p : this.preparedStatements.values()) {
					p.close();
				}
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

	private ObservableSet<Partecipation> participations;
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
		this.participations = FXCollections.observableSet(new HashSet<>());
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

				s.executeUpdate("CREATE TABLE IF NOT EXISTS player_compose_team (player_id INTEGER REFERENCES player(id) ON UPDATE CASCADE, team_id INTEGER REFERENCES team(id) ON UPDATE CASCADE, UNIQUE(player_id, team_id));");

				s.executeUpdate("CREATE TABLE IF NOT EXISTS tournament (id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(100), start_date varchar(20), end_date varchar(20));");

				s.executeUpdate("CREATE TABLE IF NOT EXISTS partecipation (team_id INTEGER REFERENCES team(id) ON UPDATE CASCADE UNIQUE, tournament_id INTEGER REFERENCES tournament(id) ON UPDATE CASCADE UNIQUE, bye integer);"); 
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
			if (this.preparedStatements.size() > 0) {
				throw new RuntimeException("multiple connections!");
			}
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
	 * @param actionWhenDeleteSucceed a series of instruction to perform if the deletion succeeds
	 * @throws DAOException if something bad happens. <tt>actionWhenDeleteSucceed</tt> won't be called at all in this scenario
	 */
	private <TOREMOVE> void abstractRemove(final TOREMOVE toRemove, TerFunction<Connection, Statement, PreparedStatements, Exception> deleteQueries, Runnable actionWhenDeleteSucceed) throws DAOException {
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
		actionWhenDeleteSucceed.run();
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
						ps.getInsertPlayer().setString(1, p.getName().get());
						ps.getInsertPlayer().setString(2, p.getSurname().get());
						ps.getInsertPlayer().setString(3, birthday.isPresent() ? birthday.get() : Utils.EMPTY_DATE);
						ps.getInsertPlayer().setString(4, p.getPhone().get().isPresent() ? p.getPhone().get().get() : Utils.EMPTY_PHONE);
						ps.getInsertPlayer().addBatch();
						c.setAutoCommit(false);
						ps.getInsertPlayer().executeBatch();
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
				() -> {this.players.remove(p.getId());}
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
						p.getTeamWithPlayer().addAll(this.getTeamsWith(id));
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
					try {
						ps.getAddPlayerComposeTeam().setLong(1, team.getPlayers().get(0).getId());
						ps.getAddPlayerComposeTeam().setLong(2, team.getId());
						ps.getAddPlayerComposeTeam().addBatch();
						ps.getAddPlayerComposeTeam().setLong(1, team.getPlayers().get(1).getId());
						ps.getAddPlayerComposeTeam().setLong(2, team.getId());
						ps.getAddPlayerComposeTeam().addBatch();
						ps.getAddPlayerComposeTeam().executeBatch();

						team.getPartecipations().addListener((ListChangeListener.Change<? extends Partecipation> e) -> {
							while(e.next()) {
								try {
									for (Partecipation p :e.getRemoved()) {
										this.remove(p);
									}
									for (Partecipation p : e.getAddedSubList()) {
										this.add(p);
									}
								} catch (DAOException ex) {
									ex.printStackTrace();
									//TODO complete
								}
							}
						});

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
		return this.abstractUpdate(
				team,
				(c,s,ps) -> {
					try {
						ps.getUpdateTeam().setString(1, team.getName().get());
						ps.getUpdateTeam().setString(2, Utils.getStandardDateFrom(team.getDate().get()));
						ps.getUpdateTeam().setLong(3, team.getId());
						ps.getUpdateTeam().addBatch();

						ps.getRemovePlayerComposeTeam().setLong(1, team.getId());
						ps.getRemovePlayerComposeTeam().addBatch();

						ps.getAddPlayerComposeTeam().setLong(1, team.getPlayers().get(0).getId());
						ps.getAddPlayerComposeTeam().setLong(2, team.getId());
						ps.getAddPlayerComposeTeam().addBatch();
						ps.getAddPlayerComposeTeam().setLong(1, team.getPlayers().get(1).getId());
						ps.getAddPlayerComposeTeam().setLong(2, team.getId());
						ps.getAddPlayerComposeTeam().addBatch();

						c.setAutoCommit(false);
						ps.getUpdateTeam().executeBatch();
						ps.getRemovePlayerComposeTeam().executeBatch();
						ps.getAddPlayerComposeTeam().executeBatch();
						c.setAutoCommit(true);
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
						t.getPartecipations().addListener((ListChangeListener.Change<? extends Partecipation> e) -> {
							while (e.next()) {
								try {
									for (Partecipation p : e.getAddedSubList()) {
										this.add(p);
									}
									for (Partecipation p : e.getRemoved()) {
										this.remove(p);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									//TODO complete
								}
							}
						});
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				(t, id) -> {
					try {
						this.computePartecipationsOfTeam(t.getId());
						t.getPlayers().addAll(this.getPlayersInTeam(id));
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
						return null;
					} catch (SQLException e) {
						return e;
					}
				},
				() -> {this.teams.remove(team.getId());}
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

	private Collection<Team> getTeamsWith(long player_id) throws DAOException {
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
				(c,s,ps) -> null,
				() -> {
					tournament.getPartecipations().addListener((ListChangeListener.Change<? extends Partecipation> e) -> {
						while (e.next()) {
							try {
								for (Partecipation p :e.getRemoved()) {
									this.remove(p);
								}
								for (Partecipation p : e.getAddedSubList()) {
									this.add(p);
								}
							}catch (DAOException ex) {
								ex.printStackTrace();
								//TODO complete
							}
						}
					});
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
						ps.getDeleteTournament().setLong(1, tournament.getId());
						ps.getDeleteTournament().executeUpdate();
						return null;
					}catch (SQLException e) {
						return e;
					}
				},
				() -> {this.tournaments.remove(tournament.getId());});
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

						t.getPartecipations().addListener((ListChangeListener.Change<? extends Partecipation> e) -> {
							try {
								while (e.next()) {
									for (Partecipation p : e.getAddedSubList()) {
										this.add(p);
									}
									for (Partecipation p : e.getRemoved()) {
										this.remove(p);
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
								//TODO complete
							}
						});
						return null;
					} catch (Exception e) {
						return e;
					}
				}, 
				(t,rs) -> {
					try {
						this.computePartecipationsInTournament(t.getId());
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
	private void computePartecipationsInTournament(long tournament_id) throws DAOException {
		Collection<Pair<Long, Boolean>> ids = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetPartecipationsInTournament().setLong(1, tournament_id);
				ResultSet rs = ps.getGetPartecipationsInTournament().executeQuery();
				while (rs.next()) {
					ids.add(new Pair<Long,Boolean>(rs.getLong("team_id"), rs.getInt("bye") > 0));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});

		for (Pair<Long, Boolean> pair : ids) {
			Optional<Tournament> tournament = this.getAllTournamentsThat(t -> t.getId() == tournament_id).stream().findFirst();
			Optional<Team> team = this.getAllTeamsThat(p -> p.getId() == pair.getKey()).stream().findFirst();
			if (tournament.isPresent() && team.isPresent()) {
				Partecipation p = new Partecipation(pair.getValue(), tournament.get(), team.get());
				if (!p.getTournament().get().getPartecipations().contains(p)) {
					p.getTournament().get().getPartecipations().add(p);
				}
				if (!p.getTeam().get().getPartecipations().contains(p)) {
					p.getTeam().get().getPartecipations().add(p);
				}
			}
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
	private void computePartecipationsOfTeam(long team_id) throws DAOException {
		Collection<Pair<Long, Boolean>> ids = new ArrayList<>();
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getGetPartecipationsOfTeam().setLong(1, team_id);
				ResultSet rs = ps.getGetPartecipationsOfTeam().executeQuery();
				while (rs.next()) {
					ids.add(new Pair<Long, Boolean>(rs.getLong("tournament_id"), rs.getInt("bye") > 0));
				}
				return null;
			} catch (Exception e) {
				return e;
			}
		});

		for (Pair<Long, Boolean> pair : ids) {
			Optional<Team> team = this.getAllTeamsThat(p -> p.getId() == team_id).stream().findFirst();
			Optional<Tournament> tournament = this.getAllTournamentsThat(t -> t.getId() == pair.getKey()).stream().findFirst();
			if (tournament.isPresent() && team.isPresent()) {
				Partecipation p = new Partecipation(pair.getValue(), tournament.get(), team.get());
				if (!p.getTournament().get().getPartecipations().contains(p)) {
					p.getTournament().get().getPartecipations().add(p);
				}
				if (!p.getTeam().get().getPartecipations().contains(p)) {
					p.getTeam().get().getPartecipations().add(p);
				}
			}
		}
	}


	private Partecipation add(Partecipation partecipation) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getInsertOrIgnorePartecipation().setLong(1, partecipation.getTeam().get().getId());
				ps.getInsertOrIgnorePartecipation().setLong(2, partecipation.getTournament().get().getId());
				ps.getInsertOrIgnorePartecipation().setInt(3, partecipation.getBye().get() ? 1 : 0);
				ps.getInsertOrIgnorePartecipation().executeUpdate();
				return null;
			} catch (Exception e) {
				return e;
			}
		});
		return partecipation;
	}


	private void remove(Partecipation partecipation) throws DAOException {
		this.connectAndThenDo((c,s,ps) -> {
			try {
				ps.getDeleteOrIgnorePartecipation().setLong(1, partecipation.getTeam().get().getId());
				ps.getDeleteOrIgnorePartecipation().setLong(2, partecipation.getTournament().get().getId());
				ps.getDeleteOrIgnorePartecipation().executeUpdate();
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}

}
