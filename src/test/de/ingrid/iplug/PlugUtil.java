/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.ingrid.utils.xml.XMLSerializer;

/**
 * tool to manipulate plug descriptions.
 * 
 * created on 09.08.2005
 * @author  sg
 * @version $Revision: 1.3 $
 */
public class PlugUtil {

    public static void main(String[] args) throws IOException {
        File inFile = new File(args[0]);
        File outFile = new File(args[1]);

        InputStream resourceAsStream = new FileInputStream(inFile);
        XMLSerializer serializer = new XMLSerializer();
        PlugDescription description = (PlugDescription) serializer
                .deSerialize(resourceAsStream);
        description.addField("datatype");
        description.addField("topic");
        description.addField("partner");
        description.addField("funct_categor");

        serializer.aliasClass(PlugDescription.class.getName(),
                PlugDescription.class);
        serializer.serialize(description, outFile);
    }

}
