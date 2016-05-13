/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.util.PlugShutdownHook;
import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.metadata.Metadata;
import de.ingrid.utils.metadata.MetadataInjectorFactory;
import de.ingrid.utils.tool.MD5Util;

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

    private int fSleepInterval = 1000 * 60;

    private PlugShutdownHook fShutdownHook;

    private long fLastSendHeartbeat = System.currentTimeMillis();

    private final PlugDescription fPlugDescription;

    private final File plugdescriptionFile;

	private List<IMetadataInjector> _metadataInjectors = new ArrayList<IMetadataInjector>();
	
    protected HeartBeatThread(File plugdescriptionFile, PlugDescription plugDescription, ICommunication communication, String busUrl, PlugShutdownHook shutdownHook) {
        this.plugdescriptionFile = plugdescriptionFile;
        this.fPlugDescription = plugDescription;
        this.fCommunication = communication;
        this.fBusUrl = busUrl;
        this.fShutdownHook = shutdownHook;
    }

    public void run() {
        if (fLogger.isInfoEnabled()) {
            fLogger.info("heartbeat for '" + this.fBusUrl + "' started (" + this.hashCode() + ").");
        }
        try {
            this.fBus = (IBus) ProxyService.createProxy(this.fCommunication, IBus.class, this.fBusUrl);
            this.fCommunication.subscribeGroup(this.fBusUrl);
    		MetadataInjectorFactory metadataInjectorFactory = new MetadataInjectorFactory(
					this.fPlugDescription, this.fBus);
			_metadataInjectors = metadataInjectorFactory.getMetadataInjectors();
			this.fPlugDescription.setMetadata(new Metadata());
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        try {
            while (!isInterrupted()) {

                String md5Hash = MD5Util.getMD5(this.plugdescriptionFile);
                String plugId = this.fPlugDescription.getPlugId();
                fLogger.info("send heartbeat (" + this.hashCode() + ") - call containsPlugdescription");
                boolean containsPlugDescription = this.fBus.containsPlugDescription(plugId, md5Hash);
                
                int oldMetadataHashCode = this.fPlugDescription.getMetadata().hashCode();
                injectMetadatas(this.fPlugDescription);
				int newMetadataHashCode = this.fPlugDescription.getMetadata().hashCode();
				boolean changedMetadata = oldMetadataHashCode != newMetadataHashCode;
                if (fLogger.isDebugEnabled()) {
                    fLogger.debug("Detect changed metadata: " + changedMetadata);
                    fLogger.debug("Metadata: " + this.fPlugDescription.getMetadata());
                }
                
                if (!containsPlugDescription
						|| changedMetadata) {
                    if (fLogger.isInfoEnabled()) {
                        fLogger.info("adding or updating plug description to bus '" + this.fBusUrl + "'... [containsPlugDescription:" + containsPlugDescription + " / changedMetaData:"
                                + changedMetadata + "]");
                    }
                    this.fPlugDescription.setMd5Hash(md5Hash);
                    this.fBus.addPlugDescription(this.fPlugDescription);
                    if (fLogger.isInfoEnabled()) {
                        fLogger.info("added or updated plug description to bus '" + this.fBusUrl + "' by heartbeat (" + this.hashCode() + ").");
                    }
                    this.fShutdownHook.addBus(this.fBusUrl, this.fBus);
                } else {
                    if (fLogger.isDebugEnabled()) {
                        fLogger.debug("I am currently connected.");
                    }
                }
                this.fLastSendHeartbeat = System.currentTimeMillis();
                sleep(this.fSleepInterval);
            }
        } catch (InterruptedException e) {
            if (fLogger.isWarnEnabled()) {
                fLogger.warn("interrupt heartbeat (" + this.hashCode() + ") thread to '" + this.fBusUrl + "'");
            }
        } catch (Throwable t) {
            if (fLogger.isErrorEnabled()) {
                fLogger.error("exception in heartbeat (" + this.hashCode() + ") thread to '" + this.fBusUrl + "'", t);
            }
        } finally {
            this.fShutdownHook.removeBus(this.fBusUrl);
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
     * 
     * @return The ibus for this heartbeat.
     */
    public IBus getIBus() {
        return this.fBus;
    }

    /**
     * Returns the time of the last sent heartbeat.
     * 
     * @return The time of the last sent heartbeat.
     */
    public long getLastSendHeartbeat() {
        return this.fLastSendHeartbeat;
    }

    /**
     * Returns the time between two heart beats.
     * 
     * @return How long the heart sleeps between the beats.
     */
    public int getSleepInterval() {
        return this.fSleepInterval;
    }

    /**
     * Sets the time between two heart beats.
     * 
     * @param sleepIntervall
     *            The time between two heart beats.
     */
    public void setSleepInterval(int sleepIntervall) {
        this.fSleepInterval = sleepIntervall;
    }
    
    private void injectMetadatas(PlugDescription plugDescription)
			throws Exception {
		Metadata metadata = plugDescription.getMetadata();
		metadata = metadata != null ? metadata : new Metadata();
		for (IMetadataInjector metadataInjector : _metadataInjectors) {
			if (fLogger.isDebugEnabled()) {
				fLogger.debug("Inject metadatas using " + metadataInjector.getClass().getName());
			}
			metadataInjector.injectMetaDatas(metadata);
		}
		if (fLogger.isDebugEnabled()) {
			fLogger.debug("Injected metadata:" + metadata);
		}
		plugDescription.setMetadata(metadata);
	}
    
}
