package org.highsource.storyteller.jung.algorithms.rank.tests;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.highsource.storyteller.jung.algorithms.layout.SimpleRankedLayoutTransformer;
import org.highsource.storyteller.jung.algorithms.rank.BreadthFirstRanker;
import org.highsource.storyteller.jung.algorithms.rank.Rank;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class DotRankLayoutDemo {

	public static void main(String[] args) {
		final DirectedGraph<String, String> dag = new DirectedSparseGraph<String, String>();

		dag.addVertex("a");
		dag.addVertex("b");
		dag.addVertex("c");
		dag.addVertex("d");
		dag.addVertex("e");
		dag.addVertex("f");
		dag.addVertex("g");
		dag.addVertex("h");

		dag.addEdge("a-b", "a", "b");
		dag.addEdge("a-e", "a", "e");
		dag.addEdge("a-f", "a", "f");

		dag.addEdge("b-c", "b", "c");

		dag.addEdge("c-d", "c", "d");

		dag.addEdge("d-h", "d", "h");

		dag.addEdge("e-g", "e", "g");

		dag.addEdge("f-g", "f", "g");

		dag.addEdge("g-h", "g", "h");

		final Rank<String, String> rank = rank(dag);

		final Dimension dimension = new Dimension(1920, 1080);
		final Layout<String, String> layout =

		new StaticLayout<String, String>(dag,
				new SimpleRankedLayoutTransformer<String, String>(rank,
						dimension, 100, 100), dimension);

		layout.setSize(dimension);

		BasicVisualizationServer<String, String> vv = new BasicVisualizationServer<String, String>(
				layout);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderContext().setEdgeLabelTransformer(
				new Transformer<String, String>() {

					@Override
					public String transform(String arg0) {
						return "";
					}
				});
		// vv.setSize(width, height);

		final JFrame frame = new JFrame();
		frame.getContentPane().add(vv);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

	private static Rank<String, String> rank(
			final DirectedGraph<String, String> graph) {
		final Rank<String, String> rank = new BreadthFirstRanker<String, String>()
				.rank(graph);
		// new OptimalFeasibleTreeReranker<String, String>().rerank(rank);
		// new GreedyBalancingReranker<String, String>().rerank(rank);
		return rank;
	}

}
