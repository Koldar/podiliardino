package com.massimobono.podiliardino.extensibles.dao;

import java.io.Closeable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Partecipation;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;

import javafx.collections.ObservableList;

/**
 * 
 * {@link DAO} is {@link Closeable}: that means you can safely use the try with resource construct, if you want:
 * 
 * <pre>{@code
 * try (DAO d = new SQLiteDAOImpl(new File("data.db3",true))) {
 * 	d.addPlayer(p1);
 *  d.addPlayer(p2);
 *  d.addPlayer(p3);
 * }</pre>
 * 
 * 
 * <h1>DAO Organization</h1>
 * 
 * The implementation should be aware of the following contracts:
 * <ul>
 * 	<li>The model is compose of 2 entities and relationships: entities are concepts big enough to stand by themselves. These are {@link Player},
 * 		{@link Team}, {@link Tournament} and {@link Day}; relationships are concepts the links the former ones: for example {@link Partecipation} are one of those.</li>
 * 	<li>The user does <b>not</b> create by themselves entities in order to make them persistence: he calls the function available from the {@link DAO}. For example if a user wants to
 * 		store a {@link Player} he will call the {@link DAO#add(Player)} function that will allow him to persist the instance.</li>
 * 	<li>In order to persist an entity instance from the model , you need to call the <tt>add</tt></li>
 * 	<li>If you have just started up the application and you want to populate your model in memory, you need to call the <tt>getAll</tt> APIs: for example if
 * 		have jsut started the application and you want to fetch all the players, you need to call {@link DAO#getAllPlayers()}</li>
 * 	<li>In order to synchronize the UI of Java FX the {@link DAO} offers to you also APIs returning {@link ObservableList}: such functions are <tt>getXXXList</tt>;
 * 		the said functions are bounded to return the same list in order that the whole application has a place where all the needed data is stored. Such list
 * 		deals only with entities, not with relationships</li>
 * 	<li>Viceversa, relationships can't be created as entities: you can use the model functions to create such relationships. So, if you want to add a {@link Day}
 * 		to a tournament, you don't query the {@link DAO}, but you query the model itself: you can use {@link Tournament#addDay(Day)} to do so. Implicitly, the {@link DAO}
 * 		will receive notification each time a model relationship is add or removed and it will synchronize the database accordingly.</li>
 * 
 * </ul>
 * 
 * @author massi
 *
 */
public interface DAO extends Closeable{
	
	public void clearAll() throws DAOException;
	
	/**
	 * Execute every instruction needed to setup the dao.
	 * 
	 * Always call this instruction before using the dao
	 * @throws DAOException
	 */
	public void setup() throws DAOException;
	
	public void tearDown() throws DAOException;

	/**
	 * Adds a new {@link Player} inside the DAO
	 * 
	 * The DAO will return you (if successful) a new {@link Player} instance. You can use that
	 * instance to remain synchronized with the DAO
	 * 
	 * @param p the player you want to add
	 * @return the same {@link Player} instane you've provided but with the ID used to synchornize it in the db
	 * @throws DAOException if somethign bad goes wrong
	 */
	public Player add(Player p) throws DAOException;
	
	/**
	 * Updates the DAO with the changes you've made on a {@link Player} instance you've changed 
	 * @param player the instance to synchronize
	 * @return the same {@link Player} instane you've provided but with the ID used to synchornize it in the db
	 * @throws DAOException if something bad happens
	 */
	public Player updatePlayer(Player player) throws DAOException;
	
	/**
	 * Removes a player within the DAO
	 * 
	 * The function will <b>automatically remove</b> all the relationships of the player as well.
	 * Hence the function will automatically remove all the teams where the player was in, with all the tournaments, the days and the matches connected
	 * This because logics heavily uses number of partecipants to take care of data
	 * 
	 * @param p
	 * @throws DAOException
	 */
	public void remove(Player p) throws DAOException;
	
	/**
	 * like {@link #getAllPlayers()} but it returns an {@link ObservableList}
	 * that can be used by JavaFX to synchronize its UI.
	 * 
	 * <b>The implementation promises to you that only one instance of {@link ObservableList} is created</b>.
	 * You can explit this method when you want to keep track of the players available in the database. If an entry is added or removed
	 * you an automatically be notified
	 * 
	 * @return
	 * @throws DAOException if something bad happens
	 */
	public ObservableList<Player> getPlayerList() throws DAOException;
	
