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
package de.ingrid.iplug;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;

import de.ingrid.utils.PlugDescription;

/**
 * @deprecated
 * This is old and is replaced by the CacheService in the base-webapp project.
 */
@Deprecated
public class CacheService {

    public static final Logger LOG = Logger.getLogger(CacheService.class);

    public static final String INGRID_CACHE = "ingrid-cache";

    public static void updateIngridCache(final PlugDescription pd) {
        final CacheManager manager = CacheManager.getInstance();

        if (pd.getCacheActive() != null) {
            
            LOG.info("removing " + INGRID_CACHE + " cache");
            if (manager.cacheExists(INGRID_CACHE))
                manager.removeCache(INGRID_CACHE);
            
            final boolean diskStore = pd.getCachedInDiskStore();
            final int elements = pd.getCachedElements();
            final int lifeTime = pd.getCachedLifeTime();

            // update cache
            if (pd.getCacheActive()) {
                LOG.info("elements: " + elements);
                LOG.info("diskStore: " + diskStore);
                LOG.info("lifeTime: " + lifeTime + "min");
                final int lifeTimeInSecs = lifeTime * 60;
                final Cache cache = new Cache(INGRID_CACHE, elements, diskStore, lifeTime <= 0, lifeTimeInSecs,
                        lifeTimeInSecs);

                manager.addCache(cache);
                LOG.info("cache is now activated");
            } else {
                LOG.info("cache is now deactivated");
            }
        }
    }
}
