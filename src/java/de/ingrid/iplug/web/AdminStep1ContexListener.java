package de.ingrid.iplug.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.utils.BeanFactory;
import de.ingrid.utils.datatype.DataTypeEditor;
import de.ingrid.utils.datatype.DataTypeProvider;
import de.ingrid.utils.datatype.IDataTypeProvider;

public class AdminStep1ContexListener implements ServletContextListener {

    private static Log LOG = LogFactory.getLog(WebContainer.class);
    
    public static final String DATA_TYPES = "/WEB-INF/config/datatypes.properties";
    
    public void contextDestroyed(ServletContextEvent servletcontextevent) {
        // TODO Auto-generated method stub

    }

    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String pdFile = servletContext.getInitParameter("plugdescription.xml");
        String communicationFile = servletContext.getInitParameter("communication.properties");
        String realPathToDatatypes = servletContext.getRealPath(DATA_TYPES);
        
        BeanFactory beanFactory = new BeanFactory();
        
        try {
            BusClient busClient = connectIBus(communicationFile);
            beanFactory.addBean("busclient", busClient);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        
        IDataTypeProvider dataTypeProvider = new DataTypeProvider(new File(realPathToDatatypes), new DataTypeEditor());
        try {
            beanFactory.addBean("dataTypeProvider", dataTypeProvider);
            beanFactory.addBean("pd_file", new File(pdFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        servletContext.setAttribute("beanFactory", beanFactory);
    }
    
    private BusClient connectIBus(String communicationFile) throws IOException {
        ICommunication communication = initCommunication(new File(communicationFile));
        BusClient busClient = BusClient.instance();
        busClient.setCommunication(communication);
        
        //DIRTY HACK; we cast the ICommunication into TcpCommunication.
        String serverName = (String) ((TcpCommunication) communication).getServerNames().get(0);
        busClient.setBusUrl(serverName);
        //DIRTY HACK
        return busClient;
    }
    
    private static ICommunication initCommunication(File communicationProperties) throws IOException {
        LOG.info("read communication.properties: " + communicationProperties.getAbsolutePath());
        FileInputStream confIS = new FileInputStream(communicationProperties);
        ICommunication communication = StartCommunication.create(confIS);
        communication.startup();
        return communication;
    }

}
