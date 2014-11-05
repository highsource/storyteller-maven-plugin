package org.highsource.storyteller.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.highsource.storyteller.artifact.graph.VersionedEdge;
import org.highsource.storyteller.artifact.graph.ext.EdgeNameProviders;
import org.highsource.storyteller.artifact.graph.ext.VertexNameProviders;
import org.highsource.storyteller.jgrapht.ext.AutoGraphExporter;
import org.highsource.storyteller.jgrapht.ext.GraphExporter;

/**
 * Export the dependency graph of the current project, or of a specified artifact.
 * @goal export-dependency-graph
 * @phase verify
 * @requiresDependencyResolution test
 * @requiresProject false
 */
public class ExportDependencyGraphMojo extends AbstractSpecifiableArtifactDependencyGraphMojo {

	/**
	 * File to export the dependency graph to. Format of the exported graph will be inferred from the file's
	 * extension. (choose from: pdf, svg, png, dot, gml, graphml)
	 * @parameter expression="${file}" default-value="dependencies.graphml"
	 * @required
	 */
	private File file;

	/**
	 * The plugin uses GraphViz package to render graphs in formats like PDF and so on. If the <code>dot</code>
	 * executable is not in PATH, it can be specified manually here.
	 * @parameter expression="${graphViz.dotFile}" default-value="dot"
	 */
	protected String graphVizDotFile;
	
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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		// Create a graph exporter
		GraphExporter<Artifact, VersionedEdge> graphExporter = new AutoGraphExporter<Artifact, VersionedEdge>(
				graphVizDotFile, useBatik, batikHints);
		// Export archive dependency graph
		try {
			graphExporter.exportGraph(artifactGraph, VertexNameProviders.ARTIFACT_VERTEX_NAME_PROVIDER, EdgeNameProviders.VERSION_EDGE_NAME_PROVIDER, file, getLog());
		} catch (IOException ioex) {
			throw new MojoExecutionException("Error exporting graph.", ioex);
		}

	}

}
