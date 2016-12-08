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

/**
 * Allows you to convert a ranking into a txt file
 * 
 * @author massi
 *
 */
public class CSVRankingFormatter implements Formatter<List<Team>, File> {

	private static final String DELIMITER = "\t";
	private static final String HEADER = String.join(DELIMITER, "RANK", "TEAM", "POINTS", "GOALS DIFFERENCE", "GOALS SCORED", "OPPONENTS POINTS", "OPPONENTS GOALS");
	
	private File csvFile;
	private Day day;
	
	public CSVRankingFormatter(String txtFile, Day day) throws IOException{
		this.csvFile = new File(txtFile);
		if (!this.csvFile.exists()) {
			this.csvFile.createNewFile();
		}
		this.day = day;
	}
	
	@Override
	public File format(List<Team> toFormat) throws FormatException {
		Team team = null;
		
		try (PrintWriter pw = new PrintWriter(this.csvFile)) {
			pw.println(HEADER);
			for (int i=0 ; i<toFormat.size(); i++) {
				team = toFormat.get(i);
				pw.println(String.join(DELIMITER,
						String.format("%3d", i+1),
						team.nameProperty().get(),
						String.format("%3d", team.getPointsScoredIn(this.day.tournamentProperty().get())),
						String.format("%3d", team.getNumberOfGoalsScored(this.day.tournamentProperty().get()) - team.getNumberOfGoalsReceived(this.day.tournamentProperty().get())),
						String.format("%3d", team.getNumberOfGoalsScored(this.day.tournamentProperty().get())),
						String.format("%3d", team.getPointsYourOpponentsScored(this.day.tournamentProperty().get())),
						String.format("%3d", team.getNumberOfGoalsYourOpponentsScored(this.day.tournamentProperty().get()))
						));
			}
			pw.flush();
		} catch (FileNotFoundException e) {
			throw new FormatException(e);
		}
		return this.csvFile;
	}

}
