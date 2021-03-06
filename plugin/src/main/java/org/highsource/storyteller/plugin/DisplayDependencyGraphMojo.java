package org.highsource.storyteller.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.highsource.storyteller.artifact.graph.VersionedEdge;
import org.jgrapht.DirectedGraph;

/**
 * Display the dependency graph of the current project, or of a specified artifact.
 * @goal display-dependency-graph
 * @phase verify
 * @requiresDependencyResolution test
 * @requiresProject false
 */
public class DisplayDependencyGraphMojo extends AbstractSpecifiableArtifactDependencyGraphMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		super.execute();
		final StringWriter sw = new StringWriter();
		displayGraph(this.artifactGraph, project.getArtifact(), "", true, new PrintWriter(sw));

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(sw.toString()));

			String line;

			while ((line = reader.readLine()) != null) {
				getLog().info(line);
			}

			reader.close();
		} catch (IOException ioex) {
			throw new MojoExecutionException("Error displaying dependency graph.", ioex);
		} finally {
			IOUtil.close(reader);
		}
	}

	public static String NODE_INDENT = "+- ";
	public static String LAST_NODE_INDENT = "\\- ";
	public static String FILL_INDENT = "|  ";
	public static String LAST_FILL_INDENT = "   ";

	public void displayGraph(DirectedGraph<Artifact, VersionedEdge> graph, Artifact node, String indent, boolean last,
			PrintWriter writer) {
		writer.print(indent);
		writer.print(last ? LAST_NODE_INDENT : NODE_INDENT);
		writer.println(node.getId());

		final Set<VersionedEdge> outgoingEdges = graph.outgoingEdgesOf(node);

		for (final Iterator<VersionedEdge> outgoingEdgesIterator = outgoingEdges.iterator(); outgoingEdgesIterator
				.hasNext();) {

			final VersionedEdge edge = outgoingEdgesIterator.next();

			final Artifact childNode = graph.getEdgeTarget(edge);

			displayGraph(graph, childNode, indent + (last ? LAST_FILL_INDENT : FILL_INDENT), !outgoingEdgesIterator
					.hasNext(), writer);
		}

	}

}
