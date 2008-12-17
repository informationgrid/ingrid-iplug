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
import net.weta.components.communication.tcp.StartCommunication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.HashUserRealm;

import de.ingrid.iplug.util.PlugShutdownHook;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.metadata.MetadataInjectorFactory;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * A server that starts the iplug as defined in the plugdescription.
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class PlugServer {

    protected final static Log fLogger = LogFactory.getLog(PlugServer.class);

    protected ICommunication fCommunication;

    private static final String PLUG_DESCRIPTION = "conf/plugdescription.xml";
    
    private IPlug fPlug;

    private HeartBeatTimeOutThread fTimeOutThread;

    protected PlugShutdownHook fShutdownHook;

    private PlugDescription fPlugDescription;

    private int fHeartBeatInterval;

    private File fPlugDescriptionFile;

    /**
     * Starts the admin server and initializes the plug server.
     * @param plugDescription
     * @param commProperties
     * @param heartBeatIntervall
     * @throws Exception
     */
    public PlugServer(PlugDescription plugDescription, File commProperties, File plugdescriptionFile, int heartBeatIntervall) throws Exception {
        this.fCommunication = initCommunication(commProperties, plugDescription);
        this.fPlugDescriptionFile = plugdescriptionFile;
        if ((plugDescription.getIplugAdminPassword() != null)
                && (plugDescription.getIplugAdminPassword().trim().length() > 0)
                && (plugDescription.getIplugAdminGuiPort() != 0)) {
            HashUserRealm realm = new HashUserRealm(plugDescription.getProxyServiceURL());
            realm.put("admin", plugDescription.getIplugAdminPassword());

            HashMap hashMap = new HashMap();
            hashMap.put("plugdescription.xml", this.fPlugDescriptionFile.getAbsolutePath());
            hashMap.put("communication.properties", commProperties.getAbsolutePath());
            AdminServer.startWebContainer(hashMap, plugDescription.getIplugAdminGuiPort(), new File("./webapp"), true, realm);
        }
        this.fPlugDescription = plugDescription;
        this.fHeartBeatInterval = heartBeatIntervall;
    }

    /**
     * Returns the setted communication.
     * @return The used communication.
     */
    public ICommunication getCommunication() {
        return this.fCommunication;
    }

    /**
     * Shuts down the plug server and its heart beat threads and communication. 
     */
    public void shutdown() {
        this.fTimeOutThread.interrupt();
        this.fCommunication.shutdown();
        try {
            this.fPlug.close();
        } catch (Exception e) {
            if (fLogger.isWarnEnabled()) {
                fLogger.warn("problems closing iplug", e);
            }
        }
    }

    /**
     * Registers the shutdwon hook, starts the heart beats and initializes the communication to the ibus(sses)
     * for the iplug.
     * @throws Exception If something goes wrong.
     */
    public void initPlugServer() throws Exception {
        if (fLogger.isInfoEnabled()) {
            fLogger.info("init plug-server with id '" + this.fPlugDescription.getPlugId() + '\'');
        }
        this.fPlug = initPlug(this.fPlugDescription);
        setUpCommunication(this.fPlugDescription.getProxyServiceURL());
        this.fShutdownHook = new PlugShutdownHook(this, this.fPlugDescription);
        Runtime.getRuntime().addShutdownHook(this.fShutdownHook);
        String[] busUrls = this.fPlugDescription.getBusUrls();
        this.fTimeOutThread = new HeartBeatTimeOutThread();
        for (int i = 0; i < busUrls.length; i++) {
            HeartBeatThread heartBeat = new HeartBeatThread(this.fPlugDescriptionFile, this.fPlugDescription, this.fCommunication, busUrls[i], this.fShutdownHook);
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
     * To start the plug server from the commandline.
     * @param args Arguments for the plug server e.g. --descriptor .
     * @throws Exception If something goes wrong.
     */
    public static void main(String[] args) throws Exception {
        Map arguments = readParameters(args);
        PlugDescription plugDescription = null;
        File plugDescriptionFile = new File(PLUG_DESCRIPTION);
        if (arguments.containsKey("--plugdescription")) {
           plugDescriptionFile = new File((String) arguments.get("--plugdescription"));
        }
        plugDescription = loadPlugDescriptionFromFile(plugDescriptionFile);
        
        injectMetadatas(plugDescription);
        
        PlugServer server = null;
        if (arguments.containsKey("--descriptor")) {
            File commConf = new File((String) arguments.get("--descriptor"));
            server = new PlugServer(plugDescription, commConf, plugDescriptionFile, 90 * 1000);
        } 
        if (server != null) {
            server.initPlugServer();
        }
    }

	private static void injectMetadatas(PlugDescription plugDescription) {
		List<IMetadataInjector> metadataInjectors = MetadataInjectorFactory
				.getMetadataInjectors();
		for (IMetadataInjector metadataInjector : metadataInjectors) {
			metadataInjector.injectMetaDatas(plugDescription);
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

    private IPlug initPlug(PlugDescription plugDescription) throws Exception {
        String plugClassStr = plugDescription.getIPlugClass();
        if (plugClassStr == null) {
            throw new NullPointerException("iplug class in plugdescription not set");
        }
        Class plugClass = Thread.currentThread().getContextClassLoader().loadClass(plugClassStr);
        IPlug plug = (IPlug) plugClass.newInstance();
        plug.configure(plugDescription);
        return plug;
    }

    private ICommunication initCommunication(File commProperties, PlugDescription plugDescription)
            throws IOException {
        this.fLogger.info("read jxta property file: " + commProperties.getAbsolutePath());
        FileInputStream confIS = new FileInputStream(commProperties);
        ICommunication communication = StartCommunication.create(confIS);
        WetagURL proxyUrl = new WetagURL(plugDescription.getProxyServiceURL());
        communication.setPeerName(proxyUrl.getPath());
        communication.startup();
        return communication;
    }

   private static PlugDescription loadPlugDescriptionFromFile(File plugDescriptionFile) throws IOException {
        fLogger.info("read plugdescription file: " + plugDescriptionFile.getAbsolutePath());
        InputStream resourceAsStream = new FileInputStream(plugDescriptionFile);
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

    public IPlug getIPlugInstance() {
    	return this.fPlug;
    }

    class HeartBeatTimeOutThread extends Thread {

        private List fHeartBeatThreads = new ArrayList();

        
        /**
         * Default value if it isn't initialised before start.
         */
        private int fHeartBeatIntervall = 2000;

        /**
         * To add a heart beat thread.
         * @param beatThread A heart beat thread.
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
                            if (fLogger.isWarnEnabled()) {
                                fLogger.warn("stopping heartbeat for '".concat(heartbeatThread.getBusUrl()) + '\'');
                            }
                            iter.remove();
                            heartbeatThread.interrupt();
                            heartbeatThread = new HeartBeatThread(fPlugDescriptionFile, fPlugDescription, fCommunication, heartbeatThread
                                    .getBusUrl(), fShutdownHook);
                            heartbeatThread.start();
                            beatsToAdd.add(heartbeatThread);
                        }
                    }
                    this.fHeartBeatThreads.addAll(beatsToAdd);
                    sleep(this.fHeartBeatIntervall * 3);
                }
            } catch (InterruptedException e) {
                if (fLogger.isInfoEnabled()) {
                    fLogger.info("stopping heartbeat timeout thread");
                }
                for (Iterator iter = this.fHeartBeatThreads.iterator(); iter.hasNext();) {
                    HeartBeatThread heartbeatThread = (HeartBeatThread) iter.next();
                    heartbeatThread.interrupt();
                }
            }
        }
    }
    
    public static PlugDescription getPlugDescription() throws IOException {
        return loadPlugDescriptionFromFile(new File(PLUG_DESCRIPTION));
    }

}
