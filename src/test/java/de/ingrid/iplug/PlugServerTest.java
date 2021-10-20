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
/*
 * Copyright 2004-2005 weta group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  $Source:  $
 */

package de.ingrid.iplug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;
import de.ingrid.ibus.Bus;
import de.ingrid.ibus.net.IPlugProxyFactoryImpl;
import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;

/**
 * Test for {@link de.ingrid.iplug.PlugServer}.
 * 
 * <p/>created on 16.05.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 * 
 */
public class PlugServerTest extends TestCase {

    private List fCommunications = new ArrayList();

    private File _file = new File("src/conf/communication-test.xml");

    protected void tearDown() throws Exception {
        Thread.sleep(500);
        for (Iterator iter = this.fCommunications.iterator(); iter.hasNext();) {
            ICommunication communication = (ICommunication) iter.next();
            communication.shutdown();
        }
        Thread.sleep(1000);
    }

    /**
     * @throws Exception
     */
    public void test2PlugsHeartBeat() throws Exception {
        IBus bus = createBus();
        PlugDescription plugDescription1 = createPlugDescription();
        PlugServer plugServer1 = new PlugServer(plugDescription1, _file, _file, 2000);
        plugServer1.initPlugServer();
        Thread.sleep(5000);
         assertNotNull(bus.getIPlug(plugDescription1.getPlugId()));
         assertEquals(1, bus.getAllIPlugs().length);
         plugServer1.shutdown();

    }

    private PlugDescription createPlugDescription() {
        PlugDescription plugDescription = new PlugDescription();
        plugDescription.setProxyServiceURL("/101tec-group:iplug");
        plugDescription.setRecordLoader(false);
        plugDescription.addBusUrl("/101tec-group:ibus");
        plugDescription.setMd5Hash(plugDescription.getProxyServiceURL());
        plugDescription.setIPlugClass(DummyPlug.class.getName());

        return plugDescription;
    }

    private IBus createBus() throws IOException {
        ICommunication communication = createCommunication();
        Bus bus = new Bus(new IPlugProxyFactoryImpl(communication));
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IBus.class, bus);
        communication.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
                messageHandler);

        return bus;
    }

    private ICommunication createCommunication() throws IOException {
        TcpCommunication communication = new TcpCommunication();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
		serverConfiguration.setPort(9191);
        communication.configure(serverConfiguration);
        communication.startup();
        this.fCommunications.add(communication);
        return communication;
    }
}
