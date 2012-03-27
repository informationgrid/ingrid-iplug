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

public class HeartBeatPlug2BusesTest extends TestCase {

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
        
        // BusClient can only be created once!
        String communicationFile = Thread.currentThread().getContextClassLoader().getResource("communication2.xml").getPath();
        BusClientFactory.createBusClient(new File(communicationFile), new DummyPlug());
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(new File(_target, "pd.xml").delete());
        assertTrue(_target.delete());
    }

    public void testHeartBeats2BusesManualDisconnect() throws Exception {
        final HeartBeatPlug plug = new TestPlug(3000);

        System.out.println("Configure iPlug");
        plug.configure(_plugDescription);
        Thread.sleep(10000);
        
        System.out.println("DISCONNECT IBUS 1 OR 2 NOW");
        
        System.out.println("wait for connection timeout and reconnect");
        
        // update bus in heartbeat
        Thread.sleep(90000);
        
        System.out.println("waited enough");
    }
    
    public void testHeartBeats2Buses() throws Exception {
        final HeartBeatPlug plug = new TestPlug(3000);

        System.out.println("Configure iPlug");
        plug.configure(_plugDescription);
        Thread.sleep(10000);
        
        System.out.println("Restart bus client - Simulate connection problem");
        BusClientFactory.getBusClient().restart();
        
        System.out.println("wait for connection timeout and reconnect");
        
        // update bus in heartbeat
        Thread.sleep(50000);
        
        System.out.println("End");
    }
    
}
