package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.Validate;

public class AssignedRank<V> implements Rank<V> {

	private int minimalRank = 0;
	private final Set<V> vertices = new HashSet<V>();
	private final Map<V, Integer> vertexRanks = new HashMap<V, Integer>();
	private final MultiMap<Integer, V> ranks = new MultiHashMap<Integer, V>();

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
	public Integer getRank(V vertex) {
		final Integer rank = vertexRanks.get(vertex);
		return rank;
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
}
