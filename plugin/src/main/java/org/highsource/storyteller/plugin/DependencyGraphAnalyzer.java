package org.highsource.storyteller.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface DependencyGraphAnalyzer {

	public void analyzeDependencyGraph(
			DirectedGraph<Artifact, DefaultEdge> graph, Log log);

}
