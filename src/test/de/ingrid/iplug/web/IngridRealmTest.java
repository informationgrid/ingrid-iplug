/*
 * Created on 14.08.2006
 */
package de.ingrid.iplug.web;

import java.security.Principal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;
import de.ingrid.ibus.client.BusClient;

/**
 * 
 */
public class IngridRealmTest extends TestCase {

    private IngridRealm fIRealm;

    protected void setUp() throws Exception {
//        BusClient client = BusClient.instance();
//        client.setBusUrl("/torwald-ibus:ibus-torwald");
//        client.setJxtaConfigurationPath("/jxta.properties");
//        this.fIRealm = new IngridRealm("IngridRealm", client.getBus(), "SHA-1");
    }
    
    public void testTest() throws Exception {
        assertTrue(true);
    }

//    public void testNLWKN() throws Exception {
//        String name = "nlwkn";
//
//        assertNotNull(this.fIRealm.authenticate(name, name, null));
//        Principal principal = this.fIRealm.getPrincipal(name);
//        Collection hierarchie = this.fIRealm.getHierarchie();
//        for (Iterator iter = hierarchie.iterator(); iter.hasNext();) {
//            Map map = (Map) iter.next();
//            System.out.println(map);
//        }
//    }
//
//    public void testMU_ADMIN() throws Exception {
//        String name = "mu_admin";
//
//        assertNotNull(this.fIRealm.authenticate(name, name, null));
//        Principal principal = this.fIRealm.getPrincipal(name);
//        Collection hierarchie = this.fIRealm.getHierarchie();
//        for (Iterator iter = hierarchie.iterator(); iter.hasNext();) {
//            Map map = (Map) iter.next();
//            System.out.println(map);
//        }
//    }
//
//    public void testADMINPORTAL() throws Exception {
//        String name = "adminportal";
//
//        assertNotNull(this.fIRealm.authenticate(name, name, null));
//        Principal principal = this.fIRealm.getPrincipal(name);
//        Collection hierarchie = this.fIRealm.getHierarchie();
//        for (Iterator iter = hierarchie.iterator(); iter.hasNext();) {
//            Map map = (Map) iter.next();
//            System.out.println(map);
//        }
//    }
//
//    public void testMB() throws Exception {
//        String name = "mb";
//        assertNotNull(this.fIRealm.authenticate(name, "mb", null));
//        Principal principal = this.fIRealm.getPrincipal(name);
//        Collection hierarchie = this.fIRealm.getHierarchie();
//        for (Iterator iter = hierarchie.iterator(); iter.hasNext();) {
//            Map map = (Map) iter.next();
//            System.out.println(map);
//        }
//
//    }

}
