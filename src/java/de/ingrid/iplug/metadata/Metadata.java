package de.ingrid.iplug.metadata;

import java.io.Serializable;

public class Metadata implements Serializable {

	private static final long serialVersionUID = -882806556761084500L;

	private MetadataAnnotation _metaDataAnnotation;

	public Metadata() {
		Package package1 = Metadata.class.getPackage();
		_metaDataAnnotation = package1.getAnnotation(MetadataAnnotation.class);
	}

	public String getVersion() {
		return _metaDataAnnotation != null ? _metaDataAnnotation.version()
				: "unknown";
	}
}
