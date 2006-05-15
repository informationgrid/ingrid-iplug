/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: $
 */

package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import de.ingrid.ibus.Bus;
import de.ingrid.ibus.net.IPlugProxyFactoryImpl;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * 
 */
public class RegisterIPlugTest extends TestCase {

    private File fSerFile = null;

    private String fBusUrl = null;

    private SocketCommunication iPlugCom;

    private SocketCommunication iBusCom;

    private PlugDescription fPlugDesc;

    protected void setUp() throws Exception {
        this.fBusUrl = AddressUtil.getWetagURL("localhost", 9192);
        this.iPlugCom=new SocketCommunication();
        this.iPlugCom.setMulticastPort(9193);
        this.iPlugCom.setUnicastPort(9194);
        this.iPlugCom.startup();
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IPlug.class, new DummyPlug());
        this.iPlugCom.getMessageQueue().getProcessorRegistry().addMessageHandler(
                ReflectMessageHandler.MESSAGE_TYPE, messageHandler);

        // remote proxy - start
        this.iBusCom=new SocketCommunication();
        this.iBusCom.setMulticastPort(9191);
        this.iBusCom.setUnicastPort(9192);
        this.iBusCom.startup();

        this.fSerFile = File.createTempFile("RegisterIPlugTest", "ser");

        this.fPlugDesc = new PlugDescription();
        this.fPlugDesc.setProxyServiceURL(AddressUtil.getWetagURL("localhost", 9194));
        this.fPlugDesc.setRecordLoader(false);
        

        XMLSerializer xmlSer = new XMLSerializer();
        xmlSer.serialize(this.fPlugDesc, this.fSerFile);
        Thread.sleep(200);
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
        IBus bus = (IBus) ProxyService.createProxy(this.iPlugCom,IBus.class, busUrl);
        bus.addPlugDescription(plugDesc);
    }
    
    class DummyPlug implements IPlug{

        public void configure(PlugDescription plugDescription) throws Exception {
            // TODO Auto-generated method stub
            
        }

        public void close() throws Exception {
            // TODO Auto-generated method stub
            
        }

        public IngridHits search(IngridQuery query, int start, int length) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] requestedFields) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] requestedFields) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
