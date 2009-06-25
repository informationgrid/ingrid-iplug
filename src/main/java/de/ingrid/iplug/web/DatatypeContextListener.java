package de.ingrid.iplug.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.utils.BeanFactory;
import de.ingrid.utils.datatype.DataTypeEditor;
import de.ingrid.utils.datatype.DataTypeProvider;
import de.ingrid.utils.datatype.IDataTypeProvider;

public class DatatypeContextListener implements ServletContextListener {

    private static Log LOG = LogFactory.getLog(DatatypeContextListener.class);

    public static final String DATA_TYPES = "/WEB-INF/config/datatypes.properties";

    public void contextDestroyed(ServletContextEvent servletcontextevent) {

    }

    public void contextInitialized(ServletContextEvent servletcontextevent) {
        ServletContext servletContext = servletcontextevent.getServletContext();
        String realPathToDatatypes = servletContext.getRealPath(DATA_TYPES);
        IDataTypeProvider dataTypeProvider = new DataTypeProvider(new File(realPathToDatatypes), new DataTypeEditor());
        BeanFactory beanFactory = (BeanFactory) servletContext.getAttribute("beanFactory");
        try {
            beanFactory.addBean("dataTypeProvider", dataTypeProvider);
        } catch (IOException e) {
            LOG.error("can not add plugdescription", e);
        }
    }

}
