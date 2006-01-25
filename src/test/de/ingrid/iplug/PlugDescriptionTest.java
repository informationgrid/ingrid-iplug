/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.File;

import junit.framework.TestCase;
import de.ingrid.utils.xml.XMLSerializer;

public class PlugDescriptionTest extends TestCase {

    public void testSerialize() throws Exception {
        File target = new File("./testFile.xml");
        PlugDescription description1 = new PlugDescription();
        description1.setPersonName("bla");

        XMLSerializer serializer = new XMLSerializer();
        serializer.aliasClass(PlugDescription.class.getName(),
                PlugDescription.class);
        serializer.serialize(description1, target);

        PlugDescription description2 = (PlugDescription) serializer.deSerialize(target);

        assertEquals(description1, description2);

    }

}
