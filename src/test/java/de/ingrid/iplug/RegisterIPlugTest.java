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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.ingrid.ibus.service.SettingsService;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ingrid.ibus.comm.Bus;
import de.ingrid.ibus.comm.net.IPlugProxyFactoryImpl;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.ManifestMetadataInjector;
import de.ingrid.utils.metadata.Metadata;
import de.ingrid.utils.xml.XMLSerializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 
 */
public class RegisterIPlugTest {

    private File fSerFile = null;

    private String fBusUrl = null;

    private TcpCommunication iPlugCom;

    private TcpCommunication iBusCom;

    private PlugDescription fPlugDesc;

    @BeforeEach
    public void setUp() throws Exception {
        // remote proxy - start
        this.iBusCom = new TcpCommunication();
        this.iBusCom.setPeerName("/101tec-group:ibus");
        ServerConfiguration serverConfiguration = new ServerConfiguration();
		serverConfiguration.setPort(9191);
		this.iBusCom.configure(serverConfiguration);
        this.iBusCom.startup();

        
        this.fBusUrl = "/101tec-group:ibus";
        this.iPlugCom = new TcpCommunication();
        ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setName("/101tec-group:iplug");
		ClientConnection clientConnection = clientConfiguration.new ClientConnection();
		clientConnection.setServerIp("127.0.0.1");
		clientConnection.setServerPort(9191);
		clientConnection.setServerName("/101tec-group:ibus");
		clientConfiguration.addClientConnection(clientConnection);
		this.iPlugCom.configure(clientConfiguration);
        this.iPlugCom.startup();
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IPlug.class, new DummyPlug());
        this.iPlugCom.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
                messageHandler);


        this.fSerFile = File.createTempFile("RegisterIPlugTest", "ser");

        this.fPlugDesc = new PlugDescription();
        this.fPlugDesc.setProxyServiceURL("/101tec-group:iplug");
        this.fPlugDesc.setRecordLoader(false);
        this.fPlugDesc.setIPlugClass("org.springframework.beans.factory.xml.XmlBeanFactory");
		ManifestMetadataInjector mdi = new ManifestMetadataInjector();
		mdi.configure(this.fPlugDesc);
		Metadata metadata = new Metadata();
		mdi.injectMetaDatas(metadata);
		this.fPlugDesc.setMetadata(metadata);

        XMLSerializer xmlSer = new XMLSerializer();
        xmlSer.serialize(this.fPlugDesc, this.fSerFile);
        Thread.sleep(200);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.iBusCom.closeConnection("/101tec-group:iplug");
        this.iBusCom.shutdown();
        this.iPlugCom.closeConnection(null);
        this.iPlugCom.shutdown();
    }

    /**
     * @throws Throwable
     * @throws Exception
     * @throws SecurityException
     * 
     */
    @Test
    public void testIPlugRegistration() throws SecurityException, Exception, Throwable {
        Bus bus = new Bus(new IPlugProxyFactoryImpl(this.iBusCom), new SettingsService());
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IBus.class, bus);
        this.iBusCom.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
                messageHandler);

        PlugDescription pd = loadXMLSerializable(new FileInputStream(this.fSerFile));
        pd.setMd5Hash("fPlugDesc.getPlugId()");
        registerIPlug(pd, this.fBusUrl);

        System.out.println("the bus: " + bus);
        pd = bus.getIPlugRegistry().getPlugDescription(pd.getPlugId());
        assertNotNull(pd);
    }

    /**
     * @param fis
     * @return The deserialized PlugDescription from a given file.
     * @throws IOException
     */
    public PlugDescription loadXMLSerializable(InputStream fis) throws IOException {
        PlugDescription result = null;

        XMLSerializer xmlSer = new XMLSerializer();
        result = (PlugDescription) xmlSer.deSerialize(fis);

        return result;
    }

    /**
     * @param plugDesc
     * @param busUrl
     */
    public void registerIPlug(PlugDescription plugDesc, String busUrl) {
        IBus bus = (IBus) ProxyService.createProxy(this.iPlugCom, IBus.class, busUrl);
        bus.addPlugDescription(plugDesc);
    }
}
