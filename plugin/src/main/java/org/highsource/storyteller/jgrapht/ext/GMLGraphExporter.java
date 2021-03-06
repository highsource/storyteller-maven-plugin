package org.highsource.storyteller.jgrapht.ext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.plugin.logging.Log;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

public class GMLGraphExporter<V, E> implements GraphExporter<V, E> {

	public void exportGraph(DirectedGraph<V, E> graph, VertexNameProvider<V> vertexLabelProvider,
			EdgeNameProvider<E> edgeLabelProvider, File targetFile, Log log) throws IOException {
		final GmlExporter<V, E> exporter = new GmlExporter<V, E>(new IntegerNameProvider<V>(), vertexLabelProvider,
				edgeLabelProvider);
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

}
