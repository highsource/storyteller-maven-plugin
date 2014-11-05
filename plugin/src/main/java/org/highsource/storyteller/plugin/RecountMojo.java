package org.highsource.storyteller.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.highsource.storyteller.artifact.MArchive;
import org.highsource.storyteller.artifact.MClass;
import org.highsource.storyteller.artifact.MPackage;
import org.highsource.storyteller.artifact.graph.VersionedEdge;
import org.highsource.storyteller.artifact.graph.alg.DescendantsInspector;
import org.highsource.storyteller.artifact.graph.ext.VertexNameProviders;
import org.highsource.storyteller.jgrapht.ext.AutoGraphExporter;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * @goal recount
 * @phase verify
 * @requiresDependencyResolution test
 */
public class RecountMojo extends AbstractDependencyGraphMojo {

	/**
	 * If specified, the plugin renders archive dependency graph into this file.
	 * @parameter expression="${archiveDependencyGraphFile}" default-value="target/storyteller/archiveDependencyGraph.gml"
	 */
	private File archiveDependencyGraphFile;

	/**
	 * If specified, the plugin renders class dependency graph into this file.
	 * @parameter expression="${classDependencyGraphFile}" default-value="target/storyteller/classDependencyGraph.gml"
	 */
	private File classDependencyGraphFile;

	/**
	 * If specified, the plugin renders "rooted" class dependency graph into this file.
	 * @parameter expression="${rootedClassDependencyGraphFile}" default-value="target/storyteller/rootedClassDependencyGraph.gml"
	 */
	private File rootedClassDependencyGraphFile;

	/**
	 * If specified, the plugin renders "rooted" archive dependency graph into this file.
	 * @parameter expression="${rootedArchiveDependencyGraphFile}" default-value="target/storyteller/rootedArchiveDependencyGraph.gml"
	 */
	private File rootedArchiveDependencyGraphFile;

	/**
	 * The plugin uses GraphViz package to render graphs in formats like PDF and so on. If the <code>dot</code>
	 * executable is not in PATH, it can be specified manually here.
	 * @parameter expression="${graphViz.dotFile}" default-value="dot"
	 */
	private String graphVizDotFile;
	
	/**
	 * Use Batik to render PNGs instead of Graphviz's default renderer.
	 * @parameter expression="${useBatik}" default-value="false"
	 */
	private boolean useBatik;
	
	/**
	 * Hints to pass to Batik when rendering.
	 * @parameter
	 */
	private Map<String, String> batikHints;

	private DirectedGraph<MClass, DefaultEdge> classDependencyGraph;
	private Collection<MArchive> rootArchives;
	private Collection<MClass> rootClasses;
	private DirectedGraph<MClass, DefaultEdge> rootedClassDependencyGraph;
	private DirectedGraph<MArchive, DefaultEdge> rootedArchiveDependencyGraph;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		// Build class dependency graph
		classDependencyGraph = buildClassDependencyGraph(archives);
		// Get root archives
		rootArchives = getRootArchives(dependencyArtifacts, archives);
		// Get root classes
		rootClasses = getRootClasses(rootArchives);
		// Create graph of classes reachable from classes of root archives
		rootedClassDependencyGraph = buildRootedClassDependencyGraph(classDependencyGraph, rootClasses);
		rootedArchiveDependencyGraph = createRootedArchiveDependencyGraph(rootedClassDependencyGraph);
		// Finally, recount on everything
		recount();
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
	@Override
	protected DirectedGraph<MArchive, DefaultEdge> buildArchiveDependencyGraph(
			final DirectedGraph<Artifact, VersionedEdge> artifactGraph, Map<Artifact, MArchive> archives)
			throws MojoExecutionException {
		final DirectedGraph<MArchive, DefaultEdge> archiveGraph = super.buildArchiveDependencyGraph(artifactGraph,
				archives);
		exportGraph(archiveGraph, VertexNameProviders.ARCHIVE_VERTEX_NAME_PROVIDER, archiveDependencyGraphFile);
		getLog().debug(HR);
		return archiveGraph;
	}

	private Collection<MArchive> getRootArchives(final Set<Artifact> dependencyArtifacts,
			final Map<Artifact, MArchive> archives) {
		getLog().debug("Retrieving root archives.");
		final Collection<MArchive> rootArchives = getRootArchives(dependencyArtifacts, archives, project);

		getLog().debug("Root archives.");
		for (MArchive rootArchive : rootArchives) {
			getLog().debug("  " + rootArchive.getArtifact().getId() + "].");
		}
		getLog().debug(HR);
		return rootArchives;
	}

