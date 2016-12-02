package com.massimobono.podiliardino.extensibles.dummymatch;

import java.util.function.BiConsumer;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Match;
import com.massimobono.podiliardino.model.Team;

/**
 * Represents a class whose job is define what happen to the team with no opponents.
 * 
 * This can easily happen when the contestants are odd: for example in the list <tt>3,5,7,4,2</tt> what will
 * happen to the last element, the "2" within the tournament? this function will determine such thing.
 * 
 * @author massi
 *
 */
@FunctionalInterface
public interface DummyMatchHandler {
	
	public void handleUnPairedTeam(Day d, Team team);
}
