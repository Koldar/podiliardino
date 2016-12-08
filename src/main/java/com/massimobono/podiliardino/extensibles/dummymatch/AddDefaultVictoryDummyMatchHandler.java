package com.massimobono.podiliardino.extensibles.dummymatch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import com.massimobono.podiliardino.extensibles.dao.DAO;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Match;
import com.massimobono.podiliardino.model.MatchStatus;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.util.Utils;

public class AddDefaultVictoryDummyMatchHandler implements DummyMatchHandler{

	
	private static final int GOALS_SCORED = 10;
	private static final int GOALS_RECEIVED = 9;
	
	@Override
	public void handleUnPairedTeam(Day day, Team unpairedTeam) {
		day.matchesProperty().add(new Match(
				unpairedTeam,
				Utils.DUMMYTEAM,
				day, 
				Utils.DEFAULT_POINTS_EARNED_FROM_WINNING,
				Utils.DEFAULT_POINTS_EARNED_FROM_LOSING, 
				GOALS_SCORED, 
				GOALS_RECEIVED,
				MatchStatus.DONE));
	}

}
