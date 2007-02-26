package de.ingrid.iplug;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.http.UserRealm;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.iplug.web.WebContainer;

/**
 * 
 */
public class AdminServer {

    /**
     * To start the admin web server from the commandline. 
     * @param args The server port and the web app folder. 
     * @throws Exception Something goes wrong.
     */
    public static void main(String[] args) throws Exception {
        String usage = "<serverPort> <webappFolder>";
        if (args.length != 2) {
            System.err.println(usage);
            return;
        }

        int port = Integer.parseInt(args[0]);
        File webFolder = new File(args[1]);

        WebContainer container = startWebContainer(new HashMap(), port, webFolder, false, null, null);
        container.join();
    }

    /**
     * Starts a web container with jetty.
     * @param plugdescriptionFilename 
     * @param port The port for the web server.
     * @param webFolder The folder where the web contexts are located.
     * @param secure True if authentication is requiered otherwise false.
     * @param realm A user password relation if it is a secure web container. 
     * @param busClient The bus client for the communication.
     * @return The started WebContainer.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws Exception
     * @throws InterruptedException
     */
    public static WebContainer startWebContainer(Map attributes, int port, File webFolder, boolean secure, UserRealm realm, BusClient busClient)
            throws IOException, NoSuchAlgorithmException, Exception, InterruptedException {
        WebContainer container = new WebContainer(port, secure);
        container.setAttribues(attributes);
        container.setBusClient(busClient);
        if (secure) {
            container.setRealm(realm);
        }
        container.startContainer();
        File[] files = webFolder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String file = files[i].getCanonicalPath();
                container.addWebapp(files[i].getName(), file);
            }
        }

        return container;
    }
}
