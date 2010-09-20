package org.highsource.storyteller.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;
import javassist.CtClass;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
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
import org.highsource.storyteller.artifact.MPackage;
import org.highsource.storyteller.artifact.graph.ArtifactGraphBuilder;
import org.highsource.storyteller.artifact.graph.alg.DescendantsInspector;
import org.highsource.storyteller.artifact.graph.ext.VertexNameProviders;
import org.highsource.storyteller.io.NestedIOException;
import org.highsource.storyteller.jgrapht.ext.GmlExporter;
import org.highsource.storyteller.plexus.logging.LogToLoggerAdapter;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoPhase;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.xml.sax.SAXException;

@MojoGoal("recount")
@MojoRequiresDependencyResolution("test")
@MojoPhase("verify")
public class RecountMojo extends AbstractMojo {

	private static final String HR = "------------------------------------------------------------------------------------------";

	private MavenProject project;

	private ArtifactResolver artifactResolver;

	private ArtifactMetadataSource artifactMetadataSource;

	private ArtifactFactory artifactFactory;

	private ArtifactRepository localRepository;

	private MavenProjectBuilder mavenProjectBuilder;

	@MojoParameter(expression = "${project}", required = true, readonly = true)
	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	/**
	 * Used internally.
	 */
	@MojoComponent
	public ArtifactResolver getArtifactResolver() {
		return artifactResolver;
	}

	public void setArtifactResolver(ArtifactResolver artifactResolver) {
		this.artifactResolver = artifactResolver;
	}

	/**
	 * Used internally.
	 */
	@MojoComponent
	public ArtifactMetadataSource getArtifactMetadataSource() {
		return artifactMetadataSource;
	}

	public void setArtifactMetadataSource(
			ArtifactMetadataSource artifactMetadataSource) {
		this.artifactMetadataSource = artifactMetadataSource;
	}

	/**
	 * Used internally.
	 */
	@MojoComponent
	public ArtifactFactory getArtifactFactory() {
		return artifactFactory;
	}

	public void setArtifactFactory(ArtifactFactory artifactFactory) {
		this.artifactFactory = artifactFactory;
	}

	/**
	 * Used internally.
	 */
	@MojoParameter(expression = "${localRepository}", required = true, readonly = true)
	public ArtifactRepository getLocalRepository() {
		return localRepository;
	}

	public void setLocalRepository(ArtifactRepository localRepository) {
		this.localRepository = localRepository;
	}

	/**
	 * Artifact factory, needed to download source jars.
	 */
	@MojoComponent(role = "org.apache.maven.project.MavenProjectBuilder")
	public MavenProjectBuilder getMavenProjectBuilder() {
		return mavenProjectBuilder;
	}

	public void setMavenProjectBuilder(MavenProjectBuilder mavenProjectBuilder) {
		this.mavenProjectBuilder = mavenProjectBuilder;
	}

	private ArtifactGraphBuilder artifactGraphBuilder;

	/**
	 * Used internally.
	 */
	@MojoComponent
	public ArtifactGraphBuilder getArtifactGraphBuilder() {
		return artifactGraphBuilder;
	}

	public void setArtifactGraphBuilder(
			ArtifactGraphBuilder artifactGraphBuilder) {
		this.artifactGraphBuilder = artifactGraphBuilder;
	}

	private File archiveDependencyGraphFile;

	/**
	 * If specified, the plugin renders archive dependency graph into this file.
	 */
	@MojoParameter(expression = "${archiveDependencyGraphFile}", defaultValue = "target/storyteller/archiveDependencyGraphFile.gml")
	public File getArchiveDependencyGraphFile() {
		return archiveDependencyGraphFile;
	}

	public void setArchiveDependencyGraphFile(File artifactGraphFile) {
		this.archiveDependencyGraphFile = artifactGraphFile;
	}

	private File classDependencyGraphFile;

	/**
	 * If specified, the plugin renders class dependency graph into this file.
	 */
	@MojoParameter(expression = "${classDependencyGraphFile}", defaultValue = "target/storyteller/classDependencyGraph.gml")
	public File getClassDependencyGraphFile() {
		return classDependencyGraphFile;
	}

	public void setClassDependencyGraphFile(File classGraphFile) {
		this.classDependencyGraphFile = classGraphFile;
	}

	private File rootedClassDependencyGraphFile;

