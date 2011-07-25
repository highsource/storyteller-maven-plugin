package org.highsource.storyteller.jung.algorithms.rank;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

public class DotRankFactory<V, E> {

	private final DirectedGraph<V, E> graph;
	private final MinimumLengthConstraint<E> minimumLengthConstraint;

	public DotRankFactory(DirectedGraph<V, E> graph) {
		this(graph, ConstantMinimumLengthConstraint.<E> one());
	}

	public DotRankFactory(DirectedGraph<V, E> graph,
			MinimumLengthConstraint<E> minimumLengthConstraint) {
		this.graph = graph;
		this.minimumLengthConstraint = minimumLengthConstraint;
	}

	public Rank<V> getRank() {

		final AssignedRank<V> rank = initialRank();

		final DirectedGraph<V, E> treeGraph = new DirectedSparseGraph<V, E>();

		final int graphVertexCount = graph.getVertexCount();

		final Set<V> treeGraphVertices = new HashSet<V>();

		final List<V> queue = new LinkedList<V>();
		queue.add(graph.getVertices().iterator().next());

		final Set<E> tightTreeEdges = new HashSet<E>();
		final Set<E> nonTightTreeEdges = new HashSet<E>();
		final Set<E> nonTreeEdges = new HashSet<E>();

		while (treeGraphVertices.size() < graphVertexCount) {
			while (!queue.isEmpty()) {
				final V currentVertex = queue.remove(0);
				treeGraph.addVertex(currentVertex);
				treeGraphVertices.add(currentVertex);
				int currentRank = rank.getRank(currentVertex);
				for (E incidentEdge : graph.getIncidentEdges(currentVertex)) {

					if (tightTreeEdges.contains(incidentEdge)
							|| nonTightTreeEdges.contains(incidentEdge)) {
						// skip edge
					} else {
						final V incidentVertex = graph.getOpposite(
								currentVertex, incidentEdge);
						int incidentRank = rank.getRank(incidentVertex);
						int slack = Math.abs(incidentRank - currentRank)
								- minimumLength(incidentEdge);
						if (slack == 0) {
							// Tight edge
							treeGraph.addEdge(incidentEdge, currentVertex,
									incidentVertex);
							nonTreeEdges.remove(incidentEdge);
							tightTreeEdges.add(incidentEdge);
							treeGraphVertices.add(incidentVertex);
							queue.add(incidentVertex);
						} else {
							if (treeGraphVertices.contains(incidentVertex)) {
								nonTreeEdges.remove(incidentEdge);
								nonTightTreeEdges.add(incidentEdge);
							} else {
								nonTreeEdges.add(incidentEdge);
							}
						}
					}
				}
			}

			E minimalSlackEdge = null;
			int minimalSlack = 0;
			int delta = 0;
			V minimalSlackVertex = null;
			for (E nonTreeEdge : nonTreeEdges) {
				final V source = graph.getSource(nonTreeEdge);
				final V dest = graph.getDest(nonTreeEdge);
				if (treeGraphVertices.contains(source)) {
					final int slack = Math.abs(rank.getRank(source)
							- rank.getRank(dest))
							- minimumLength(nonTreeEdge);
					if (minimalSlackEdge == null || slack < minimalSlack) {
						minimalSlackEdge = nonTreeEdge;
						minimalSlack = slack;
						delta = minimalSlack;
						minimalSlackVertex = dest;
					}
				} else if (treeGraphVertices.contains(dest)) {
					final int slack = Math.abs(rank.getRank(source)
							- rank.getRank(dest))
							- minimumLength(nonTreeEdge);
					if (minimalSlackEdge == null || slack < minimalSlack) {
						minimalSlackEdge = nonTreeEdge;
						minimalSlack = slack;
						delta = -minimalSlack;
						minimalSlackVertex = source;
					}
				}
			}

			if (minimalSlack > 0) {
				for (V vertex : treeGraph.getVertices()) {
					rank.assignRank(vertex, rank.getRank(vertex) - delta);
				}

				queue.add(minimalSlackVertex);
			}

		}
		rank.normalize();

		return rank;
	}

	private int minimumLength(E incidentEdge) {
		return minimumLengthConstraint.getMinimumLength(incidentEdge);
	}

	private AssignedRank<V> initialRank() {
		final AssignedRank<V> rank = new AssignedRank<V>();
		List<V> queue = new LinkedList<V>();
		Set<V> visitedVerices = new HashSet<V>();

		for (V vertex : graph.getVertices()) {
			if (graph.getSuccessorCount(vertex) == 0) {
				queue.add(vertex);
				rank.assignRank(vertex, 0);
			}
		}

		while (!queue.isEmpty()) {
			final V currentVertex = queue.remove(0);
			if (!visitedVerices.contains(currentVertex)) {
				final int currentRank = rank.getRank(currentVertex);
				for (E predecessorEdge : graph.getInEdges(currentVertex)) {
					final V predecessor = graph.getSource(predecessorEdge);
					final int minimumPredecessorRank = currentRank
							+ minimumLength(predecessorEdge);
					final Integer existingPredecessorRank = rank
							.getRank(predecessor);
					if (existingPredecessorRank == null
							|| existingPredecessorRank < minimumPredecessorRank) {
						rank.assignRank(predecessor, minimumPredecessorRank);
					}
					queue.add(predecessor);
				}
			}
		}
		return rank;
	}

	private void assignInitialRanks(final AssignedRank<V> rank,
			Tree<V, E> tree, V currentVertex, int currentRank) {
		rank.assignRank(currentVertex, currentRank);
		int childRank = currentRank + 1;
		for (V childVertex : tree.getChildren(currentVertex)) {
			assignInitialRanks(rank, tree, childVertex, childRank);
		}
	}

}
