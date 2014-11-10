/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.utils.BeanFactory;

public class PlugdescriptionContextListener implements ServletContextListener {

    private static Log LOG = LogFactory.getLog(PlugdescriptionContextListener.class);

    public void contextDestroyed(ServletContextEvent servletcontextevent) {

    }

    public void contextInitialized(ServletContextEvent servletcontextevent) {
        ServletContext servletContext = servletcontextevent.getServletContext();
        String pdFile = servletContext.getInitParameter("plugdescription.xml");
        BeanFactory beanFactory = (BeanFactory) servletContext.getAttribute("beanFactory");
        try {
            beanFactory.addBean("pd_file", new File(pdFile));
        } catch (IOException e) {
            LOG.error("can not add plugdescription", e);
        }

    }

}
