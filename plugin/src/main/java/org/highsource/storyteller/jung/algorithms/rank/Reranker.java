package org.highsource.storyteller.jung.algorithms.rank;

public interface Reranker<V, E> {

	public Rank<V, E> rerank(Rank<V, E> rank);

}
