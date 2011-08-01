package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

public class TightTreeCreator<V, E> {

	public Tree<V, E> createTightTree(final DirectedGraph<V, E> graph,
			final Rank<V, E> rank) {
		Validate.notNull(graph);
		Validate.notNull(rank);

		// Create a new tree
		final Tree<V, E> tree = new DelegateTree<V, E>(
				new DirectedSparseGraph<V, E>());

		// Queue of vertices to process
		final List<V> queue = new LinkedList<V>();

		final V firstVertex = graph.getVertices().iterator().next();
		queue.add(firstVertex);
		tree.addVertex(firstVertex);

		final Set<E> nonTreeEdges = new HashSet<E>();
		final Set<E> nonTightTreeEdges = new HashSet<E>();

		final int graphVertexCount = graph.getVertexCount();
		while (tree.getVertexCount() < graphVertexCount) {
			while (!queue.isEmpty()) {
				final V currentVertex = queue.remove(0);
				for (E incidentEdge : graph.getIncidentEdges(currentVertex)) {

					if (tree.containsEdge(incidentEdge)
							|| nonTightTreeEdges.contains(incidentEdge)) {
						// skip edge
					} else {

						final V incidentVertex = graph.getOpposite(
								currentVertex, incidentEdge);
						final int slack = rank.getSlack(incidentEdge,
								incidentVertex, currentVertex);

						if (tree.containsVertex(incidentVertex)) {
							nonTreeEdges.remove(incidentEdge);
							if (slack == 0) {
								// Tight edge
							} else {
								nonTightTreeEdges.add(incidentEdge);
							}
						} else {
							if (slack == 0) {
								// Tight edge
								tree.addEdge(incidentEdge, currentVertex,
										incidentVertex);
								nonTreeEdges.remove(incidentEdge);
								queue.add(incidentVertex);
							} else {
								nonTreeEdges.add(incidentEdge);
							}
						}
					}
				}
			}

			final E minimalSlackEdge = findMinimalSlackEdge(graph, rank,
					nonTreeEdges);

			if (minimalSlackEdge != null) {
				final V source = graph.getSource(minimalSlackEdge);
				final V dest = graph.getDest(minimalSlackEdge);
				final int minimalSlack = rank.getSlack(minimalSlackEdge,
						source, dest);
				final int delta;
				final V minimalSlackVertex;
				final V treeMinimalSlackVertex;

				if (tree.containsVertex(source)) {
					delta = minimalSlack;
					minimalSlackVertex = dest;
					treeMinimalSlackVertex = source;

				} else {
					delta = -minimalSlack;
					minimalSlackVertex = source;
					treeMinimalSlackVertex = dest;

				}

				rank.shift(tree.getVertices(), delta);
				tree.addEdge(minimalSlackEdge, treeMinimalSlackVertex,
						minimalSlackVertex);
				nonTreeEdges.remove(minimalSlackEdge);
				queue.add(minimalSlackVertex);
			}
		}

		return tree;
	}

	private E findMinimalSlackEdge(final DirectedGraph<V, E> graph,
			final Rank<V, E> rank, final Collection<E> nonTreeEdges) {
		E minimalSlackEdge = null;
		int minimalSlack = 0;
		for (E nonTreeEdge : nonTreeEdges) {
			final V source = graph.getSource(nonTreeEdge);
			final V dest = graph.getDest(nonTreeEdge);
			final int slack = rank.getSlack(nonTreeEdge, source, dest);
			Validate.isTrue(slack > 0);
			if (minimalSlackEdge == null || slack < minimalSlack) {
				minimalSlackEdge = nonTreeEdge;
				minimalSlack = slack;
			}
		}
		return minimalSlackEdge;
	}

}
