package com.massimobono.podiliardino.extensibles.matches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Team;

import javafx.util.Pair;

/**
 * A {@link PairComputer} which pair item based on their position inside the collection
 * 
 * For example, if the collection is <tt>1,4,7,6,9,4</tt> the pairs will be:
 * <ol>
 * 	<li>1,4</li>
 * 	<li>7,6</li>
 * 	<li>9,4</li>
 * </ol>
 * 
 * If the collection is <tt>1,4,7,6,9,4,5</tt> instead:
 * <ol>
 * 	<li>1,4</li>
 * 	<li>7,6</li>
 * 	<li>9,4</li>
 * 	<li>5,null</li>
 * </ol>
 * 
 * 
 * 
 * @author massi
 *
 * @param <E>
 */
public class SubsequentPairComputer implements PairComputer<Team>{

	@Override
	public List<Pair<Team,Team>> computePairs(Day d, Collection<Team> toPair) {
		List<Pair<Team,Team>> retVal = new ArrayList<>();
		Iterator<Team> iterator = toPair.iterator();
		Team first, second;
		
		while (iterator.hasNext()) {
			first = iterator.next();
			second = iterator.hasNext() ? iterator.next() : null;
			retVal.add(new Pair<>(first, second));
		}
		return retVal;
	}

}
