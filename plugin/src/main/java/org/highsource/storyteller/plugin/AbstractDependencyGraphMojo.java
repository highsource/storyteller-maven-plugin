package org.highsource.storyteller.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;
import javassist.CtClass;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.highsource.storyteller.artifact.MArchive;
import org.highsource.storyteller.artifact.MClass;
import org.highsource.storyteller.artifact.graph.ArtifactGraphBuilder;
import org.highsource.storyteller.artifact.graph.VersionedEdge;
import org.highsource.storyteller.plexus.logging.LogToLoggerAdapter;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public abstract class AbstractDependencyGraphMojo extends AbstractMojo {

	protected static final String HR = "------------------------------------------------------------------------------------------";

	/**
	 * @component
	 */
	protected ArtifactResolver artifactResolver;

	/**
	 * @component
	 */
	protected ArtifactMetadataSource artifactMetadataSource;

	/**
	 * @component
	 */
	protected ArtifactFactory artifactFactory;

	/**
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	protected ArtifactRepository localRepository;

	/**
	 * @component role="org.apache.maven.project.MavenProjectBuilder"
	 */
	protected MavenProjectBuilder mavenProjectBuilder;

	/**
	 * @component
	 */
	protected ArtifactGraphBuilder artifactGraphBuilder;

	/**
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @required
	 * @readonly
	 */
	protected List<ArtifactRepository> remoteArtifactRepositories;

	/**
	 * @parameter expression="${project}"
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * Creates dependency artifacts for the current project.
	 * 
	 * @return Set of artifacts for the dependencies of the current project.
	 * @throws MojoExecutionException
	 *             In case of invalid dependency version.
	 */
	protected Set<Artifact> createDependencyArtifacts() throws MojoExecutionException {
		getLog().debug("Creating dependency artifacts.");
		try {
			@SuppressWarnings("unchecked")
			final Set<Artifact> dependencyArtifacts = MavenMetadataSource.createArtifacts(artifactFactory, project
					.getDependencies(), "compile", null, project);
			getLog().debug(HR);
			return dependencyArtifacts;
		} catch (InvalidDependencyVersionException idvex) {
			throw new MojoExecutionException("Could not create artifacts for dependencies.", idvex);
		}
	}

	/**
	 * Builds the graph of dependency artifacts.
	 * 
	 * @param artifacts
	 *            root artifacts of the dependency graph.
	 * @return Directed graph of resolved artifact dependencies.
	 * @throws MojoExecutionException
	 *             In case artifacts can not be found or resolved.
	 */
	protected DirectedGraph<Artifact, VersionedEdge> buildArtifactDependencyGraph(Set<Artifact> artifacts)
			throws MojoExecutionException {
		getLog().debug("Building artifact graph.");
		try {
			final DirectedGraph<Artifact, VersionedEdge> graph = artifactGraphBuilder.buildArtifactGraph(artifacts,
					project.getArtifact(), Collections.EMPTY_MAP, localRepository, remoteArtifactRepositories,
					artifactMetadataSource, null, null, new LogToLoggerAdapter("", getLog()));

			getLog().debug(HR);
			return graph;
		} catch (ArtifactResolutionException arex) {
			throw new MojoExecutionException("Error resolving dependency artifacts.", arex);
		} catch (ArtifactNotFoundException anfex) {
			throw new MojoExecutionException("Artifact could not be found.", anfex);
		}
	}

	/**
	 * Creates archives for artifacts and builds archive dependency graph.
	 * 
	 * @param artifactGraph
	 *            artifact graph.
	 * @return Directed graph of archives.
	 * @throws MojoExecutionException
	 *             In case of problems creating archives.
	 */
	protected DirectedGraph<MArchive, DefaultEdge> buildArchiveDependencyGraph(
			final DirectedGraph<Artifact, VersionedEdge> artifactGraph, Map<Artifact, MArchive> archives)
			throws MojoExecutionException {

		getLog().debug("Building archive dependency graph.");

		final DirectedGraph<MArchive, DefaultEdge> archiveGraph = new DefaultDirectedGraph<MArchive, DefaultEdge>(
				DefaultEdge.class);
		for (Entry<Artifact, MArchive> entry : archives.entrySet()) {
			final MArchive archive = entry.getValue();
			archiveGraph.addVertex(archive);
			for (VersionedEdge artifactEdge : artifactGraph.incomingEdgesOf(archive.getArtifact())) {
				final Artifact sourceArtifact = artifactGraph.getEdgeSource(artifactEdge);
				final MArchive sourceArchive = archives.get(sourceArtifact);
				if (sourceArchive != null) {
					archiveGraph.addVertex(sourceArchive);
					archiveGraph.addEdge(sourceArchive, archive);
				}
			}

			for (VersionedEdge artifactEdge : artifactGraph.outgoingEdgesOf(archive.getArtifact())) {
				final Artifact targetArtifact = artifactGraph.getEdgeTarget(artifactEdge);
				final MArchive targetArchive = archives.get(targetArtifact);
				if (targetArchive != null) {
					archiveGraph.addVertex(targetArchive);
					archiveGraph.addEdge(archive, targetArchive);
				}
			}
		}
		getLog().debug(HR);
		return archiveGraph;
	}

	protected Map<Artifact, MArchive> createArchives(Collection<Artifact> artifacts) throws MojoExecutionException {
		getLog().debug("Creating archives.");
		final Map<Artifact, MArchive> archives = new HashMap<Artifact, MArchive>();
		for (Artifact artifact : artifacts) {
			final MArchive archive = createArchive(artifact);
			if (archive != null) {
				archives.put(artifact, archive);
			}
		}
		getLog().debug(HR);
		return archives;
	}

	private MArchive createArchive(final Artifact artifact) throws MojoExecutionException {
		final ClassPool classPool = new ClassPool();
		final File artifactFile = artifact.getFile();
		final MArchive archive = new MArchive(artifact);
		if (artifactFile != null && artifactFile.getName().toLowerCase().endsWith(".jar")) {
			try {
				final JarFile artifactJarFile = new JarFile(artifactFile);
				final Enumeration<JarEntry> jarEntries = artifactJarFile.entries();

				while (jarEntries.hasMoreElements()) {
					final JarEntry jarEntry = jarEntries.nextElement();

					if (jarEntry.getName().endsWith(".class")) {
						InputStream is = null;
						CtClass ctClass = null;
						try {
							is = artifactJarFile.getInputStream(jarEntry);
							ctClass = classPool.makeClass(is);
						} catch (IOException ioex1) {
							// (ugly workaround) this one class exists, corrupt, in icu4j-2.6.1.jar
							String entry = artifactFile.getName() + "/" + jarEntry.getName();
							if ("icu4j-2.6.1.jar/com/ibm/icu/impl/data/LocaleElements_zh__PINYIN.class".equals(entry)) {
								continue;
							}
							throw new MojoExecutionException("Could not load class from JAR entry ["
									+ artifactFile.getAbsolutePath() + "/" + jarEntry.getName() + "].");
						} finally {
							try {
								if (is != null) is.close();
							} catch (IOException ignored) {
								// Ignore
							}
						}

						final String className = ctClass.getName();

						final MClass theClass = archive.getOrCreateClass(className);
						getLog().debug("Processing class [" + theClass.getClassName() + "].");

						@SuppressWarnings("unchecked")
						final Collection<String> referencedClassNames = ctClass.getRefClasses();
						for (String referencedClassName : referencedClassNames) {
							if (!className.equals(referencedClassName)) {
								theClass.addReferencedClassName(referencedClassName.intern());
								// getLog().debug(
								// "Class [" + className + "] depends on ["
								// + referencedClassName + "].");
							}
						}
					}
				}
			} catch (IOException ioex) {
				throw new MojoExecutionException("Could not analyze archive [" + artifactFile.getAbsolutePath() + "].",
						ioex);
			}
		}
		return archive;
	}

	protected Set<Artifact> dependencyArtifacts;
	protected DirectedGraph<Artifact, VersionedEdge> artifactGraph;
	protected Map<Artifact, MArchive> archives;
	protected DirectedGraph<MArchive, DefaultEdge> archiveDependencyGraph;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug(HR);
		// Get the artifacts for dependencies
		dependencyArtifacts = createDependencyArtifacts();
		// Build the artifact graph
		// Artifacts are also resolved on this step
		artifactGraph = buildArtifactDependencyGraph(dependencyArtifacts);
		// Create archives for artifacts
		archives = createArchives(artifactGraph.vertexSet());
		// Create an archive dependency graph out of artifact dependency graph
		archiveDependencyGraph = buildArchiveDependencyGraph(artifactGraph, archives);
	}

}
