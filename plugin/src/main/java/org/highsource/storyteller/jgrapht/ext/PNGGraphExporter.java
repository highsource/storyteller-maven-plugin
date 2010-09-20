package org.highsource.storyteller.jgrapht.ext;

import java.io.File;

public class PNGGraphExporter<V, E> extends GraphVizGraphExporter<V, E> {

	public PNGGraphExporter(File graphVizDotFile) {
		super(graphVizDotFile);
	}

	@Override
	protected String getFormat() {
		return "png";
	}

}
