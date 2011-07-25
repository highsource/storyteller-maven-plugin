package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;

public interface Rank<V> {

	public Integer getRank(V vertex);

	public Collection<V> getVerticies(int rank);

}
