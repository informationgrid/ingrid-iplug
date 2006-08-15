/*
 * Created on 14.08.2006
 */
package de.ingrid.iplug.web;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import de.ingrid.ibus.client.BusClient;
import junit.framework.TestCase;

/**
 * 
 */
public class IngridRealmTest extends TestCase {

    private IngridRealm fIRealm;

    protected void setUp() throws Exception {
        BusClient client = BusClient.instance();
        client.setBusUrl("/torwald-ibus:ibus-torwald");
        client.setJxtaConfigurationPath("/jxta.properties");
        this.fIRealm = new IngridRealm("IngridRealm", client.getBus(), "SHA-1");
    }

//    /**
//     */
//    public void testAuthenticate() {
//        assertNotNull(this.fIRealm.authenticate("admin_partner", "admin", null));
//
//        Principal principal = this.fIRealm.getPrincipal("admin_partner");
//        assertNotNull(principal);
//
//        assertTrue(this.fIRealm.reauthenticate(principal));
//
//        assertTrue(this.fIRealm.isUserInRole(principal, "admin_partner"));
//    }
//
//    /**
//     * 
//     */
//    public void testAdminPartner() {
//        assertNotNull(this.fIRealm.authenticate("admin_partner", "admin", null));
//
//        Principal principal = this.fIRealm.getPrincipal("admin_partner");
//
//        String[] array = this.fIRealm.getPartner(principal, "admin_partner");
//        assertEquals(2, array.length);
//        List list = Arrays.asList(array);
//        assertTrue(list.contains("he"));
//        assertTrue(list.contains("st"));
//
//        array = this.fIRealm.getProvider(principal, "admin_partner");
//        assertEquals(0, array.length);
//    }
//
//    /**
//     * 
//     */
//    public void testAdminProvider() {
//        assertNotNull(this.fIRealm.authenticate("admin_provider", "admin", null));
//
//        Principal principal = this.fIRealm.getPrincipal("admin_provider");
//
//        String[] array = this.fIRealm.getPartner(principal, "admin_partner");
//        assertEquals(0, array.length);
//
//        array = this.fIRealm.getProvider(principal, "admin_provider");
//        assertEquals(3, array.length);
//        List list = Arrays.asList(array);
//        assertTrue(list.contains("bu_bmu"));
//        assertTrue(list.contains("bu_uba"));
//        assertTrue(list.contains("he_hmulv"));
//    }
}
