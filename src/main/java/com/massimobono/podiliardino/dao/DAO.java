package com.massimobono.podiliardino.dao;

import java.util.Collection;
import java.util.function.Function;

import com.massimobono.podiliardino.model.Player;

public interface DAO {
	
	public void clearAll() throws DAOException;
	
	public void setup() throws DAOException;
	
	public void tearDown() throws DAOException;

	public void addPlayer(Player p) throws DAOException;
	
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
