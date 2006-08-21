package de.ingrid.iplug;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.mortbay.http.UserRealm;

import de.ingrid.iplug.web.WebContainer;

/**
 * 
 */
public class AdminServer {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String usage = "<serverPort> <webappFolder>";
        if (args.length != 2) {
            System.err.println(usage);
            return;
        }

        int port = Integer.parseInt(args[0]);
        File webFolder = new File(args[1]);

        WebContainer container = startWebContainer(port, webFolder, false, null);
        container.join();
    }

    /**
     * @param port
     * @param webFolder
     * @param secure
     * @param realm
     * @return The started WebContainer.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws Exception
     * @throws InterruptedException
     */
    public static WebContainer startWebContainer(int port, File webFolder, boolean secure, UserRealm realm)
            throws IOException, NoSuchAlgorithmException, Exception, InterruptedException {
        WebContainer container = new WebContainer(port, secure);

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
