/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;
import java.io.InputStream;

import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.proxies.ProxyService;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * A server that starts the iplug class as defined in the plugdescription, can
 * also be used as singleton
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class PlugServer {

    private static IPlug fInstance;

    /**
     * reads the plug description from a xml file in the classpath
     * 
     * @return
     * @throws IOException
     */
    public static PlugDescription getPlugDescription() throws IOException {
        InputStream resourceAsStream = PlugServer.class
                .getResourceAsStream("plugdescription.xml");
        XMLSerializer serializer = new XMLSerializer();
        return (PlugDescription) serializer.deSerialize(resourceAsStream);

    }

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

    public static void main(String[] args) throws Exception {
        String usage = "multicastport unicastport";
        if (args.length < 2) {
            System.err.println(usage);
            System.exit(-1);
        }
        int mPort;
        int uPort;
        try {
            mPort = Integer.parseInt(args[0]);
            uPort = Integer.parseInt(args[1]);

            PlugServer.getIPlugInstance();
            // start the communication
            SocketCommunication communication = new SocketCommunication();
            communication.setMulticastPort(mPort);
            communication.setUnicastPort(uPort);

            try {
                communication.startup();
            } catch (IOException e) {
                System.err.println("Cannot start the communication: "
                        + e.getMessage());
            }

            // start the proxy server
            ProxyService proxy = new ProxyService();
            proxy.setCommunication(communication);
            try {
                proxy.startup();
            } catch (IllegalArgumentException e) {
                System.err
                        .println("Wrong arguments supplied to the proxy service: "
                                + e.getMessage());
            } catch (Exception e) {
                System.err.println("Cannot start the proxy server: "
                        + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println(usage);
            System.exit(-1);
        }
    }
}
