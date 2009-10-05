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
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.MD5Util;

public abstract class HeartBeatPlug implements IPlug {

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
            } else {
                LOG.info("heartbeat is disabled");
            }

        }

    }

    private final List<HeartBeat> _heartBeats = new ArrayList<HeartBeat>();
    private final int _period;
    private PlugDescription _plugDescription;

	public HeartBeatPlug(final File communicationXml, final int period) throws Exception {
        _period = period;
		BusClientFactory.createBusClient(communicationXml);
    }

    @Override
    public void configure(final PlugDescription plugDescription) throws Exception {

        _plugDescription = plugDescription;
		final BusClient busClient = BusClientFactory.getBusClient();
		busClient.start();
		_plugDescription.setProxyServiceURL(busClient.getPeerName());

        // configure heartbeat's
        // FIXME bad hack, we cast into TcpCommunication
		for (final IBus bus : busClient.getNonCacheableIBusses()) {
            final HeartBeat heartBeat = new HeartBeat(bus, _plugDescription, _period);
            _heartBeats.add(heartBeat);
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
        for (final HeartBeat heartBeat : _heartBeats) {
            heartBeat.enable();
        }
    }

    public void stopHeartBeats() {
        for (final HeartBeat heartBeat : _heartBeats) {
            heartBeat.disable();
        }
    }
}
