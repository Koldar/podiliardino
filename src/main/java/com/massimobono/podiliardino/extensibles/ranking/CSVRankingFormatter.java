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
import com.massimobono.podiliardino.util.I18N;

/**
 * Allows you to convert a ranking into a txt file
 * 
 * @author massi
 *
 */
public class CSVRankingFormatter implements Formatter<List<Team>, File> {
	
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
		
		String[] header = new String[] {
				I18N.get().getString("rank"),
				I18N.get().getString("team"),
				I18N.get().getString("points"),
				I18N.get().getString("goals_difference"),
				I18N.get().getString("goals_scored"),
				I18N.get().getString("opponents_points"),
				I18N.get().getString("opponents_goals")
		};
		
		try (CSVHandler csvHandler = new CSVHandler(this.csvFilePath, header)) {
			csvHandler.addOption("sep", CSVHandler.DEFAULT_DELIMITER);
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
