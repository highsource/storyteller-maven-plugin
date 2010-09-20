package org.highsource.storyteller.artifact;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class MClass {

	private final MPackage thePackage;

	private final String className;

	private final Collection<String> referencedClassNames = new LinkedList<String>();

	MClass(MPackage thePackage, String className) {
		super();
		// todo check arguments
		this.thePackage = thePackage;
		this.className = className;
	}

	public MPackage getPackage() {
		return thePackage;
	}

	public String getClassName() {
		return className;
	}

	public Collection<String> getReferencedClassNames() {
		return Collections.unmodifiableCollection(this.referencedClassNames);
	}

	public void addReferencedClassName(String referencedClassName) {
		this.referencedClassNames.add(referencedClassName);

	}
}
