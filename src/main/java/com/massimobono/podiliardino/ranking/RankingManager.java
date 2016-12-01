package com.massimobono.podiliardino.ranking;

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
public interface RankingManager {

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
	public List<Team> getDayRanking(Day d);
	
	public ObservableList<Team> getDayObservableRanking(Day d);
}
