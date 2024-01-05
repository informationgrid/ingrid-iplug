/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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

import de.ingrid.ibus.service.SettingsService;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.ingrid.ibus.comm.Bus;
import de.ingrid.ibus.comm.net.IPlugProxyFactoryImpl;
import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
public class PlugServerTest {

    private List fCommunications = new ArrayList();

    private File _file = new File("src/conf/communication-test.xml");

    @AfterEach
    public void tearDown() throws Exception {
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
    @Test
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
        Bus bus = new Bus(new IPlugProxyFactoryImpl(communication), new SettingsService());
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
