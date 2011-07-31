package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;
import org.highsource.storyteller.jung.graph.utils.DirectedGraphUtils;

import edu.uci.ics.jung.graph.DirectedGraph;

public class BreadthFirstRanker<V, E> {

	// private final DirectedGraph<V, E> graph;
	private final Transformer<E, Integer> minimumDistanceConstraint;

	public BreadthFirstRanker(
	// DirectedGraph<V, E> graph,
			Transformer<E, Integer> minimumDistanceConstraint) {
		Validate.notNull(minimumDistanceConstraint);
		this.minimumDistanceConstraint = minimumDistanceConstraint;
	}

	private int getMinimumDistance(E edge) {
		Validate.notNull(edge);
		final Integer distance = this.minimumDistanceConstraint.transform(edge);
		Validate.notNull(distance);
		return distance.intValue();
	}

	public Rank<V> createRank(DirectedGraph<V, E> graph) {
		final Rank<V> rank = new AssignedRank<V>();
		final Collection<V> sinks = DirectedGraphUtils.findSinks(graph);
		Validate.isTrue(!sinks.isEmpty());

		// Set roots at the zero rank
		for (final V sink : sinks) {
			rank.setRank(sink, 0);
		}
		// Queue the roots
		final List<V> queue = new LinkedList<V>(sinks);
		final Set<V> visitedVertices = new HashSet<V>();

		while (!queue.isEmpty()) {
			// Get the current vertex
			final V currentVertex = queue.remove(0);

			// If it was not yet visited
			if (!visitedVertices.contains(currentVertex)) {
				final Integer currentRank = rank.getRank(currentVertex);
				// All queued vertices must have already been ranked
				assert currentRank != null;
				// Iterate over predecessors
				for (final E predecessorEdge : graph.getInEdges(currentVertex)) {
					final V predecessor = graph.getSource(predecessorEdge);
					// Calculate the minimum predecessor rank
					final int minimumPredecessorRank = currentRank
							+ getMinimumDistance(predecessorEdge);
					final Integer existingPredecessorRank = rank
							.getRank(predecessor);
					if (existingPredecessorRank == null
							|| existingPredecessorRank < minimumPredecessorRank) {
						rank.setRank(predecessor, minimumPredecessorRank);
					}
					// At this point the predecessor is guaranteed to be ranked
					queue.add(predecessor);
				}
			}
		}
		return rank;
	}

}