	private Collection<MArchive> getRootArchives(final Set<Artifact> dependencyArtifacts,
			final Map<Artifact, MArchive> archives, MavenProject project) {
		final Collection<MArchive> rootArchives;
		if (archives.containsKey(project.getArtifact())) {
			final MArchive archive = archives.get(project.getArtifact());
			if (!archive.getPackages().isEmpty()) {
				getLog().debug(
						"Project artifact is found in graph and it contains packages. Using it as the root archive.");
				rootArchives = Collections.<MArchive> singleton(archive);
			} else {
				getLog().debug("Project artifact is found in graph but it does not contain packages, using dependency artifacts as roots.");
				rootArchives = new ArrayList<MArchive>(dependencyArtifacts.size());
				for (final Artifact dependencyArtifact : dependencyArtifacts) {
					final MArchive dependencyArchive = archives.get(dependencyArtifact);
					if (dependencyArchive != null) {
						rootArchives.add(dependencyArchive);
					}
				}
			}
		} else {
			getLog().debug("Project artifact is not found in graph, using dependency artifacts as roots.");
			rootArchives = new ArrayList<MArchive>(dependencyArtifacts.size());
			for (final Artifact dependencyArtifact : dependencyArtifacts) {
				final MArchive dependencyArchive = archives.get(dependencyArtifact);
				if (dependencyArchive != null) {
					rootArchives.add(dependencyArchive);
				}
			}
		}
		return rootArchives;
	}

	private DirectedGraph<MClass, DefaultEdge> buildClassDependencyGraph(final Map<Artifact, MArchive> archives) {
		getLog().debug("Building class dependency graph.");
		final MultiMap<String, MClass> classes = new MultiHashMap<String, MClass>();

		for (final MArchive archive : archives.values()) {
			for (MPackage _package : archive.getPackages()) {
				for (MClass _class : _package.getClasses()) {
					classes.put(_class.getClassName(), _class);
				}
			}
		}

		final DirectedGraph<MClass, DefaultEdge> classesGraph = new DefaultDirectedGraph<MClass, DefaultEdge>(
				DefaultEdge.class);

		for (final Entry<String, Collection<MClass>> entry : classes.entrySet()) {
			final String className = entry.getKey();
			final Collection<MClass> _classes = entry.getValue();
			if (_classes.size() > 1) {
				getLog().warn("The class [" + className + "] is present in more than one archive:");
				for (final MClass _class : _classes) {
					final MArchive archive = _class.getPackage().getArchive();
					final Artifact artifact = archive.getArtifact();
					getLog().warn(artifact.getId());
				}
			}

			for (final MClass _class : _classes) {
				for (final String referencedClassName : _class.getReferencedClassNames()) {
					classesGraph.addVertex(_class);
					final Collection<MClass> _referencedClasses = classes.get(referencedClassName);
					if (_referencedClasses != null) {
						for (final MClass _referencedClass : _referencedClasses) {
							classesGraph.addVertex(_referencedClass);
							classesGraph.addEdge(_class, _referencedClass);
						}
					}
				}
			}
		}
		exportGraph(classesGraph, VertexNameProviders.CLASS_VERTEX_NAME_PROVIDER, classDependencyGraphFile);
		getLog().debug(HR);
		return classesGraph;
	}

	private Collection<MClass> getRootClasses(final Collection<MArchive> rootArchives) {
		getLog().debug("Getting root classes.");
		final Collection<MClass> currentClasses = new HashSet<MClass>();
		for (MArchive archive : rootArchives) {
			for (MPackage _package : archive.getPackages()) {
				for (MClass _class : _package.getClasses()) {
					currentClasses.add(_class);
				}
			}
		}
		getLog().debug(HR);
		return currentClasses;
	}

	private DirectedGraph<MClass, DefaultEdge> buildRootedClassDependencyGraph(
			final DirectedGraph<MClass, DefaultEdge> classesGraph, final Collection<MClass> rootClasses) {

		getLog().debug("Building rooted class dependency graph.");
		final DirectedGraph<MClass, DefaultEdge> dependentClassesGraph = new DefaultDirectedGraph<MClass, DefaultEdge>(
				DefaultEdge.class);

		final List<MClass> currentClasses = new LinkedList<MClass>(rootClasses);

		final Set<MClass> visitedClasses = new HashSet<MClass>();

		while (!currentClasses.isEmpty()) {
			final MClass currentClass = currentClasses.remove(0);
			if (!visitedClasses.contains(currentClass)) {
				visitedClasses.add(currentClass);
				dependentClassesGraph.addVertex(currentClass);
				for (DefaultEdge edge : classesGraph.outgoingEdgesOf(currentClass)) {
					final MClass referencedClass = classesGraph.getEdgeTarget(edge);
					dependentClassesGraph.addVertex(referencedClass);
					dependentClassesGraph.addEdge(currentClass, referencedClass);
					if (!visitedClasses.contains(referencedClass)) {
						currentClasses.add(referencedClass);
					}
				}
			}
		}
		exportGraph(dependentClassesGraph, VertexNameProviders.CLASS_VERTEX_NAME_PROVIDER,
				rootedClassDependencyGraphFile);
		getLog().debug(HR);

		return dependentClassesGraph;
	}

