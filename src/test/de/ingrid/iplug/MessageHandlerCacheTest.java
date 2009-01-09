package de.ingrid.iplug;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.reflect.ReflectMessage;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class MessageHandlerCacheTest extends TestCase {

	public class TestHandler implements IMessageHandler {

		private int _counter;
		@Override
		public Message handleMessage(Message arg0) {
			_counter++;
			return new Message();
		}

	}

	public void testReflectMessage() throws Exception {
		
		TestHandler testHandler = new TestHandler();
		MessageHandlerCache cache = new MessageHandlerCache(testHandler);

		assertEquals(0, testHandler._counter);
		for (int i = 0; i < 10; i++) {
			IngridQuery ingridQuery = QueryStringParser.parse("foo" + i
					+ " cache:on");
			cache.handleMessage(new ReflectMessage("search", IPlug.class
					.getName(), new Object[] { ingridQuery, 10, 10 }));
		}
		assertEquals(10, testHandler._counter);
		
		for (int i = 0; i < 10; i++) {
			IngridQuery ingridQuery = QueryStringParser.parse("foo" + i
					+ " cache:on");
			cache.handleMessage(new ReflectMessage("search", IPlug.class
					.getName(), new Object[] { ingridQuery, 10, 10 }));
		}
		assertEquals(10, testHandler._counter);
		
	}
}
