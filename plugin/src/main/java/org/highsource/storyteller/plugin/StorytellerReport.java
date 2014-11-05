package org.highsource.storyteller.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.reporting.sink.SinkFactory;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.highsource.storyteller.artifact.graph.VersionedEdge;
import org.highsource.storyteller.artifact.graph.ext.EdgeNameProviders;
import org.highsource.storyteller.artifact.graph.ext.VertexNameProviders;
import org.highsource.storyteller.jgrapht.ext.AutoGraphExporter;
import org.highsource.storyteller.jgrapht.ext.GraphExporter;

/**
 * @goal report
 * @phase site
 */
public class StorytellerReport extends AbstractDependencyGraphMojo implements MavenReport {

	/**
	 * @component
	 */
	protected Renderer siteRenderer;

	/**
	 * @parameter expression="${project.reporting.outputDirectory}"
	 * @required
	 * @readonly
	 */
	protected String outputDirectory;

	/**
	 * The plugin uses GraphViz package to render graphs in formats like PDF and so on. If the <code>dot</code>
	 * executable is not in PATH, it can be specified manually here.
	 * 
	 * @parameter expression="${graphViz.dotFile}" default-value="dot"
	 */
	protected String graphVizDotFile;
	
	/**
	 * Use Batik to render PNGs instead of Graphviz's default renderer.
	 * @parameter expression="${useBatik}" default-value="false"
	 */
	private boolean useBatik;
	
	/**
	 * Hints to pass to Batik when rendering.
	 * @parameter
	 */
	private Map<String, String> batikHints;

	public String getOutputName() {
		return "storyteller";
	}

	public String getName(Locale locale) {
		return "Dependency Graph";
	}

	public String getDescription(Locale locale) {
		return null;
	}

	protected void executeReport(Locale locale) throws MavenReportException {
		try {
			super.execute();
		} catch (Exception ex) {
			throw new MavenReportException("Failure generating dependency graph.", ex);
		}

		sink.head();
		sink.title();
		sink.text(getName(locale));
		sink.title_();
		sink.head_();
		sink.body();
		sink.section1();
		sink.sectionTitle1();
		sink.text("Dependency Graph for " + project.getName());
		sink.sectionTitle1_();
		sink.lineBreak();
		sink.lineBreak();
		generateDependencyGraph();
		sink.figure();
		sink.figureGraphics(getGraphLocationInSite());
		sink.figureCaption();
		sink.text("Dependency Graph");
		sink.figureCaption_();
		sink.figure_();
		sink.lineBreak();
		sink.section1_();
		sink.body_();
		sink.flush();
		sink.close();
	}

	private void generateDependencyGraph() throws MavenReportException {
		File output = new File(getGraphLocation());
		GraphExporter<Artifact, VersionedEdge> graphExporter = new AutoGraphExporter<Artifact, VersionedEdge>(
				graphVizDotFile, useBatik, batikHints);
		try {
			graphExporter.exportGraph(artifactGraph, VertexNameProviders.ARTIFACT_VERTEX_NAME_PROVIDER,
					EdgeNameProviders.VERSION_EDGE_NAME_PROVIDER, output, getLog());
		} catch (IOException ioex) {
			throw new MavenReportException("Error exporting graph.", ioex);
		}
	}

	private String getGraphLocation() {
		return getReportOutputDirectory().getAbsolutePath() + "/" + getGraphLocationInSite();
	}

	private String getGraphLocationInSite() {
		return "images/" + getOutputName() + ".png";
	}

	// Single inheritance puts the bulk of AbstractMavenReport here

	private Sink sink;

	private Locale locale = Locale.ENGLISH;

	private File reportOutputDirectory;

	@Override
	public void execute() throws MojoExecutionException {
		SiteRendererSink sink;
		try {
			sink = SinkFactory.createSink(new File(outputDirectory), getOutputName() + ".html");

			generate(sink, Locale.getDefault());

			// TODO: add back when skinning support is in the site renderer
			// getSiteRenderer().copyResources( outputDirectory, "maven" );
		} catch (MavenReportException e) {
			throw new MojoExecutionException("An error has occurred in " + getName(locale) + " report generation.", e);
		}

		File outputHtml = new File(outputDirectory, getOutputName() + ".html");
		outputHtml.getParentFile().mkdirs();

		Writer writer = null;
		try {
			SiteRenderingContext context = new SiteRenderingContext();
			context.setDecoration(new DecorationModel());
			context.setTemplateName("org/apache/maven/doxia/siterenderer/resources/default-site.vm");
			context.setLocale(locale);

			writer = WriterFactory.newXmlWriter(outputHtml);

			siteRenderer.generateDocument(writer, sink, context);
		} catch (RendererException e) {
			throw new MojoExecutionException("An error has occurred in " + getName(Locale.ENGLISH)
					+ " report generation.", e);
		} catch (IOException e) {
			throw new MojoExecutionException("An error has occurred in " + getName(Locale.ENGLISH)
					+ " report generation.", e);
		} finally {
			IOUtil.close(writer);
		}
	}

	@SuppressWarnings("deprecation")
	public void generate(org.codehaus.doxia.sink.Sink sink, Locale locale) throws MavenReportException {
		if (sink == null) {
			throw new MavenReportException("You must specify a sink.");
		}
		this.sink = sink;

		executeReport(locale);

		this.sink.close();
	}

	public String getCategoryName() {
		return CATEGORY_PROJECT_REPORTS;
	}

	public File getReportOutputDirectory() {
		if (reportOutputDirectory == null) {
			reportOutputDirectory = new File(outputDirectory);
		}
		return reportOutputDirectory;
	}

	public void setReportOutputDirectory(File outputDirectory) {
		reportOutputDirectory = outputDirectory;
	}

	public boolean isExternalReport() {
		return false;
	}

	public boolean canGenerateReport() {
		return true;
	}

}
