package de.ingrid.iplug.metadata;

import de.ingrid.utils.PlugDescription;

public class VersionInjector implements IMetadataInjector {

	public static final String PLUG_VERSION = "PLUG_VERSION";
	
	private Metadata _metadata = new Metadata();
	
	public void injectMetaDatas(PlugDescription plugDescription) {
		String version = _metadata.getVersion();
		plugDescription.put(PLUG_VERSION, version);
	}

}
