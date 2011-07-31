package org.highsource.storyteller.jung.traverse;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.Tree;

public class PreOrderIterator<V> implements Iterator<V> {

	private final Tree<V, ?> tree;

	private final Stack<V> queue = new Stack<V>();

	public PreOrderIterator(Tree<V, ?> tree) {
		this(tree, tree.getRoot());
	}

	public PreOrderIterator(Tree<V, ?> tree, V vertex) {
		Validate.notNull(tree);
		Validate.notNull(vertex);
		Validate.isTrue(tree.containsVertex(vertex));
		this.tree = tree;
		queue.push(vertex);
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public V next() {
		if (!queue.isEmpty()) {
			final V current = queue.pop();
			final Collection<V> children = tree.getChildren(current);
			final int insertPosition = queue.size();
			for (V child : children) {
				queue.add(insertPosition, child);
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
