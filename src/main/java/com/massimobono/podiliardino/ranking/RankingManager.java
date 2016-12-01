package com.massimobono.podiliardino.ranking;

import java.util.List;
import java.util.PriorityQueue;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Team;

/**
 * It's the class whose job is to elaborate the ranking in a unspecified day of an unspecified tournament
 * @author massi
 *
 */
public interface RankingManager{

	public void setup();
	
	/**
	 * Computes the ranking of an unspecified day
	 * 
	 * @param d the day when we want to compute the ranking
	 * @return the ranking
	 */
	public List<Team> getDayRanking(Day d);
}