	public Collection<Player> getAllPlayersThat(Function<Player,Boolean> filter) throws DAOException;
	
	public default Collection<Player> getAllPlayers() throws DAOException {
		return this.getAllPlayersThat(p -> true);
	}
	
	public default Optional<Player> getPlayerThat(Function<Player, Boolean> filter) throws DAOException {
		return this.getAllPlayersThat(filter).stream().findFirst();
	}
	
	
	/**
	 * Updates the DAO with the changes you've made on a {@link Team} instance you've changed 
	 * @param team the instance to synchronize
	 * @return the same {@link Team} instance you've provided but with the ID used to synchornize it in the db
	 * @throws DAOException if something bad happens
	 */
	public Team addTeam(Team team) throws DAOException;
	
	public Team update(Team team) throws DAOException;
	
	public Collection<Team> getAllTeamsThat(Function<Team, Boolean> filter) throws DAOException;
	
	/**
	 * Removes a team within the DAO
	 * 
	 * The function will <b>automatically remove</b> all the relationships of the team as well.
	 * Hence the function will automatically remove all the matches where the team took part and all the tournament it partecipate in.
	 * This because logics heavily uses number of partecipants to take care of data
	 * 
	 * @param tournament
	 * @throws DAOException
	 */
	public void remove(Team team) throws DAOException;
	
	/**
	 * 
	 * @return all the team inside the database
	 * @throws DAOException if something bad happens
	 */
	public default Collection<Team> getAllTeams() throws DAOException {
		return this.getAllTeamsThat(p -> true);
	}
	
	/**
	 * like {@link #getTeamList()} but it returns an {@link ObservableList}
	 * that can be used by JavaFX to synchronize its UI
	 * 
	 * <b>The implementation promises to you that only one instance of {@link ObservableList} is created
	 * 
	 * @return
	 * @throws DAOException if something bad happens
	 */
	public ObservableList<Team> getTeamList() throws DAOException;
	
	/**
	 * like {@link #getAllTeamsThat(Function)} but returns only the first entry
	 * 
	 * @param filter
	 * @return
	 * @throws DAOException
	 */
	public default Optional<Team> getTeamThat(Function<Team, Boolean> filter) throws DAOException {
		return this.getAllTeamsThat(filter).stream().findFirst();
	}
	
	// TOURNAMENTS
	
	public Tournament add(Tournament tournament) throws DAOException;
	
	public Tournament update(Tournament tournament) throws DAOException;
	
	/**
	 * Removes a tournament within the DAO
	 * 
	 * The function will <b>automatically remove</b> all the relationships of the tournament as well.
	 * Hence the function wil lautomatically remove dependencies with Day and all {@link Partecipation} linked
	 * 
	 * @param tournament
	 * @throws DAOException
	 */
	public void remove(Tournament tournament) throws DAOException;
	
	public ObservableList<Tournament> getTournamentList() throws DAOException;
	
	public Collection<Tournament> getAllTournamentsThat(Function<Tournament, Boolean> filter) throws DAOException;
	
	public default Collection<Tournament> getAllTournaments() throws DAOException {
		return this.getAllTournamentsThat(t -> true);
	}
	
	/**
	 * like {@link #getAllTournamentsThat(Function)} but return only the first entry
	 * 
	 * @param filter
	 * @return
	 * @throws DAOException
	 */
	public default Optional<Tournament> getTournamentThat(Function<Tournament, Boolean> filter) throws DAOException {
		return this.getAllTournamentsThat(filter).stream().findFirst();
	}
	
	// DAYS
	
	public Day add(Day day) throws DAOException;
	
	public Day update(Day day) throws DAOException;
	
	public Collection<Day> getAllDaysThat(Function<Day, Boolean> filter) throws DAOException;
	
	public void remove(Day day) throws DAOException;
	
	public ObservableList<Day> getDaysList() throws DAOException;
	
	/**
	 * like {@link #getAllDaysThat(Function)} but return only the first entry
	 * 
	 * @param filter
	 * @return
	 * @throws DAOException
	 */
	public default Optional<Day> getDayThat(Function<Day, Boolean> filter) throws DAOException {
		return this.getAllDaysThat(filter).stream().findFirst();
	}
	
}
