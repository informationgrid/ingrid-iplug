package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.MD5Util;

public abstract class HeartBeatPlug implements IPlug {

    static class HeartBeat extends TimerTask {

        private Timer _timer;
        private boolean _enable = false;
        private static final Log LOG = LogFactory.getLog(HeartBeat.class);
        private final PlugDescription _plugDescription;
        private final IBus _bus;
        private long _heartBeatCount;

        @Autowired
        public HeartBeat(IBus bus, PlugDescription plugDescription, long period) {
            _bus = bus;
            _plugDescription = plugDescription;
            _timer = new Timer(true);
            _timer.schedule(this, new Date(), period);
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
                    File plugdescriptionAsFile = _plugDescription.getDesrializedFromFolder();
                    String md5 = MD5Util.getMD5(plugdescriptionAsFile);
                    _plugDescription.setMd5Hash(md5);
                    if (!_bus.containsPlugDescription(_plugDescription.getPlugId(), _plugDescription.getMd5Hash())) {
                        _bus.addPlugDescription(_plugDescription);
                    }
                } catch (Throwable e) {
                    LOG.error("can not send heartbeat [" + _heartBeatCount + "]", e);
                }
            } else {
                LOG.info("heartbeat is disabled");
            }

        }

    }

    private ICommunication _communication;
    private List<HeartBeat> _heartBeats = new ArrayList<HeartBeat>();
    private final int _period;

    public HeartBeatPlug(ICommunication communication, int period) {
        _communication = communication;
        _period = period;
    }

    public HeartBeatPlug(File communicationXml, int period) throws FileNotFoundException, IOException {
        _period = period;
        _communication = StartCommunication.create(new FileInputStream(communicationXml));
    }

    public HeartBeatPlug(InputStream inputStream, int period) throws FileNotFoundException, IOException {
        _period = period;
        _communication = StartCommunication.create(inputStream);
        _communication.startup();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configure(PlugDescription plugDescription) throws Exception {

        // configure communication
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IPlug.class, this);
        MessageHandlerCache cache = new MessageHandlerCache(messageHandler);
        _communication.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE, cache);

        // configure heartbeat's
        // FIXME bad hack, we cast into TcpCommunication
        List serverNames = ((TcpCommunication) _communication).getServerNames();
        for (Object busUrl : serverNames) {
            IBus bus = (IBus) ProxyService.createProxy(_communication, IBus.class, (String) busUrl);
            HeartBeat heartBeat = new HeartBeat(bus, plugDescription, _period);
            _heartBeats.add(heartBeat);
        }

    }

    @Override
    public void close() throws Exception {
        _communication.shutdown();
    }

    public void startHeartBeats() throws IOException {
        for (HeartBeat heartBeat : _heartBeats) {
            heartBeat.enable();
        }
    }

    public void stopHeartBeats() {
        for (HeartBeat heartBeat : _heartBeats) {
            heartBeat.disable();
        }
    }

    public IBus getMotherIBus() {
        return !_heartBeats.isEmpty() ? _heartBeats.get(0)._bus : null;
    }
}
