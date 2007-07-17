
package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.TcpCommunication;
import de.ingrid.ibus.Bus;
import de.ingrid.ibus.net.IPlugProxyFactoryImpl;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;
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
        this.iBusCom.addServer("127.0.0.1:9191");
        this.iBusCom.setIsCommunicationServer(true);
        this.iBusCom.startup();

        
        this.fBusUrl = "/101tec-group:ibus";
        this.iPlugCom = new TcpCommunication();
        this.iPlugCom.setPeerName("/101tec-group:iplug");
        this.iPlugCom.addServer("127.0.0.1:9191");
        this.iPlugCom.addServerName("/101tec-group:ibus");
        this.iPlugCom.setIsCommunicationServer(false);
        this.iPlugCom.startup();
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IPlug.class, new DummyPlug());
        this.iPlugCom.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
                messageHandler);


        this.fSerFile = File.createTempFile("RegisterIPlugTest", "ser");

        this.fPlugDesc = new PlugDescription();
        this.fPlugDesc.setProxyServiceURL("/101tec-group:iplug");
        this.fPlugDesc.setRecordLoader(false);

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