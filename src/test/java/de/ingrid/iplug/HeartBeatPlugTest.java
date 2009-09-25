package de.ingrid.iplug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.communication.reflect.ReflectMessage;
import net.weta.components.communication.tcp.TcpCommunication;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

public class HeartBeatPlugTest extends TestCase {

    class TestPlug extends HeartBeatPlug {

        public TestPlug(ICommunication communication, int period) {
            super(communication, period);
        }

        @Override
        public IngridHits search(IngridQuery arg0, int arg1, int arg2) throws Exception {
            return null;
        }

        @Override
        public IngridHitDetail getDetail(IngridHit arg0, IngridQuery arg1, String[] arg2) throws Exception {
            return null;
        }

        @Override
        public IngridHitDetail[] getDetails(IngridHit[] arg0, IngridQuery arg1, String[] arg2) throws Exception {
            return null;
        }

    }

    @Mock
    private TcpCommunication _communication;

    private File _target = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis());

    private PlugDescription _plugDescription;

    @Override
    protected void setUp() throws Exception {
        assertTrue(_target.mkdirs());
        PlugDescription plugDescription = new PlugDescription();
        plugDescription.setProxyServiceURL("testProxy");
        plugDescription.setMd5Hash("md5-hash");
        PlugdescriptionSerializer plugdescriptionSerializer = new PlugdescriptionSerializer();
        plugdescriptionSerializer.serialize(plugDescription, new File(_target, "pd.xml"));
        _plugDescription = plugdescriptionSerializer.deSerialize(new File(_target, "pd.xml"));
        MockitoAnnotations.initMocks(this);
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(new File(_target, "pd.xml").delete());
        assertTrue(_target.delete());
    }

    public void testHeartBeats() throws Exception {
        HeartBeatPlug plug = new TestPlug(_communication, 1000);

        List<String> serverNames = new ArrayList<String>();
        serverNames.add("foo");

        Mockito.when(_communication.getMessageQueue()).thenReturn(new MessageQueue());
        Mockito.when(_communication.getServerNames()).thenReturn(serverNames);
        plug.configure(_plugDescription);
        Mockito.verify(_communication).getMessageQueue();
        Mockito.verify(_communication).getServerNames();

        Mockito.when(_communication.sendSyncMessage(Mockito.any(ReflectMessage.class), Mockito.eq("foo"))).thenReturn(new PayloadMessage(false, ""));
        plug.startHeartBeats();
        Thread.sleep(5000);
        Mockito.verify(_communication, Mockito.times(10)).sendSyncMessage(Mockito.any(Message.class), Mockito.eq("foo"));

        plug.stopHeartBeats();
    }
}
