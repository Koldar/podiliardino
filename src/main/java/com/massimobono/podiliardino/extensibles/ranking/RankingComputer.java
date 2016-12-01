package com.massimobono.podiliardino.extensibles.ranking;

import java.util.List;
import java.util.PriorityQueue;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Team;

import javafx.collections.ObservableList;

/**
 * It's the class whose job is to elaborate the ranking in a unspecified day of an unspecified tournament
 * @author massi
 *
 */
public interface RankingComputer<E> {

	public void setup();
	
	/**
	 * Computes the ranking of an unspecified day
	 * 
	 * The implementation should note that, if 2 team can't be sorted out, the sorting should be <b>random</b>:
	 * in this way the software can call this method at day0 to randomly sort the teams
	 * 
	 * @param d the day when we want to compute the ranking
	 * @return the ranking
	 */
	public List<E> getDayRanking(Day d);
	
	/**
	 * like {@link #getDayRanking(Day)}, but it will return an instance you can observe
	 * 
	 * <b>the implementation needs to return always the same instance in order to send events to listeners correctly</b>
	 * 
	 * @param d
	 * @return
	 */
	public ObservableList<E> getDayObservableRanking(Day d);
}
