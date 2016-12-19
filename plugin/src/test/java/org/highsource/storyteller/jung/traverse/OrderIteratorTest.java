package org.highsource.storyteller.jung.traverse.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.collections15.IteratorUtils;
import org.highsource.storyteller.jung.traverse.PostOrderIterator;
import org.highsource.storyteller.jung.traverse.PreOrderIterator;
import org.highsource.storyteller.jung.traverse.RootOrderIterator;
import org.junit.Test;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

public class OrderIteratorTest {

	@Test
	public void checksIterationIsCorrect() {
		final DelegateTree<String, String> tree = new DelegateTree<String, String>(
				new DirectedSparseGraph<String, String>());

		String root = ".";
		String _0 = "0";
		String _1 = "1";
		String _00 = "00";
		String _01 = "01";
		String _10 = "10";
		String _11 = "11";
		tree.addVertex(root);
		// tree.addVertex(_0);
		// tree.addVertex(_1);
		// tree.addVertex(_00);
		// tree.addVertex(_01);
		// tree.addVertex(_10);
		// tree.addVertex(_11);
		tree.addEdge(".-0", root, _0);
		tree.addEdge(".-1", root, _1);
		tree.addEdge("0-00", _0, _00);
		tree.addEdge("0-01", _0, _01);
		tree.addEdge("1-10", _1, _10);
		tree.addEdge("1-11", _1, _11);

		{
			Iterator<String> iterator = new PostOrderIterator<String>(tree);

			List<String> recursivePostOrder = new ArrayList<String>();
			postOrder(recursivePostOrder, tree, root);
			List<String> nonRecursivePostOrder = IteratorUtils.toList(iterator);
			Assert.assertEquals(recursivePostOrder, nonRecursivePostOrder);
		}
		{
			Iterator<String> iterator = new PreOrderIterator<String>(tree);

			List<String> recursivePreOrder = new ArrayList<String>();
			preOrder(recursivePreOrder, tree, root);
			List<String> nonRecursivePreOrder = IteratorUtils.toList(iterator);
			Assert.assertEquals(recursivePreOrder, nonRecursivePreOrder);
		}

		{
			{
				Iterator<String> iterator = new RootOrderIterator<String, String>(
						tree, root);

				List<String> recursiveRootOrder = new ArrayList<String>();
				reRoot(recursiveRootOrder, tree, null, root);
				List<String> nonRecursiveRootOrder = IteratorUtils
						.toList(iterator);
				Assert.assertEquals(recursiveRootOrder, nonRecursiveRootOrder);
			}
			for (String vertex : tree.getVertices()) {
				Iterator<String> iterator = new RootOrderIterator<String, String>(
						tree, vertex);

				List<String> recursiveRootOrder = new ArrayList<String>();
				reRoot(recursiveRootOrder, tree, null, vertex);
				List<String> nonRecursiveRootOrder = IteratorUtils
						.toList(iterator);
				Assert.assertEquals(recursiveRootOrder, nonRecursiveRootOrder);
			}

		}
	}

	private void postOrder(List<String> list, Tree<String, ?> tree, String v) {
		for (String child : tree.getChildren(v)) {
			postOrder(list, tree, child);
		}
		list.add(v);

	}

	private void preOrder(List<String> list, Tree<String, ?> tree, String v) {
		list.add(v);
		for (String child : tree.getChildren(v)) {
			preOrder(list, tree, child);
		}

	}

	private void reRoot(List<String> list, Tree<String, ?> tree,
			String lastParent, String current) {
		list.add(current);
		for (String neighbor : tree.getNeighbors(current)) {
			if (neighbor != lastParent) {
				reRoot(list, tree, current, neighbor);
			}
		}

	}

}
