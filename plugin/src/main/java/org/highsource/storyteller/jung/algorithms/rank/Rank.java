package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;

public interface Rank<V> {

	public Integer getRank(V vertex);

	public void setRank(V vertex, int rank);

	public void shift(Collection<V> vertices, int delta);

	public Collection<V> getVerticies(int rank);

	public void normalize();

}
