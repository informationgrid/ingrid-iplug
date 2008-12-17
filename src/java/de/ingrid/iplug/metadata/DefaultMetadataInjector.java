package de.ingrid.iplug.metadata;

import de.ingrid.utils.PlugDescription;

public class DefaultMetadataInjector implements IMetadataInjector {

	public static final String DEFAULT_METADATA = "DEFAULT_METADATA";
	
	private Metadata _metadata = new Metadata();
	
	public void injectMetaDatas(PlugDescription plugDescription) {
		plugDescription.put(DEFAULT_METADATA, _metadata);
	}

}
