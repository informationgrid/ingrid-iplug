package de.ingrid.iplug;

import java.io.File;

import de.ingrid.iplug.web.WebContainer;

public class AdminServer {

    
    public static void main(String[] args) throws Exception {
        String usage ="<serverPort> <webappFolder> <adminPassword>";
        if(args.length!=3){
            System.err.println(usage);
            return;
        }
        int port = Integer.parseInt(args[0]);
        File webFolder = new File(args[1]);
        String password = args[2];
        
        WebContainer container = new WebContainer(port, true);
        container.start();
        container.addUser("admin", password);
        File[] files = webFolder.listFiles();
        if(files!=null){
            for (int i = 0; i < files.length; i++) {
                String file = files[i].getCanonicalPath();
                container.addWebapp(files[i].getName(), file);
            }
        }
        container.join();
    }
}
