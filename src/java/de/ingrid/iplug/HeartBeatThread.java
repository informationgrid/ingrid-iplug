/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import de.ingrid.ibus.Bus;

/**
 * sends the plug description as a kind heard beat continues to the ibus.
 * 
 * created on 09.08.2005
 * @author  sg
 * @version $Revision: 1.3 $
 */
public class HeartBeatThread extends Thread {

    private Bus fBus;

    private PlugDescription fDescription;

    private long fSleepInterval;

    public HeartBeatThread(Bus bus, PlugDescription plugDesc, long sleepInterval) {
        this.fBus = bus;
        this.fDescription = plugDesc;
        this.fSleepInterval = sleepInterval;
    }

    public void run() {
        while (!isInterrupted()) {
            fBus.addIPlug(fDescription);
            try {
                sleep(fSleepInterval);
            } catch (InterruptedException e) {
                // do nothing just continue..
            }
        }
    }

}
