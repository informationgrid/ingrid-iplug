package de.ingrid.iplug.metadata;


public class Metadata {

	private MetadataAnnotation _metaDataAnnotation;

	public Metadata() {
		Package package1 = Metadata.class.getPackage();
		_metaDataAnnotation = package1.getAnnotation(MetadataAnnotation.class);
	}

	public String getVersion() {
		return _metaDataAnnotation.version();
	}
}
