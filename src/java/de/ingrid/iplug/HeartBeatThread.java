/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.util.PlugShutdownHook;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;

/**
 * sends the plug description as a kind heard beat continues to the ibus.
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public abstract class HeartBeatThread extends Thread {

    protected final static Log fLogger = LogFactory.getLog(HeartBeatThread.class);

    private ICommunication fCommunication;

    private IBus fBus;

    private IPlug fPlug;

    protected PlugDescription fPlugDescripion;

    private int fSleepInterval;

    private PlugShutdownHook fShutdownHook;

    protected HeartBeatThread(IPlug plug, PlugShutdownHook shutdownHook) throws IOException {
        this.fPlug = plug;
        this.fShutdownHook = shutdownHook;
        this.fSleepInterval = 1000 * 30; // FIXME make this configurable
        this.fPlugDescripion = PlugServer.getPlugDescription();
    }

    public void run() {
        try {
            connectToIBus();
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        while (!isInterrupted()) {
            try {
                this.fBus.addPlugDescription(this.fPlugDescripion);
                this.fShutdownHook.addBus(getIBusUrl(), this.fBus);
            } catch (Throwable t) {
                fLogger.error("unable to connect ibus: ", t);
                this.fShutdownHook.removeBus(getIBusUrl());
            }
            try {
                sleep(this.fSleepInterval);
            } catch (InterruptedException e) {
                // do nothing just continue..
            }
        }
    }

    protected abstract ICommunication initCommunication() throws Exception;

    protected abstract String getIBusUrl();

    /**
     * @return the ibus of this heartbeat
     */
    public IBus getIBus() {
        return this.fBus;
    }

    private void connectToIBus() throws Exception {
        this.fCommunication = initCommunication();
        startProxyService();
        createBusProxy();
    }

    private void createBusProxy() {
        String iBusUrl = getIBusUrl();
        this.fBus = (IBus) ProxyService.createProxy(this.fCommunication, IBus.class, iBusUrl);
    }

    private void startProxyService() throws Exception {
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IPlug.class, this.fPlug);
        this.fCommunication.getMessageQueue().getProcessorRegistry().addMessageHandler(
                ReflectMessageHandler.MESSAGE_TYPE, messageHandler);
    }
}
