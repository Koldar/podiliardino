package com.massimobono.podiliardino.dao;

import java.io.Closeable;
import java.util.Collection;
import java.util.function.Function;

import com.massimobono.podiliardino.model.Player;

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
	
}
