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
