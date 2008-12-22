package de.ingrid.iplug;

import junit.framework.TestCase;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class CacheSearcherTest extends TestCase {

	public void testSearch() throws Exception {
		CacheSearcher cacheSearcher = new CacheSearcher();
		cacheSearcher.configure(null);
		for (int i = 0; i < 10; i++) {
			final IngridQuery query = QueryStringParser.parse("foo" + i);
			IngridHits hits = cacheSearcher.search(query, 10, 10);
			assertNull(hits);
			IngridHits ingridHits = new IngridHits();
			ingridHits.put("foo", "bar");
			cacheSearcher.addToCache(query, 10, 10, ingridHits);

			hits = cacheSearcher.search(query, 10, 10);
			assertNotNull(hits);
			assertEquals(ingridHits, hits);
		}

		for (int i = 0; i < 10; i++) {
			final IngridQuery query = QueryStringParser.parse("foo" + i);
			IngridHits hits = cacheSearcher.search(query, 10, 10);
			assertNotNull(hits);
		}

	}

	public void testGetDetail() throws Exception {

		CacheSearcher cacheSearcher = new CacheSearcher();
		cacheSearcher.configure(null);

		for (int i = 0; i < 10; i++) {

			// define query
			final IngridQuery query = QueryStringParser.parse("foo" + i);

			// define hit
			IngridHit ingridHit = new IngridHit("plugId", i, i, i);

			// get details from hit
			IngridHitDetail detail = cacheSearcher.getDetail(ingridHit, query,
					new String[] { "content" });

			// no cached hit
			assertNull(detail);

			// setup hit detail
			IngridHitDetail ingridHitDetail = new IngridHitDetail();
			ingridHitDetail.put("foo", "bar");

			// add detail to cache
			cacheSearcher.addToCache(ingridHit, new String[] { "content" },
					ingridHitDetail);

			detail = cacheSearcher.getDetail(ingridHit, query,
					new String[] { "content" });
			assertNotNull(detail);
			assertEquals(ingridHitDetail, detail);
		}

		
		for (int i = 0; i < 10; i++) {
			// define query
			final IngridQuery query = QueryStringParser.parse("foo" + i);
			// define hit
			IngridHit ingridHit = new IngridHit("plugId", i, i, i);
			// get details from hit
			IngridHitDetail detail = cacheSearcher.getDetail(ingridHit, query,
					new String[] { "content" });
			// no cached hit
			assertNotNull(detail);
		}
	}
}
