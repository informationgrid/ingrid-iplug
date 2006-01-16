/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;
import java.io.InputStream;

import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import net.weta.components.proxies.ProxyService;
import net.weta.components.proxies.remote.RemoteInvocationController;
import de.ingrid.ibus.Bus;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * A server that starts the iplug class as defined in the plugdescription, can also be used as singleton
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class PlugServer {

    private static IPlug fInstance;

    /**
     * Reads the plug description from a xml file in the classpath.
     * 
     * @return The plug description.
     * @throws IOException
     */
    public static PlugDescription getPlugDescription() throws IOException {
        InputStream resourceAsStream = PlugServer.class.getResourceAsStream("plugdescription.xml");
        XMLSerializer serializer = new XMLSerializer();
        return (PlugDescription) serializer.deSerialize(resourceAsStream);

    }

    /**
     * Returns the IPlug instance.
     * 
     * @return The IPlug instance.
     * @throws Exception
     */
    public static IPlug getIPlugInstance() throws Exception {
        synchronized (PlugServer.class) {
            if (fInstance == null) {
                PlugDescription plugDescription = getPlugDescription();
                String plugClassStr = plugDescription.getIPlugClass();
                Class plugClass = Thread.currentThread().getContextClassLoader().loadClass(plugClassStr);
                fInstance = (IPlug) plugClass.newInstance();
                fInstance.configure(plugDescription);

            }
        }
        return fInstance;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String usage = "multicastport unicastport IBusHost IBusPort";
        if (args.length != 4) {
            System.err.println(usage);
            System.exit(-1);
        }
        int mPort;
        int uPort;
        int iBusPort;
        String iBustHost;
        try {
            mPort = Integer.parseInt(args[0]);
            uPort = Integer.parseInt(args[1]);
            iBustHost = args[2];
            iBusPort = Integer.parseInt(args[3]);

            PlugServer.getIPlugInstance();
            // start the communication
            SocketCommunication communication = new SocketCommunication();
            communication.setMulticastPort(mPort);
            communication.setUnicastPort(uPort);

            try {
                communication.startup();
            } catch (IOException e) {
                System.err.println("Cannot start the communication: " + e.getMessage());
            }

            // start the proxy server
            ProxyService proxy = new ProxyService();
            proxy.setCommunication(communication);
            try {
                proxy.startup();
            } catch (IllegalArgumentException e) {
                System.err.println("Wrong arguments supplied to the proxy service: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Cannot start the proxy server: " + e.getMessage());
            }

            // register the IPlug
            String iBusUrl = AddressUtil.getWetagURL(iBustHost, iBusPort);
            PlugDescription plugDesc = getPlugDescription();

            RemoteInvocationController ric = proxy.createRemoteInvocationController(iBusUrl);
            try {
                ric.invoke(Bus.class, Bus.class.getMethod("addIPlug", new Class[] { PlugDescription.class }),
                        new Object[] { plugDesc });
            } catch (Throwable t) {
                System.err.println("Cannot register IPlug: " + t.getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println(usage);
            System.exit(-1);
        }
    }
}
