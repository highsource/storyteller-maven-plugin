package org.highsource.storyteller.jung.algorithms.rank.tests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.highsource.storyteller.jung.algorithms.rank.DotRankFactory;
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

		final Rank<String> rank = new DotRankFactory<String, String>(dag)
				.getRank();

		assertEquals(4, rank.getRank("a").intValue());

		assertEquals(3, rank.getRank("b").intValue());
		assertEquals(3, rank.getRank("e").intValue());
		assertEquals(3, rank.getRank("f").intValue());

		assertEquals(2, rank.getRank("c").intValue());
		assertEquals(2, rank.getRank("g").intValue());

		assertEquals(1, rank.getRank("d").intValue());

		assertEquals(0, rank.getRank("h").intValue());
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

		final Rank<String> rank = new DotRankFactory<String, String>(dag)
				.getRank();

		assertEquals(4, rank.getRank("a").intValue());

		assertEquals(3, rank.getRank("b").intValue());
		assertEquals(3, rank.getRank("i").intValue());
		assertEquals(3, rank.getRank("e").intValue());
		assertEquals(3, rank.getRank("f").intValue());

		assertEquals(2, rank.getRank("c").intValue());
		assertEquals(2, rank.getRank("j").intValue());
		assertEquals(2, rank.getRank("g").intValue());

		assertEquals(1, rank.getRank("d").intValue());

		assertEquals(0, rank.getRank("h").intValue());
	}

	@Test
	public void rank03() {
		DirectedGraph<String, String> graph = createGraph("S8 -> 9; S24 -> 27; S24 -> 25; S1 -> 10; S1 -> 2; S35 -> 36;"
				+ "S35 -> 43; S30 -> 31; S30 -> 33; 9 -> 42; 9 -> T1; 25 -> T1;"
				+ "25 -> 26; 27 -> T24; 2 -> 3; 2 -> 16; 2 -> 17; 2 -> T1; 2 -> 18;"
				+ "10 -> 11; 10 -> 14; 10 -> T1; 10 -> 13; 10 -> 12;"
				+ "31 -> T1; 31 -> 32; 33 -> T30; 33 -> 34; 42 -> 4; 26 -> 4;"
				+ "3 -> 4; 16 -> 15; 17 -> 19; 18 -> 29; 11 -> 4; 14 -> 15;"
				+ "37 -> 39; 37 -> 41; 37 -> 38; 37 -> 40; 13 -> 19; 12 -> 29;"
				+ "43 -> 38; 43 -> 40; 36 -> 19; 32 -> 23; 34 -> 29; 39 -> 15;"
				+ "41 -> 29; 38 -> 4; 40 -> 19; 4 -> 5; 19 -> 21; 19 -> 20;"
				+ "19 -> 28; 5 -> 6; 5 -> T35; 5 -> 23; 21 -> 22; 20 -> 15; 28 -> 29;"
				+ "6 -> 7; 15 -> T1; 22 -> 23; 22 -> T35; 29 -> T30; 7 -> T8;"
				+ "23 -> T24; 23 -> T1");

		final Rank<String> rank = new DotRankFactory<String, String>(graph)
				.getRank();
		Assert.assertEquals(8, rank.getRank("S1").intValue());
		Assert.assertEquals(8, rank.getRank("S35").intValue());

		Assert.assertEquals(7, rank.getRank("S8").intValue());
		Assert.assertEquals(7, rank.getRank("S24").intValue());
		Assert.assertEquals(7, rank.getRank("37").intValue());
		Assert.assertEquals(7, rank.getRank("43").intValue());
		// TODO: 7
		Assert.assertEquals(6, rank.getRank("36").intValue());
		Assert.assertEquals(7, rank.getRank("10").intValue());
		Assert.assertEquals(7, rank.getRank("2").intValue());

		Assert.assertEquals(6, rank.getRank("9").intValue());
		Assert.assertEquals(6, rank.getRank("25").intValue());
		Assert.assertEquals(6, rank.getRank("38").intValue());
		Assert.assertEquals(6, rank.getRank("40").intValue());
		Assert.assertEquals(6, rank.getRank("13").intValue());
		Assert.assertEquals(6, rank.getRank("17").intValue());
		// TODO: 6
		Assert.assertEquals(4, rank.getRank("12").intValue());
		Assert.assertEquals(6, rank.getRank("S30").intValue());

		// TODO
		Assert.assertEquals(2, rank.getRank("27").intValue());
		Assert.assertEquals(5, rank.getRank("42").intValue());
		Assert.assertEquals(5, rank.getRank("26").intValue());
		Assert.assertEquals(5, rank.getRank("11").intValue());
		Assert.assertEquals(5, rank.getRank("3").intValue());
		// TODO
		Assert.assertEquals(4, rank.getRank("41").intValue());
		Assert.assertEquals(5, rank.getRank("19").intValue());
		// TODO
		Assert.assertEquals(4, rank.getRank("14").intValue());
		Assert.assertEquals(5, rank.getRank("33").intValue());

		Assert.assertEquals(4, rank.getRank("4").intValue());
		Assert.assertEquals(4, rank.getRank("39").intValue());
		Assert.assertEquals(4, rank.getRank("21").intValue());
		Assert.assertEquals(4, rank.getRank("20").intValue());
		Assert.assertEquals(4, rank.getRank("16").intValue());
		Assert.assertEquals(4, rank.getRank("28").intValue());
		Assert.assertEquals(4, rank.getRank("18").intValue());
		Assert.assertEquals(4, rank.getRank("34").intValue());
		Assert.assertEquals(4, rank.getRank("31").intValue());

		Assert.assertEquals(3, rank.getRank("5").intValue());
		Assert.assertEquals(3, rank.getRank("22").intValue());
		Assert.assertEquals(3, rank.getRank("15").intValue());
		Assert.assertEquals(3, rank.getRank("29").intValue());
		Assert.assertEquals(3, rank.getRank("32").intValue());

		Assert.assertEquals(2, rank.getRank("6").intValue());
		Assert.assertEquals(2, rank.getRank("T35").intValue());
		Assert.assertEquals(2, rank.getRank("23").intValue());
		Assert.assertEquals(2, rank.getRank("T30").intValue());

		Assert.assertEquals(1, rank.getRank("T24").intValue());
		Assert.assertEquals(1, rank.getRank("7").intValue());
		Assert.assertEquals(1, rank.getRank("T1").intValue());

		Assert.assertEquals(0, rank.getRank("T8").intValue());

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
