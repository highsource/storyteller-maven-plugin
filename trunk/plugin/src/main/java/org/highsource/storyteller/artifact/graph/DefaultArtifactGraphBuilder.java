package org.highsource.storyteller.artifact.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.codehaus.plexus.logging.Logger;
import org.highsource.storyteller.plugin.DependencyGraphResolutionListener;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DefaultArtifactGraphBuilder implements ArtifactGraphBuilder {

	private ArtifactResolver artifactResolver;

	private ArtifactMetadataSource artifactMetadataSource;

	public DirectedGraph<Artifact, DefaultEdge> buildArtifactGraph(
			Set<Artifact> artifacts, Artifact originatingArtifact,
			Map managedVersions, ArtifactRepository localRepository,
			List<ArtifactRepository> remoteRepositories,
			ArtifactMetadataSource source, ArtifactFilter filter,
			List<ResolutionListener> listeners, Logger logger)
			throws ArtifactNotFoundException, ArtifactResolutionException {

		final DependencyGraphResolutionListener dependencyGraphResolutionListener = new DependencyGraphResolutionListener(
				logger);
		final DebugResolutionListener debugResolutionListener = new DebugResolutionListener(
				logger);

		final List<ResolutionListener> moreListeners = listeners != null ? new ArrayList<ResolutionListener>(
				listeners)
				: new ArrayList<ResolutionListener>(2);
		moreListeners.add(dependencyGraphResolutionListener);

		moreListeners.add(debugResolutionListener);
		artifactResolver.resolveTransitively(artifacts, originatingArtifact,
				managedVersions, localRepository, remoteRepositories,
				artifactMetadataSource, filter, moreListeners);

		final DirectedGraph<Artifact, DefaultEdge> graph = dependencyGraphResolutionListener
				.getGraph();
		return graph;
	}
}
