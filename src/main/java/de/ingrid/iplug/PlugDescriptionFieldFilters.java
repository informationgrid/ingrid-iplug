package de.ingrid.iplug;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.utils.PlugDescription;

public class PlugDescriptionFieldFilters {

    private static final Log LOG = LogFactory.getLog(PlugDescriptionFieldFilters.class);

    private final IPlugdescriptionFieldFilter[] _filters;

    public PlugDescriptionFieldFilters(IPlugdescriptionFieldFilter... filters) {
        _filters = filters;
    }

    public PlugDescription filter(final PlugDescription plugDescription) {
        PlugDescription clone = (PlugDescription) plugDescription.clone();
        Iterator<Entry<?, ?>> iterator = clone.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
            Object key = entry.getKey();
            for (IPlugdescriptionFieldFilter filter : _filters) {
                if (filter.filter(key)) {
                    LOG.info("remove field: " + key);
                    iterator.remove();
                }
            }
        }
        return clone;
    }
}
