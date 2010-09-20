package org.highsource.storyteller.jgrapht.ext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.VertexNameProvider;

public class AutoGraphExporter<V, E> implements GraphExporter<V, E> {

	private final Map<String, GraphExporter<V, E>> exporters;

	public AutoGraphExporter(final File graphVizDotFile) {
		final Map<String, GraphExporter<V, E>> exporters = new HashMap<String, GraphExporter<V, E>>();

		exporters.put("dot", new DOTGraphExporter<V, E>());
		exporters.put("gml", new GMLGraphExporter<V, E>());
		exporters.put("graphml", new GraphMLGraphExporter<V, E>());
		exporters.put("pdf", new PDFGraphExporter<V, E>(graphVizDotFile));
		exporters.put("png", new PNGGraphExporter<V, E>(graphVizDotFile));
		this.exporters = exporters;
	}

	public Map<String, GraphExporter<V, E>> getExporters() {
		return Collections.unmodifiableMap(this.exporters);
	}

	public void exportGraph(DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile, Log log)
			throws IOException {

		final String name = targetFile.getName();
		final int lastIndexOfDot = name.lastIndexOf('.');
		final String extension = (0 <= lastIndexOfDot && lastIndexOfDot < (name
				.length() - 1)) ? name.substring(lastIndexOfDot + 1) : null;

		final GraphExporter<V, E> exporter = getExporters().get(extension);

		if (exporter != null) {
			exporter.exportGraph(graph, vertexNameProvider, targetFile, log);
		} else {
			log.warn("Could not find graph exporter for the [" + extension
					+ "] file extension.");
		}
	}

}
