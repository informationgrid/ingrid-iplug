package de.ingrid.iplug;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;

import de.ingrid.utils.PlugDescription;

public class CacheService {

    public static final Logger LOG = Logger.getLogger(CacheService.class);

    public static final String INGRID_CACHE = "ingrid-cache";

    public static final String DEFAULT_CACHE = "default-cache";

    public static void updateIngridCache(final PlugDescription pd) {
        final CacheManager manager = CacheManager.getInstance();

        // clear the cache
        if (manager.cacheExists(INGRID_CACHE)) {
            LOG.info("removing " + INGRID_CACHE + " cache");
            manager.removeCache(INGRID_CACHE);
        }
        if (manager.cacheExists(DEFAULT_CACHE)) {
            LOG.info("removing " + DEFAULT_CACHE + " cache");
            manager.removeCache(DEFAULT_CACHE);
        }

        final boolean active = pd.getCacheActive() == null ? true : pd.getCacheActive();
        final boolean diskStore = pd.getCachedInDiskStore() == null ? false : pd.getCachedInDiskStore();
        final int elements = pd.getCachedElements() == null ? 1000 : pd.getCachedElements();
        final int lifeTime = pd.getCachedLifeTime() == null ? 600 : pd.getCachedLifeTime();

        // update cache
        if (active) {
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
