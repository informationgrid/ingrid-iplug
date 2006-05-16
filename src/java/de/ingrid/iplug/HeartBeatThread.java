/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.util.PlugShutdownHook;
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

    protected final static Log fLogger = LogFactory.getLog(HeartBeatThread.class);

    private ICommunication fCommunication;

    private String fBusUrl;

    private IBus fBus;

    private int fSleepInterval = 1000 * 30;

    private PlugShutdownHook fShutdownHook;

    private long fLastSendHeartbeat = System.currentTimeMillis();

    protected HeartBeatThread(ICommunication communication, String busUrl, PlugShutdownHook shutdownHook) {
        this.fCommunication = communication;
        this.fBusUrl = busUrl;
        this.fShutdownHook = shutdownHook;
    }

    public void run() {
        fLogger.info("heartbeat for '" + this.fBusUrl + "' started");
        PlugDescription plugDescription;
        try {
            plugDescription = PlugServer.getPlugDescription();
            this.fBus = (IBus) ProxyService.createProxy(this.fCommunication, IBus.class, this.fBusUrl);
            this.fCommunication.subscribeGroup(this.fBusUrl);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        try {
            while (!isInterrupted()) {
                try {
                    String md5Hash = PlugServer.getPlugDescriptionMd5();
                    String plugId = plugDescription.getPlugId();
                    if (!this.fBus.containsPlugDescription(plugId, md5Hash)) {
                        fLogger.info("adding or updating plug description to bus '" + this.fBusUrl + "'");
                        plugDescription = PlugServer.getPlugDescription();
                        plugDescription.setMd5Hash(md5Hash);
                        this.fBus.addPlugDescription(plugDescription);
                    }
                    this.fLastSendHeartbeat = System.currentTimeMillis();
                    this.fShutdownHook.addBus(this.fBusUrl, this.fBus);
                } catch (Throwable t) {
                    this.fShutdownHook.removeBus(this.fBusUrl);
                    if (t instanceof InterruptedException) {
                        throw (InterruptedException) t;
                    } else if (t instanceof UndeclaredThrowableException
                            && t.getCause() instanceof InterruptedException) {
                        throw (InterruptedException) t.getCause();
                    } else if (t instanceof ThreadDeath) {
                        throw new InterruptedException("thread death");
                    }
                    fLogger.error("unable to connect ibus: ", t);
                }
                sleep(this.fSleepInterval);
            }
        } catch (InterruptedException e) {
            fLogger.warn("interrupt heartbeat thread from '" + this.fBusUrl + "'");
            try {
                this.fCommunication.closeConnection(this.fBusUrl);
            } catch (IOException e1) {
                fLogger.warn("problems on closing connection to " + this.fBusUrl);
            }
        }
    }

    /**
     * @return the url of the bus
     */
    public String getBusUrl() {
        return this.fBusUrl;
    }

    /**
     * @return the ibus of this heartbeat
     */
    public IBus getIBus() {
        return this.fBus;
    }

    /**
     * @return the time of the last send heartbeat
     */
    public long getLastSendHeartbeat() {
        return this.fLastSendHeartbeat;
    }

    /**
     * @return how long the heart sleeps between the beats
     */
    public int getSleepInterval() {
        return this.fSleepInterval;
    }

    /**
     * @param sleepIntervall
     */
    public void setSleepInterval(int sleepIntervall) {
        this.fSleepInterval = sleepIntervall;
    }
}
