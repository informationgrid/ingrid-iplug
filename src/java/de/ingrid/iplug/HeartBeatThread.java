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

    protected PlugDescription fPlugDescripion;

    private int fSleepInterval;

    protected HeartBeatThread() throws IOException {
        this.fSleepInterval = 1000 * 30; // FIXME make this configurable
        this.fPlugDescripion = PlugServer.getPlugDescription();
    }

    public void run() {
        while (!isInterrupted()) {
            try {
                this.fBus.addPlugDescription(this.fPlugDescripion);
            } catch (Throwable t) {
                fLogger.error("unable to connect ibus: ",t);
                this.fCommunication.shutdown();
                try {
                    connectToIBus();
                } catch (Throwable e) {
                    fLogger.error("reconnecting IBus failed, will try again later, reason:" + t.toString());
                }
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
     * @throws Throwable
     */
    public void connectToIBus() throws Throwable {
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
        messageHandler.addObjectToCall(IPlug.class, PlugServer.getIPlugInstance());
        this.fCommunication.getMessageQueue().getProcessorRegistry().addMessageHandler(
                ReflectMessageHandler.MESSAGE_TYPE, messageHandler);
    }
}
