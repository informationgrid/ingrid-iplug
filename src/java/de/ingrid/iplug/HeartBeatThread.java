/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;

import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import net.weta.components.proxies.ProxyService;
import net.weta.components.proxies.remote.RemoteInvocationController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;

/**
 * sends the plug description as a kind heard beat continues to the ibus.
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class HeartBeatThread extends Thread {

    private final static Log fLogger = LogFactory.getLog(HeartBeatThread.class);

    private SocketCommunication fCommunication;

    private ProxyService fProxy;

    private int fMPort;

    private int fUPort;

    private String fIBustHost;

    private int fIBusPort;

    private IBus fBus;

    private PlugDescription fPlugDescripion;

    private int fSleepInterval;

    public HeartBeatThread(int mPort, int uPort, String iBustHost, int iBusPort) throws Throwable {
        fSleepInterval = 1000 * 30; // FIXME make this configurable
        fMPort = mPort;
        fUPort = uPort;
        fIBustHost = iBustHost;
        fIBusPort = iBusPort;
        connectIBus();
        fBus.addPlugDescription(fPlugDescripion);
    }

    public void run() {
        while (!isInterrupted()) {
            try {
                fBus.addPlugDescription(fPlugDescripion);
            } catch (Throwable t) {
                fLogger.error("unable to connect ibus: " + t.toString());
                shutDownCommunication();
                try {
                    connectIBus();
                } catch (Throwable e) {
                    fLogger.error("reconnecting IBus failed, will try again later, reason:" + t.toString());
                }
            }

            try {
                sleep(fSleepInterval);
            } catch (InterruptedException e) {
                // do nothing just continue..
            }
        }
    }

    private void connectIBus() throws Throwable {

        // start the communication
        fCommunication = new SocketCommunication();
        fCommunication.setMulticastPort(fMPort);
        fCommunication.setUnicastPort(fUPort);

        try {
            fCommunication.startup();
        } catch (IOException e) {
            System.err.println("Cannot start the communication: " + e.getMessage());
        }

        // start the proxy server
        fProxy = new ProxyService();
        fProxy.setCommunication(fCommunication);

        fProxy.startup();

        // register the IPlug
        String iBusUrl = AddressUtil.getWetagURL(fIBustHost, fIBusPort);
        fPlugDescripion = PlugServer.getPlugDescription();

        RemoteInvocationController ric = fProxy.createRemoteInvocationController(iBusUrl);
        fBus = (IBus) ric.invoke(IBus.class, IBus.class.getMethod("getInstance", null), null);

    }

    private void shutDownCommunication() {
        fProxy.shutdown();
        fCommunication.shutdown();
    }

}
