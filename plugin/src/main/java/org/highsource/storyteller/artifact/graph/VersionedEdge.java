package org.highsource.storyteller.artifact.graph;

import org.jgrapht.graph.DefaultEdge;

public class VersionedEdge extends DefaultEdge {
	private static final long serialVersionUID = 1L;
	
	protected String version;
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

}
