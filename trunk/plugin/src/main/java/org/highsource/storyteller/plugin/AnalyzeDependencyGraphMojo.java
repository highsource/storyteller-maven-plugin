package org.highsource.storyteller.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoPhase;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.jfrog.maven.annomojo.annotations.MojoRequiresProject;

@MojoGoal("analyze-dependency-graph")
@MojoPhase("verify")
@MojoRequiresProject(false)
@MojoRequiresDependencyResolution("test")
public class AnalyzeDependencyGraphMojo extends AbstractDependencyGraphMojo {

	private DependencyGraphAnalyzer[] dependencyGraphAnalyzers = new DependencyGraphAnalyzer[] { new DependencyGraphRedundancyAnalyzer() };

	@MojoParameter(required = false, readonly = true)
	public DependencyGraphAnalyzer[] getDependencyGraphAnalyzers() {
		return dependencyGraphAnalyzers;
	}

	public void setDependencyGraphAnalyzers(
			DependencyGraphAnalyzer[] dependencyGraphAnalyzers) {
		this.dependencyGraphAnalyzers = dependencyGraphAnalyzers;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		for (final DependencyGraphAnalyzer analyzer : getDependencyGraphAnalyzers()) {
			analyzer.analyzeDependencyGraph(this.artifactGraph, getLog());
		}
	}

}
