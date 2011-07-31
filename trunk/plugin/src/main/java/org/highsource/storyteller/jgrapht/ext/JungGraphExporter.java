package org.highsource.storyteller.jgrapht.ext;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;
import org.apache.maven.plugin.logging.Log;
import org.highsource.storyteller.jung.algorithms.layout.SugiyamaLayout;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.VertexNameProvider;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public abstract class JungGraphExporter<V, E> implements GraphExporter<V, E> {

	@Override
	public void exportGraph(DirectedGraph<V, E> digraph,
			final VertexNameProvider<V> vertexNameProvider, File targetFile,
			Log log) throws IOException {

		final edu.uci.ics.jung.graph.DirectedGraph<V, E> graph = new DirectedSparseGraph<V, E>();
		// new GraphAdapter<V, E>(digraph);

		for (V vertex : digraph.vertexSet()) {
			graph.addVertex(vertex);
		}

		for (E edge : digraph.edgeSet()) {
			graph.addEdge(edge, digraph.getEdgeSource(edge),
					digraph.getEdgeTarget(edge));
		}

		int scale = 1;
		int width = 1920 * scale;
		int height = 1080 * scale;

		// MinimumSpanningForest2<V, E> prim = new MinimumSpanningForest2<V, E>(
		// graph, new DelegateForest<V, E>(),
		// , );

		// @SuppressWarnings("unchecked")
		// final Transformer<E, Double> constantTransformer = (Transformer<E,
		// Double>) ConstantTransformer
		// .getInstance(1.0);
		// final Forest<V, E> forest = new MinimumSpanningForest2<V, E>(graph,
		// new DelegateForest<V, E>(), DelegateTree.<V, E> getFactory(),
		// constantTransformer).getForest();

		Layout<V, E> layout = new SugiyamaLayout<V, E>(graph);
		// new StaticLayout<V, E>(graph,
		// new TreeLayout<V, E>(forest));

		Dimension preferredSize = new Dimension(width, height);
		layout.setSize(preferredSize);

		VisualizationImageServer<V, E> visualisationServer = new VisualizationImageServer<V, E>(
				layout, preferredSize);
		visualisationServer.getRenderContext().setVertexLabelTransformer(

		new Transformer<V, String>() {
			@Override
			public String transform(V vertex) {
				return vertexNameProvider.getVertexName(vertex);
			}
		});
		visualisationServer.getRenderContext().setEdgeLabelTransformer(
				new Transformer<E, String>() {
					@Override
					public String transform(E edge) {
						return "";
					}
				});

		Image image = visualisationServer.getImage(new Point(width / 2,
				height / 2), preferredSize);
		// BufferedImage bufImage = ScreenImage.createImage((JComponent)
		// jPanel1);
		// File outFile = new File(imageFileName);

		OutputStream os = null;
		try {
			os = new FileOutputStream(targetFile);
			ImageIO.write((BufferedImage) image, getFormat(), targetFile);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	protected abstract String getFormat();

}
