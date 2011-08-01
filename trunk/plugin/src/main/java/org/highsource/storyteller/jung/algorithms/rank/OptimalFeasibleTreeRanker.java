package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.highsource.storyteller.jung.traverse.PostOrderIterator;
import org.highsource.storyteller.jung.traverse.PreOrderIterator;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;

public class OptimalFeasibleTreeRanker<V, E> {

	private final Transformer<E, Integer> minimumDistance;
	private final Transformer<E, Integer> weight;

	public OptimalFeasibleTreeRanker() {
		this(ConstantTransformer.<E> one(), ConstantTransformer.<E> one());
	}

	public OptimalFeasibleTreeRanker(Transformer<E, Integer> minimumDistance,
			Transformer<E, Integer> weight) {
		this.minimumDistance = minimumDistance;
		this.weight = weight;
	}

	public Rank<V, E> createRank(DirectedGraph<V, E> graph) {

		final Rank<V, E> rank = new BreadthFirstRanker<V, E>(minimumDistance)
				.createRank(graph);

		final TightTreeCreator<V, E> tightTreeCreator = new TightTreeCreator<V, E>();

		Tree<V, E> tree = tightTreeCreator.createTightTree(graph, rank);

		for (E leaveEdge = findLeaveEdge(graph, tree); leaveEdge != null; leaveEdge = findLeaveEdge(
				graph, tree)) {

			final Set<V> tailComponentVertices = getTailComponentVertices(tree,
					leaveEdge);
			final E enterEdge = findEnterEdge(graph, rank, tree,
					tailComponentVertices);
			Validate.notNull(enterEdge);
			final V enterEdgeSource = graph.getSource(enterEdge);
			final V enterEdgeDest = graph.getDest(enterEdge);
			final int slack = rank.getSlack(enterEdge, enterEdgeSource,
					enterEdgeDest);
			final int delta = tailComponentVertices.contains(enterEdgeSource) ? slack
					: -slack;
			rank.shift(tailComponentVertices, delta);
			tree = tightTreeCreator.createTightTree(graph, rank);
		}
		rank.normalize();
		return rank;
	}

	private E findEnterEdge(final DirectedGraph<V, E> graph,
			final Rank<V, E> rank, Tree<V, E> tree,
			final Set<V> tailComponentVertices) {
		E minimalEnterEdge = null;
		int minimalEnterEdgeSlack = 0;

		for (E potentialEnterEdge : graph.getEdges()) {
			V w = graph.getSource(potentialEnterEdge);
			V x = graph.getDest(potentialEnterEdge);

			if (tree.containsVertex(w) && tree.containsVertex(x)
					&& rank.getSlack(potentialEnterEdge, w, x) > 0) {
				E enterEdge = potentialEnterEdge;
				int enterEdgeSlack = rank.getSlack(enterEdge, w, x);
				if (tailComponentVertices.contains(w)
						^ tailComponentVertices.contains(x)) {
					if (minimalEnterEdge == null
							|| enterEdgeSlack < minimalEnterEdgeSlack) {
						minimalEnterEdge = enterEdge;
						minimalEnterEdgeSlack = enterEdgeSlack;
					}
				}
			}
		}
		return minimalEnterEdge;
	}

	private Set<V> getTailComponentVertices(Tree<V, E> tree, E leaveEdge) {
		final Set<V> tailComponentVertices = new HashSet<V>();
		V leaveEdgeDest = tree.getDest(leaveEdge);

		for (Iterator<V> tailComponentVertexIterator = new PreOrderIterator<V>(
				tree, leaveEdgeDest); tailComponentVertexIterator.hasNext();) {
			final V tailComponentVertex = tailComponentVertexIterator.next();
			tailComponentVertices.add(tailComponentVertex);
		}
		return tailComponentVertices;
	}

	private E findLeaveEdge(final DirectedGraph<V, E> graph,
			final Tree<V, E> tree) {
		final Map<E, Integer> cutValues = calculateCutValues(graph, tree);

		for (Entry<E, Integer> entry : cutValues.entrySet()) {
			if (entry.getValue() < 0) {
				return entry.getKey();
			}

		}
		return null;
	}

	private Map<E, Integer> calculateCutValues(final DirectedGraph<V, E> graph,
			final Tree<V, E> tree) {
		final Map<E, Integer> cutValues = new HashMap<E, Integer>();

		for (Iterator<V> iterator = new PostOrderIterator<V>(tree); iterator
				.hasNext();) {
			final V vertex = iterator.next();

			final Collection<E> parentEdges = tree.getInEdges(vertex);
			if (parentEdges.isEmpty()) {
				break;
			}
			final E parentEdge = parentEdges.iterator().next();

			int sign = (ObjectUtils.equals(graph.getSource(parentEdge), vertex)) ? 1
					: -1;
			int cutValue = 0;

			for (E childEdge : tree.getChildEdges(vertex)) {
				final V childEdgeSource = graph.getSource(childEdge);
				cutValue = cutValue
						+ (ObjectUtils.equals(vertex, childEdgeSource) ? -sign
								: sign) * cutValues.get(childEdge);

			}
			for (E incidentEdge : graph.getIncidentEdges(vertex)) {
				V incidentEdgeSource = graph.getSource(incidentEdge);
				cutValue = cutValue
						+ (ObjectUtils.equals(vertex, incidentEdgeSource) ? sign
								: -sign) * getWeight(incidentEdge);

			}
			cutValues.put(parentEdge, cutValue);
		}
		return cutValues;
	}

	private int getWeight(E edge) {
		Validate.notNull(edge);
		Integer weight = this.weight.transform(edge);
		Validate.notNull(weight);
		return weight.intValue();
	}

}
