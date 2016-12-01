package com.massimobono.podiliardino.extensibles.matcher;

import java.util.Collection;
import java.util.List;

import com.massimobono.podiliardino.extensibles.ranking.RankingComputer;

import javafx.util.Pair;

/**
 * A {@link PairComputer} represents the class whose job is to couple different objects in pairs
 * 
 * It should be used after computing the ranking via {@link RankingComputer} to pair the team one against another
 * to create matches. Of course this procedure is totally different from the {@link RankingComputer}: whilst {@link RankingComputer}
 * aims to rank different teams, this class job is to pair them; one may pair the strongest together, or be less incentive and pair the strongest
 * with the weakest. Or maybe pair randomly.
 * 
 * @author massi
 *
 * @param <E> the class to pair
 */
@FunctionalInterface
public interface PairComputer<E> {

	/**
	 * 
	 * @param toPair the collection to pair
	 * @return the pairs computed. Every item inside <tt>toPair</tt> is present in exactly one pair, but nothing is said about whether it is in {@link Pair#getKey()}
	 * or in {@link Pair#getValue()}. If the size of <tt>toPair</tt> is odd, the last pair will have the {@link Pair#getValue()} set to "null"
	 */
	public List<Pair<E,E>> computePairs(Collection<E> toPair);
}