	/**
	 * If specified, the plugin renders "rooted" class dependency graph into
	 * this file.
	 */
	@MojoParameter(expression = "${rootedClassDependencyGraphFile}", defaultValue = "target/storyteller/rootedClassDependencyGraph.gml")
	public File getRootedClassDependencyGraphFile() {
		return rootedClassDependencyGraphFile;
	}

	public void setRootedClassDependencyGraphFile(File dependentClassGraphFile) {
		this.rootedClassDependencyGraphFile = dependentClassGraphFile;
	}

	private File rootedArchiveDependencyGraphFile;

	/**
	 * If specified, the plugin renders "rooted" archive dependency graph into
	 * this file.
	 */
	@MojoParameter(expression = "${rootedArchiveDependencyGraphFile}", defaultValue = "target/storyteller/rootedArchiveDependencyGraph.pdf")
	public File getRootedArchiveDependencyGraphFile() {
		return rootedArchiveDependencyGraphFile;
	}

	public void setRootedArchiveDependencyGraphFile(
			File dependentArchiveGraphFile) {
		this.rootedArchiveDependencyGraphFile = dependentArchiveGraphFile;
	}

	private File graphVizDotFile;

	/**
	 * The plugin uses GraphViz package to render graphs in formats like PDF and
	 * so on. For this to work, you'll need to specify the path to the
	 * executable <code>dot</code> of GraphViz in this property.
	 */
	@MojoParameter(expression = "${graphViz.dotFile}")
	public File getGraphVizDotFile() {
		return graphVizDotFile;
	}

	public void setGraphVizDotFile(File dot) {
		this.graphVizDotFile = dot;
	}

	private Set<Artifact> dependencyArtifacts;

	private DirectedGraph<Artifact, DefaultEdge> artifactGraph;

	private Map<Artifact, MArchive> archives;

	private DirectedGraph<MArchive, DefaultEdge> archiveDependencyGraph;

	private DirectedGraph<MClass, DefaultEdge> classDependencyGraph;

	private Collection<MArchive> rootArchives;

	private Collection<MClass> rootClasses;

	private DirectedGraph<MClass, DefaultEdge> rootedClassDependencyGraph;

