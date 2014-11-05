package org.highsource.storyteller.jgrapht.ext;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;

public interface GraphExporter<V, E> {

	public void exportGraph(final DirectedGraph<V, E> graph, VertexNameProvider<V> vertexLabelProvider,
			EdgeNameProvider<E> edgeLabelProvider, File targetFile, Log log) throws IOException;

}
