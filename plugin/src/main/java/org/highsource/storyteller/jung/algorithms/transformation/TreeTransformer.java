package org.highsource.storyteller.jung.algorithms.transformation;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.graph.Tree;

public class TreeTransformer {

	public static <V, E> void rotateTree(Tree<V, E> oldTree, V newRoot,
			Tree<V, E> newTree) {
		final Set<V> visited = new HashSet<V>();
		final Stack<V> queue = new Stack<V>();
		queue.push(newRoot);
		while (!queue.isEmpty()) {
			V parent = queue.pop();

			int insertPosition = queue.size();
			for (E edge : oldTree.getIncidentEdges(parent)) {
				V child = oldTree.getOpposite(parent, edge);
				if (!visited.contains(child)) {
					newTree.addEdge(edge, parent, child);
					queue.add(insertPosition, child);
				}
			}
			visited.add(parent);
		}
	}
}
