package org.highsource.storyteller.jgrapht.ext;

public class JungPNGGraphExporter<V, E> extends JungGraphExporter<V, E> {

	@Override
	protected String getFormat() {
		return "png";
	}

}
