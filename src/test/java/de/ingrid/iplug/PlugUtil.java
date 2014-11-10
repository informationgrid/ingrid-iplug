/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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

import de.ingrid.utils.PlugDescription;
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

        serializer.aliasClass(PlugDescription.class.getName(),
                PlugDescription.class);
        serializer.serialize(description, outFile);
    }

}
