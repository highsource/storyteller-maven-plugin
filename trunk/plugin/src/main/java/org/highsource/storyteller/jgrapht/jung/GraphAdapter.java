package org.highsource.storyteller.jgrapht.jung;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class GraphAdapter<V, E> extends AbstractTypedGraph<V, E> implements
		DirectedGraph<V, E> {

	private static final long serialVersionUID = 1L;

	private final org.jgrapht.DirectedGraph<V, E> digraph;

	public GraphAdapter(org.jgrapht.DirectedGraph<V, E> digraph) {
		super(EdgeType.DIRECTED);
		Validate.notNull(digraph);
		this.digraph = digraph;
	}

	@Override
	public Collection<E> getInEdges(V vertex) {
		return digraph.incomingEdgesOf(vertex);
	}

	@Override
	public Collection<E> getOutEdges(V vertex) {
		return digraph.outgoingEdgesOf(vertex);
	}

	@Override
	public Collection<V> getPredecessors(V vertex) {

		final Set<V> verticies = new HashSet<V>();
		for (E edge : digraph.incomingEdgesOf(vertex)) {
			verticies.add(digraph.getEdgeSource(edge));
		}
		return verticies;
	}

	@Override
	public Collection<V> getSuccessors(V vertex) {
		final Set<V> verticies = new HashSet<V>();
		for (E edge : digraph.outgoingEdgesOf(vertex)) {
			verticies.add(digraph.getEdgeTarget(edge));
		}
		return verticies;
	}

	@Override
	public int inDegree(V vertex) {
		return digraph.inDegreeOf(vertex);
	}

	@Override
	public int outDegree(V vertex) {
		return digraph.outDegreeOf(vertex);
	}

	@Override
	public boolean isPredecessor(V v1, V v2) {
		return digraph.containsEdge(v1, v2);
	}

	@Override
	public boolean isSuccessor(V v1, V v2) {
		return digraph.containsEdge(v2, v1);
	}

	@Override
	public V getSource(E edge) {
		return digraph.getEdgeSource(edge);
	}

	@Override
	public V getDest(E edge) {
		return digraph.getEdgeTarget(edge);
	}

	@Override
	public boolean isSource(V vertex, E edge) {
		return ObjectUtils.equals(digraph.getEdgeSource(edge), vertex);
	}

	@Override
	public boolean isDest(V vertex, E edge) {
		return ObjectUtils.equals(digraph.getEdgeTarget(edge), vertex);
	}

	@Override
	public Pair<V> getEndpoints(E edge) {
		return new Pair<V>(digraph.getEdgeSource(edge),
				digraph.getEdgeTarget(edge));
	}

	@Override
	public Collection<E> getEdges() {
		return digraph.edgeSet();
	}

	@Override
	public Collection<V> getVertices() {
		return digraph.vertexSet();
	}

	@Override
	public boolean containsVertex(V vertex) {
		return digraph.containsVertex(vertex);
	}

	@Override
	public boolean containsEdge(E edge) {
		return digraph.containsEdge(edge);
	}

	@Override
	public int getEdgeCount() {
		return digraph.edgeSet().size();
	}

	@Override
	public int getVertexCount() {
		return digraph.vertexSet().size();
	}

	@Override
	public Collection<V> getNeighbors(V vertex) {
		if (digraph.containsVertex(vertex)) {
			final Set<V> verticies = new HashSet<V>();
			for (E edge : digraph.incomingEdgesOf(vertex)) {
				verticies.add(digraph.getEdgeSource(edge));

			}
			for (E edge : digraph.outgoingEdgesOf(vertex)) {
				verticies.add(digraph.getEdgeTarget(edge));
			}
			return verticies;
		} else {
			return null;
		}
	}

	@Override
	public Collection<E> getIncidentEdges(V vertex) {
		if (digraph.containsVertex(vertex)) {
			return digraph.edgesOf(vertex);
		} else {
			return null;
		}
	}

	@Override
	public E findEdge(V v1, V v2) {
		return digraph.getEdge(v1, v2);
	}

	@Override
	public Collection<E> findEdgeSet(V v1, V v2) {
		return digraph.getAllEdges(v1, v2);
	}

	@Override
	public boolean addVertex(V vertex) {
		return digraph.addVertex(vertex);
	}

	@Override
	public boolean removeVertex(V vertex) {
		return digraph.removeVertex(vertex);
	}

	@Override
	public boolean removeEdge(E edge) {
		return digraph.removeEdge(edge);
	}

	@Override
	public boolean addEdge(E edge, Pair<? extends V> endpoints,
			EdgeType edgeType) {
		validateEdgeType(edgeType);

		Pair<V> new_endpoints = getValidatedEndpoints(edge, endpoints);
		if (new_endpoints == null)
			return false;

		V source = new_endpoints.getFirst();
		V dest = new_endpoints.getSecond();

		if (!digraph.containsVertex(source)) {
			digraph.addVertex(source);
		}

		if (!digraph.containsVertex(dest)) {
			digraph.addVertex(dest);
		}

		return digraph.addEdge(source, dest, edge);
	}

}
