package de.ingrid.iplug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.UserRealm;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.iplug.web.WebContainer;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * 
 */
public class AdminServer {

  protected final static Log fLogger = LogFactory.getLog(PlugServer.class);
  
  private static final String PLUG_DESCRIPTION = "conf/plugdescription.xml";
  
    /**
     * To start the admin web server from the commandline. 
     * @param args The server port and the web app folder. 
     * @throws Exception Something goes wrong.
     */
    public static void main(String[] args) throws Exception {
        String usage = "<serverPort> <webappFolder>";
        if ((args.length != 2) && (args.length != 4)) {
            System.err.println(usage);
            return;
        }
        
        Map arguments = readParameters(args);
        File plugDescriptionFile = new File(PLUG_DESCRIPTION);
        if (arguments.containsKey("--plugdescription")) {
          plugDescriptionFile = new File((String) arguments.get("--plugdescription"));
       }
        
        int port = Integer.parseInt(args[0]);
        File webFolder = new File(args[1]);
        Map map = new HashMap();
        map.put("pd_file", plugDescriptionFile);
        WebContainer container = startWebContainer(map, port, webFolder, false, null, null);
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
}
