package de.ingrid.iplug;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import de.ingrid.utils.ISearcher;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

public class CacheSearcher implements ISearcher {

	private Cache _cache;

	public CacheSearcher() throws CacheException {
		CacheManager cacheManager = CacheManager.create();
		_cache = cacheManager.getCache("ingrid-cache");
		if (_cache == null) {
			_cache = new Cache("default", 1000, false, false, 600, 600);
			cacheManager.addCache(_cache);
		}
	}

	public IngridHits search(IngridQuery query, int start, int length)
			throws Exception {
		String queryString = query.toString();
		Element element = _cache != null ? _cache.get(queryString) : null;
		return element != null ? (IngridHits) element.getValue() : null;
	}

	public void addToCache(IngridQuery ingridQuery, IngridHits hits) {
		String ingridQueryString = ingridQuery.toString();
		Element element = new Element(ingridQueryString, hits);
		_cache.put(element);
	}

}
