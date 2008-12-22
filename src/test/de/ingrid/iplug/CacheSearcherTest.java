package de.ingrid.iplug;

import junit.framework.TestCase;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class CacheSearcherTest extends TestCase {

	public void testCache() throws Exception {
		CacheSearcher cacheSearcher = new CacheSearcher();
		for (int i = 0; i < 10; i++) {
			final IngridQuery query = QueryStringParser.parse("foo" + i);
			IngridHits hits = cacheSearcher.search(query, 10, 10);
			assertNull(hits);
			IngridHits ingridHits = new IngridHits();
			ingridHits.put("foo", "bar");
			cacheSearcher.addToCache(query, ingridHits);

			hits = cacheSearcher.search(query, 10, 10);
			assertNotNull(hits);
			assertEquals(ingridHits, hits);
		}
	}
}
