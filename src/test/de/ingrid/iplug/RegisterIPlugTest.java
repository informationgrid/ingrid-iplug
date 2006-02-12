/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: $
 */

package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import net.weta.components.proxies.ProxyService;
import net.weta.components.proxies.remote.RemoteInvocationController;

import de.ingrid.ibus.Bus;
import de.ingrid.ibus.registry.Registry;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * 
 */
public class RegisterIPlugTest extends TestCase {

    private Log fLOGGER = LogFactory.getLog(this.getClass());

    private File fSerFile = null;

    private String fBusUrl = null;

    private ICommunication fCommunication;

    private PlugDescription fPlugDesc;

    private ProxyService fPs;

    private RemoteInvocationController fRic;


    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        this.fBusUrl = AddressUtil.getWetagURL("localhost", 9192);

        SocketCommunication iPlugCom = new SocketCommunication();
        iPlugCom.setMulticastPort(9193);
        iPlugCom.setUnicastPort(9194);
        iPlugCom.startup();
        this.fCommunication = iPlugCom;

        // remote proxy - start
        SocketCommunication com = new SocketCommunication();
        com.setMulticastPort(9191);
        com.setUnicastPort(9192);
        com.startup();

        this.fPs = new ProxyService();
        this.fPs.setCommunication(com);
        this.fPs.startup();
        // remote proxy - end

        this.fSerFile = File.createTempFile("RegisterIPlugTest", "ser");

        this.fPlugDesc = new PlugDescription();
        this.fPlugDesc.setPlugId("plugID");

        XMLSerializer xmlSer = new XMLSerializer();
        xmlSer.serialize(this.fPlugDesc, this.fSerFile);
    }

    /**
     * @throws Throwable 
     * @throws Exception 
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * 
     */
    public void testIPlugRegistration() throws SecurityException, NoSuchMethodException, Exception, Throwable {
        Bus bus = new Bus(null);
        fRic = new RemoteInvocationController(this.fCommunication, this.fBusUrl);

        PlugDescription pd = loadXMLSerializable(new FileInputStream(
                this.fSerFile));
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
//        RemoteInvocationController ric = new RemoteInvocationController(this.fCommunication, busUrl);
        try {
            Bus bus = (Bus) fRic.invoke(Bus.class, Bus.class.getMethod("getInstance", null), null);
            bus.addPlugDescription(plugDesc);
        } catch (Throwable t) {
            this.fLOGGER.error("Cannot register IPlug: " + t.getMessage(), t);
        }
    }
}
