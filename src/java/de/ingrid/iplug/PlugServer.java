/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.ingrid.iplug.util.PlugShutdownHook;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.MD5Util;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * A server that starts the iplug class as defined in the plugdescription, that
 * can also be used as singleton.
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class PlugServer {

    /***/
    public static final String PLUG_DESCRIPTION = "/plugdescription.xml";

    private static String fPlugId;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map arguments = readParameters(args);
        PlugDescription plugDescription = getPlugDescription();
        IPlug plug = initPlug(plugDescription);
        PlugShutdownHook shutdownHook = new PlugShutdownHook(plug);
        shutdownHook.setPlugDescription(plugDescription);
        HeartBeatThread heartBeat;
        if (arguments.containsKey("--descriptor") && arguments.containsKey("--busurl")) {
            String jxtaConf = (String) arguments.get("--descriptor");
            String iBusUrl = (String) arguments.get("--busurl");
            heartBeat = new JxtaHeartBeatThread(jxtaConf, iBusUrl, plug, shutdownHook);
        } else {
            int mPort = Integer.parseInt(args[0]);
            int uPort = Integer.parseInt(args[1]);
            String iBustHost = args[2];
            int iBusPort = Integer.parseInt(args[3]);
            heartBeat = new SocketHeartBeatThread(mPort, uPort, iBustHost, iBusPort, plug, shutdownHook);
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        heartBeat.start();
    }

    private static Map readParameters(String[] args) {
        Map argumentMap = new HashMap();
        // convert and validate the supplied arguments
        if (4 != args.length) {
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
                .println("Usage: You must set --descriptor <filename> --busurl <wetag url> for jxta or <multicastport> <unicastport> <IBusHost> <IBusPort> for socket communication");
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

    /**
     * Reads the plug description from a xml file in the classpath.
     * 
     * @return The plug description.
     * @throws IOException
     */
    public static PlugDescription getPlugDescription() throws IOException {
        InputStream resourceAsStream = PlugServer.class.getResourceAsStream(PLUG_DESCRIPTION);
        XMLSerializer serializer = new XMLSerializer();
        PlugDescription plugDescription = (PlugDescription) serializer.deSerialize(resourceAsStream);
        try {
            plugDescription.setRecordLoader(IRecordLoader.class.isAssignableFrom(Class.forName(plugDescription
                    .getIPlugClass())));
        } catch (ClassNotFoundException e) {
            new RuntimeException("iplug class not in classpath", e);
        }
        fPlugId = plugDescription.getPlugId();
        return plugDescription;
    }

    /**
     * @return the md5 hash of the plugdescription
     * @throws IOException
     */
    public static String getPlugDescriptionMd5() throws IOException {
        InputStream resourceAsStream = PlugServer.class.getResourceAsStream(PLUG_DESCRIPTION);
        String md5 = MD5Util.getMD5(resourceAsStream);
        md5 = md5 + fPlugId;
        return md5;
    }
}
