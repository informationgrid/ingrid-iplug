/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.iplug;

import junit.framework.TestCase;
import de.ingrid.utils.PlugDescription;

public class PlugDescriptionFieldFiltersTest extends TestCase {

    public void testFilter() throws Exception {
        IPlugdescriptionFieldFilter[] filters = new IPlugdescriptionFieldFilter[] { new IPlugdescriptionFieldFilter() {
            @Override
            public boolean filter(Object key) {
                return "foo".equals(key.toString());
            }
        } };
        PlugDescriptionFieldFilters fieldFilters = new PlugDescriptionFieldFilters(filters);
        PlugDescription plugDescription = new PlugDescription();
        plugDescription.put("foo", "bar");
        plugDescription.put("bar", "foo");
        PlugDescription filteredDescription = fieldFilters.filter(plugDescription);
        assertNotSame(plugDescription, filteredDescription);

        assertEquals(2, plugDescription.size());
        assertEquals("foo", plugDescription.get("bar"));
        assertEquals("bar", plugDescription.get("foo"));

        assertEquals(1, filteredDescription.size());
        assertEquals("foo", filteredDescription.get("bar"));

    }
}
