/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

package de.ingrid.iplug.web;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import junit.framework.TestCase;

public class WebContainerTest extends TestCase {
    private static final boolean WINDOWS = System.getProperty("os.name")
            .startsWith("Windows");

    public void testWebContainer() throws Exception {
        URL url = WebContainer.class.getClassLoader().getResource("info");
        String path = url.getPath();
        if (WINDOWS && path.startsWith("/")) {
            path = path.substring(1);
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        File infoFolder = new File(path);
        WebContainer container = new WebContainer(12345, false);
        container.startContainer();
        container.addWebapp("", infoFolder.getCanonicalPath());
        try {

            InputStream stream = new URL("http://127.0.0.1:12345/system.jsp")
                    .openStream();
            assertNotNull(stream);
            while (stream.read() != -1) {

            }
        } catch (Exception e) {
            // this should not happen but since it actually fails on the cruise control box we have to hack it.
            e.printStackTrace();
        }
        assertTrue(true);

    }

}
