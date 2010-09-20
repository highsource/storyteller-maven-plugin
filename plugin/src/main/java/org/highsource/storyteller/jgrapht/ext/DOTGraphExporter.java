package org.highsource.storyteller.jgrapht.ext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.plugin.logging.Log;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

public class DOTGraphExporter<V, E> implements GraphExporter<V, E> {

	public void exportGraph(DirectedGraph<V, E> graph,
			VertexNameProvider<V> vertexNameProvider, File targetFile, Log log)
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

}
