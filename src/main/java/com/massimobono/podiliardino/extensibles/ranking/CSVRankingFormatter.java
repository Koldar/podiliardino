package com.massimobono.podiliardino.extensibles.ranking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Formattable;
import java.util.List;

import com.massimobono.podiliardino.extensibles.FormatException;
import com.massimobono.podiliardino.extensibles.Formatter;
import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.CSVHandler;

/**
 * Allows you to convert a ranking into a txt file
 * 
 * @author massi
 *
 */
public class CSVRankingFormatter implements Formatter<List<Team>, File> {

	private static final String[] HEADER = new String[] {"RANK", "TEAM", "POINTS", "GOALS DIFFERENCE", "GOALS SCORED", "OPPONENTS POINTS", "OPPONENTS GOALS"};
	
	private Day day;
	private String csvFilePath;
	
	public CSVRankingFormatter(String txtFile, Day day) {
		super();
		this.day = day;
		this.csvFilePath = txtFile;
	}
	
	@Override
	public File format(List<Team> toFormat) throws FormatException {
		Team team = null;
		
		try (CSVHandler csvHandler = new CSVHandler(this.csvFilePath, HEADER)) {
			csvHandler.addOption("sep", ",");
			csvHandler.setDelimiter(",");
			for (int i=0 ; i<toFormat.size(); i++) {
				team = toFormat.get(i);
				csvHandler.printRow(
						String.format("%3d", i+1),
						team.getName(),
						String.format("%3d", team.getPointsScoredIn(this.day.getTournament())),
						String.format("%3d", team.getNumberOfGoalsScored(this.day.getTournament()) - team.getNumberOfGoalsReceived(this.day.getTournament())),
						String.format("%3d", team.getNumberOfGoalsScored(this.day.getTournament())),
						String.format("%3d", team.getPointsYourOpponentsScored(this.day.getTournament())),
						String.format("%3d", team.getNumberOfGoalsYourOpponentsScored(this.day.getTournament()))
						);
			}
		} catch (IOException e) {
			throw new FormatException(e);
		}
		return new File(this.csvFilePath);
	}

}
