package org.highsource.storyteller.jgrapht.ext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.maven.plugin.logging.Log;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

public abstract class GraphVizGraphExporter<V, E> implements
		GraphExporter<V, E> {
	private final File graphVizDotFile;

	/**
	 * The plugin uses GraphViz package to render graphs in formats like PDF and
	 * so on. For this to work, you'll need to specify the path to the
	 * executable <code>dot</code> of GraphViz in this property.
	 */
	@MojoParameter(expression = "${graphViz.dotFile}")
	public File getGraphVizDotFile() {
		return graphVizDotFile;
	}

	public GraphVizGraphExporter(File graphVizDotFile) {
		super();
		this.graphVizDotFile = graphVizDotFile;
	}

	protected abstract String getFormat();

	public void exportGraph(DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile, Log log)
			throws IOException {

		if (getGraphVizDotFile() == null) {
			log
					.warn("Could not export graph to ["
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
						"-T" + getFormat(), dotFile.getAbsolutePath() });

		final InputStream inputStream = process.getInputStream();
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			log.debug(line);
		}

		try {
			final int exitValue = process.waitFor();
			if (exitValue != 0) {
				log.warn("GraphViz [dot] process quit with exit value ["
						+ exitValue + "].");
			}
		} catch (InterruptedException iex) {
			log.warn("GraphViz [dot] prcoessess was interrupted.", iex);
		}
		dotFile.delete();

	}
}
