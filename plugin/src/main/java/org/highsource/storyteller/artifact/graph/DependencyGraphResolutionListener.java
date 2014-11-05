package org.highsource.storyteller.artifact.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.logging.Logger;
import org.highsource.storyteller.artifact.graph.VersionedEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class DependencyGraphResolutionListener implements ResolutionListener {

	private final Stack<Artifact> nodes = new Stack<Artifact>();

	private final DirectedGraph<Artifact, VersionedEdge> graph;

	private final Logger logger;

	public DependencyGraphResolutionListener(Logger logger) {
		this.graph = new DefaultDirectedGraph<Artifact, VersionedEdge>(VersionedEdge.class);
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
		graph.addVertex(artifact);
		if (!nodes.empty()) {
			graph.addEdge(nodes.peek(), artifact);
		}
	}

	public void omitForNearer(Artifact omitted, Artifact kept) {
		if (omitted.equals(kept)) {
			if (!nodes.empty()) {
				VersionedEdge edge = graph.addEdge(nodes.peek(), kept);
				if (edge != null) {
					logger.debug("omitForNearer[" + nodes.peek() + "]: identity replace of " + omitted + " added edge");
				}
			}
		} else if (graph.containsVertex(omitted)) {
			logger.debug("omitForNearer[" + nodes.peek() + "]: replacing " + omitted + " with " + kept);
			replaceNode(omitted, kept);
			
			if (!nodes.empty()) {
				graph.addEdge(nodes.peek(), kept);
			}
		} else {
			if (!nodes.empty()) {
				VersionedEdge edge = graph.addEdge(nodes.peek(), kept);
				if (edge != null) {
					logger.debug("omitForNearer[" + nodes.peek() + "]: replacing " + omitted + " with " + kept);
					edge.setVersion(omitted.getVersion());
				}
			}
		}
	}

	private void replaceNode(Artifact oldNode, Artifact newNode) {
		final Set<VersionedEdge> incomingEdges = graph.incomingEdgesOf(oldNode);
		final Collection<Artifact> ins = new ArrayList<Artifact>(incomingEdges.size());
		for (VersionedEdge edge : incomingEdges) {
			ins.add(graph.getEdgeSource(edge));
		}
	
		removeOrphans(oldNode);
		graph.removeVertex(oldNode);
		graph.addVertex(newNode);
		for (Artifact in : ins) {
			VersionedEdge edge = graph.addEdge(in, newNode);
			if (edge != null) {
				edge.setVersion(oldNode.getVersion());
			}
		}
	}

	private void removeOrphans(Artifact oldNode) {
		final Set<VersionedEdge> outgoingEdges = graph.outgoingEdgesOf(oldNode);
		final Set<Artifact> orphaned = new HashSet<Artifact>();
		for (VersionedEdge edge : outgoingEdges) {
			final Artifact node = graph.getEdgeTarget(edge);
			if (graph.inDegreeOf(node) == 1) {
				orphaned.add(node);
			}
		}
		for (Artifact node : orphaned) {
			logger.debug("deletion of " + oldNode + " orphans " + node);
			removeOrphans(node);
			graph.removeVertex(node);
		}
	}

	public void updateScope(Artifact artifact, String scope) {
		logger.debug("updateScope " + artifact + ", " + scope);
	}

	public void manageArtifact(Artifact artifact, Artifact replacement) {
		logger.debug("manageArtifact " + artifact + ", " + replacement);
	}

	public void omitForCycle(Artifact artifact) {
		if (graph.containsVertex(artifact)) {
			logger.debug("omitForCycle " + artifact);
			graph.removeVertex(artifact);
		}
	}

	public void updateScopeCurrentPom(Artifact artifact, String scopeIgnored) {
		logger.debug("updateScopeCurrentPom " + artifact + ", " + scopeIgnored);
	}

	public void selectVersionFromRange(Artifact artifact) {
		logger.debug("selectVersionFromRange " + artifact);
	}

	public void restrictRange(Artifact artifact, Artifact replacement, VersionRange versionRange) {
		logger.debug("restrictRange " + artifact + ", " + replacement + ", " + versionRange);
	}

	public DirectedGraph<Artifact, VersionedEdge> getGraph() {
		return graph;
	}
}
