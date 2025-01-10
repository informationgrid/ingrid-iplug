/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
        Iterator<Entry<Object, Object>> iterator = clone.entrySet().iterator();
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
