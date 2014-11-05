package org.highsource.storyteller.jgrapht.ext;

import java.io.PrintWriter;
import java.io.Writer;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;

/**
 * Exports a graph into a GML file (Graph Modelling Language).
 *
 * <p>For a description of the format see <a
 * href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www.infosun.fmi.uni-passau.de/Graphlet/GML/</a>.</p>
 *
 * @author Dimitrios Michail
 */
public class GmlExporter<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String creator = "JGraphT GML Exporter";
    private static final String version = "1";

    private static final String delim = " ";
    private static final String tab1 = "\t";
    private static final String tab2 = "\t\t";
    private static final String tab3 = "\t\t\t";

    //~ Instance fields --------------------------------------------------------

    private final VertexNameProvider<V> vertexIDProvider;
    private final VertexNameProvider<V> vertexLabelProvider;
    
    private final EdgeNameProvider<E> edgeLabelProvider;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GmlExporter object.
     */
    public GmlExporter(VertexNameProvider<V> vertexIDProvider,
            VertexNameProvider<V> vertexLabelProvider,
            EdgeNameProvider<E> edgeLabelProvider)
    {
    	this.vertexIDProvider = vertexIDProvider;
    	this.vertexLabelProvider = vertexLabelProvider;
    	this.edgeLabelProvider = edgeLabelProvider;
    	
    }

    //~ Methods ----------------------------------------------------------------

    private String quoted(final String s)
    {
        return "\"" + s + "\"";
    }

    private void exportHeader(PrintWriter out)
    {
        out.println("Creator" + delim + quoted(creator));
        out.println("Version" + delim + version);
    }

    private void exportVertices(
        PrintWriter out,
        Graph<V, E> g)
    {
        for (V from : g.vertexSet()) {
            out.println(tab1 + "node");
            out.println(tab1 + "[");
            out.println(tab2 + "id" + delim + vertexIDProvider.getVertexName(from));
            if (vertexLabelProvider != null)
            {
                out.println(tab2 + "label" + delim + quoted(vertexLabelProvider.getVertexName(from)));
            }
            out.println(tab1 + "]");
        }
    }

    private void exportEdges(
        PrintWriter out,
        Graph<V, E> g)
    {
        for (E edge : g.edgeSet()) {
            out.println(tab1 + "edge");
            out.println(tab1 + "[");
            String s = vertexIDProvider.getVertexName(g.getEdgeSource(edge));
            out.println(tab2 + "source" + delim + s);
            String t = vertexIDProvider.getVertexName(g.getEdgeTarget(edge));
            out.println(tab2 + "target" + delim + t);
            if (edgeLabelProvider != null)
            {
                out.println(tab2 + "label" + delim + quoted(edgeLabelProvider.getEdgeName(edge)));
            }
            if (g instanceof DirectedGraph<?,?>)
            {
                out.println(tab2 + "graphics");
                out.println(tab2 + "[");
                out.println(tab3 + "targetArrow" + delim + "\"standard\"");
                out.println(tab2 + "]");
            	
            }
            out.println(tab1 + "]");
        }
    }

    private void export(Writer output, Graph<V, E> g, boolean directed)
    {
        PrintWriter out = new PrintWriter(output);

        exportHeader(out);
        out.println("graph");
        out.println("[");
        out.println(tab1 + "label" + delim + quoted(""));
        if (directed) {
            out.println(tab1 + "directed" + delim + "1");
        } else {
            out.println(tab1 + "directed" + delim + "0");
        }
        exportVertices(out, g);
        exportEdges(out, g);
        out.println("]");
        out.flush();
    }

    /**
     * Exports an undirected graph into a plain text file in GML format.
     *
     * @param output the writer to which the graph to be exported
     * @param g the undirected graph to be exported
     */
    public void export(Writer output, UndirectedGraph<V, E> g)
    {
        export(output, g, false);
    }

    /**
     * Exports a directed graph into a plain text file in GML format.
     *
     * @param output the writer to which the graph to be exported
     * @param g the directed graph to be exported
     */
    public void export(Writer output, DirectedGraph<V, E> g)
    {
        export(output, g, true);
    }
}

// End GmlExporter.java
