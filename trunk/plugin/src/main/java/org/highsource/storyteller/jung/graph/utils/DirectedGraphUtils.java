package org.highsource.storyteller.jung.graph.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.DirectedGraph;

public class DirectedGraphUtils {

	public static class IsSink<V> implements Predicate<V> {
		private final DirectedGraph<V, ?> graph;

		public IsSink(DirectedGraph<V, ?> graph) {
			Validate.notNull(graph);
			this.graph = graph;
		}

		@Override
		public boolean evaluate(V vertex) {
			Validate.notNull(vertex);
			return this.graph.getSuccessorCount(vertex) == 0;
		}

		public static <V> Predicate<V> of(DirectedGraph<V, ?> graph) {
			Validate.notNull(graph);
			return new IsSink<V>(graph);
		}
	}

	public static <V> Collection<V> findSinks(DirectedGraph<V, ?> graph) {
		Validate.notNull(graph);
		final List<V> vertices = new LinkedList<V>(graph.getVertices());
		CollectionUtils.filter(vertices, IsSink.of(graph));
		return Collections.unmodifiableList(vertices);
	}

}