	private DirectedGraph<MArchive, DefaultEdge> rootedArchiveDependencyGraph;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug(HR);
		// Get the artifacts for dependencies
		this.dependencyArtifacts = createDependencyArtifacts();
		// Build the artifact graph
		// Artifacts are also resolved on this step
		this.artifactGraph = buildArtifactDependencyGraph(dependencyArtifacts);
		// Create archives for artifacts
		this.archives = createArchives(artifactGraph.vertexSet());
		// Create an archive dependency graph out of artifact dependency graph
		this.archiveDependencyGraph = buildArchiveDependencyGraph(
				artifactGraph, archives);
		// Build class dependency graph
		this.classDependencyGraph = buildClassDependencyGraph(archives);
		// Get root archives
		this.rootArchives = getRootArchives(dependencyArtifacts, archives);
		// Get root classes
		this.rootClasses = getRootClasses(rootArchives);
		// Create graph of classes reachable from classes of root archives
		this.rootedClassDependencyGraph = buildRootedClassDependencyGraph(
				classDependencyGraph, rootClasses);
		this.rootedArchiveDependencyGraph = createRootedArchiveDependencyGraph(rootedClassDependencyGraph);
		// Finally, recount on everything
		recount();
	}

	/**
	 * Creates dependency artifacts for the current project.
	 * 
	 * @return Set of artifacts for the dependencies of the current project.
	 * @throws MojoExecutionException
	 *             In case of invalid dependency version.
	 */
	protected Set<Artifact> createDependencyArtifacts()
			throws MojoExecutionException {
		getLog().debug("Creating dependency artifacts.");

		MavenProject project = getProject();
		getLog().info("Project packaging is [" + project.getPackaging() + "]");
		return createDependencyArtifacts(project);
	}

	private Set<Artifact> createDependencyArtifacts(MavenProject project)
			throws MojoExecutionException {
		try {
			@SuppressWarnings("unchecked")
			final Set<Artifact> dependencyArtifacts = MavenMetadataSource
					.createArtifacts(getArtifactFactory(), project
							.getDependencies(), "compile", null, project);
			getLog().debug(HR);
			return dependencyArtifacts;
		} catch (InvalidDependencyVersionException idvex) {
			throw new MojoExecutionException(
					"Could not create artifacts for dependencies.", idvex);
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
	protected DirectedGraph<Artifact, DefaultEdge> buildArtifactDependencyGraph(
			Set<Artifact> artifacts) throws MojoExecutionException {
		getLog().debug("Building artifact graph.");
		try {
			@SuppressWarnings("unchecked")
			final List<ArtifactRepository> remoteArtifactRepositories = project
					.getRemoteArtifactRepositories();

			final DirectedGraph<Artifact, DefaultEdge> graph = getArtifactGraphBuilder()
					.buildArtifactGraph(artifacts, project.getArtifact(),
							Collections.EMPTY_MAP, getLocalRepository(),
							remoteArtifactRepositories,
							getArtifactMetadataSource(), null, null,
							new LogToLoggerAdapter("", getLog()));

			getLog().debug(HR);
			return graph;
		} catch (ArtifactResolutionException arex) {
			throw new MojoExecutionException(
					"Error resolving dependency artifacts.", arex);
		} catch (ArtifactNotFoundException anfex) {
			throw new MojoExecutionException("Artifact could not be found.",
					anfex);
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
			final DirectedGraph<Artifact, DefaultEdge> artifactGraph,
			Map<Artifact, MArchive> archives) throws MojoExecutionException {

		getLog().debug("Building archive dependency graph.");

		final DirectedGraph<MArchive, DefaultEdge> archiveGraph = new DefaultDirectedGraph<MArchive, DefaultEdge>(
				DefaultEdge.class);
		for (Entry<Artifact, MArchive> entry : archives.entrySet()) {
			final MArchive archive = entry.getValue();
			archiveGraph.addVertex(archive);
			for (DefaultEdge artifactEdge : artifactGraph
					.incomingEdgesOf(archive.getArtifact())) {
				final Artifact sourceArtifact = artifactGraph
						.getEdgeSource(artifactEdge);
				final MArchive sourceArchive = archives.get(sourceArtifact);
				if (sourceArchive != null) {
					archiveGraph.addVertex(sourceArchive);
					archiveGraph.addEdge(sourceArchive, archive);
				}
			}

			for (DefaultEdge artifactEdge : artifactGraph
					.outgoingEdgesOf(archive.getArtifact())) {
				final Artifact targetArtifact = artifactGraph
						.getEdgeTarget(artifactEdge);
				final MArchive targetArchive = archives.get(targetArtifact);
				if (targetArchive != null) {
					archiveGraph.addVertex(targetArchive);
					archiveGraph.addEdge(archive, targetArchive);
				}
			}
		}
		exportGraph(archiveGraph,
				VertexNameProviders.ARCHIVE_VERTEX_NAME_PROVIDER,
				getArchiveDependencyGraphFile());
		getLog().debug(HR);
		return archiveGraph;
	}

	private Map<Artifact, MArchive> createArchives(
			Collection<Artifact> artifacts) throws MojoExecutionException {
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

	private Collection<MArchive> getRootArchives(
			final Set<Artifact> dependencyArtifacts,
			final Map<Artifact, MArchive> archives) {
		getLog().debug("Retrieving root archives.");
		final Collection<MArchive> rootArchives = getRootArchives(
				dependencyArtifacts, archives, getProject());

		getLog().debug("Root archives.");
		for (MArchive rootArchive : rootArchives) {
			getLog().debug("  " + rootArchive.getArtifact().getId() + "].");
		}
		getLog().debug(HR);
		return rootArchives;
	}

	private Collection<MArchive> getRootArchives(
			final Set<Artifact> dependencyArtifacts,
			final Map<Artifact, MArchive> archives, MavenProject project) {
		final Collection<MArchive> rootArchives;
		if (archives.containsKey(project.getArtifact())) {
			final MArchive archive = archives.get(project.getArtifact());
			if (!archive.getPackages().isEmpty()) {
				getLog()
						.debug(
								"Project artifact is found in graph and it contains packages. Using it as the root archive.");
				rootArchives = Collections.<MArchive> singleton(archive);
			} else {
				getLog()
						.debug(
								"Project artifact is found in graph but it does not contain packages, using dependency artifacts as roots.");
				rootArchives = new ArrayList<MArchive>(dependencyArtifacts
						.size());
				for (final Artifact dependencyArtifact : dependencyArtifacts) {
					final MArchive dependencyArchive = archives
							.get(dependencyArtifact);
					if (dependencyArchive != null) {
						rootArchives.add(dependencyArchive);
					}
				}
			}
		} else {
			getLog()
					.debug(
							"Project artifact is not found in graph, using dependency artifacts as roots.");
			rootArchives = new ArrayList<MArchive>(dependencyArtifacts.size());
			for (final Artifact dependencyArtifact : dependencyArtifacts) {
				final MArchive dependencyArchive = archives
						.get(dependencyArtifact);
				if (dependencyArchive != null) {
					rootArchives.add(dependencyArchive);
				}
			}
		}
		return rootArchives;
	}

	private DirectedGraph<MClass, DefaultEdge> buildClassDependencyGraph(
			final Map<Artifact, MArchive> archives) {
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
				getLog().warn(
						"The class [" + className
								+ "] is present in more than one archive:");
				for (final MClass _class : _classes) {
					final MArchive archive = _class.getPackage().getArchive();
					final Artifact artifact = archive.getArtifact();
					getLog().warn(artifact.getId());
				}
			}

			for (final MClass _class : _classes) {
				for (final String referencedClassName : _class
						.getReferencedClassNames()) {
					classesGraph.addVertex(_class);
					final Collection<MClass> _referencedClasses = classes
							.get(referencedClassName);
					if (_referencedClasses != null) {
						for (final MClass _referencedClass : _referencedClasses) {
							classesGraph.addVertex(_referencedClass);
							classesGraph.addEdge(_class, _referencedClass);
						}
					}
				}
			}
		}
		exportGraph(classesGraph,
				VertexNameProviders.CLASS_VERTEX_NAME_PROVIDER,
				getClassDependencyGraphFile());
		getLog().debug(HR);
		return classesGraph;
	}

	private Collection<MClass> getRootClasses(
			final Collection<MArchive> rootArchives) {
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
			final DirectedGraph<MClass, DefaultEdge> classesGraph,
			final Collection<MClass> rootClasses) {

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
				for (DefaultEdge edge : classesGraph
						.outgoingEdgesOf(currentClass)) {
					final MClass referencedClass = classesGraph
							.getEdgeTarget(edge);
					dependentClassesGraph.addVertex(referencedClass);
					dependentClassesGraph
							.addEdge(currentClass, referencedClass);
					if (!visitedClasses.contains(referencedClass)) {
						currentClasses.add(referencedClass);
					}
				}
			}
		}
		exportGraph(dependentClassesGraph,
				VertexNameProviders.CLASS_VERTEX_NAME_PROVIDER,
				getRootedClassDependencyGraphFile());
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
			for (DefaultEdge edge : classDependencyGraph
					.outgoingEdgesOf(_class)) {
				final MClass _referencedClass = classDependencyGraph
						.getEdgeTarget(edge);
				final MArchive referencedArchive = _referencedClass
						.getPackage().getArchive();
				graph.addVertex(referencedArchive);
				if (!referencedArchive.equals(archive)) {
					graph.addEdge(archive, referencedArchive);
				}
			}
		}
		exportGraph(graph, VertexNameProviders.ARCHIVE_VERTEX_NAME_PROVIDER,
				getRootedArchiveDependencyGraphFile());
		getLog().debug(HR);
		return graph;
	}

	private <V, E> void exportGraph(final DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile) {
		if (targetFile == null) {
			return;
		}
		if (graph.vertexSet().isEmpty()) {
			getLog().warn("Graph is empty.");
			return;

		}
		try {
			if (targetFile.getAbsolutePath().endsWith(".dot")) {
				exportGraphAsDOT(graph, vertexNameProvider, targetFile);
			} else if (targetFile.getAbsolutePath().endsWith(".gml")) {
				exportGraphAsGML(graph, vertexNameProvider, targetFile);
			} else if (targetFile.getAbsolutePath().endsWith(".graphml")) {
				exportGraphAsGraphML(graph, vertexNameProvider, targetFile);
			} else if (targetFile.getAbsolutePath().endsWith(".pdf")) {
				exportGraphAsPDF(graph, vertexNameProvider, targetFile);
			} else if (targetFile.getAbsolutePath().endsWith(".png")) {
				exportGraphAsPNG(graph, vertexNameProvider, targetFile);
			} else {
				getLog().warn(
						"Could not export graph to ["
								+ targetFile.getAbsolutePath()
								+ "], unknown format.");
			}
		} catch (IOException ioex) {
			getLog().error(
					"Could not write the graph to the ["
							+ targetFile.getAbsolutePath() + "].", ioex);
		}
	}

	private <V, E> void exportGraphAsPDF(final DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile)
			throws IOException {
		if (getGraphVizDotFile() == null) {
			getLog()
					.warn(
							"Could not export graph to ["
									+ targetFile.getAbsolutePath()
									+ "], "
									+ "location of the GraphViz [dot] executable must be specified in the [graphVizDotFile] property.");
			return;
		}

		final DOTExporter<V, E> exporter = new DOTExporter<V, E>(
				new IntegerNameProvider<V>(), vertexNameProvider, null);
		final File dotFile = new File(targetFile.getAbsolutePath() + ".dot");
		dotFile.getParentFile().mkdirs();

		Writer writer = null;
		try {
			writer = new FileWriter(dotFile);
			exporter.export(writer, graph);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException x) {

				}
			}

		}
		final String command = getGraphVizDotFile().getAbsolutePath();
		final Process process = Runtime.getRuntime().exec(

				new String[] { command, "-o", targetFile.getAbsolutePath(),
						"-Tpdf", dotFile.getAbsolutePath() });

		final InputStream inputStream = process.getInputStream();
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			getLog().debug(line);
		}

		try {
			final int exitValue = process.waitFor();
			if (exitValue != 0) {
				getLog().warn(
						"GraphViz [dot] process quit with exit value ["
								+ exitValue + "].");
			}
		} catch (InterruptedException iex) {
			getLog().warn("GraphViz [dot] prcoessess was interrupted.", iex);
		}
		dotFile.delete();
	}

	private <V, E> void exportGraphAsPNG(final DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile)
			throws IOException {
		if (getGraphVizDotFile() == null) {
			getLog()
					.warn(
							"Could not export graph to ["
									+ targetFile.getAbsolutePath()
									+ "], "
									+ "location of the GraphViz [dot] executable must be specified in the [graphVizDotFile] property.");
			return;
		}

		final DOTExporter<V, E> exporter = new DOTExporter<V, E>(
				new IntegerNameProvider<V>(), vertexNameProvider, null);
		final File dotFile = new File(targetFile.getAbsolutePath() + ".dot");
		dotFile.getParentFile().mkdirs();

		Writer writer = null;
		try {
			writer = new FileWriter(dotFile);
			exporter.export(writer, graph);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException x) {

				}
			}

		}
		final String command = getGraphVizDotFile().getAbsolutePath();
		final Process process = Runtime.getRuntime().exec(

				new String[] { command, "-o", targetFile.getAbsolutePath(),
						"-Tpng", dotFile.getAbsolutePath() });

		final InputStream inputStream = process.getInputStream();
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			getLog().debug(line);
		}

		try {
			final int exitValue = process.waitFor();
			if (exitValue != 0) {
				getLog().warn(
						"GraphViz [dot] process quit with exit value ["
								+ exitValue + "].");
			}
		} catch (InterruptedException iex) {
			getLog().warn("GraphViz [dot] prcoessess was interrupted.", iex);
		}
		dotFile.delete();
	}

	private <V, E> void exportGraphAsDOT(final DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile)
			throws IOException {
		final DOTExporter<V, E> exporter = new DOTExporter<V, E>(
				new IntegerNameProvider<V>(), vertexNameProvider, null);
		Writer writer = null;
		try {
			targetFile.getParentFile().mkdirs();
			writer = new FileWriter(targetFile);
			exporter.export(writer, graph);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException x) {

				}
			}
		}
	}

	private <V, E> void exportGraphAsGraphML(final DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile)
			throws IOException {
		final GraphMLExporter<V, E> exporter = new GraphMLExporter<V, E>(
				new IntegerNameProvider<V>(), vertexNameProvider,
				new IntegerEdgeNameProvider<E>(), null);
		Writer writer = null;
		try {
			targetFile.getParentFile().mkdirs();
			writer = new FileWriter(targetFile);
			exporter.export(writer, graph);
		} catch (TransformerConfigurationException tcex) {
			throw new NestedIOException(tcex);
		} catch (SAXException saxex) {
			throw new NestedIOException(saxex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException x) {

				}
			}
		}
	}

	private <V, E> void exportGraphAsGML(final DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile)
			throws IOException {
		final GmlExporter<V, E> exporter = new GmlExporter<V, E>(
				new IntegerNameProvider<V>(), vertexNameProvider,
				new IntegerEdgeNameProvider<E>());
		exporter.setPrintLabels(GmlExporter.PRINT_VERTEX_LABELS);
		Writer writer = null;
		try {
			targetFile.getParentFile().mkdirs();
			writer = new FileWriter(targetFile);
			exporter.export(writer, graph);
			// } catch (TransformerConfigurationException tcex) {
			// throw new IOException(tcex);
			// } catch (SAXException saxex) {
			// throw new IOException(saxex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException x) {

				}
			}
		}
	}

	// private final ClassPool classPool = new ClassPool();

	private MArchive createArchive(final Artifact artifact)
			throws MojoExecutionException {
		final ClassPool classPool = new ClassPool();
		final File artifactFile = artifact.getFile();
		if (artifactFile != null
				&& artifactFile.getName().toLowerCase().endsWith(".jar")) {

			try {
				final MArchive archive = new MArchive(artifact);
				final JarFile artifactJarFile = new JarFile(artifactFile);
				final Enumeration<JarEntry> jarEntries = artifactJarFile
						.entries();

				while (jarEntries.hasMoreElements()) {
					final JarEntry jarEntry = jarEntries.nextElement();

					if (jarEntry.getName().endsWith(".class")) {
						InputStream is = null;
						CtClass ctClass = null;
						try {
							is = artifactJarFile.getInputStream(jarEntry);
							ctClass = classPool.makeClass(is);
						} catch (IOException ioex1) {
							throw new MojoExecutionException(
									"Could not load class from JAR entry ["
											+ artifactFile.getAbsolutePath()
											+ "/" + jarEntry.getName() + "].");
						} finally {
							try {
								if (is != null)
									is.close();
							} catch (IOException ignored) {
								// Ignore
							}
						}

						final String className = ctClass.getName();

						final MClass theClass = archive
								.getOrCreateClass(className);
						getLog().debug(
								"Processing class [" + theClass.getClassName()
										+ "].");

						@SuppressWarnings("unchecked")
						final Collection<String> referencedClassNames = ctClass
								.getRefClasses();
						for (String referencedClassName : referencedClassNames) {
							if (!className.equals(referencedClassName)) {
								theClass
										.addReferencedClassName(referencedClassName
												.intern());
								// getLog().debug(
								// "Class [" + className + "] depends on ["
								// + referencedClassName + "].");
							}
						}
					}
				}
				return archive;
			} catch (IOException ioex) {
				throw new MojoExecutionException("Could not analyze archive ["
						+ artifactFile.getAbsolutePath() + "].", ioex);
			}
		} else {
			return null;
		}
	}

	protected void recount() {
		getLog().info(HR);
		recountArchiveDependencies(archiveDependencyGraph);
		recountDirectlyReferencedArchives(rootedArchiveDependencyGraph,
				rootArchives);
		recountTransitivelyReferencedArchives(rootedArchiveDependencyGraph,
				rootArchives);
		recountCoveredTransitivelyReferencedArtifacts(
				rootedArchiveDependencyGraph, rootArchives);
		recountUnusedArtifacts(archiveDependencyGraph,
				rootedArchiveDependencyGraph, rootArchives);

	}

	private void recountArchiveDependencies(
			final DirectedGraph<MArchive, DefaultEdge> archiveGraph) {
		if (getLog().isDebugEnabled()) {
			getLog().debug("Recounting archive dependencies.");

			for (MArchive archive : archiveGraph.vertexSet()) {

				getLog().debug(HR);
				final Artifact artifact = archive.getArtifact();
				getLog().debug("Artifact " + artifact.toString() + ".");
				if (artifact.getFile() == null) {
					getLog().debug("Artifact file is null.");
				} else {
					getLog().debug(
							"Artifact file ["
									+ artifact.getFile().getAbsolutePath()
									+ "].");

				}
				final Set<DefaultEdge> incomingEdges = archiveGraph
						.incomingEdgesOf(archive);
				if (incomingEdges.isEmpty()) {
					getLog().debug(
							"No other artifact depends on this artifact.");
				} else {
					getLog().debug(
							"Following artifacts depends on this artifact:");
					for (DefaultEdge edge : incomingEdges) {
						final MArchive source = archiveGraph
								.getEdgeSource(edge);
						getLog().debug("  " + source.getArtifact().getId());
					}
				}
				final Set<DefaultEdge> outgoingEdges = archiveGraph
						.outgoingEdgesOf(archive);
				if (outgoingEdges.isEmpty()) {
					getLog()
							.debug(
									"This artifact does not depend on other artifacts.");
				} else {
					getLog().debug(
							"This artifact depends on following artifacts:");
					for (DefaultEdge edge : outgoingEdges) {
						final MArchive target = archiveGraph
								.getEdgeTarget(edge);
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
				for (DefaultEdge edge : rootedArchiveDependencyGraph
						.outgoingEdgesOf(rootArchive)) {
					final MArchive referencedArchive = rootedArchiveDependencyGraph
							.getEdgeTarget(edge);
					getLog().info(
							"    " + referencedArchive.getArtifact().getId());
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
					getLog().info(
							"    " + referencedArchive.getArtifact().getId());
				}
			}
		}
		getLog().info(HR);
	}

	private void recountUnusedArtifacts(
			final DirectedGraph<MArchive, DefaultEdge> archiveDependencyGraph,
			final DirectedGraph<MArchive, DefaultEdge> rootedArchiveDependencyGraph,
			final Collection<MArchive> rootArchives) {
		getLog().info("Recounting unnecessary artifacts.");
		final DescendantsInspector<MArchive, DefaultEdge> archiveDependencyGraphConnectivityInspector1 = new DescendantsInspector<MArchive, DefaultEdge>(
				archiveDependencyGraph);
		final DescendantsInspector<MArchive, DefaultEdge> rootedArchiveDependencyGraphConnectivityInspector1 = new DescendantsInspector<MArchive, DefaultEdge>(
				rootedArchiveDependencyGraph);
		for (final MArchive rootArchive : rootArchives) {
			if (rootedArchiveDependencyGraph.containsVertex(rootArchive)) {
				final Set<MArchive> declared = new HashSet<MArchive>(
						archiveDependencyGraphConnectivityInspector1
								.descendantsOf(rootArchive));
				final Set<MArchive> required = new HashSet<MArchive>();
				for (MArchive referencedArchive : rootedArchiveDependencyGraphConnectivityInspector1
						.descendantsOf(rootArchive)) {
					required.add(referencedArchive);
				}
				declared.removeAll(required);
				if (declared.isEmpty()) {
					getLog().info(
							"Artifact tree of [" + rootArchive.getArtifact()
									+ "] does not declare unused artifacts.");
				} else {
					getLog().info(
							"Artifact tree of [" + rootArchive.getArtifact()
									+ "] declares unused artifacts.");
					for (MArchive declaredUnusedArchive : declared) {
						getLog().info(
								"  "
										+ declaredUnusedArchive.getArtifact()
												.getId());
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
		final DescendantsInspector<MArchive, DefaultEdge> rootedArchiveDependencyGraphConnectivityInspector = new DescendantsInspector<MArchive, DefaultEdge>(
				rootedArchiveDependencyGraph);
		for (final MArchive rootArchive : rootArchives) {
			if (rootedArchiveDependencyGraph.containsVertex(rootArchive)) {

				final List<MArchive> toCover = new ArrayList<MArchive>(
						rootedArchiveDependencyGraphConnectivityInspector
								.descendantsOf(rootArchive));

				final Set<MArchive> covered = new HashSet<MArchive>();

				final List<MArchive> roots = new LinkedList<MArchive>();

				while (!toCover.isEmpty()) {
					final MArchive toBeCovered = toCover.remove(0);
					if (!covered.contains(toBeCovered)) {
						final Set<MArchive> willBeCovered = rootedArchiveDependencyGraphConnectivityInspector
								.descendantsOf(toBeCovered);
						for (ListIterator<MArchive> iterator = roots
								.listIterator(); iterator.hasNext();) {
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
						"Transitively referenced artifacts for ["
								+ rootArchive.getArtifact().getId()
								+ "] are covered by the following artifacts.");
				for (MArchive root : roots) {
					getLog().info("  " + root.getArtifact().getId());
				}
			}
		}

		getLog().info(HR);
	}

}
