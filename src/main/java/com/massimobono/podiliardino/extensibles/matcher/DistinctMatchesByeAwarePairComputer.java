package com.massimobono.podiliardino.extensibles.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.PodiliardinoException;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.Utils;

import javafx.util.Pair;

/**
 * Represents a object that matches a list of team using the following rules:
 * 
 * <ul>
 * 	<li>matches ranked similar are matched together (the first against the second, the third against the fourth and so in)</li>
 * 	<li>A team can't fight another team twice in the whole tournament</li>
 * 	<li>A team can't exploits the "bye" more than once in the whole tournament</li>
 * 	<li>A unpaired team can use the "bye"</li>
 * </ul>
 * @author massi
 *
 */
public class DistinctMatchesByeAwarePairComputer<T extends Team> implements PairComputer<T>{
	
	private static final Logger LOG = LogManager.getLogger(DistinctMatchesByeAwarePairComputer.class);

	@Override
	public List<Pair<T, T>> computePairs(Day d, Collection<T> toPair) throws PodiliardinoException{
		
		T team1, team2;
		List<T> teams = new ArrayList<>(toPair);
		List<Pair<T,T>> retVal = new ArrayList<>();
		Team teamToBye = null;
		//if every team has "maximumNumberOfMatchesAllowed" against eachother, we assume no one has a match against noone
		int maximumNumberOfMatchesAllowed = 0;
		//if inside the main for loop a team has found its opponent
		boolean pairFound = false;
		
		//first we need to determinate whether or not a bye is necessary and, whether is necessary, we need to detemrine which team will bye
		//if every team has a bye we act like none really has (in this way the tournament can keep going more than the minimum day length)
		if (d.getTournament().get().hasOddPartecipants()) {
			//ok, there are a odd number of partecipants. We need to determine which team will have no pair
			teamToBye = this.computeTeamToBye(teams, d.getTournament().get());
			LOG.info("In this pairs {} will bye", teamToBye);
		}
		
		LOG.debug("Computing pairs...");
		for (int i=0; i<teams.size(); i++) {
			team1 = teams.get(i);
			maximumNumberOfMatchesAllowed = 0;
			pairFound = false;
			
			if (team1 == teamToBye) {
				//we found the team to bye. we add the handicapped pair
				retVal.add(new Pair<>(team1, null));
				pairFound = true;
			}
			//we need to check if the team is already paired with someone else
			if (this.hasAlreadyASetupMatch(team1, retVal)) {
				continue;
			}
			LOG.info("Analyzing {}...", team1);
			
			while (!pairFound) {
				//we first goes directly to "team2 next to team1": every team before the "team1" 
				//is ranked higher than "team1" itself, hence we shouldn't create the match at all
				for (int j=(i+1); j<teams.size(); j++) {
					team2 = teams.get(j);
					if (team2 == teamToBye) {
						//the team to bye is not involved in any computation
						continue;
					}
					//we need to check if the team is already paired with someone else
					if (this.hasAlreadyASetupMatch(team2, retVal)) {
						continue;
					}
					//we check if team1 and team2 has already fought against
					if (d.getTournament().get().hasAMatchAgainst(team1, team2, maximumNumberOfMatchesAllowed)){
						continue;
					}
					//ok, we have found the first team2 that "team1" has never fought against. We pair them
					retVal.add(new Pair<>(team1, team2));
					LOG.info("{} paired with lower-ranked {}", team1, team2);
					pairFound = true;
					break;
				}
				
				if (pairFound) {
					break;
				}
				//ok, this team has fought all the teams below itself (according to a specific ranking). We look at teams higher in ranking
				for (int j=(i-1); j>=0; j--) {
					team2 = teams.get(j);
					if (team2 == teamToBye) {
						//the team to bye is not involved in any computation
						continue;
					}
					//we need to check if the team is already paired with someone else
					if (this.hasAlreadyASetupMatch(team2, retVal)) {
						continue;
					}
					//we check if team1 and team2 has already fought against
					if (d.getTournament().get().hasAMatchAgainst(team1, team2, maximumNumberOfMatchesAllowed)){
						continue;
					}
					//ok, we have found the first team2 that "team1" has never fought against. We pair them
					retVal.add(new Pair<>(team1, team2));
					LOG.info("{} paired with higher-ranked {}", team1, team2);
					pairFound = true;
					break;
				}
				
				if (pairFound) {
					break;
				}
				
				//ok, it seems this team has fought against everyone. It appears we need to increase maximumNumberOfMatchesAllowed
				maximumNumberOfMatchesAllowed++;
			}
			
		}
		if (retVal.size()*2 != (teams.size() + (teamToBye == null ? 0 : 1))) {
			//we check that all the teams have a pair. the unpaired team will have fake additional team, hence the 1
			throw new PodiliardinoException(String.format("pairs: %d; teams: %d, byed team: %s", retVal.size(), teams.size(), teamToBye));
			
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param t the team involved
	 * @param buildingPairs the pairs to check
	 * @return True if inside the <tt>buildingPairs</tt> there is a pair involving the team <t>t</t>, flase otherwise
	 */
	private boolean hasAlreadyASetupMatch(T t, Collection<Pair<T,T>> buildingPairs) {
		Optional<Pair<T,T>> teamPair = buildingPairs.parallelStream().filter(p -> ((p.getKey() == t) || (p.getValue() == t))).findFirst();
		return teamPair.isPresent();
	}
	
	/**
	 * Computes the team that will receive the bye
	 * 
	 * The team is computed as follow: we compute the team within <tt>ranking</tt> with minimum bye used, then
	 * we pick up one of those teams randomly.
	 * 
	 * Please note the following:
	 * <ol>
	 * 	<li>every team is treated equally. No {@link Utils#DUMMYTEAM} specific code is present.</li>
	 * </ol>
	 * 
	 * @param ranking a list of teams where the first one is the highest in ranking whilst the last one is ranked the lowest
	 * @param tournament the tournament involved
	 * @return the team which will receive the bye
	 */
	private T computeTeamToBye(List<T> ranking, Tournament tournament) {
		T teamAnalyzed = null;
		int minBye = Integer.MAX_VALUE;
		int bye = Integer.MAX_VALUE;
		List<T> teamsWithMinimumBye = new ArrayList<>();
		Random rnd = new Random(ranking.size() * System.currentTimeMillis());
		//we generate a random sequence that depends on the clock AND on the ranking involved 
		
		//compute the list of teams with minimum bye
		for (int i=(ranking.size()-1); i>=0; i--){
			teamAnalyzed = ranking.get(i);
			bye = teamAnalyzed.checkByeNumber(tournament, false);
			if (minBye > bye) {
				minBye = bye;
				teamsWithMinimumBye.clear();
			}
			if (minBye == bye) {
				teamsWithMinimumBye.add(teamAnalyzed);
			}
		}
		//pick a team inside the just computed collection randomly
		return teamsWithMinimumBye.get(rnd.nextInt(teamsWithMinimumBye.size()));
	}

}
