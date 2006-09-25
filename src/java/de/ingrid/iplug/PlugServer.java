/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.WetagURL;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import net.weta.components.peer.StartJxtaConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.HashUserRealm;

import de.ingrid.iplug.util.PlugShutdownHook;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.MD5Util;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * A server that starts the iplug class as defined in the plugdescription, that can also be used as singleton.
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class PlugServer {

    protected final static Log fLogger = LogFactory.getLog(PlugServer.class);

    /***/
    public static final String PLUG_DESCRIPTION = "/plugdescription.xml";

    protected ICommunication fCommunication;

    private IPlug fPlug;

    private HeartBeatTimeOutThread fTimeOutThread;

    protected PlugShutdownHook fShutdownHook;

    private PlugDescription fPlugDescription;

    private int fHeartBeatInterval;

    private static PlugServer fPlugServer;

    /**
     * @param plugDescription
     * @param jxtaProperties
     * @param heartBeatIntervall
     * @throws Exception
     */
    public PlugServer(PlugDescription plugDescription, String jxtaProperties, int heartBeatIntervall) throws Exception {
        fPlugServer = this;
        this.fCommunication = initJxtaCommunication(jxtaProperties, plugDescription);
        if ((plugDescription.getIplugAdminPassword() != null)
                && (plugDescription.getIplugAdminPassword().trim().length() > 0)
                && (plugDescription.getIplugAdminGuiPort() != 0)) {
            HashUserRealm realm = new HashUserRealm(plugDescription.getProxyServiceURL());
            realm.put("admin", plugDescription.getIplugAdminPassword());
            AdminServer.startWebContainer(plugDescription.getIplugAdminGuiPort(), new File("./webapp"), true, realm);
        }
        this.fPlugDescription = plugDescription;
        this.fHeartBeatInterval = heartBeatIntervall;
    }

    /**
     * @return the communication
     */
    public ICommunication getCommunication() {
      return this.fCommunication;
    }
    
    /**
     * @param plugDescription
     * @param unicastPort
     * @param multicastPort
     * @param heartBeatIntervall
     * @throws Exception
     */
    public PlugServer(PlugDescription plugDescription, int unicastPort, int multicastPort, int heartBeatIntervall)
            throws Exception {
        fPlugServer = this;
        this.fCommunication = initSocketCommunication(unicastPort, multicastPort);
        this.fPlugDescription = plugDescription;
        this.fHeartBeatInterval = heartBeatIntervall;
    }

    /**
     * 
     */
    public void shutdown() {
        this.fTimeOutThread.interrupt();
        this.fCommunication.shutdown();
        try {
            this.fPlug.close();
        } catch (Exception e) {
            fLogger.warn("problems closing iplug", e);
        }
    }

    public void initPlugServer() throws Exception {
        fLogger.info("init plug-server with id '" + this.fPlugDescription.getPlugId() + '\'');
        this.fPlug = initPlug(this.fPlugDescription);
        setUpCommunication(this.fPlugDescription.getProxyServiceURL());
        this.fShutdownHook = new PlugShutdownHook(this, this.fPlugDescription);
        Runtime.getRuntime().addShutdownHook(this.fShutdownHook);
        String[] busUrls = this.fPlugDescription.getBusUrls();
        this.fTimeOutThread = new HeartBeatTimeOutThread();
        for (int i = 0; i < busUrls.length; i++) {
            HeartBeatThread heartBeat = new HeartBeatThread(this.fCommunication, busUrls[i], this.fShutdownHook);
            heartBeat.setSleepInterval(this.fHeartBeatInterval);
            this.fTimeOutThread.addHearBeatThread(heartBeat);
            heartBeat.start();
        }
        this.fTimeOutThread.start();
    }

    private void setUpCommunication(String plugUrl) throws Exception {
        this.fCommunication.subscribeGroup(new WetagURL(plugUrl).getGroupPath());
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(IPlug.class, this.fPlug);
        this.fCommunication.getMessageQueue().getProcessorRegistry().addMessageHandler(
                ReflectMessageHandler.MESSAGE_TYPE, messageHandler);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map arguments = readParameters(args);
        PlugDescription plugDescription = getPlugDescription();
        PlugServer server = null;
        if (arguments.containsKey("--descriptor")) {
            String jxtaConf = (String) arguments.get("--descriptor");
            server = new PlugServer(plugDescription, jxtaConf, 30 * 1000);
        } else {
            int mPort = Integer.parseInt(args[0]);
            int uPort = Integer.parseInt(args[1]);
            String busUrl = AddressUtil.getWetagURL(args[2], Integer.parseInt(args[3]));
            // TODO remove 2 lines below
            plugDescription.remove(PlugDescription.BUSES);
            plugDescription.addBusUrl(busUrl);
            server = new PlugServer(plugDescription, uPort, mPort, 30 * 1000);
        }
        if(server != null) {
          server.initPlugServer();
        }
    }

    private static Map readParameters(String[] args) {
        Map argumentMap = new HashMap();
        // convert and validate the supplied arguments
        if (2 != args.length && 4 != args.length) {
            printUsage();
            System.exit(1);
        }
        for (int i = 0; i < args.length; i = i + 2) {
            argumentMap.put(args[i], args[i + 1]);
        }
        return argumentMap;
    }

    private static void printUsage() {
        System.err
                .println("Usage: You must set --descriptor <filename> for jxta or <multicastport> <unicastport> <IBusHost> <IBusPort> for socket communication");
    }

    private static IPlug initPlug(PlugDescription plugDescription) throws Exception {
        String plugClassStr = plugDescription.getIPlugClass();
        if (plugClassStr == null) {
            throw new NullPointerException("iplug class in plugdescription not set");
        }
        Class plugClass = Thread.currentThread().getContextClassLoader().loadClass(plugClassStr);
        IPlug plug = (IPlug) plugClass.newInstance();
        plug.configure(plugDescription);
        return plug;
    }

    private ICommunication initJxtaCommunication(String jxtaProperties, PlugDescription plugDescription)
            throws IOException {
        FileInputStream confIS = new FileInputStream(jxtaProperties);
        ICommunication communication = StartJxtaConfig.configureFromProperties(confIS);
        WetagURL proxyUrl = new WetagURL(plugDescription.getProxyServiceURL());
        communication.setPeerName(proxyUrl.getPeerName());
        communication.startup();
        return communication;
    }

    private ICommunication initSocketCommunication(int unicastPort, int multicastPort) throws IOException {
        SocketCommunication communication = new SocketCommunication();
        communication.setMulticastPort(multicastPort);
        communication.setUnicastPort(unicastPort);
        communication.startup();
        return communication;
    }

    /**
     * Reads the plug description from a xml file in the classpath.
     * 
     * @return The plug description.
     * @throws IOException
     */
    public static PlugDescription getPlugDescription() throws IOException {
        if (fPlugServer != null) {
            return fPlugServer.loadPlugDescription();
        }
        return loadPlugDescriptionFromFile();
    }

    protected PlugDescription loadPlugDescription() throws IOException {
        return loadPlugDescriptionFromFile();
    }

    private static PlugDescription loadPlugDescriptionFromFile() throws IOException {
        InputStream resourceAsStream = PlugServer.class.getResourceAsStream(PLUG_DESCRIPTION);
        XMLSerializer serializer = new XMLSerializer();
        PlugDescription plugDescription = (PlugDescription) serializer.deSerialize(resourceAsStream);
        try {
            plugDescription.setRecordLoader(IRecordLoader.class.isAssignableFrom(Class.forName(plugDescription
                    .getIPlugClass())));
        } catch (ClassNotFoundException e) {
            new RuntimeException("iplug class not in classpath", e);
        }
        return plugDescription;
    }

    /**
     * @return the md5 hash of the plugdescription
     * @throws IOException
     */
    public static String getPlugDescriptionMd5() throws IOException {
        InputStream resourceAsStream = PlugServer.class.getResourceAsStream(PLUG_DESCRIPTION);
        String md5 = MD5Util.getMD5(resourceAsStream);
        return md5;
    }

    class HeartBeatTimeOutThread extends Thread {

        private List fHeartBeatThreads = new ArrayList();

        private int fHeartBeatIntervall = 2000;

        /**
         * @param beatThread
         */
        public void addHearBeatThread(HeartBeatThread beatThread) {
            this.fHeartBeatThreads.add(beatThread);
            this.fHeartBeatIntervall = beatThread.getSleepInterval();
        }

        public void run() {
            try {
                while (true) {
                    List beatsToAdd = new ArrayList(3);
                    for (Iterator iter = this.fHeartBeatThreads.iterator(); iter.hasNext();) {
                        HeartBeatThread heartbeatThread = (HeartBeatThread) iter.next();
                        if (heartbeatThread.getLastSendHeartbeat() + heartbeatThread.getSleepInterval() * 2 < System
                                .currentTimeMillis()) {
                            fLogger.warn("stopping heartbeat for '".concat(heartbeatThread.getBusUrl()) + '\'');
                            iter.remove();
                            heartbeatThread.stop();
                            heartbeatThread = new HeartBeatThread(PlugServer.this.fCommunication, heartbeatThread
                                    .getBusUrl(), PlugServer.this.fShutdownHook);
                            heartbeatThread.start();
                            beatsToAdd.add(heartbeatThread);
                        }
                    }
                    this.fHeartBeatThreads.addAll(beatsToAdd);
                    sleep(this.fHeartBeatIntervall * 3);
                }
            } catch (InterruptedException e) {
                fLogger.info("stopping heartbeat timeout thread");
                for (Iterator iter = this.fHeartBeatThreads.iterator(); iter.hasNext();) {
                    HeartBeatThread heartbeatThread = (HeartBeatThread) iter.next();
                    heartbeatThread.interrupt();
                }
            }
        }
    }

}
