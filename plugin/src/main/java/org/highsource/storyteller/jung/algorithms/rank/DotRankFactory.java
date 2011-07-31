package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;
import org.highsource.storyteller.jung.traverse.PostOrderIterator;
import org.highsource.storyteller.jung.traverse.PreOrderIterator;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;

public class DotRankFactory<V, E> {

	private final DirectedGraph<V, E> graph;
	private final Transformer<E, Integer> minimumDistanceConstraint;

	public DotRankFactory(DirectedGraph<V, E> graph) {
		this(graph, ConstantMinimumLengthConstraint.<E> one());
	}

	public DotRankFactory(DirectedGraph<V, E> graph,
			Transformer<E, Integer> minimumLengthConstraint) {
		this.graph = graph;
		this.minimumDistanceConstraint = minimumLengthConstraint;
	}

	public Rank<V> getRank() {

		final Rank<V> rank = new BreadthFirstRanker<V, E>(
				minimumDistanceConstraint).createRank(graph);

		final TightTreeCreator<V, E> tightTreeCreator = new TightTreeCreator<V, E>(
				minimumDistanceConstraint);

		Tree<V, E> tree = tightTreeCreator.createTightTree(graph, rank);

		for (E leaveEdge = findLeaveEdge(tree); leaveEdge != null; leaveEdge = findLeaveEdge(tree)) {

			V leaveEdgeDest = tree.getDest(leaveEdge);
			final Set<V> headComponentVertices = new HashSet<V>(
					tree.getVertices());
			final Set<V> tailComponentVertices = new HashSet<V>();

			for (Iterator<V> tailComponentVertexIterator = new PreOrderIterator<V>(
					tree, leaveEdgeDest); tailComponentVertexIterator.hasNext();) {
				final V tailComponentVertex = tailComponentVertexIterator
						.next();
				headComponentVertices.remove(tailComponentVertex);
				tailComponentVertices.add(tailComponentVertex);
			}

			// final UnderTree<V> underTree = new UnderTree<V>(tree);
			E minimalEnterEdge = null;
			int minimalEnterEdgeSlack = 0;
			int minimalEnterEdgeDelta = 0;

			for (E potentialEnterEdge : graph.getEdges()) {
				V w = graph.getSource(potentialEnterEdge);
				V x = graph.getDest(potentialEnterEdge);

				if (tree.containsVertex(w) && tree.containsVertex(x)
						&& getSlack(rank, potentialEnterEdge, w, x) > 0) {
					if (headComponentVertices.contains(w)
							&& tailComponentVertices.contains(x)) {
						E enterEdge = potentialEnterEdge;
						int enterEdgeSlack = getSlack(rank, enterEdge, w, x);
						if (minimalEnterEdge == null
								|| enterEdgeSlack < minimalEnterEdgeSlack) {
							minimalEnterEdge = enterEdge;
							minimalEnterEdgeSlack = enterEdgeSlack;
							minimalEnterEdgeDelta = enterEdgeSlack;
						}
					} else if (tailComponentVertices.contains(w)
							&& headComponentVertices.contains(x)) {
						E enterEdge = potentialEnterEdge;
						int enterEdgeSlack = getSlack(rank, enterEdge, w, x);
						if (minimalEnterEdge == null
								|| enterEdgeSlack < minimalEnterEdgeSlack) {
							minimalEnterEdge = enterEdge;
							minimalEnterEdgeSlack = enterEdgeSlack;
							minimalEnterEdgeDelta = -enterEdgeSlack;
						}
					}
				}
			}

			if (minimalEnterEdge != null) {
				for (V vertex : tailComponentVertices) {
					rank.setRank(vertex, rank.getRank(vertex)
							+ minimalEnterEdgeDelta);
				}

				tree = tightTreeCreator.createTightTree(this.graph, rank);
			}
		}
		rank.normalize();
		return rank;
	}

	private E findLeaveEdge(final Tree<V, E> tree) {
		Map<E, Integer> cutValues = new HashMap<E, Integer>();
		Stack<E> leaveEdges = new Stack<E>();

		for (Iterator<V> iterator = new PostOrderIterator<V>(tree); iterator
				.hasNext();) {
			final V vertex = iterator.next();
			final Collection<E> parentEdges = tree.getInEdges(vertex);
			if (parentEdges.isEmpty()) {
				break;
			}
			final E parentEdge = parentEdges.iterator().next();

			int sign = (graph.getSource(parentEdge) == vertex) ? 1 : -1;
			int cutValue = 0;

			for (E childEdge : tree.getChildEdges(vertex)) {
				final V childEdgeSource = graph.getSource(childEdge);
				cutValue = cutValue
						+ (vertex == childEdgeSource ? -sign : sign)
						* cutValues.get(childEdge);

			}
			for (E incidentEdge : graph.getIncidentEdges(vertex)) {
				V incidentEdgeSource = graph.getSource(incidentEdge);
				cutValue = cutValue
						+ (vertex == incidentEdgeSource ? sign : -sign)
						* getWeight(incidentEdge);

			}
			cutValues.put(parentEdge, cutValue);
			if (cutValue < 0) {
				leaveEdges.push(parentEdge);
			}
		}
		if (leaveEdges.isEmpty()) {
			return null;
		} else {
			return leaveEdges.pop();
		}
	}

	private int getSlack(final Rank<V> rank, E nonTreeEdge, final V source,
			final V dest) {
		final int slack = Math.abs(rank.getRank(source) - rank.getRank(dest))
				- minimumLength(nonTreeEdge);
		return slack;
	}

	private int getWeight(E edge) {
		return 1;
	}

	private int minimumLength(E incidentEdge) {
		return minimumDistanceConstraint.transform(incidentEdge);
	}

}
