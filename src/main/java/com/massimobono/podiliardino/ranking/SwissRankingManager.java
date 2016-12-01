package com.massimobono.podiliardino.ranking;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;

/**
 * Rank every team based upon the swiss tournament type rules
 * 
 * 
 * The rules used to rank a team better than the other are the following ones (from the higher priority to the lowest one):
 * <ol>
 * 	<li>Team1 is ranked higher than team2 if the points scored by team1 are more;</li>
 * 	<li>If the points scored by team1 and team2 are the same, then the tie breaking is solved by looking at the number of goals: whoever has score more goals is ranked higher;</li>
 * 	<li>If there is still a tie, it is broken by looking at the subtraction between goals scored and goals received. Whoever has such higher difference is ranked higher;</li>
 * 	<li>If there is still a tie, it is broken by looking at the number of total goals all your opponents have scored in the tournament. The team whose opponents have score higher is ranked higher.</li>
 * </ol>
 * 
 * @author massi
 *
 */
public class SwissRankingManager implements RankingManager {

	/**
	 * the ranking you 
	 */
	private PriorityQueue<Team> ranking;
	private Random random;
	
	private Day currentDay;
	
	public SwissRankingManager() {
		this.random = new Random(System.nanoTime());
		this.ranking = new PriorityQueue<>(new Comparator<Team>() {

			@Override
			public int compare(Team o1, Team o2) {
				int score1 = o1.getPointsScoredIn(currentDay.getTournament().get());
				int score2 = o2.getPointsScoredIn(currentDay.getTournament().get());
				if (score1 != score2) {
					return score1 - score2;
				}
				int goals1 = o1.getNumberOfGoalsScored(currentDay.getTournament().get());
				int goals2 = o2.getNumberOfGoalsScored(currentDay.getTournament().get());
				if (goals1 != goals2) {
					return goals1 - goals2;
				}
				int goalsReceived1 = o1.getNumberOfGoalsReceived(currentDay.getTournament().get());
				int goalsReceived2 = o2.getNumberOfGoalsReceived(currentDay.getTournament().get());
				if (goalsReceived1 != goalsReceived2) {
					return goalsReceived1 - goalsReceived2;
				}
				int opponentsGoals1 = o1.getNumberOfGoalsYourOpponentsScored(currentDay.getTournament().get());
				int opponentsGoals2 = o2.getNumberOfGoalsYourOpponentsScored(currentDay.getTournament().get());
				if (opponentsGoals1 != opponentsGoals2) {
					return opponentsGoals1 - opponentsGoals1;
				}
				
				//ok, the 2 teams are equal. This happens when we're trying to create a ranking at the beginning.
				//in order to use this system even at the very beginning of the tournament we choose randomly between the 2
				int r = 50;
				while (r == 50) {
					r = random.nextInt(100);
				}
				return 50 - r;
			}
		});
	}
	
	@Override
	public void setup() {
		this.ranking.clear();
		this.currentDay = null;
	}

	@Override
	public List<Team> getDayRanking(Day d) {
		List<Team> retVal = new ArrayList<>();
		this.currentDay = d;
		
		//we use a priority queue to automatically sort the teams
		this.ranking.addAll(d.getTournament().get().getPartecipatingTeams());
		retVal.addAll(this.ranking);
		return retVal;
	}

}
