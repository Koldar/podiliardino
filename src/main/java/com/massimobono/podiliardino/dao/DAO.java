package com.massimobono.podiliardino.dao;

import java.io.Closeable;
import java.util.Collection;
import java.util.function.Function;

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
	public Player addPlayer(Player p) throws DAOException;
	
	/**
	 * Updates the DAO with the changes you've made on a {@link Player} instance you've changed 
	 * @param player the instance to synchronize
	 * @return the same {@link Player} instane you've provided but with the ID used to synchornize it in the db
	 * @throws DAOException if something bad happens
	 */
	public Player updatePlayer(Player player) throws DAOException;
	
	public void removePlayer(Player p) throws DAOException;
	
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
	
	public default Collection<Player> getPlayerby(String name, String surname) throws DAOException{
		return this.getAllPlayersThat(p -> {
			return ((name == null)||(p.getName().get().equalsIgnoreCase(name))) && ((surname==null)&&(p.getSurname().get().equalsIgnoreCase(surname)));
		});
	}
	
	/**
	 * 
	 * @return all the players inside the database
	 * @throws DAOException if something bad happens
	 */
	public default Collection<Player> getAllPlayers() throws DAOException {
		return this.getAllPlayersThat(p -> true);
	}
	
	public default Collection<Player> getPlayerByName(String name) throws DAOException {
		return this.getPlayerby(name, null);
	}
	
	public default Collection<Player> getPlayerBySurname(String surname) throws DAOException {
		return this.getPlayerby(null, surname);
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
	
	public void removeTeam(Team team) throws DAOException;
	
	/**
	 * 
	 * @return all the team inside the database
	 * @throws DAOException if something bad happens
	 */
	public default Collection<Team> getAllTeams() throws DAOException {
		return this.getAllTeamsThat(p -> true);
	}
	
	public default Collection<Team> getTeamByName(String name) throws DAOException {
		return this.getAllTeamsThat(t -> t.getName().get().equalsIgnoreCase(name));
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
	
	public Tournament add(Tournament tournament) throws DAOException;
	
	public Tournament update(Tournament tournament) throws DAOException;
	
	public void remove(Tournament tournament) throws DAOException;
	
	public ObservableList<Tournament> getTournamentList() throws DAOException;
	
	public Collection<Tournament> getTournamentsThat(Function<Tournament, Boolean> filter) throws DAOException;
	
	public default Collection<Tournament> getAllTournaments() throws DAOException {
		return this.getTournamentsThat(t -> true);
	}
}
