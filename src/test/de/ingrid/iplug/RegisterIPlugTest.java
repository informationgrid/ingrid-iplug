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

import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import net.weta.components.proxies.ProxyService;
import net.weta.components.proxies.remote.RemoteInvocationController;

import org.apache.log4j.Logger;

import de.ingrid.ibus.Bus;
import de.ingrid.ibus.registry.Registry;
import de.ingrid.utils.xml.XMLSerializer;

/**
 *  
 */
public class RegisterIPlugTest extends TestCase {

    private Logger fLOGGER = Logger.getLogger(this.getClass());

    private File fSerFile = null;

    private String fBusUrl = null;

    private ICommunication fCommunication;

    private PlugDescription fPlugDesc;

    private ProxyService fPs;

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
     * @throws IOException
     * @throws FileNotFoundException
     *  
     */
    public void testIPlugRegistration() throws FileNotFoundException, IOException {
        Bus bus = new Bus(null);
        PlugDescription pd = loadXMLSerializable(new FileInputStream(this.fSerFile));
        registerIPlug(pd, this.fBusUrl);

        Registry reg = bus.getIPlugRegistry();
        assertNotNull(reg);

        pd = reg.getIPlug(this.fPlugDesc.getPlugId());
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
        RemoteInvocationController ric = new RemoteInvocationController(this.fCommunication, busUrl);
        try {
            ric.invoke(Bus.class, Bus.class.getMethod("addIPlug", new Class[] { PlugDescription.class }),
                    new Object[] { plugDesc });
        } catch (Throwable t) {
            this.fLOGGER.error("Cannot register IPlug: " + t.getMessage(), t);
        }
    }
}
