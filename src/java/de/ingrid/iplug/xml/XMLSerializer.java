/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.xml;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.xml.sax.SAXException;

public class XMLSerializer {

    public static void serializeAsXML(Object description, File target) throws IOException, SAXException,
            IntrospectionException {
        FileWriter writer = new FileWriter(target);
        writer.write("<?xml version='1.0' ?>");
        BeanWriter beanWriter = new BeanWriter(writer);
        beanWriter.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
        beanWriter.getBindingConfiguration().setMapIDs(true);
        beanWriter.enablePrettyPrint();
        // beanWriter.setWriteEmptyElements(true);
        beanWriter.write(description.getClass().getName(), description);
        writer.close();
    }

    public static Object loadDescriptionFromXML(Class clazz, File target) throws IntrospectionException, IOException,
            SAXException {
        FileReader fileReader = new FileReader(target);
        BufferedReader xmlReader = new BufferedReader(fileReader);

        BeanReader beanReader = new BeanReader();
        beanReader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
        beanReader.getBindingConfiguration().setMapIDs(true);
        beanReader.registerBeanClass(clazz.getName(), clazz);

        Object object = beanReader.parse(xmlReader);
        xmlReader.close();
        return object;

    }

}
