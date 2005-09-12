/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.xml;

import java.io.File;

import de.ingrid.iplug.PlugDescription;
import de.ingrid.iplug.xml.XMLSerializer;

import junit.framework.TestCase;

public class XMLSerializerTest extends TestCase {

    public void testWriteDescription() throws Exception {
        PlugDescription description = getDescription();
        File target = new File(System.getProperty("java.io.tmpdir"), "test.xml");
        XMLSerializer.serializeAsXML(description, target);
        PlugDescription description2 = (PlugDescription) XMLSerializer.loadDescriptionFromXML(PlugDescription.class,
                target);
        assertEquals(description.getPlugId(), description2.getPlugId());

    }

    private PlugDescription getDescription() {
        PlugDescription description = new PlugDescription();
        description.setCronBasedIndexing(true);
        description.setDataType("bla");
        description.setOraganisation("organisation");
        description.setPersoneMail("mail");
        description.setPersonName("name");
        description.setPersonSureName("surename");
        description.setPlugId("aId");
        description.setWorkinDirectory(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
        return description;
    }
}
