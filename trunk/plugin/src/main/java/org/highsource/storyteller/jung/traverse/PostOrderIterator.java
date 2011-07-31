package org.highsource.storyteller.jung.traverse;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.Tree;

public class PostOrderIterator<V> implements Iterator<V> {

	private final Tree<V, ?> tree;
	private final Stack<V> queue = new Stack<V>();
	private final Stack<V> visited = new Stack<V>();
	private V current = null;

	public PostOrderIterator(Tree<V, ?> tree) {
		Validate.notNull(tree);
		this.tree = tree;
		final V root = tree.getRoot();
		Validate.notNull(root);
		this.queue.push(root);
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public V next() {
		while (!queue.isEmpty()) {
			current = queue.peek();
			if (tree.getChildCount(current) != 0) {
				if (visited.size() == 0 || visited.peek() != current) {
					visited.push(current);
					final Collection<V> children = tree.getChildren(current);
					final int insertPosition = queue.size();
					for (V child : children) {
						queue.add(insertPosition, child);
					}
					continue;
				}
				visited.pop();
			}
			queue.pop();
			return current;
		}
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

}
