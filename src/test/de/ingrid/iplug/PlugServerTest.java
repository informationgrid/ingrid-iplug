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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;
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

    private File _file = null;

    protected void setUp() throws Exception {
        _file = File.createTempFile("plugTest", "properties");
        Properties properties = new Properties();
        properties.put("Server.1", "127.0.0.1:9191");
        properties.put("ServerName.1", "/101tec-group:ibus");
        properties.put("IsCommunicationServer", "false");
        properties.put("PeerName", "bus-client");
        properties.put("Proxy", "127.0.0.1:8080");
        properties.store(new FileOutputStream(_file), "");
    }

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

        // PlugDescription plugDescription2 = createPlugDescription(plugPort2,
        // busPort);
        // PlugServer plugServer2= new new PlugServer(plugDescription1, file,
        // file, 2000);
        // Thread.sleep(2000);
        // assertNotNull(bus.getIPlug(plugDescription1.getPlugId()));
        // assertNotNull(bus.getIPlug(plugDescription2.getPlugId()));
        // assertEquals(2, bus.getAllIPlugs().length);
        // plugServer2.shutdown();
    }

    //    
    // /**
    // * @throws Exception
    // */
    // public void test2BusHeartBeat() throws Exception {
    // int busPort1 = 11001;
    // int busPort2 = 11002;
    // int plugPort = 21001;
    // IBus bus1 = createBus(busPort1);
    // IBus bus2 = createBus(busPort2);
    //        
    // PlugDescription plugDescription = createPlugDescription(plugPort,
    // busPort1);
    // plugDescription.addBusUrl(AddressUtil.getWetagURL("localhost",busPort2));
    // PlugServer plugServer=new TestPlugServer(plugDescription, plugPort, 1,
    // 2000);
    // Thread.sleep(1000);
    //        
    // assertNotNull(bus1.getIPlug(plugDescription.getPlugId()));
    // assertEquals(1, bus1.getAllIPlugs().length);
    // assertNotNull(bus2.getIPlug(plugDescription.getPlugId()));
    // assertEquals(1, bus2.getAllIPlugs().length);
    // plugServer.shutdown();
    // }
    //    
    // /**
    // * @throws Exception
    // */
    // public void testRestartHeartbeat() throws Exception {
    // int busPort = 12001;
    // int plugPort = 22001;
    // PlugDescription plugDescription = createPlugDescription(plugPort,
    // busPort);
    // PlugServer plugServer=new TestPlugServer(plugDescription, plugPort, 1,
    // 500);
    // Thread.sleep(2000);
    //        
    // IBus bus = createBus(busPort);
    // Thread.sleep(8000);
    // assertNotNull(bus.getIPlug(plugDescription.getPlugId()));
    // assertEquals(1, bus.getAllIPlugs().length);
    // plugServer.shutdown();
    // }

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
        communication.addServer("127.0.0.1:9191");
        communication.setIsCommunicationServer(true);
        communication.startup();
        this.fCommunications.add(communication);
        return communication;
    }

    // class TestPlugServer extends PlugServer {
    //
    // private PlugDescription fPlugDescription;
    //
    // /**
    // * @param plugDescription
    // * @param unicastPort
    // * @param multicastPort
    // * @param heartBeatIntervall
    // * @throws Exception
    // */
    // public TestPlugServer(PlugDescription plugDescription, int unicastPort,
    // int multicastPort,
    // int heartBeatIntervall) throws Exception {
    // super(plugDescription, unicastPort, multicastPort, heartBeatIntervall);
    // this.fPlugDescription = plugDescription;
    // }
    //
    // protected PlugDescription loadPlugDescription() throws IOException {
    // return this.fPlugDescription;
    // }
    // }
}
