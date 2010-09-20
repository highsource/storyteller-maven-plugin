package org.highsource.storyteller.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Stack;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.logging.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DependencyGraphResolutionListener implements ResolutionListener {

	private final Stack<Artifact> nodes = new Stack<Artifact>();

	private final DirectedGraph<Artifact, DefaultEdge> graph;

	@SuppressWarnings("unused")
	private final Logger logger;

	public DependencyGraphResolutionListener(Logger logger) {
		this.graph = new DefaultDirectedGraph<Artifact, DefaultEdge>(
				DefaultEdge.class);
		this.logger = logger;
	}

	public void startProcessChildren(Artifact artifact) {
		this.nodes.push(artifact);
	}

	public void endProcessChildren(Artifact artifact) {
		this.nodes.pop();
	}

	public void testArtifact(Artifact artifact) {
	}

	public void includeArtifact(Artifact artifact) {
		if (graph.containsVertex(artifact)) {
			replaceNode(artifact, artifact);
		} else {
			graph.addVertex(artifact);
		}
		if (!nodes.empty()) {
			graph.addEdge(nodes.peek(), artifact);
		}
	}

	public void omitForNearer(Artifact omitted, Artifact kept) {
		if (graph.containsVertex(omitted)) {
			replaceNode(omitted, kept);
		} else {
			graph.addVertex(kept);
		}
		if (!nodes.empty()) {
			graph.addEdge(nodes.peek(), kept);
		}
	}

	private void replaceNode(Artifact oldNode, Artifact newNode) {
		final Set<DefaultEdge> incomingEdges = graph.incomingEdgesOf(oldNode);
		final Collection<Artifact> ins = new ArrayList<Artifact>(incomingEdges
				.size());
		for (DefaultEdge edge : incomingEdges) {
			final Artifact node = graph.getEdgeSource(edge);
			ins.add(node);
		}
		final Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(oldNode);
		final Collection<Artifact> outs = new ArrayList<Artifact>(outgoingEdges
				.size());

		for (DefaultEdge edge : outgoingEdges) {
			final Artifact node = graph.getEdgeTarget(edge);
			outs.add(node);
		}

		graph.removeVertex(oldNode);
		graph.addVertex(newNode);
		for (Artifact in : ins) {
			graph.addEdge(in, newNode);
		}
		for (Artifact out : outs) {
			graph.addEdge(newNode, out);
		}
	}

	public void updateScope(Artifact artifact, String scope) {
	}

	public void manageArtifact(Artifact artifact, Artifact replacement) {
	}

	public void omitForCycle(Artifact artifact) {
		if (graph.containsVertex(artifact)) {
			graph.removeVertex(artifact);
		}
	}

	public void updateScopeCurrentPom(Artifact artifact, String scopeIgnored) {
	}

	public void selectVersionFromRange(Artifact artifact) {
	}

	public void restrictRange(Artifact artifact, Artifact replacement,
			VersionRange versionRange) {
	}

	public DirectedGraph<Artifact, DefaultEdge> getGraph() {
		return graph;
	}
}
