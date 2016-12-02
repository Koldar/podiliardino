package com.massimobono.podiliardino.extensibles.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
public class SubsequentPairComputer<E> implements PairComputer<E>{

	@Override
	public List<Pair<E, Optional<E>>> computePairs(Collection<E> toPair) {
		List<Pair<E,Optional<E>>> retVal = new ArrayList<>();
		Iterator<E> iterator = toPair.iterator();
		E first;
		Optional<E> second;
		
		while (iterator.hasNext()) {
			first = iterator.next();
			second = Optional.of(iterator.hasNext() ? iterator.next() : null);
			retVal.add(new Pair<>(first, second));
		}
		return retVal;
	}

}
