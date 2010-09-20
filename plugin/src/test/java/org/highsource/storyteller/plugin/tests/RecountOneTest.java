package org.highsource.storyteller.plugin.tests;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.highsource.storyteller.plugin.RecountMojo;

public class RecountOneTest extends AbstractRecountTest {

	public void testExecute() throws Exception {

		final File pom = new File(getBaseDir(),
				"src/test/resources/org/highsource/storyteller/plugin/tests/RecountOneTest.xml");

		final MavenProject mavenProject = mavenProjectBuilder.build(pom,
				localRepository, null);

		final RecountMojo mojo = (RecountMojo) lookupMojo("recount", pom);
		mojo.setProject(mavenProject);
		mojo.setArtifactResolver(artifactResolver);
		mojo.setArtifactMetadataSource(artifactMetadataSource);
		mojo.setArtifactFactory(artifactFactory);
		mojo.setLocalRepository(localRepository);
		mojo.setMavenProjectBuilder(mavenProjectBuilder);
		mojo.execute();
	}
}
