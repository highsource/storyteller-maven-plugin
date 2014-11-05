package org.highsource.storyteller.jgrapht.ext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints.Key;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.transcoder.keys.IntegerKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;

public class BatikGraphVizGraphExporter<V, E> extends GraphVizGraphExporter<V, E> {

	private final Map<Key, Object> hints = new LinkedHashMap<Key, Object>();

	public BatikGraphVizGraphExporter(String graphVizDotFile) {
		super(graphVizDotFile, "svg");
	}
	
	public BatikGraphVizGraphExporter(String graphVizDotFile, Map<Key, Object> hints) {
		this(graphVizDotFile);
		this.hints.putAll(hints);
	}
	
	private static Key getHintKeyForName(String name) {
		// allow user to specify "defaultFontFamily" instead of "KEY_DEFAULT_FONT_FAMILY"
		if (!name.startsWith("KEY_")) {
			name = "KEY_" + name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ENGLISH);
		}
		try {
			Field field = PNGTranscoder.class.getField(name);
			if (Modifier.isStatic(field.getModifiers()) && Key.class.isAssignableFrom(field.getType())) {
				return (Key) field.get(null);
			}
		} catch (Exception e) {
			// no need to be specific
		}
		return null;
	}
	
	public static Map<Key, Object> parseHints(Map<String, String> hints) {
		Map<Key, Object> parsed = new LinkedHashMap<Key, Object>();
		if (hints != null) {
			for (Entry<String, String> entry : hints.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				Key key = getHintKeyForName(name);
				if (key == null) {
					throw new IllegalArgumentException(name + " is not the name of a valid hint");
				} else if (key instanceof BooleanKey) {
					parsed.put(key, Boolean.valueOf(value));
				} else if (key instanceof IntegerKey) {
					parsed.put(key, Integer.valueOf(value));
				} else if (key instanceof FloatKey) {
					parsed.put(key, Float.valueOf(value));
				} else if (key instanceof LengthKey) {
					parsed.put(key, Float.valueOf(value));
				} else if (key instanceof StringKey) {
					parsed.put(key, value);
				} else {
					throw new IllegalArgumentException("Unsupported hint type " + key.getClass().getSimpleName());
				}
			}
		}
		return parsed;
	}

	@Override
	public void exportGraph(DirectedGraph<V, E> graph, VertexNameProvider<V> vertexLabelProvider,
			EdgeNameProvider<E> edgeLabelProvider, File targetFile, Log log) throws IOException {
		File tempFile = File.createTempFile("batik", ".svg", targetFile.getParentFile());
		super.exportGraph(graph, vertexLabelProvider, edgeLabelProvider, tempFile, log);
		
		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			input = new FileInputStream(tempFile);
			output = new FileOutputStream(targetFile);
			
			PNGTranscoder transcoder = new PNGTranscoder();
			for (Entry<Key, Object> entry : hints.entrySet()) {
				transcoder.addTranscodingHint(entry.getKey(), entry.getValue());
			}
			transcoder.transcode(new TranscoderInput(input), new TranscoderOutput(output));
		} catch (TranscoderException e) {
			throw new IOException(e);
		} finally {
			IOUtil.close(input);
			IOUtil.close(output);
		}
		tempFile.delete();
	}

}
