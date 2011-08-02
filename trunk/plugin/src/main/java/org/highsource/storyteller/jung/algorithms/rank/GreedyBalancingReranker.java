package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.DirectedGraph;

public class GreedyBalancingReranker<V, E> implements Reranker<V, E> {

	@Override
	public Rank<V, E> rerank(final Rank<V, E> rank) {

		Validate.notNull(rank);

		final DirectedGraph<V, E> graph = rank.getGraph();

		for (final V vertex : graph.getVertices()) {
			if (graph.getSuccessorCount(vertex) == graph
					.getPredecessorCount(vertex)) {

				int vertexRank = rank.getRank(vertex);
				int vertexRankSize = rank.getVerticies(vertexRank).size();
				int minInSlack = getMinimumSlack(rank, graph.getInEdges(vertex));
				int minOutSlack = getMinimumSlack(rank,
						graph.getOutEdges(vertex));

				int targetRank = vertexRank;
				int targetRankSize = vertexRankSize;
				int minInRank = vertexRank + minInSlack;
				int maxOutRank = vertexRank - minOutSlack;
				for (int currentRank = maxOutRank; currentRank <= minInRank; currentRank++) {
					if (currentRank == vertexRank) {
						continue;
					}
					Collection<V> verticies = rank.getVerticies(currentRank);
					int currentRankSize = verticies.size();
					if (currentRankSize < (targetRankSize - 1)) {
						targetRank = currentRank;
						targetRankSize = currentRankSize;
					}
				}

				if (targetRank != vertexRank) {
					rank.shift(Collections.singleton(vertex), vertexRank
							- targetRank);
				}
			}
		}
		return rank;
	}

	private int getMinimumSlack(final Rank<V, E> rank, Collection<E> ins) {
		int minInSlack = Integer.MAX_VALUE;
		for (E edge : ins) {
			int slack = rank.getSlack(edge);
			if (minInSlack > slack) {
				minInSlack = slack;
			}
		}
		Validate.isTrue(minInSlack != Integer.MAX_VALUE);
		return minInSlack;
	}
}
