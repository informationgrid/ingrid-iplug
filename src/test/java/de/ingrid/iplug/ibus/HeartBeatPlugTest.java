package de.ingrid.iplug.ibus;

import java.io.File;

import junit.framework.TestCase;
import net.weta.components.communication.tcp.TcpCommunication;

import org.mockito.MockitoAnnotations;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.iplug.DummyPlug;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

public class HeartBeatPlugTest extends TestCase {

    class TestPlug extends HeartBeatPlug {

        public TestPlug(final int period) throws Exception {
            super(period, new PlugDescriptionFieldFilters(), new IMetadataInjector[] {}, new IPreProcessor[] {}, new IPostProcessor[] {});
        }

        @Override
        public IngridHits search(final IngridQuery arg0, final int arg1, final int arg2) throws Exception {
            return null;
        }

        @Override
        public IngridHitDetail getDetail(final IngridHit arg0, final IngridQuery arg1, final String[] arg2) throws Exception {
            return null;
        }

        @Override
        public IngridHitDetail[] getDetails(final IngridHit[] arg0, final IngridQuery arg1, final String[] arg2) throws Exception {
            return null;
        }
    }

    private final File _communicationXml = new File("communicatino.xml");

    private final File _target = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());

    private PlugDescription _plugDescription;

    private TcpCommunication iBusCom;

    private String fBusUrl;

    private TcpCommunication iPlugCom;

    @Override
    protected void setUp() throws Exception {
        assertTrue(_target.mkdirs());
        final PlugDescription plugDescription = new PlugDescription();
        plugDescription.setProxyServiceURL("testProxy");
        plugDescription.setMd5Hash("md5-hash");
        plugDescription.setRecordLoader(false);
        final PlugdescriptionSerializer plugdescriptionSerializer = new PlugdescriptionSerializer();
        plugdescriptionSerializer.serialize(plugDescription, new File(_target, "pd.xml"));
        _plugDescription = plugdescriptionSerializer.deSerialize(new File(_target, "pd.xml"));
        MockitoAnnotations.initMocks(this);
        
     // remote proxy - start
        /*this.iBusCom = new TcpCommunication();
        this.iBusCom.setPeerName("/101tec-group:ibus");
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(9191);
        this.iBusCom.configure(serverConfiguration);
        this.iBusCom.startup();*/

        
        //this.fBusUrl = "/101tec-group:ibus";
        /*this.iPlugCom = new TcpCommunication();
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
        */
        
        // BusClient can only be created once!
        String communicationFile = Thread.currentThread().getContextClassLoader().getResource("communication.xml").getPath();
        BusClientFactory.createBusClient(new File(communicationFile), new DummyPlug());
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(new File(_target, "pd.xml").delete());
        assertTrue(_target.delete());
    }

    public void testHeartBeats() throws Exception {
        final HeartBeatPlug plug = new TestPlug(5000);

        plug.configure(_plugDescription);
        Thread.sleep(5000);
        
        System.out.println("Restart bus client - Simulate connection problem");
        BusClientFactory.getBusClient().restart();
        
        System.out.println("Sleep 50s");
        Thread.sleep(50000);
        BusClientFactory.getBusClient().shutdown();
    }
    
}