	private DirectedGraph<MArchive, DefaultEdge> createRootedArchiveDependencyGraph(
			final DirectedGraph<MClass, DefaultEdge> classDependencyGraph) {
		getLog().debug("Building rooted archive graph.");
		final DirectedGraph<MArchive, DefaultEdge> graph = new DefaultDirectedGraph<MArchive, DefaultEdge>(
				DefaultEdge.class);

		for (final MClass _class : classDependencyGraph.vertexSet()) {
			MArchive archive = _class.getPackage().getArchive();
			graph.addVertex(archive);
			for (DefaultEdge edge : classDependencyGraph.outgoingEdgesOf(_class)) {
				final MClass _referencedClass = classDependencyGraph.getEdgeTarget(edge);
				final MArchive referencedArchive = _referencedClass.getPackage().getArchive();
				graph.addVertex(referencedArchive);
				if (!referencedArchive.equals(archive)) {
					graph.addEdge(archive, referencedArchive);
				}
			}
		}
		exportGraph(graph, VertexNameProviders.ARCHIVE_VERTEX_NAME_PROVIDER, rootedArchiveDependencyGraphFile);
		getLog().debug(HR);
		return graph;
	}

	private <V, E> void exportGraph(final DirectedGraph<V, E> graph, VertexNameProvider<V> vertexNameProvider,
			File targetFile) {
		if (targetFile == null) {
			return;
		}
		if (graph.vertexSet().isEmpty()) {
			getLog().warn("Graph is empty.");
			return;
		}
		try {
			new AutoGraphExporter<V, E>(graphVizDotFile, useBatik, batikHints).exportGraph(graph, vertexNameProvider, null, targetFile, getLog());
		} catch (IOException ioex) {
			getLog().error("Could not write the graph to the [" + targetFile.getAbsolutePath() + "].", ioex);
		}
	}

	protected void recount() {
		getLog().info(HR);
		recountArchiveDependencies(archiveDependencyGraph);
		recountDirectlyReferencedArchives(rootedArchiveDependencyGraph, rootArchives);
		recountTransitivelyReferencedArchives(rootedArchiveDependencyGraph, rootArchives);
		recountCoveredTransitivelyReferencedArtifacts(rootedArchiveDependencyGraph, rootArchives);
		recountUnusedArtifacts(archiveDependencyGraph, rootedArchiveDependencyGraph, rootArchives);
	}

	private void recountArchiveDependencies(final DirectedGraph<MArchive, DefaultEdge> archiveGraph) {
		if (getLog().isDebugEnabled()) {
			getLog().debug("Recounting archive dependencies.");

			for (MArchive archive : archiveGraph.vertexSet()) {

				getLog().debug(HR);
				final Artifact artifact = archive.getArtifact();
				getLog().debug("Artifact " + artifact.toString() + ".");
				if (artifact.getFile() == null) {
					getLog().debug("Artifact file is null.");
				} else {
					getLog().debug("Artifact file [" + artifact.getFile().getAbsolutePath() + "].");

				}
				final Set<DefaultEdge> incomingEdges = archiveGraph.incomingEdgesOf(archive);
				if (incomingEdges.isEmpty()) {
					getLog().debug("No other artifact depends on this artifact.");
				} else {
					getLog().debug("Following artifacts depends on this artifact:");
					for (DefaultEdge edge : incomingEdges) {
						final MArchive source = archiveGraph.getEdgeSource(edge);
						getLog().debug("  " + source.getArtifact().getId());
					}
				}
				final Set<DefaultEdge> outgoingEdges = archiveGraph.outgoingEdgesOf(archive);
				if (outgoingEdges.isEmpty()) {
					getLog().debug("This artifact does not depend on other artifacts.");
				} else {
					getLog().debug("This artifact depends on following artifacts:");
					for (DefaultEdge edge : outgoingEdges) {
						final MArchive target = archiveGraph.getEdgeTarget(edge);
						getLog().debug("  " + target.getArtifact().getId());
					}
				}
			}
		}
		getLog().debug(HR);
	}

	private void recountDirectlyReferencedArchives(
			final DirectedGraph<MArchive, DefaultEdge> rootedArchiveDependencyGraph,
			final Collection<MArchive> rootArchives) {
		getLog().info("Recounting directly referenced artifacts.");
		for (final MArchive rootArchive : rootArchives) {
			getLog().info("  " + rootArchive.getArtifact().getId());
			if (rootedArchiveDependencyGraph.containsVertex(rootArchive)) {
				for (DefaultEdge edge : rootedArchiveDependencyGraph.outgoingEdgesOf(rootArchive)) {
					final MArchive referencedArchive = rootedArchiveDependencyGraph.getEdgeTarget(edge);
					getLog().info("    " + referencedArchive.getArtifact().getId());
				}
			}
		}
		getLog().info(HR);
	}

