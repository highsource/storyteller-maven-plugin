package org.highsource.storyteller.plugin.tests;

import java.io.File;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.highsource.storyteller.plugin.RecountMojo;

public abstract class AbstractRecountTest extends AbstractMojoTestCase {

	static {
		System.setProperty("basedir", getBaseDir().getAbsolutePath());
	}

	protected Mojo lookupMojo(String goal, File pom) throws Exception {
		String artifactId = "maven-storyteller-plugin";

		String groupId = "org.highsource.storyteller";

		String version = "TEST";

		PlexusConfiguration pluginConfiguration = extractPluginConfiguration(
				artifactId, pom);

		return lookupMojo(groupId, artifactId, version, goal,
				pluginConfiguration);
	}

	protected MavenProjectBuilder mavenProjectBuilder;

	protected ArtifactRepository localRepository;

	protected LoggerManager loggerManager;
	
	protected ArtifactResolver artifactResolver;
	
	protected ArtifactMetadataSource artifactMetadataSource;
	
	protected ArtifactFactory artifactFactory;

	protected void setUp() throws Exception {
		super.setUp();

		loggerManager = (LoggerManager) getContainer().lookup(
				LoggerManager.ROLE);
		loggerManager.setThreshold(Logger.LEVEL_DEBUG);
		mavenProjectBuilder = (MavenProjectBuilder) getContainer().lookup(
				MavenProjectBuilder.ROLE);

		MavenSettingsBuilder mavenSettingsBuilder = (MavenSettingsBuilder) getContainer()
				.lookup(MavenSettingsBuilder.ROLE);

		Settings settings = mavenSettingsBuilder.buildSettings();

		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) getContainer()
				.lookup(ArtifactRepositoryLayout.ROLE, "default");

		String url = settings.getLocalRepository();

		if (!url.startsWith("file:")) {
			url = "file://" + url;
		}

		localRepository = new DefaultArtifactRepository("local", url,
				repositoryLayout);
		
		artifactResolver = (ArtifactResolver) getContainer().lookup(ArtifactResolver.ROLE);

		artifactMetadataSource = (ArtifactMetadataSource) getContainer().lookup(ArtifactMetadataSource.ROLE);

		artifactFactory = (ArtifactFactory) getContainer().lookup(ArtifactFactory.ROLE);
	}

	protected static File getBaseDir() {
		try {
			return (new File(AbstractRecountTest.class.getProtectionDomain()
					.getCodeSource().getLocation().getFile())).getParentFile()
					.getParentFile().getAbsoluteFile();
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	public void testExecute() throws Exception {

		final File pom = new File(getBaseDir(),
				"src/test/resources/org/highsource/storyteller/plugin/tests/RecountOneTest.xml");

		final MavenProject mavenProject = mavenProjectBuilder.build(pom,
				localRepository, null);

		final RecountMojo mojo = (RecountMojo) lookupMojo("recount", pom);
		mojo.setProject(mavenProject);
		mojo.setLocalRepository(localRepository);
		mojo.execute();
	}
}
