package org.highsource.storyteller.artifact.graph.ext;

import org.highsource.storyteller.artifact.graph.VersionedEdge;
import org.jgrapht.ext.EdgeNameProvider;

public class EdgeNameProviders {

	public static final EdgeNameProvider<VersionedEdge> VERSION_EDGE_NAME_PROVIDER = new EdgeNameProvider<VersionedEdge>() {
		public String getEdgeName(VersionedEdge edge) {
			return edge.getVersion() == null ? "" : edge.getVersion();
		}
	};
	
}
