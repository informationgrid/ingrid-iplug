/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
package de.ingrid.iplug.ibus;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.ibus.comm.Bus;
import de.ingrid.ibus.comm.net.IPlugProxyFactoryImpl;
import de.ingrid.ibus.service.SettingsService;
import de.ingrid.iplug.DummyPlug;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.*;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.xml.PlugdescriptionSerializer;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeartBeatPlugStartWith2IbussesTest {

    class TestPlug extends HeartBeatPlug {

        public TestPlug(final int period) throws Exception {
            super(period, new PlugDescriptionFieldFilters(), new IMetadataInjector[]{}, new IPreProcessor[]{},
                    new IPostProcessor[]{});
        }

        @Override
        public IngridHits search(final IngridQuery arg0, final int arg1, final int arg2) throws Exception {
            return null;
        }

        @Override
        public IngridHitDetail getDetail(final IngridHit arg0, final IngridQuery arg1, final String[] arg2)
                throws Exception {
            return null;
        }

        @Override
        public IngridHitDetail[] getDetails(final IngridHit[] arg0, final IngridQuery arg1, final String[] arg2)
                throws Exception {
            return null;
        }

        @Override
        public IngridDocument call(IngridCall arg0) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private final File _target = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());

    private PlugDescription _plugDescription;

    private TcpCommunication iBusCom;
    private TcpCommunication iBusCom2;

    @BeforeEach
    public void setUp() throws Exception {
        assertTrue(_target.mkdirs());
        final PlugDescription plugDescription = new PlugDescription();
        plugDescription.setProxyServiceURL("/ingrid-group:iplug-test");
        plugDescription.setMd5Hash("md5-hash");
        plugDescription.setRecordLoader(false);
        final PlugdescriptionSerializer plugdescriptionSerializer = new PlugdescriptionSerializer();
        plugdescriptionSerializer.serialize(plugDescription, new File(_target, "pd.xml"));
        _plugDescription = plugdescriptionSerializer.deSerialize(new File(_target, "pd.xml"));
        MockitoAnnotations.initMocks(this);

        // BusClient can only be created once!
        String communicationFile = Thread.currentThread().getContextClassLoader().getResource("communication2.xml")
                .getPath();
        BusClientFactory.createBusClient(new File(communicationFile), new DummyPlug());
    }

    private void startIBus1() throws Exception {
        // remote proxy - start
        this.iBusCom = new TcpCommunication();
        this.iBusCom.setPeerName("/101tec-group:ibus");
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(9191);
        this.iBusCom.configure(serverConfiguration);
        this.iBusCom.startup();

        Bus bus = new Bus(new IPlugProxyFactoryImpl(this.iBusCom), new SettingsService());
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IBus.class, bus);
        this.iBusCom.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
                messageHandler);
    }

    private void startIBus2() throws Exception {
        // remote proxy - start
        this.iBusCom2 = new TcpCommunication();
        this.iBusCom2.setPeerName("/101tec-group:ibus2");
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(9192);
        this.iBusCom2.configure(serverConfiguration);
        this.iBusCom2.startup();

        Bus bus = new Bus(new IPlugProxyFactoryImpl(this.iBusCom2), new SettingsService());
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IBus.class, bus);
        this.iBusCom2.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
                messageHandler);
    }


    @AfterEach
    public void tearDown() throws Exception {
        System.out.println("Shut down server.");
        this.iBusCom.shutdown();
        this.iBusCom2.shutdown();
        assertTrue(new File(_target, "pd.xml").delete());
        assertTrue(_target.delete());
    }

    @Test
    public void testHeartBeats() throws Exception {
        final HeartBeatPlug plug = new TestPlug(1000);

        plug.configure(_plugDescription);
        System.out.println("Wait for 1 sec.");
        Thread.sleep(1000);
        assertFalse(plug.sendingAccurate());

        System.out.println("Start iBus 1.");
        startIBus1();
        System.out.println("Wait for 3 sec.");
        Thread.sleep(3000);
        assertFalse(plug.sendingAccurate());

        System.out.println("Start iBus 2.");
        startIBus2();
        System.out.println("Wait for 3 sec.");
        Thread.sleep(3000);
        assertTrue(plug.sendingAccurate());

        System.out.println("Restart bus client - Simulate connection problem");
        BusClientFactory.getBusClient().restart();
        plug.reconfigure();

        Thread.sleep(3000);
        assertTrue(plug.sendingAccurate());

        plug.stopHeartBeats();
        BusClientFactory.getBusClient().shutdown();
    }

}
