package de.ingrid.iplug.metadata;

import junit.framework.TestCase;
import de.ingrid.utils.PlugDescription;

public class VersionInjectorTest extends TestCase {

	public void testInjectVersion() throws Exception {
		IMetadataInjector injector = new VersionInjector();
		PlugDescription plugDescription = new PlugDescription();
		
		assertEquals(null, plugDescription.get(VersionInjector.PLUG_VERSION));
		injector.injectMetaDatas(plugDescription);
		assertEquals("0.1.2", plugDescription.get(VersionInjector.PLUG_VERSION));
	}
}
