package org.highsource.storyteller.artifact;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.Artifact;

public class MArchive {

	private final Artifact artifact;

	public MArchive(final Artifact artifact) {
		this.artifact = artifact;
	}

	public Artifact getArtifact() {
		return artifact;
	}

	private final Map<String, MPackage> packages = new HashMap<String, MPackage>();

	public MPackage getOrCreatePackage(String packageName) {
		final MPackage cached = this.packages.get(packageName);
		if (cached != null) {
			return cached;
		} else {
			final MPackage thePackage = new MPackage(this, packageName);
			this.packages.put(packageName, thePackage);
			return thePackage;
		}
	}

	public MClass getOrCreateClass(String className) {
		// todo Check arguments
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

		final MPackage thePackage = getOrCreatePackage(packageName);
		return thePackage.getOrCreateClass(className);
	}

	public Collection<MPackage> getPackages() {
		return packages.values();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result
				+ ((artifact == null) ? 0 : artifact.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MArchive other = (MArchive) obj;
		if (artifact == null) {
			if (other.artifact != null)
				return false;
		} else if (!artifact.equals(other.artifact))
			return false;
		return true;
	}

}
