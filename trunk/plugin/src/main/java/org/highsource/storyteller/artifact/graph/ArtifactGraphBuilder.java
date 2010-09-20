package org.highsource.storyteller.artifact.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.codehaus.plexus.logging.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface ArtifactGraphBuilder {

	public DirectedGraph<Artifact, DefaultEdge> buildArtifactGraph(
			Set<Artifact> artifacts, Artifact originatingArtifact,
			Map managedVersions, ArtifactRepository localRepository,
			List<ArtifactRepository> remoteRepositories,
			ArtifactMetadataSource source, ArtifactFilter filter,
			List<ResolutionListener> listeners,
			Logger logger)
			throws ArtifactNotFoundException, ArtifactResolutionException;
}
