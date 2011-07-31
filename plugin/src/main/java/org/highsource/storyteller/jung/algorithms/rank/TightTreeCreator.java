package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

public class TightTreeCreator<V, E> {

	private final Transformer<E, Integer> minimumDistanceConstraint;

	public TightTreeCreator(Transformer<E, Integer> minimumDistanceConstraint) {
		Validate.notNull(minimumDistanceConstraint);
		this.minimumDistanceConstraint = minimumDistanceConstraint;
	}

	public Tree<V, E> createTightTree(final DirectedGraph<V, E> graph,
			final Rank<V> rank) {
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

		final Set<E> tightTreeEdges = new HashSet<E>();
		final Set<E> nonTreeEdges = new HashSet<E>();
		final Set<E> nonTightTreeEdges = new HashSet<E>();

		final int graphVertexCount = graph.getVertexCount();
		while (tree.getVertexCount() < graphVertexCount) {
			while (!queue.isEmpty()) {
				final V currentVertex = queue.remove(0);
				for (E incidentEdge : graph.getIncidentEdges(currentVertex)) {

					if (tightTreeEdges.contains(incidentEdge)
							|| nonTightTreeEdges.contains(incidentEdge)) {
						// skip edge
					} else {

						final V incidentVertex = graph.getOpposite(
								currentVertex, incidentEdge);
						final int slack = getSlack(rank, incidentEdge,
								incidentVertex, currentVertex);

						if (tree.containsVertex(incidentVertex)) {
							nonTreeEdges.remove(incidentEdge);
							if (slack == 0) {
								// Tight edge
								// tree.addVertex(incidentVertex);
								tightTreeEdges.add(incidentEdge);
							} else {
								nonTightTreeEdges.add(incidentEdge);
							}
						} else {
							if (slack == 0) {
								// Tight edge
								// tree.addVertex(incidentVertex);
								tree.addEdge(incidentEdge, currentVertex,
										incidentVertex);
								nonTreeEdges.remove(incidentEdge);
								tightTreeEdges.add(incidentEdge);
								// treeGraphVertices.add(incidentVertex);
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
				final int minimalSlack = getSlack(rank, minimalSlackEdge,
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
				tightTreeEdges.add(minimalSlackEdge);
				nonTreeEdges.remove(minimalSlackEdge);
				queue.add(minimalSlackVertex);
			}
		}

		return tree;
	}

	private E findMinimalSlackEdge(final DirectedGraph<V, E> graph,
			final Rank<V> rank, final Collection<E> nonTreeEdges) {
		E minimalSlackEdge = null;
		int minimalSlack = 0;
		for (E nonTreeEdge : nonTreeEdges) {
			final V source = graph.getSource(nonTreeEdge);
			final V dest = graph.getDest(nonTreeEdge);
			final int slack = getSlack(rank, nonTreeEdge, source, dest);
			Validate.isTrue(slack > 0);
			if (minimalSlackEdge == null || slack < minimalSlack) {
				minimalSlackEdge = nonTreeEdge;
				minimalSlack = slack;
			}
		}
		return minimalSlackEdge;
	}

	private int getSlack(final Rank<V> rank, E nonTreeEdge, final V source,
			final V dest) {
		Integer sourceRank = rank.getRank(source);
		Integer destRank = rank.getRank(dest);
		Validate.notNull(sourceRank, "Source vertex must have been ranked.");
		Validate.notNull(destRank, "Dest vertex must have been ranked.");
		final int slack = Math.abs(sourceRank.intValue() - destRank.intValue())
				- getMinimumDistance(nonTreeEdge);
		return slack;
	}

	private int getMinimumDistance(E edge) {
		Validate.notNull(edge);
		final Integer distance = this.minimumDistanceConstraint.transform(edge);
		Validate.notNull(distance);
		return distance.intValue();
	}

}
