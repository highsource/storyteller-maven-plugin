package org.highsource.storyteller.jung.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.highsource.storyteller.jung.traverse.PostOrderIterator;

import edu.uci.ics.jung.graph.Tree;

public class UnderTree<V> {

	private final Tree<V, ?> tree;

	private final Map<V, Integer> low = new HashMap<V, Integer>();
	private final Map<V, Integer> lim = new HashMap<V, Integer>();

	public UnderTree(Tree<V, ?> tree) {
		Validate.notNull(tree);
		this.tree = tree;
		int currentLim = 0;
		for (Iterator<V> iterator = new PostOrderIterator<V>(tree); iterator
				.hasNext();) {
			final V node = iterator.next();
			lim.put(node, currentLim);
			if (tree.getChildCount(node) > 0) {
				low.put(node, low.get(tree.getChildren(node).iterator().next()));
			} else {
				low.put(node, currentLim);
			}
			currentLim++;
		}
	}

	public boolean isUnder(V up, V down) {
		Validate.notNull(up);
		Validate.notNull(down);
		Integer lowUp = low.get(up);
		Integer limUp = lim.get(up);
		Integer limDown = lim.get(down);
		Validate.notNull(lowUp);
		Validate.notNull(limUp);
		Validate.notNull(limDown);
		return (lowUp <= limDown) && (limDown <= limUp);
	}

}
