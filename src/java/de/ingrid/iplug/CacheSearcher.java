package de.ingrid.iplug;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

public class CacheSearcher implements IPlug {

	private Cache _cache;
	private CacheManager _cacheManager;

	public IngridHits search(IngridQuery query, int start, int length)
			throws Exception {
		Element element = null;
		if (_cache != null) {
			String cacheKey = getSearchCacheKey(query, start, length);
			element = _cache != null ? _cache.get(cacheKey) : null;
		}
		return element != null ? (IngridHits) element.getValue() : null;
	}

	@Override
	public IngridHitDetail getDetail(IngridHit hit, IngridQuery query,
			String[] requestedFields) throws Exception {
		Element element = null;
		if (_cache != null) {
			String cacheKey = getDetailCacheKey(hit, requestedFields);
			element = _cache != null ? _cache.get(cacheKey) : null;
		}
		return element != null ? (IngridHitDetail) element.getValue() : null;
	}

	@Override
	public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query,
			String[] requestedFields) throws Exception {
		List<IngridHitDetail> list = new ArrayList<IngridHitDetail>();
		for (IngridHit hit : hits) {
			IngridHitDetail detail = getDetail(hit, query, requestedFields);
			list.add(detail);
		}
		return list.toArray(new IngridHitDetail[list.size()]);
	}

	@Override
	public void close() throws Exception {
	}

	@Override
	public void configure(PlugDescription arg0) throws Exception {
		_cacheManager = CacheManager.getInstance();
		_cache = _cacheManager.getCache("ingrid-cache");
		if (_cache == null) {
			if (!_cacheManager.cacheExists("default")) {
				_cache = new Cache("default", 1000, false, false, 600, 600);
				_cacheManager.addCache(_cache);
			} else {
				_cache = _cacheManager.getCache("default");
			}
		}
	}

	public void addToCache(IngridQuery ingridQuery, int start, int length,
			IngridHits hits) {
		String ingridQueryString = getSearchCacheKey(ingridQuery, start, length);
		Element element = new Element(ingridQueryString, hits);
		_cache.put(element);
	}

	public void addToCache(IngridHit ingridHit, String[] requestedFields,
			IngridHitDetail detail) {
		String ingridQueryString = getDetailCacheKey(ingridHit, requestedFields);
		Element element = new Element(ingridQueryString, detail);
		_cache.put(element);
	}

	private String getSearchCacheKey(IngridQuery query, int start, int length) {
		String queryString = query.toString() + "_" + start + "_" + length;
		return queryString;
	}

	private String getDetailCacheKey(IngridHit ingridHit,
			String[] requestedFields) {
		String ingridHitId = ingridHit.getDataSourceId() + "_"
				+ ingridHit.getDocumentId();
		for (String field : requestedFields) {
			ingridHitId += "_" + field;
		}
		return ingridHitId;
	}

}
