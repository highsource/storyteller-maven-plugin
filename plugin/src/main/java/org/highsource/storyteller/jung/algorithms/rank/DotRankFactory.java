package org.highsource.storyteller.jung.algorithms.rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.highsource.storyteller.jung.traverse.PostOrderIterator;
import org.highsource.storyteller.jung.traverse.PreOrderIterator;

import edu.uci.ics.jung.graph.DelegateTree;
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

		final AssignedRank<V> rank = createInitialRank();
		final Set<E> nonTightTreeEdges = new HashSet<E>();

		Tree<V, E> tree = createFeasibleTree(rank, nonTightTreeEdges);

		for (E leaveEdge = findLeaveEdge(tree); leaveEdge != null; leaveEdge = findLeaveEdge(tree)) {

			V leaveEdgeDest = tree.getDest(leaveEdge);
			V leaveEdgeSource = tree.getSource(leaveEdge);
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
			V minimalEnterEdgeSource = null;
			V minimalEnterEdgeDest = null;
			for (E potentialEnterEdge : nonTightTreeEdges) {
				V w = graph.getSource(potentialEnterEdge);
				V x = graph.getDest(potentialEnterEdge);
				if (headComponentVertices.contains(w)
						&& tailComponentVertices.contains(x)) {
					E enterEdge = potentialEnterEdge;
					int enterEdgeSlack = getSlack(rank, enterEdge, w, x);
					if (minimalEnterEdge == null
							|| enterEdgeSlack < minimalEnterEdgeSlack) {
						minimalEnterEdge = enterEdge;
						minimalEnterEdgeSlack = enterEdgeSlack;
						minimalEnterEdgeDelta = enterEdgeSlack;
						minimalEnterEdgeSource = w;
						minimalEnterEdgeDest = x;
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
						minimalEnterEdgeSource = w;
						minimalEnterEdgeDest = x;
					}
				}
			}

			if (minimalEnterEdge != null) {
				// final Tree<V, E> newTree = new DelegateTree<V, E>(
				// new DirectedSparseGraph<V, E>());
				// // newTree.addVertex(minimalEnterEdgeSource);
				// // newTree.addEdge(minimalEnterEdge, minimalEnterEdgeSource,
				// // minimalEnterEdgeDest);
				//
				// V current = null;
				// for (final Iterator<V> iterator = new
				// PreOrderIterator<V>(tree,
				// leaveEdgeDest); iterator.hasNext();) {
				// final V next = iterator.next();
				//
				// if (current == null) {
				// newTree.addVertex(next);
				// } else {
				// newTree.addEdge(tree.getParentEdge(next), current, next);
				// }
				// current = next;
				// }
				tree.removeEdge(leaveEdge);
				// tree.removeVertex(leaveEdgeDest);
				//
				// final V t;
				// if (tree.containsVertex(minimalEnterEdgeDest)) {
				// newTree.addEdge(minimalEnterEdge, minimalEnterEdgeSource,
				// minimalEnterEdgeDest);
				// t = minimalEnterEdgeDest;
				// } else {
				// newTree.addEdge(minimalEnterEdge, minimalEnterEdgeSource,
				// minimalEnterEdgeDest);
				// t = minimalEnterEdgeSource;
				// }
				//
				// TreeTransformer.rotateTree(tree, t, newTree);
				for (V vertex : tailComponentVertices) {
					rank.assignRank(vertex, rank.getRank(vertex)
							+ minimalEnterEdgeDelta);
				}

				nonTightTreeEdges.clear();
				tree = createFeasibleTree(rank, nonTightTreeEdges);
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
			// Validate.isTrue(tree.getSource(parentEdge) == vertex
			// || tree.getDest(parentEdge) == vertex);
			// int realCutValue = getCutValue(tree, parentEdge);
			// Validate.isTrue(tree.getSource(parentEdge) == vertex
			// || tree.getDest(parentEdge) == vertex);
			// if (cutValue != realCutValue) {
			// getCutValue(tree, parentEdge);
			// }
			if (cutValue < 0) {
				leaveEdges.push(parentEdge);
			}
		}
		if (leaveEdges.isEmpty()) {
			return null;
		} else {
			final E leaveEdge = leaveEdges.pop();
			// int cutValue = getCutValue(tree, leaveEdge);
			// Integer oldCutValue = cutValues.get(leaveEdge);
			// if (cutValue != oldCutValue) {
			// // We have a problem
			// throw new IllegalStateException();
			// }
			return leaveEdge;
		}
	}

	// private int getCutValue(final Tree<V, E> tree, final E cutEdge) {
	// final V cutEdgeDest = graph.getDest(cutEdge);
	//
	// final Set<V> head = new HashSet<V>(tree.getVertices());
	// final Set<V> tail = new HashSet<V>();
	// for (Iterator<V> iterator = new RootOrderIterator<V, E>(tree,
	// cutEdgeDest, cutEdge); iterator.hasNext();) {
	// V vertex = iterator.next();
	// head.remove(vertex);
	// tail.add(vertex);
	// }
	// int cutValue = 0;
	// Set<E> headToTail = new HashSet<E>();
	// Set<E> tailToHead = new HashSet<E>();
	// for (E edge : graph.getEdges()) {
	// final V edgeSource = graph.getSource(edge);
	// final V edgeDest = graph.getDest(edge);
	// if (head.contains(edgeSource) && tail.contains(edgeDest)) {
	// cutValue++;
	// headToTail.add(edge);
	// } else if (head.contains(edgeDest) && tail.contains(edgeSource)) {
	// cutValue--;
	// tailToHead.add(edge);
	// }
	// }
	// return cutValue;
	// }

	private Tree<V, E> createFeasibleTree(final AssignedRank<V> rank,
			final Set<E> nonTightTreeEdges) {
		final DelegateTree<V, E> tree = new DelegateTree<V, E>(
				new DirectedSparseGraph<V, E>());
		final Set<V> treeGraphVertices = new HashSet<V>();

		final List<V> queue = new LinkedList<V>();
		final V firstVertex = graph.getVertices().iterator().next();
		queue.add(firstVertex);
		tree.addVertex(firstVertex);

		final Set<E> tightTreeEdges = new HashSet<E>();
		final Set<E> nonTreeEdges = new HashSet<E>();

		final int graphVertexCount = graph.getVertexCount();
		while (treeGraphVertices.size() < graphVertexCount) {
			while (!queue.isEmpty()) {
				final V currentVertex = queue.remove(0);
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

						if (treeGraphVertices.contains(incidentVertex)) {
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
								treeGraphVertices.add(incidentVertex);
								queue.add(incidentVertex);
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
			V treeMinimalSlackVertex = null;
			for (E nonTreeEdge : nonTreeEdges) {
				final V source = graph.getSource(nonTreeEdge);
				final V dest = graph.getDest(nonTreeEdge);
				if (treeGraphVertices.contains(source)) {
					final int slack = getSlack(rank, nonTreeEdge, source, dest);
					if (minimalSlackEdge == null || slack < minimalSlack) {
						minimalSlackEdge = nonTreeEdge;
						minimalSlack = slack;
						delta = minimalSlack;
						minimalSlackVertex = dest;
						treeMinimalSlackVertex = source;
					}
				} else if (treeGraphVertices.contains(dest)) {
					final int slack = getSlack(rank, nonTreeEdge, source, dest);
					if (minimalSlackEdge == null || slack < minimalSlack) {
						minimalSlackEdge = nonTreeEdge;
						minimalSlack = slack;
						delta = -minimalSlack;
						minimalSlackVertex = source;
						treeMinimalSlackVertex = dest;
					}
				}
			}

			if (minimalSlack > 0) {
				for (V vertex : tree.getVertices()) {
					rank.assignRank(vertex, rank.getRank(vertex) - delta);
				}

				tree.addEdge(minimalSlackEdge, treeMinimalSlackVertex,
						minimalSlackVertex);
				tightTreeEdges.add(minimalSlackEdge);
				nonTreeEdges.remove(minimalSlackEdge);
				queue.add(minimalSlackVertex);
			}
		}

		return tree;
	}

	private int getSlack(final AssignedRank<V> rank, E nonTreeEdge,
			final V source, final V dest) {
		final int slack = Math.abs(rank.getRank(source) - rank.getRank(dest))
				- minimumLength(nonTreeEdge);
		return slack;
	}

	private int getWeight(E edge) {
		return 1;
	}

	private int minimumLength(E incidentEdge) {
		return minimumLengthConstraint.getMinimumLength(incidentEdge);
	}

	private AssignedRank<V> createInitialRank() {
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
