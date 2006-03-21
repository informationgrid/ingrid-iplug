/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.FileInputStream;
import java.io.IOException;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import net.weta.components.peer.PeerService;
import net.weta.components.peer.StartJxtaConfig;
import net.weta.components.proxies.ProxyService;
import net.weta.components.proxies.remote.RemoteInvocationController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.ibus.Bus;
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

    private ICommunication fCommunication;

    private ProxyService fProxy;

    private int fMPort;

    private int fUPort;

    private String fIBusHost;

    private int fIBusPort;

    private IBus fBus;

    private PlugDescription fPlugDescripion;

    private int fSleepInterval;

    private String fJxtaConf;

    private String fIBusUrl;

    private String fPlugUrl;

    /**
     * @param mPort
     * @param uPort
     * @param iBustHost
     * @param iBusPort
     * @throws Throwable
     */
    public HeartBeatThread(int mPort, int uPort, String iBustHost, int iBusPort) throws Throwable {
        this.fSleepInterval = 1000 * 30; // FIXME make this configurable
        this.fMPort = mPort;
        this.fUPort = uPort;
        this.fIBusHost = iBustHost;
        this.fIBusPort = iBusPort;
        connectIBus();
        this.fBus.addPlugDescription(this.fPlugDescripion);
    }

    /**
     * @param jxtaConf
     * @param busUrl
     * @throws Throwable
     */
    public HeartBeatThread(String jxtaConf, String busUrl) throws Throwable {
        this.fSleepInterval = 1000 * 30; // FIXME make this configurable
        this.fJxtaConf = jxtaConf;
        this.fIBusUrl = busUrl;
        connectJxtaBus();
        this.fBus.addPlugDescription(this.fPlugDescripion);
    }

    private void connectJxtaBus() throws Throwable {
        this.fPlugDescripion = PlugServer.getPlugDescription();
        this.fPlugUrl = this.fPlugDescripion.getProxyServiceURL();
        try {
            FileInputStream confIS = new FileInputStream(this.fJxtaConf);
            this.fCommunication = StartJxtaConfig.start(confIS);
            this.fCommunication.subscribeGroup(this.fPlugUrl);
            this.fCommunication.subscribeGroup(this.fIBusUrl);
        } catch (Exception e) {
            System.err.println("Cannot start the communication: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // start the proxy server
        this.fProxy = new ProxyService();
        this.fProxy.setCommunication(this.fCommunication);
        try {
            this.fProxy.startup();
        } catch (IllegalArgumentException e) {
            System.err.println("Wrong arguments supplied to the proxy service: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Cannot start the proxy server: " + e.getMessage());
            System.exit(1);
        }

        RemoteInvocationController ric = this.fProxy.createRemoteInvocationController(this.fIBusUrl);
        this.fBus = (IBus) ric.invoke(Bus.class, Bus.class.getMethod("getInstance", null), null);
    }

    public void run() {
        while (!isInterrupted()) {
            try {
                fBus.addPlugDescription(fPlugDescripion);
            } catch (Throwable t) {
                fLogger.error("unable to connect ibus: " + t.toString());
                shutDownCommunication();
                try {
                    if (this.fCommunication instanceof SocketCommunication) {
                        connectIBus();
                    } else if (this.fCommunication instanceof PeerService) {
                        connectJxtaBus();
                    }
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
        SocketCommunication communication = new SocketCommunication();
        communication.setMulticastPort(fMPort);
        communication.setUnicastPort(fUPort);

        try {
            communication.startup();
            fCommunication = communication;
        } catch (IOException e) {
            System.err.println("Cannot start the communication: " + e.getMessage());
        }

        // start the proxy server
        fProxy = new ProxyService();
        fProxy.setCommunication(fCommunication);

        fProxy.startup();

        // register the IPlug
        String iBusUrl = AddressUtil.getWetagURL(fIBusHost, fIBusPort);
        fPlugDescripion = PlugServer.getPlugDescription();

        RemoteInvocationController ric = fProxy.createRemoteInvocationController(iBusUrl);
        fBus = (IBus) ric.invoke(Bus.class, Bus.class.getMethod("getInstance", null), null);
    }

    private void shutDownCommunication() {
        this.fProxy.shutdown();
        if (this.fCommunication instanceof SocketCommunication) {
            ((SocketCommunication) this.fCommunication).shutdown();
        } else if (this.fCommunication instanceof PeerService) {
            ((PeerService) this.fCommunication).shutdown();
        }
    }
}
