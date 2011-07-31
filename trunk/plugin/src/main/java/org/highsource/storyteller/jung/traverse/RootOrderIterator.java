package org.highsource.storyteller.jung.traverse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.Tree;

public class RootOrderIterator<V, E> implements Iterator<V> {

	private final Tree<V, E> tree;
	private final Set<E> visited = new HashSet<E>();
	private final Stack<V> queue = new Stack<V>();

	public RootOrderIterator(Tree<V, E> tree, V root) {
		this(tree, root, null);
	}

	public RootOrderIterator(Tree<V, E> tree, V root, E parentEdge) {
		Validate.notNull(tree);
		Validate.notNull(root);
		Validate.isTrue(tree.containsVertex(root));
		if (parentEdge != null) {
			Validate.isTrue(tree.containsEdge(parentEdge));
			V source = tree.getSource(parentEdge);
			V dest = tree.getDest(parentEdge);
			Validate.isTrue(source == root || dest == root);
			this.visited.add(parentEdge);
		}
		this.tree = tree;
		this.queue.push(root);
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public V next() {
		if (!queue.isEmpty()) {
			V current = queue.pop();

			final int insertPosition = queue.size();

			final Collection<E> incidentEdges = tree.getIncidentEdges(current);

			for (E incidentEdge : incidentEdges) {
				if (!visited.contains(incidentEdge)) {
					final V neighbour = tree.getOpposite(current, incidentEdge);
					queue.add(insertPosition, neighbour);
					visited.add(incidentEdge);
				}
			}

			return current;
		} else {
			throw new NoSuchElementException();
		}

	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

}
