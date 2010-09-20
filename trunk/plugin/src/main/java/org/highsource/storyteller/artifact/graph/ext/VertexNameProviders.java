package org.highsource.storyteller.artifact.graph.ext;

import org.apache.maven.artifact.Artifact;
import org.highsource.storyteller.artifact.MArchive;
import org.highsource.storyteller.artifact.MClass;
import org.jgrapht.ext.VertexNameProvider;

public class VertexNameProviders {

	public static final VertexNameProvider<MArchive> ARCHIVE_VERTEX_NAME_PROVIDER = new VertexNameProvider<MArchive>() {
		public String getVertexName(MArchive archive) {
			return archive.getArtifact().getId();
		}
	};
	public static final VertexNameProvider<MClass> CLASS_VERTEX_NAME_PROVIDER = new VertexNameProvider<MClass>() {
		public String getVertexName(MClass theClass) {
			return theClass.getClassName();
		}
	};
	public static final VertexNameProvider<Artifact> ARTIFACT_VERTEX_NAME_PROVIDER = new VertexNameProvider<Artifact>() {
		public String getVertexName(Artifact artifact) {
			return artifact.getId();
		}
	};

}
