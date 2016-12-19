package org.highsource.storyteller.jung.algorithms.layout.tests;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.apache.commons.collections15.functors.ConstantTransformer;
import org.highsource.storyteller.jung.algorithms.layout.SugiyamaLayout;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class SugiyamaLayoutDemo {

	public static void main(String[] args) {
		final DirectedGraph<String, Object> digraph = new DirectedSparseGraph<String, Object>();

		digraph.addVertex("a");
		digraph.addVertex("b");
		digraph.addVertex("c");
		digraph.addVertex("d");
		digraph.addVertex("e");

		digraph.addEdge(new Object(), "e", "d");
		digraph.addEdge(new Object(), "e", "c");
		digraph.addEdge(new Object(), "e", "b");
		digraph.addEdge(new Object(), "e", "a");

		digraph.addEdge(new Object(), "d", "c");
		digraph.addEdge(new Object(), "d", "b");

		digraph.addEdge(new Object(), "c", "b");
		digraph.addEdge(new Object(), "c", "a");

		digraph.addEdge(new Object(), "b", "a");

		final Layout<String, Object> layout = new SugiyamaLayout<String, Object>(
				digraph);

		layout.setSize(new Dimension(1920, 1080));

		BasicVisualizationServer<String, Object> vv = new BasicVisualizationServer<String, Object>(
				layout);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderContext().setEdgeLabelTransformer(
				ConstantTransformer.getInstance(""));
		// vv.setSize(width, height);

		final JFrame frame = new JFrame();
		frame.getContentPane().add(vv);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

}
