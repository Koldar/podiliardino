package com.massimobono.podiliardino.extensibles.matches;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.extensibles.FormatException;
import com.massimobono.podiliardino.extensibles.Formatter;
import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Match;
import com.massimobono.podiliardino.model.MatchStatus;
import com.massimobono.podiliardino.util.CSVHandler;
import com.massimobono.podiliardino.util.I18N;

/**
 * A Formatter that save inside a CSV all the match to do in a day
 * 
 * Every other match that is not n {@link MatchStatus#TODO} will be ignored
 * The class is "simple" because the CSV will contain only the first and the second team, nothing more and nothing less
 * 
 * 
 * @author massi
 *
 */
public class SimpleCSVMatchesFormatter implements Formatter<Day, File> {
	
	private static final Logger LOG = LogManager.getLogger(SimpleCSVMatchesFormatter.class);
	
	private File csvFile;
	
	public SimpleCSVMatchesFormatter(String txtFile) throws IOException{
		this.csvFile = new File(txtFile);
	}
	
	@Override
	public File format(Day toFormat) throws FormatException{
		int i = 0;
		
		String[] header = new String[] {I18N.get("match_number"), I18N.get("team1"), I18N.get("team2")};
		
		try (CSVHandler csvHandler = new CSVHandler(this.csvFile.getAbsolutePath(), header)) {
			csvHandler.addOption("sep", CSVHandler.DEFAULT_DELIMITER);
			for (Match match : toFormat.matchesProperty()){
				if (match.getStatus().get() != MatchStatus.TODO) {
					continue;
				}
				i++;
				csvHandler.printRow(
						String.format("%3d", i),
						match.getTeam1().get().nameProperty().get(),
						match.getTeam2().get().nameProperty().get()
				);
			}
		} catch (IOException e) {
			throw new FormatException(e);
		}
		return this.csvFile;
	}
	
}
