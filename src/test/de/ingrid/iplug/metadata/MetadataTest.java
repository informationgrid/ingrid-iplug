package de.ingrid.iplug.metadata;

import junit.framework.TestCase;

public class MetadataTest extends TestCase {

	public void testGetVersion() throws Exception {
		Metadata metadata = new Metadata();
		String version = metadata.getVersion();
		assertEquals("0.1.2", version);
	}
}
