package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;

import edu.uci.ics.jung.graph.DirectedGraph;

public interface Rank<V, E> {

	public DirectedGraph<V, E> getGraph();

	public int getRank(V vertex);

	public void setRank(V vertex, int rank);

	public void shift(Collection<V> vertices, int delta);

	public Collection<V> getVerticies(int rank);

	public void normalize();

	public int getSlack(E edge, final V source, final V dest);

	public int getSlack(E edge);
}
