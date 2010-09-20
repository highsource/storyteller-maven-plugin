package org.highsource.storyteller.artifact;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MPackage {

	private final MArchive archive;

	private final String name;

	private final Map<String, MClass> classes = new HashMap<String, MClass>();

	MPackage(MArchive archive, String packageName) {
		// todo check arguments
		this.archive = archive;
		this.name = packageName;
	}

	public MArchive getArchive() {
		return archive;
	}

	public String getPackageName() {
		return name;
	}

	public MClass getOrCreateClass(String className) {
		// todo check arguments
		final MClass cached = this.classes.get(className);
		if (cached != null) {
			return cached;
		}

		int dot = className.lastIndexOf('.');
		final String packageName;
		if (dot < 0) {
			packageName = "";
		} else if (dot == 0 || dot == className.length() - 1) {
			throw new IllegalArgumentException("Illegal class name ["
					+ className + "].");
		} else {
			packageName = className.substring(0, dot - 1);
		}

		if (!packageName.equals(getPackageName())) {
			throw new IllegalArgumentException("Wrong package name.");
		}

		final MClass theClass = new MClass(this, className);
		this.classes.put(className, theClass);
		return theClass;
	}
	
	public Collection<MClass> getClasses()
	{
		return classes.values();
	}

}
