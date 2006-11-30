/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Random;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.util.PlugShutdownHook;
import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;

/**
 * Sends the plug description as a heart beat continuesly to the ibus.
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class HeartBeatThread extends Thread {

    protected final static Log fLogger = LogFactory.getLog(HeartBeatThread.class);

    private ICommunication fCommunication;

    private String fBusUrl;

    private IBus fBus;

    private Random fRandom = new Random(System.currentTimeMillis());

    private int fSleepInterval = 1000 * 30;

    private PlugShutdownHook fShutdownHook;

    private long fLastSendHeartbeat = System.currentTimeMillis();

    protected HeartBeatThread(ICommunication communication, String busUrl, PlugShutdownHook shutdownHook) {
        this.fCommunication = communication;
        this.fBusUrl = busUrl;
        this.fShutdownHook = shutdownHook;
    }

    public void run() {
        if (fLogger.isInfoEnabled()) {
            fLogger.info("heartbeat for '" + this.fBusUrl + "' started");
        }
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
                        if (fLogger.isInfoEnabled()) {
                            fLogger.info("adding or updating plug description to bus '" + this.fBusUrl + "'...");
                        }
                        plugDescription = PlugServer.getPlugDescription();
                        plugDescription.setMd5Hash(md5Hash);
                        this.fBus.addPlugDescription(plugDescription);
                        if (fLogger.isInfoEnabled()) {
                            fLogger.info("added or updated plug description to bus '" + this.fBusUrl + '\'');
                        }
                    } else {
                        if (fLogger.isDebugEnabled()) {
                            fLogger.debug("I am currently connected. Higher the heartbeat intervall.");
                        }
                        this.fSleepInterval = (this.fRandom.nextInt(60) + 30) * 1000;
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
                    if (fLogger.isErrorEnabled()) {
                        fLogger.error("unable to connect ibus: ", t);
                    }
                    this.fSleepInterval = 1000 * 30;
                }
                sleep(this.fSleepInterval);
            }
        } catch (InterruptedException e) {
            if (fLogger.isWarnEnabled()) {
                fLogger.warn("interrupt heartbeat thread from '" + this.fBusUrl + "'");
            }
            try {
                this.fCommunication.closeConnection(this.fBusUrl);
            } catch (IOException e1) {
                if (fLogger.isWarnEnabled()) {
                    fLogger.warn("problems on closing connection to " + this.fBusUrl);
                }
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
     * Returns the ibus for this heartbeat
     * @return The ibus for this heartbeat.
     */
    public IBus getIBus() {
        return this.fBus;
    }

    /**
     * Returns the time of the last sent heartbeat.
     * @return The time of the last sent heartbeat.
     */
    public long getLastSendHeartbeat() {
        return this.fLastSendHeartbeat;
    }

    /**
     * Returns the time between two heart beats.
     * @return How long the heart sleeps between the beats.
     */
    public int getSleepInterval() {
        return this.fSleepInterval;
    }

    /**
     * Sets the time between two heart beats.
     * @param sleepIntervall The time between two heart beats.
     */
    public void setSleepInterval(int sleepIntervall) {
        this.fSleepInterval = sleepIntervall;
    }
}
