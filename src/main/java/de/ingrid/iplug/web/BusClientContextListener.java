/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.BeanFactory;

public class BusClientContextListener implements ServletContextListener {

    private static Log LOG = LogFactory.getLog(BusClientContextListener.class);

    public void contextDestroyed(ServletContextEvent servletcontextevent) {

    }

    public void contextInitialized(ServletContextEvent servletcontextevent) {
        ServletContext servletContext = servletcontextevent.getServletContext();
        String communicationFile = servletContext.getInitParameter("communication.xml");
        BusClient busClient = null;
        try {
            busClient = connectIBus(communicationFile);
            BeanFactory beanFactory = (BeanFactory) servletContext.getAttribute("beanFactory");
            beanFactory.addBean("busClient", busClient);
        } catch (Exception e1) {
            LOG.error("can not connect to ibus", e1);
        }

    }

    private BusClient connectIBus(String communicationFile) throws Exception {
        BusClient busClient = BusClientFactory.createBusClient(new File(communicationFile));
        return busClient;
    }

}