	private void recountTransitivelyReferencedArchives(
			final DirectedGraph<MArchive, DefaultEdge> rootedArchiveDependencyGraph,
			final Collection<MArchive> rootArchives) {
		final DescendantsInspector<MArchive, DefaultEdge> rootedArchiveDependencyGraphConnectivityInspector = new DescendantsInspector<MArchive, DefaultEdge>(
				rootedArchiveDependencyGraph);
		getLog().info("Recounting transitively referenced artifacts.");
		for (final MArchive rootArchive : rootArchives) {

			getLog().info("  " + rootArchive.getArtifact().getId());
			if (rootedArchiveDependencyGraph.containsVertex(rootArchive)) {
				for (MArchive referencedArchive : rootedArchiveDependencyGraphConnectivityInspector
						.descendantsOf(rootArchive)) {
					getLog().info("    " + referencedArchive.getArtifact().getId());
				}
			}
		}
		getLog().info(HR);
	}

	private void recountUnusedArtifacts(final DirectedGraph<MArchive, DefaultEdge> archiveDependencyGraph,
			final DirectedGraph<MArchive, DefaultEdge> rootedArchiveDependencyGraph,
			final Collection<MArchive> rootArchives) {
		getLog().info("Recounting unnecessary artifacts.");
		final DescendantsInspector<MArchive, DefaultEdge> archiveDependencyGraphConnectivityInspector1 = new DescendantsInspector<MArchive, DefaultEdge>(
				archiveDependencyGraph);
		final DescendantsInspector<MArchive, DefaultEdge> rootedArchiveDependencyGraphConnectivityInspector1 = new DescendantsInspector<MArchive, DefaultEdge>(
				rootedArchiveDependencyGraph);
		for (final MArchive rootArchive : rootArchives) {
			if (rootedArchiveDependencyGraph.containsVertex(rootArchive)) {
				final Set<MArchive> declared = new HashSet<MArchive>(archiveDependencyGraphConnectivityInspector1
						.descendantsOf(rootArchive));
				final Set<MArchive> required = new HashSet<MArchive>();
				for (MArchive referencedArchive : rootedArchiveDependencyGraphConnectivityInspector1
						.descendantsOf(rootArchive)) {
					required.add(referencedArchive);
				}
				declared.removeAll(required);
				if (declared.isEmpty()) {
					getLog().info(
							"Artifact tree of [" + rootArchive.getArtifact() + "] does not declare unused artifacts.");
				} else {
					getLog().info("Artifact tree of [" + rootArchive.getArtifact() + "] declares unused artifacts.");
					for (MArchive declaredUnusedArchive : declared) {
						getLog().info("  " + declaredUnusedArchive.getArtifact().getId());
					}
				}
			}
		}
		getLog().info(HR);
	}

	private void recountCoveredTransitivelyReferencedArtifacts(
			final DirectedGraph<MArchive, DefaultEdge> rootedArchiveDependencyGraph,
			final Collection<MArchive> rootArchives) {
		getLog().info("Recounting covered transitively referenced artifacts.");
		final DescendantsInspector<MArchive, DefaultEdge> rootedArchiveDependencyGraphConnectivityInspector =
				new DescendantsInspector<MArchive, DefaultEdge>(rootedArchiveDependencyGraph);
		for (final MArchive rootArchive : rootArchives) {
			if (rootedArchiveDependencyGraph.containsVertex(rootArchive)) {

				final List<MArchive> toCover = new ArrayList<MArchive>(
						rootedArchiveDependencyGraphConnectivityInspector.descendantsOf(rootArchive));

				final Set<MArchive> covered = new HashSet<MArchive>();

				final List<MArchive> roots = new LinkedList<MArchive>();

				while (!toCover.isEmpty()) {
					final MArchive toBeCovered = toCover.remove(0);
					if (!covered.contains(toBeCovered)) {
						final Set<MArchive> willBeCovered = rootedArchiveDependencyGraphConnectivityInspector
								.descendantsOf(toBeCovered);
						for (ListIterator<MArchive> iterator = roots.listIterator(); iterator.hasNext();) {
							final MArchive r = iterator.next();
							if (willBeCovered.contains(r)) {
								iterator.remove();
							}
						}
						covered.addAll(willBeCovered);
						roots.add(toBeCovered);
						covered.add(toBeCovered);
					}
				}
				getLog().info(
						"Transitively referenced artifacts for [" + rootArchive.getArtifact().getId()
								+ "] are covered by the following artifacts.");
				for (MArchive root : roots) {
					getLog().info("  " + root.getArtifact().getId());
				}
			}
		}
		getLog().info(HR);
	}

}
