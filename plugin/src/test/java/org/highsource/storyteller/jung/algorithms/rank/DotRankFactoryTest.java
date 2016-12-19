package org.highsource.storyteller.jung.algorithms.rank.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.highsource.storyteller.jung.algorithms.rank.BreadthFirstRanker;
import org.highsource.storyteller.jung.algorithms.rank.GreedyBalancingReranker;
import org.highsource.storyteller.jung.algorithms.rank.OptimalFeasibleTreeReranker;
import org.highsource.storyteller.jung.algorithms.rank.Rank;
import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class DotRankFactoryTest {

	@Test
	public void rank00()

	{
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
		assertEquals(4, rank.getRank("a"));

		assertEquals(3, rank.getRank("b"));
		assertEquals(3, rank.getRank("e"));
		assertEquals(3, rank.getRank("f"));

		assertEquals(2, rank.getRank("c"));
		assertEquals(2, rank.getRank("g"));

		assertEquals(1, rank.getRank("d"));

		assertEquals(0, rank.getRank("h"));
	}

	private Rank<String, String> rank(final DirectedGraph<String, String> graph) {
		final Rank<String, String> rank = new BreadthFirstRanker<String, String>()
				.rank(graph);
		new OptimalFeasibleTreeReranker<String, String>().rerank(rank);
		new GreedyBalancingReranker<String, String>().rerank(rank);
		return rank;
	}

	@Test
	public void rank01()

	{
		final DirectedGraph<String, String> dag = new DirectedSparseGraph<String, String>();

		dag.addVertex("a");
		dag.addVertex("b");
		dag.addVertex("c");
		dag.addVertex("d");
		dag.addVertex("e");
		dag.addVertex("f");
		dag.addVertex("g");
		dag.addVertex("h");
		dag.addVertex("i");
		dag.addVertex("j");

		dag.addEdge("a-b", "a", "b");
		dag.addEdge("a-e", "a", "e");
		dag.addEdge("a-f", "a", "f");
		dag.addEdge("a-i", "a", "i");
		dag.addEdge("a-j", "a", "j");

		dag.addEdge("b-c", "b", "c");

		dag.addEdge("c-d", "c", "d");

		dag.addEdge("d-h", "d", "h");

		dag.addEdge("e-g", "e", "g");

		dag.addEdge("f-g", "f", "g");

		dag.addEdge("g-h", "g", "h");

		dag.addEdge("i-j", "i", "j");

		final Rank<String, String> rank = rank(dag);

		assertEquals(4, rank.getRank("a"));

		assertEquals(3, rank.getRank("b"));
		assertEquals(3, rank.getRank("i"));
		assertEquals(3, rank.getRank("e"));
		assertEquals(3, rank.getRank("f"));

		assertEquals(2, rank.getRank("c"));
		assertEquals(2, rank.getRank("j"));
		assertEquals(2, rank.getRank("g"));

		assertEquals(1, rank.getRank("d"));

		assertEquals(0, rank.getRank("h"));
	}

	@Test
	public void rank03() throws IOException {
		final String text = IOUtils.toString(getClass().getResourceAsStream(
				"graph03.txt"));
		DirectedGraph<String, String> graph = createGraph(text);

		final Rank<String, String> rank = rank(graph);
		Assert.assertEquals(8, rank.getRank("S1"));
		Assert.assertEquals(8, rank.getRank("S35"));

		Assert.assertEquals(7, rank.getRank("S8"));
		Assert.assertEquals(7, rank.getRank("S24"));
		Assert.assertEquals(7, rank.getRank("37"));
		Assert.assertEquals(7, rank.getRank("43"));
		Assert.assertEquals(7, rank.getRank("36"));
		Assert.assertEquals(7, rank.getRank("10"));
		Assert.assertEquals(7, rank.getRank("2"));

		Assert.assertEquals(6, rank.getRank("9"));
		Assert.assertEquals(6, rank.getRank("25"));
		Assert.assertEquals(6, rank.getRank("38"));
		Assert.assertEquals(6, rank.getRank("40"));
		Assert.assertEquals(6, rank.getRank("13"));
		Assert.assertEquals(6, rank.getRank("17"));
		Assert.assertEquals(6, rank.getRank("S30"));
		Assert.assertEquals(6, rank.getRank("14"));

		Assert.assertEquals(5, rank.getRank("42"));
		Assert.assertEquals(5, rank.getRank("26"));
		Assert.assertEquals(5, rank.getRank("11"));
		Assert.assertEquals(5, rank.getRank("3"));
		Assert.assertEquals(5, rank.getRank("39"));
		Assert.assertEquals(5, rank.getRank("16"));
		Assert.assertEquals(5, rank.getRank("18"));
		Assert.assertEquals(5, rank.getRank("19"));
		Assert.assertEquals(5, rank.getRank("33"));

		Assert.assertEquals(4, rank.getRank("12"));
		Assert.assertEquals(4, rank.getRank("41"));
		Assert.assertEquals(4, rank.getRank("4"));
		Assert.assertEquals(4, rank.getRank("21"));
		Assert.assertEquals(4, rank.getRank("20"));
		Assert.assertEquals(4, rank.getRank("28"));
		Assert.assertEquals(4, rank.getRank("34"));
		Assert.assertEquals(4, rank.getRank("31"));

		Assert.assertEquals(3, rank.getRank("5"));
		Assert.assertEquals(3, rank.getRank("22"));
		Assert.assertEquals(3, rank.getRank("15"));
		Assert.assertEquals(3, rank.getRank("29"));
		Assert.assertEquals(3, rank.getRank("32"));

		Assert.assertEquals(2, rank.getRank("27"));
		Assert.assertEquals(2, rank.getRank("6"));
		Assert.assertEquals(2, rank.getRank("T35"));
		Assert.assertEquals(2, rank.getRank("23"));
		Assert.assertEquals(2, rank.getRank("T30"));

		Assert.assertEquals(1, rank.getRank("T24"));
		Assert.assertEquals(1, rank.getRank("7"));
		Assert.assertEquals(1, rank.getRank("T1"));

		Assert.assertEquals(0, rank.getRank("T8"));

	}

	public DirectedGraph<String, String> createGraph(String graphString) {
		final DirectedGraph<String, String> graph = new DirectedSparseGraph<String, String>();
		final Map<String, String> vertexMap = new HashMap<String, String>();
		for (String edge : StringUtils.split(graphString, ';')) {
			if (!StringUtils.isBlank(edge)) {
				final int edgeDelimiterPosition = edge.indexOf("->");

				if (edgeDelimiterPosition != -1) {
					String head = edge.substring(0, edgeDelimiterPosition)
							.trim();
					String tail = edge.substring(edgeDelimiterPosition + 2)
							.trim();
					if (!vertexMap.containsKey(head)) {
						graph.addVertex(head);
						vertexMap.put(head, head);
					} else {
						head = vertexMap.get(head);
					}
					if (!vertexMap.containsKey(tail)) {
						graph.addVertex(tail);
						vertexMap.put(tail, tail);
					} else {
						tail = vertexMap.get(tail);
					}
					graph.addEdge(head + " -> " + tail, head, tail);
				}
			}
		}
		return graph;
	}

}
