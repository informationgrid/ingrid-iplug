/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
            IngridQuery ingridQuery = QueryStringParser.parse("foo" + i + " cache: true");
            ingridQuery.put("BUS_URL", "a_bus");
            cache.handleMessage(new ReflectMessage("search", IPlug.class.getName(), new Object[] { ingridQuery, 10, i }));
        }
        assertEquals(10, testHandler._counter);

        for (int i = 0; i < 10; i++) {
            IngridQuery ingridQuery = QueryStringParser.parse("foo" + i + " cache: true");
            ingridQuery.put("BUS_URL", "a_bus");
            cache.handleMessage(new ReflectMessage("search", IPlug.class.getName(), new Object[] { ingridQuery, 10, i }));
        }
        assertEquals(10, testHandler._counter);

        for (int i = 0; i < 10; i++) {
            final IngridQuery ingridQuery = QueryStringParser.parse("foo" + i + " cache: true");
            ingridQuery.put("BUS_URL", "another_bus");
            cache.handleMessage(new ReflectMessage("search", IPlug.class.getName(), new Object[] { ingridQuery, 10, i }));
        }
        assertEquals(20, testHandler._counter);
    }
}
