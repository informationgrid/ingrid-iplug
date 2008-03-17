/*
 * Created on 14.08.2006
 */
package de.ingrid.iplug.web;

import java.io.FileInputStream;
import java.security.Principal;
import java.util.List;


import junit.framework.TestCase;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication.tcp.StartCommunication;
import de.ingrid.ibus.client.BusClient;
import de.ingrid.iplug.web.IngridRealm.User;

/**
 * 
 */
public class IngridRealmTest extends TestCase {


    
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
    public void testADMINPORTAL() throws Exception {
        String name = "TestKatAdmin";
        BusClient client = BusClient.instance();
        client.setBusUrl("/torwald-group:torwald-ibus");
        ICommunication communication = StartCommunication.create(new FileInputStream("src/conf/communication.properties"));
        communication.startup();
        client.setCommunication(communication);
        IngridRealm ingridRealm = new IngridRealm("IngridRealm", client.getBus(), "SHA-1");
        assertNotNull(ingridRealm.authenticate(name, name, null));
        Principal principal = ingridRealm.getPrincipal(name);
        System.out.println(principal.getName());
        assertNotNull(principal);
        User user = (User) principal;
        List partnerWithProvider = user.getPartnerWithProvider();
        System.out.println(partnerWithProvider);
    }
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
