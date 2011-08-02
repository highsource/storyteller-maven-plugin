package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.DirectedGraph;

public class AssignedRank<V, E> implements Rank<V, E> {

	public final static int ABSENT_RANK = Integer.MIN_VALUE;

	private final DirectedGraph<V, E> graph;
	private final Transformer<E, Integer> minimumDistanceConstraint;
	private int minimalRank = 0;
	private final Set<V> vertices = new HashSet<V>();
	private final Map<V, Integer> vertexRanks = new HashMap<V, Integer>();
	private final MultiMap<Integer, V> ranks = new MultiHashMap<Integer, V>();

	public AssignedRank(DirectedGraph<V, E> graph,
			Transformer<E, Integer> minimumDistanceConstraint) {
		Validate.notNull(graph);
		Validate.notNull(minimumDistanceConstraint);
		this.graph = graph;
		this.minimumDistanceConstraint = minimumDistanceConstraint;
	}

	@Override
	public DirectedGraph<V, E> getGraph() {
		return graph;
	}

	@Override
	public void setRank(V vertex, int rank) {
		this.vertices.add(vertex);
		final Integer currentRank = this.vertexRanks.get(vertex);
		if (currentRank != null) {
			this.ranks.remove(currentRank, vertex);
		}
		this.vertexRanks.put(vertex, rank);
		this.ranks.put(rank, vertex);
		if (rank < minimalRank) {
			this.minimalRank = rank;
		}
	}

	@Override
	public int getRank(V vertex) {
		final Integer rank = vertexRanks.get(vertex);
		if (rank == null) {
			return ABSENT_RANK;
		} else {
			return rank.intValue();
		}
	}

	@Override
	public Collection<V> getVerticies(int rank) {
		return ranks.get(rank);
	}

	@Override
	public void shift(Collection<V> vertices, int delta) {
		Validate.notNull(vertices);
		for (V vertex : vertices) {
			setRank(vertex, getRank(vertex) - delta);
		}

	}

	@Override
	public void normalize() {
		if (minimalRank != 0) {
			for (V vertex : vertices) {
				setRank(vertex, getRank(vertex) - minimalRank);
			}
			minimalRank = 0;
		}
	}

	@Override
	public int getSlack(E edge, final V source, final V dest) {
		Validate.notNull(edge);
		Validate.notNull(source);
		Validate.notNull(dest);
		Validate.notNull(minimumDistanceConstraint);
		Integer sourceRank = getRank(source);
		Integer destRank = getRank(dest);
		Validate.notNull(sourceRank, "Source vertex must have been ranked.");
		Validate.notNull(destRank, "Dest vertex must have been ranked.");
		final int slack = Math.abs(sourceRank.intValue() - destRank.intValue())
				- getMinimumDistance(edge);
		return slack;
	}

	@Override
	public int getSlack(E edge) {
		Validate.notNull(edge);
		Validate.isTrue(getGraph().containsEdge(edge));
		final V source = getGraph().getSource(edge);
		final V dest = getGraph().getDest(edge);
		final int sourceRank = getRank(source);
		final int destRank = getRank(dest);
		Validate.isTrue(sourceRank != ABSENT_RANK,
				"Source vertex must have been ranked.");
		Validate.isTrue(sourceRank != ABSENT_RANK,
				"Dest vertex must have been ranked.");
		final int slack = Math.abs(sourceRank - destRank)
				- getMinimumDistance(edge);
		return slack;
	}

	private int getMinimumDistance(E edge) {
		Validate.notNull(edge);
		final Integer distance = minimumDistanceConstraint.transform(edge);
		Validate.notNull(distance);
		return distance.intValue();
	}

}
