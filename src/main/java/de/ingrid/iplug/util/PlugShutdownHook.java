/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright 2004-2005 weta group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  $Source:  $
 */

package de.ingrid.iplug.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.PlugServer;
import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;

/**
 * ShutdownHook for an IPlug. Closes the plug and deregister it from all busses
 * on jvm shutdown.
 * 
 * <p/>created on 28.04.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 * 
 */
public class PlugShutdownHook extends Thread {

    protected static Log fLogger = LogFactory.getLog(PlugShutdownHook.class);

    protected PlugServer fPlugServer;

    protected PlugDescription fPlugDescription;

    private Map fBusByUrl = new HashMap(3);

    /**
     * Initializes the the PlugShutdownHook.
     * @param plugServer The plug server.
     * @param plugDescription The plug description.
     */
    public PlugShutdownHook(PlugServer plugServer, PlugDescription plugDescription) {
        this.fPlugServer = plugServer;
        this.fPlugDescription = plugDescription;
        setDaemon(true);
    }

    /**
     * Sets the PlugDescription.
     * @param plugDescription A PlugDescrption
     */
    public void setPlugDescription(PlugDescription plugDescription) {
        this.fPlugDescription = plugDescription;
    }

    /**
     * Add a Bus.
     * @param busUrl The bus url.
     * @param bus The bus instance.
     */
    public void addBus(String busUrl, IBus bus) {
        this.fBusByUrl.put(busUrl, bus);
    }

    /**
     * Removes a bus.
     * @param busUrl The bus to remove by its url.
     */
    public void removeBus(String busUrl) {
        this.fBusByUrl.remove(busUrl);
    }

    public void run() {
        long time = System.currentTimeMillis();
        fLogger.info("Shutting plug '" + this.fPlugDescription.getPlugId() + "' down");

        Set busUrls = this.fBusByUrl.keySet();
        for (Iterator iter = busUrls.iterator(); iter.hasNext();) {
            String busUrl = (String) iter.next();
            IBus bus = (IBus) this.fBusByUrl.get(busUrl);
            PlugRemovalThread removalThread = new PlugRemovalThread(bus);
            removalThread.setName(busUrl);
            removalThread.start();
        }

        try {
            Thread.sleep(500);
            PlugServerShutdownThread shutdownThread = new PlugServerShutdownThread();
            shutdownThread.start();
            shutdownThread.join(500);
        } catch (Exception e) {
            fLogger.warn("problems on shutting the plug sever down", e);
        }
        fLogger.info("Plug shutdown in " + (System.currentTimeMillis() - time) + " ms");
    }

    /**
     * Thread for non-blocking removal of a plug from a bus.
     */ 
    public class PlugRemovalThread extends Thread {

        private IBus fBus;

        /**
         * Initilaizes the PlugRemovalThread.
         * @param bus
         */
        public PlugRemovalThread(IBus bus) {
            this.fBus = bus;
            setDaemon(true);
        }

        public void run() {
            try {
                this.fBus.removePlugDescription(PlugShutdownHook.this.fPlugDescription);
            } catch (Throwable e) {
                fLogger.warn("problems on deregistering from ibus '" + getName() + "'", e);
            }
        }
    }
    
    /**
     * Thread for non-blocking plug server shutdown.
     */
    public class PlugServerShutdownThread extends Thread {
        
      
        /**
         * Initializes the PlugServerShutdownThread.
         */
        public PlugServerShutdownThread() {
            setDaemon(true);
        }

        public void run() {
            try {
                PlugShutdownHook.this.fPlugServer.shutdown();
            } catch (Throwable e) {
                fLogger.warn("problems on shutting plug server down '" + getName() + "'", e);
            }
        }
    }
}
