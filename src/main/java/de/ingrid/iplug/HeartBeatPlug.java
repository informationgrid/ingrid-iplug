package de.ingrid.iplug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.MD5Util;

public abstract class HeartBeatPlug implements IPlug, IConfigurable {

    static class HeartBeat extends TimerTask {

        private boolean _enable = false;

        private static final Log LOG = LogFactory.getLog(HeartBeat.class);

        private final PlugDescription _plugDescription;

        private final IBus _bus;

        private long _heartBeatCount;

        @Autowired
        public HeartBeat(final IBus bus, final PlugDescription plugDescription, final long period) {
            _bus = bus;
            _plugDescription = plugDescription;
            final Timer timer = new Timer(true);
            timer.schedule(this, new Date(), period);
        }

        public void enable() throws IOException {
            _enable = true;
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
                LOG.info("send heartbeat [" + (_heartBeatCount) + "]");
                try {
                    final File plugdescriptionAsFile = _plugDescription.getDesrializedFromFolder();
                    final String md5 = MD5Util.getMD5(plugdescriptionAsFile);
                    _plugDescription.setMd5Hash(md5);
                    if (!_bus.containsPlugDescription(_plugDescription.getPlugId(), _plugDescription.getMd5Hash())) {
                        _bus.addPlugDescription(_plugDescription);
                    }
                } catch (final Throwable e) {
                    LOG.error("can not send heartbeat [" + _heartBeatCount + "]", e);
                }
            }

        }

    }

    private static final Log LOG = LogFactory.getLog(HeartBeatPlug.class);

    private final List<HeartBeat> _heartBeats = new ArrayList<HeartBeat>();

    private final int _period;

    private PlugDescription _plugDescription;

    public HeartBeatPlug(final int period) {
        _period = period;
    }

    @Override
    public void configure(final PlugDescription plugDescription) {

        final BusClient busClient = BusClientFactory.getBusClient();
        if (busClient != null && busClient.allConnected()) {
            _plugDescription = plugDescription;
            _plugDescription.setProxyServiceURL(busClient.getPeerName());

            // configure heartbeat's
            for (final IBus bus : busClient.getNonCacheableIBusses()) {
                final HeartBeat heartBeat = new HeartBeat(bus, _plugDescription, _period);
                _heartBeats.add(heartBeat);
            }
        }

    }

    @Override
    public void close() throws Exception {
        startHeartBeats();
        for (final HeartBeat heartBeat : _heartBeats) {
            final IBus bus = heartBeat._bus;
            bus.removePlugDescription(_plugDescription);
        }
        BusClientFactory.getBusClient().shutdown();
    }

    public void startHeartBeats() throws IOException {
        LOG.info("start heart beats");
        for (final HeartBeat heartBeat : _heartBeats) {
            heartBeat.enable();
        }
    }

    public void stopHeartBeats() {
        LOG.info("stop heart beats");
        for (final HeartBeat heartBeat : _heartBeats) {
            heartBeat.disable();
        }
    }

    public boolean sendingHeartBeats() {
        boolean bit = false;
        for (final HeartBeat heartBeat : _heartBeats) {
            if (heartBeat.isEnable()) {
                bit = true;
            }
        }
        return bit;
    }
}
