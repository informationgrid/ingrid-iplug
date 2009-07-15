
package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;
import de.ingrid.ibus.Bus;
import de.ingrid.ibus.net.IPlugProxyFactoryImpl;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.ManifestMetadataInjector;
import de.ingrid.utils.metadata.Metadata;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * 
 */
public class RegisterIPlugTest extends TestCase {

    private File fSerFile = null;

    private String fBusUrl = null;

    private TcpCommunication iPlugCom;

    private TcpCommunication iBusCom;

    private PlugDescription fPlugDesc;

    protected void setUp() throws Exception {
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

    protected void tearDown() throws Exception {
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
    public void testIPlugRegistration() throws SecurityException, Exception, Throwable {
        Bus bus = new Bus(new IPlugProxyFactoryImpl(this.iBusCom));
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