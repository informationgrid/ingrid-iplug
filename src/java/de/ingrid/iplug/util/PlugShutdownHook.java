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

import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
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

    private IPlug fPlug;

    private PlugDescription fPlugDescription;

    private Map fBusByUrl = new HashMap(3);

    /**
     * @param plug
     */
    public PlugShutdownHook(IPlug plug) {
        this.fPlug = plug;
        setDaemon(true);
    }

    /**
     * @param plugDescription
     */
    public void setPlugDescription(PlugDescription plugDescription) {
        this.fPlugDescription = plugDescription;
    }

    /**
     * @param busUrl
     * @param bus
     */
    public void addBus(String busUrl, IBus bus) {
        this.fBusByUrl.put(busUrl, bus);
    }

    /**
     * @param busUrl
     */
    public void removeBus(String busUrl) {
        this.fBusByUrl.remove(busUrl);
    }

    public void run() {
        long time = System.currentTimeMillis();
        fLogger.info("Shutting plug '" + this.fPlugDescription.getId() + "' down");

        Set busUrls = this.fBusByUrl.keySet();
        for (Iterator iter = busUrls.iterator(); iter.hasNext();) {
            String busUrl = (String) iter.next();
            IBus bus = (IBus) this.fBusByUrl.get(busUrl);
            try {
                bus.removePlugDescription(this.fPlugDescription);
            } catch (Throwable e) {
                fLogger.warn("problems on deregistering from ibus '" + busUrl + "'");
            }
        }

        try {
            this.fPlug.close();
        } catch (Exception e) {
            fLogger.warn("problems on shutting the plug down", e);
        }
        fLogger.info("Plug shutdown in " + (System.currentTimeMillis() - time) + " ms");
    }
}
