package org.highsource.storyteller.jung.algorithms.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;
import org.highsource.storyteller.jung.algorithms.rank.Rank;

public class SimpleRankedLayoutTransformer<V, E> implements
		Transformer<V, Point2D> {

	private final Rank<V, E> rank;
	private final Dimension dimension;
	private final int horizontalSpacing;
	private final int verticalSpacing;

	public SimpleRankedLayoutTransformer(Rank<V, E> rank, Dimension dimension,
			int horizontalSpacing, int verticalSpacing) {
		Validate.notNull(rank);
		Validate.notNull(dimension);
		this.rank = rank;
		this.dimension = dimension;
		this.horizontalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
	}

	@Override
	public Point2D transform(V vertex) {
		int ry = rank.getRank(vertex);
		final List<V> vertices = new ArrayList<V>(rank.getVerticies(ry));
		int rc = vertices.size();
		int rx = vertices.indexOf(vertex);
		int w = dimension.width;

		int x = ((2 * rx - rc) * horizontalSpacing + w) / 2;
		int y = ry * verticalSpacing;

		return new Point(x, y);

	}

}
