package de.ingrid.iplug;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.metadata.Metadata;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.tool.MD5Util;

public abstract class HeartBeatPlug implements IPlug, IConfigurable {

    static class HeartBeat extends TimerTask {

        static class ShutdownHook extends Thread {
            private final HeartBeat _heartBeat;

            public ShutdownHook(HeartBeat heartBeat) {
                _heartBeat = heartBeat;
            }

            @Override
            public void run() {
                _heartBeat.disable();
            }

        }

        private boolean _enable = false;

        private static final Log LOG = LogFactory.getLog(HeartBeat.class);

        private PlugDescription _plugDescription;

        private final IBus _bus;

        private long _heartBeatCount;

        private final IMetadataInjector[] _metadataInjectors;

        private final String _busUrl;

        private final String _name;

        private ShutdownHook _shutdownHook;

        private Timer _timer;

        public HeartBeat(String name, String busUrl, final IBus bus, final PlugDescription plugDescription, final long period, final IMetadataInjector... metadataInjectors) {
            _name = name;
            _busUrl = busUrl;
            _bus = bus;
            _plugDescription = plugDescription;
            _metadataInjectors = metadataInjectors;
            _timer = new Timer(true);
            _timer.schedule(this, new Date(), period);
            _shutdownHook = new ShutdownHook(this);
            Runtime.getRuntime().addShutdownHook(_shutdownHook);
        }

        public void enable() throws IOException {
            _enable = true;
            run();
        }

        public void disable() {
            _enable = false;
            if (_bus.containsPlugDescription(_plugDescription.getPlugId(), _plugDescription.getMd5Hash())) {
                _bus.removePlugDescription(_plugDescription);
            }
        }

        public boolean isEnable() {
            return _enable;
        }

        @Override
        public void run() {
            if (_enable) {
                _heartBeatCount++;
                try {

                    int oldMetadataHashCode = _plugDescription.getMetadata().hashCode();
                    injectMetadatas(_plugDescription);
                    int newMetadataHashCode = _plugDescription.getMetadata().hashCode();
                    boolean changedMetadata = oldMetadataHashCode != newMetadataHashCode;

                    final File plugdescriptionAsFile = _plugDescription.getDesrializedFromFolder();
                    final String md5 = MD5Util.getMD5(plugdescriptionAsFile);
                    String plugId = _plugDescription.getPlugId();

                    if (changedMetadata) {
                        LOG.info("Detect changed metadata: " + changedMetadata);
                        LOG.info("Metadata: " + _plugDescription.getMetadata());
                        if (_bus.containsPlugDescription(plugId, md5)) {
                            LOG.info("remove plugdescription.");
                            _bus.removePlugDescription(_plugDescription);
                        }
                    }

                    LOG.info("heartbeat#" + _name + " send heartbeat [" + (_heartBeatCount) + "] to bus [" + _busUrl + "]");
                    boolean containsPlugDescription = _bus.containsPlugDescription(plugId, md5);

                    if (!containsPlugDescription) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("adding or updating plug description to bus [" + _busUrl + "]");
                        }
                        _plugDescription.setMd5Hash(md5);
                        _bus.addPlugDescription(_plugDescription);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("added or updated plug description to bus [" + _busUrl + "]");
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("I am currently connected.");
                        }
                    }

                } catch (final Throwable e) {
                    LOG.error("can not send heartbeat [" + _heartBeatCount + "]", e);
                }
            }

        }

        private void injectMetadatas(PlugDescription plugDescription) throws Exception {
            Metadata metadata = plugDescription.getMetadata();
            metadata = metadata != null ? metadata : new Metadata();
            for (IMetadataInjector metadataInjector : _metadataInjectors) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Inject metadatas using " + metadataInjector.getClass().getName());
                }
                metadataInjector.injectMetaDatas(metadata);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Injected metadata:" + metadata);
            }
            plugDescription.setMetadata(metadata);
        }

        public void setPlugDescription(PlugDescription plugDescription) {
            _plugDescription = plugDescription;
        }

    }

    private static final Log LOG = LogFactory.getLog(HeartBeatPlug.class);

    private final Map<String, HeartBeat> _heartBeats = new LinkedHashMap<String, HeartBeat>();

    private final int _period;

    private PlugDescription _plugDescription;

    private final PlugDescriptionFieldFilters _filters;

    private final IMetadataInjector[] _injectors;

    private final IPostProcessor[] _postProcessors;

    private final IPreProcessor[] _preProcessors;

    public HeartBeatPlug(final int period, PlugDescriptionFieldFilters plugDescriptionFieldFilters, IMetadataInjector[] injectors, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors) {
        _period = period;
        _filters = plugDescriptionFieldFilters;
        _injectors = injectors;
        _preProcessors = preProcessors;
        _postProcessors = postProcessors;
    }

    @Override
    public void configure(final PlugDescription plugDescription) {
        // stop and remove existing heartbeats
        _plugDescription = _filters.filter(plugDescription);
        _plugDescription.setMetadata(new Metadata());
        final BusClient busClient = BusClientFactory.getBusClient();
        if (busClient != null && busClient.allConnected()) {
            _plugDescription.setProxyServiceURL(busClient.getPeerName());
            // configure heartbeat's
            List<IBus> busses = busClient.getNonCacheableIBusses();
            for (int i = 0; i < busses.size(); i++) {
                IBus iBus = busses.get(i);
                String busUrl = busClient.getBusUrl(i);
                if (!_heartBeats.containsKey(busUrl)) {
                    final HeartBeat heartBeat = new HeartBeat("no." + _heartBeats.size(), busUrl, iBus, _plugDescription, _period, _injectors);
                    _heartBeats.put(busUrl, heartBeat);
                }
                HeartBeat heartBeat = _heartBeats.get(busUrl);
                heartBeat.setPlugDescription(_plugDescription);
            }
        }
        
        // start sending HeartBeats to connected iBuses
        try {
			startHeartBeats();
		} catch (IOException e) {
			LOG.error("Couldn't start HeartBeats!", e);
		}
    }

    @Override
    public void close() throws Exception {
        stopHeartBeats();
        for (final HeartBeat heartBeat : _heartBeats.values()) {
            final IBus bus = heartBeat._bus;
            bus.removePlugDescription(_plugDescription);
        }
        BusClientFactory.getBusClient().shutdown();
    }

    public void startHeartBeats() throws IOException {
        LOG.info("start heart beats");
        Iterator<HeartBeat> iterator = _heartBeats.values().iterator();
        while (iterator.hasNext()) {
            HeartBeatPlug.HeartBeat heartBeat = iterator.next();
            heartBeat.enable();
        }
    }

    public void stopHeartBeats() {
        LOG.info("stop heart beats");
        Iterator<HeartBeat> iterator = _heartBeats.values().iterator();
        while (iterator.hasNext()) {
            HeartBeatPlug.HeartBeat heartBeat = iterator.next();
            heartBeat.disable();
        }
    }

    public boolean sendingHeartBeats() {
        boolean bit = false;
        for (final HeartBeat heartBeat : _heartBeats.values()) {
            if (heartBeat.isEnable()) {
                bit = true;
            }
        }
        return bit;
    }

    protected void preProcess(IngridQuery ingridQuery) throws Exception {
        for (IPreProcessor processor : _preProcessors) {
            processor.process(ingridQuery);
        }
    }

    protected void postProcess(IngridQuery ingridQuery, IngridDocument[] documents) throws Exception {
        for (IPostProcessor processor : _postProcessors) {
            processor.process(ingridQuery, documents);
        }
    }

}
