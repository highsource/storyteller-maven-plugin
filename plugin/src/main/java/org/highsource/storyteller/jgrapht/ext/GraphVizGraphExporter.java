package org.highsource.storyteller.jgrapht.ext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.maven.plugin.logging.Log;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

public class GraphVizGraphExporter<V, E> implements GraphExporter<V, E> {

	private final String graphVizDotFile;
	private final String format;

	public GraphVizGraphExporter(String graphVizDotFile, String format) {
		super();
		this.graphVizDotFile = graphVizDotFile;
		this.format = format;
	}

	public void exportGraph(DirectedGraph<V, E> graph, VertexNameProvider<V> vertexLabelProvider,
			EdgeNameProvider<E> edgeLabelProvider, File targetFile, Log log) throws IOException {

		if (graphVizDotFile == null) {
			log.warn("Could not export graph to [" + targetFile.getAbsolutePath() + "], "
					+ "location of the GraphViz [dot] executable must be specified in the [graphVizDotFile] property.");
			return;
		}

		final DOTExporter<V, E> exporter = new DOTExporter<V, E>(new IntegerNameProvider<V>(), vertexLabelProvider,
				edgeLabelProvider);
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
		final String[] command = { graphVizDotFile, "-o", targetFile.getAbsolutePath(), "-T" + format,
				dotFile.getAbsolutePath() };
		final Process process = Runtime.getRuntime().exec(command);

		final InputStream inputStream = process.getInputStream();
		final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			log.debug(line);
		}

		try {
			final int exitValue = process.waitFor();
			if (exitValue != 0) {
				log.warn("GraphViz [dot] process quit with exit value [" + exitValue + "].");
			}
		} catch (InterruptedException iex) {
			log.warn("GraphViz [dot] process was interrupted.", iex);
		}
		dotFile.delete();
	}
}
