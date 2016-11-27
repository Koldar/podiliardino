package com.massimobono.podiliardino.dao;

import java.util.Collection;
import java.util.function.Function;

import com.massimobono.podiliardino.model.Player;

public interface DAO {
	
	public void clearAll() throws DAOException;
	
	public void setup() throws DAOException;
	
	public void tearDown() throws DAOException;

	/**
	 * Adds a new {@link Player} inside the DAO
	 * 
	 * The DAO will return you (if successful) a new {@link Player} instance. You can use that
	 * instance to remain synchronized with the DAO
	 * 
	 * @param p the player you want to add
	 * @return the instance added inside the db
	 * @throws DAOException if somethign bad goes wrong
	 */
	public Player addPlayer(Player p) throws DAOException;
	
	/**
	 * Updates the DAO with the changes you've made on a {@link Player} instance you've changed 
	 * @param player the instance to synchronize
	 * @return a new {@link Player} synchronized with theDAO
	 * @throws DAOException if something bad happens
	 */
	public Player updatePlayer(Player player) throws DAOException;
	
	public void removePlayer(Player p) throws DAOException;
	
	public Collection<Player> getAllPlayersThat(Function<Player,Boolean> filter) throws DAOException;
	
	public default Collection<Player> getPlayerby(String name, String surname) throws DAOException{
		return this.getAllPlayersThat(p -> {
			return ((name == null)||(p.getName().equalsIgnoreCase(name))) && ((surname==null)&&(p.getSurname().equalsIgnoreCase(surname)));
		});
	}
	
	public default Collection<Player> getPlayerByName(String name) throws DAOException {
		return this.getPlayerby(name, null);
	}
	
	public default Collection<Player> getPlayerBySurname(String surname) throws DAOException {
		return this.getPlayerby(null, surname);
	}
	
}
