package org.highsource.storyteller.artifact.graph.alg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class DescendantsInspector<V, E> {
	private final Map<V, Set<V>> vertexToDescendats = new HashMap<V, Set<V>>();

	private final Graph<V, E> graph;

	public DescendantsInspector(DirectedGraph<V, E> g) {
		this.graph = g;
	}

	public Set<V> descendantsOf(V vertex) {
		Set<V> descendants = vertexToDescendats.get(vertex);

		if (descendants == null) {
			descendants = new HashSet<V>();

			final BreadthFirstIterator<V, E> i = new BreadthFirstIterator<V, E>(
					graph, vertex);

			while (i.hasNext()) {

				final V v = i.next();
				if (v != vertex) {
					descendants.add(v);
				}
			}

			vertexToDescendats.put(vertex, descendants);
		}

		return descendants;
	}
}