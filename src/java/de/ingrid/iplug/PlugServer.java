/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;
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

    private static IPlug fInstance;

    /**
     * Returns the IPlug instance.
     * 
     * @return The IPlug instance.
     * @throws Exception
     */
    public static IPlug getIPlugInstance() throws Exception {
		synchronized (PlugServer.class) {
			if (fInstance != null) {
				fInstance.close();
			}
			PlugDescription plugDescription = getPlugDescription();
			String plugClassStr = plugDescription.getIPlugClass();
			Class plugClass = Thread.currentThread().getContextClassLoader()
					.loadClass(plugClassStr);
			fInstance = (IPlug) plugClass.newInstance();
			fInstance.configure(plugDescription);
		}
		return fInstance;
	}

    /**
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        final String usage = "Wrong numbers of arguments. You must set --descriptor <filename> --busurl <wetag url> "
                + "for jxta or <multicastport> <unicastport> <IBusHost> <IBusPort> for socket communication";
        HashMap arguments = new HashMap();

        // convert and validate the supplied arguments
        if (4 != args.length) {
            System.err.println(usage);
            System.exit(1);
        }
        for (int i = 0; i < args.length; i = i + 2) {
            arguments.put(args[i], args[i + 1]);
        }

        HeartBeatThread thread = null;
        if (arguments.containsKey("--descriptor") && arguments.containsKey("--busurl")) {
            String filename = (String) arguments.get("--descriptor");
            String iBusUrl = (String) arguments.get("--busurl");

            try {
                PlugServer.getIPlugInstance();
                thread = new HeartBeatThread(filename, iBusUrl);
            } catch (Throwable t) {
                System.err.println("Cannot register IPlug: ");
                t.printStackTrace();
                System.err.println(usage);
                System.exit(-1);
            }
        } else {
            int mPort;
            int uPort;
            int iBusPort;
            String iBustHost;
            try {
                mPort = Integer.parseInt(args[0]);
                uPort = Integer.parseInt(args[1]);
                iBustHost = args[2];
                iBusPort = Integer.parseInt(args[3]);

                PlugServer.getIPlugInstance();

                thread = new HeartBeatThread(mPort, uPort, iBustHost, iBusPort);
            } catch (Throwable t) {
                System.err.println("Cannot register IPlug: ");
                t.printStackTrace();
                System.err.println(usage);
                System.exit(-1);
            }
        }

        thread.start();
    }

    /**
     * Reads the plug description from a xml file in the classpath.
     * 
     * @return The plug description.
     * @throws IOException
     */
    public static PlugDescription getPlugDescription() throws IOException {
        InputStream resourceAsStream = PlugServer.class.getResourceAsStream("/plugdescription.xml");
        XMLSerializer serializer = new XMLSerializer();
        return (PlugDescription) serializer.deSerialize(resourceAsStream);
    }
}
