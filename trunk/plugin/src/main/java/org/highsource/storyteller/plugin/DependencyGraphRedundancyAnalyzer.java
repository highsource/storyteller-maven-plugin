package org.highsource.storyteller.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.highsource.storyteller.artifact.graph.alg.DescendantsInspector;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class DependencyGraphRedundancyAnalyzer implements
		DependencyGraphAnalyzer {

	public void analyzeDependencyGraph(
			DirectedGraph<Artifact, DefaultEdge> graph, Log log) {

		final DescendantsInspector<Artifact, DefaultEdge> graphConnectivityInspector = new DescendantsInspector<Artifact, DefaultEdge>(
				graph);

		for (final Iterator<Artifact> iterator = new TopologicalOrderIterator<Artifact, DefaultEdge>(
				graph); iterator.hasNext();) {

			final Artifact artifact = iterator.next();

			final Set<Artifact> dependencies = new HashSet<Artifact>();

			for (DefaultEdge edge : graph.outgoingEdgesOf(artifact)) {
				dependencies.add(graph.getEdgeTarget(edge));
			}

			final List<Artifact> toCover = new ArrayList<Artifact>(
					graphConnectivityInspector.descendantsOf(artifact));

			final Set<Artifact> covered = new HashSet<Artifact>();

			final List<Artifact> roots = new LinkedList<Artifact>();

			while (!toCover.isEmpty()) {
				final Artifact toBeCovered = toCover.remove(0);
				if (!covered.contains(toBeCovered)) {
					final Set<Artifact> willBeCovered = graphConnectivityInspector
							.descendantsOf(toBeCovered);
					for (ListIterator<Artifact> iterator1 = roots
							.listIterator(); iterator1.hasNext();) {
						final Artifact r = iterator1.next();
						if (willBeCovered.contains(r)) {
							iterator1.remove();
						}
					}
					covered.addAll(willBeCovered);
					roots.add(toBeCovered);
					covered.add(toBeCovered);
				}
			}
			if (log.isDebugEnabled()) {
				if (!roots.isEmpty()) {
					log.debug("Transitively referenced artifacts for ["
							+ artifact.getId()
							+ "] are covered by the following artifacts.");
					for (Artifact root : roots) {
						log.debug("   " + root.getId());
					}
				}
			}
			dependencies.removeAll(roots);
			if (!dependencies.isEmpty()) {
				log.info("Redundant dependencies of [" + artifact.getId()
						+ "]:");
				for (Artifact root : dependencies) {
					log.info("   " + root.getId());
				}
			}
		}

		// TODO Auto-generated method stub

	}

}
