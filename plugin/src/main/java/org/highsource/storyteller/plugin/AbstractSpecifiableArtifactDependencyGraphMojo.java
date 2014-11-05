package org.highsource.storyteller.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.ProjectBuildingException;

public abstract class AbstractSpecifiableArtifactDependencyGraphMojo extends AbstractDependencyGraphMojo {

	/**
	 * @parameter expression="${groupId}"
	 */
	private String groupId;

	/**
	 * @parameter expression="${artifactId}"
	 */
	private String artifactId;

	/**
	 * @parameter expression="${version}"
	 */
	private String version;

	/**
	 * @parameter expression="${type}" default-value="jar"
	 */
	private String type = "jar";

	/**
	 * @parameter expression="${classifier}"
	 */
	private String classifier;

	/**
	 * @parameter expression="${repositoryId}" default-value="default"
	 */
	private String repositoryId;

	/**
	 * @parameter expression="${repositoryURL}"
	 */
	private String repositoryURL;

	/**
	 * @parameter expression="${repositoryLayout}"
	 */
	private String repositoryLayout;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		initSpecifiedRepository();
		initSpecifiedProject();

		super.execute();
	}

	private void initSpecifiedProject() throws MojoExecutionException {
		if (groupId != null && artifactId != null && version != null) {
			final Artifact artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, type,
					classifier);

			try {
				project = mavenProjectBuilder
						.buildFromRepository(artifact, remoteArtifactRepositories, localRepository);
			} catch (ProjectBuildingException pbex) {
				throw new MojoExecutionException("Could not create the project for [" + artifactId + "].", pbex);
			}
		}
	}

	private void initSpecifiedRepository() throws MojoExecutionException {
		if (repositoryURL != null) {
			if ("legacy".equals(repositoryLayout)) {
				throw new MojoExecutionException("Legacy repository layout is no longer supported");
			}
			remoteArtifactRepositories.add(new DefaultArtifactRepository(repositoryId, repositoryURL, new DefaultRepositoryLayout()));
		}
	}

}
