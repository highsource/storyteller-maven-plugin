package org.highsource.storyteller.jgrapht.ext;

import java.io.File;

public class PDFGraphExporter<V, E> extends GraphVizGraphExporter<V, E> {

	public PDFGraphExporter(File graphVizDotFile) {
		super(graphVizDotFile);
	}

	@Override
	protected String getFormat() {
		return "pdf";
	}

}
