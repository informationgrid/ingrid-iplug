/*
 * Created on 14.08.2006
 */
package de.ingrid.iplug.web;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SSORealm;
import org.mortbay.http.UserRealm;
import org.mortbay.util.Credential;

import sun.misc.BASE64Encoder;
import de.ingrid.ibus.client.BusClient;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * Is for authentication with the jetty. Can also be used outsside from jetty.
 * It can be used for single sign on.
 */
public class IngridRealm implements UserRealm, SSORealm {

    private IBus fIBus;

    private String fRealmName;

    private MessageDigest fMessageDigest;

    private HashMap fRolesToUser = new HashMap();

    private HashMap fUser = new HashMap();

    private static final String ROLE_PORTAL = "admin.portal";

    private static final String ROLE_PARTNER = "admin.portal.partner";

    private static final String ROLE_PROVIDER_INDEX = "admin.portal.partner.provider.index";

    private static final String ROLE_PROVIDER_CATALOG = "admin.portal.partner.provider.catalog";

    private SSORealm _ssoRealm;

    private class KnownUser extends User {

        private String fCredential;

        protected HashMap fRoleToPartners = new HashMap(0);

        protected HashMap fRoleToProviders = new HashMap(0);

        boolean authenticate(Object credentials) {
            return ((this.fCredential != null) && (this.fCredential.trim().equals(credentials)));
        }

        public String getName() {
            return this.fUserName;
        }

        /**
         * @see de.ingrid.iplug.web.IngridRealm.User#isAuthenticated()
         */
        public boolean isAuthenticated() {
            return true;
        }

        protected void addPartnerToRole(final String role, final String partner) {
            if (this.fRoleToPartners.containsKey(role)) {
                ArrayList partners = (ArrayList) this.fRoleToPartners.get(role);
                if (!partners.contains(partner)) {
                    partners.add(partner);
                    this.fRoleToPartners.put(role, partners);
                }
            } else {
                ArrayList partners = new ArrayList(1);
                partners.add(partner);
                this.fRoleToPartners.put(role, partners);
            }
        }

        protected void addProviderToRole(final String role, final String provider) {
            if (this.fRoleToProviders.containsKey(role)) {
                ArrayList providers = (ArrayList) this.fRoleToProviders.get(role);
                if (!providers.contains(provider)) {
                    providers.add(provider);
                    this.fRoleToProviders.put(role, providers);
                }
            } else {
                ArrayList providers = new ArrayList(1);
                providers.add(provider);
                this.fRoleToProviders.put(role, providers);
            }
        }

        public List getPartnerWithProvider() {
            List partnerWithProvider = super.getPartnerWithProvider();
            sortByPartnerName(partnerWithProvider);
            sortByProviderName(partnerWithProvider);
            return partnerWithProvider;
        }
        
        private void sortByProviderName(List list) {
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Map partnerMap = (Map) iter.next();
                List providerList = (List) partnerMap.get("providers");
                Collections.sort(providerList, new Comparator() {
                    public int compare(Object arg0, Object arg1) {
                        Map map1 = (Map) arg0;
                        Map map2 = (Map) arg1;
                        String name1 = (String) map1.get("name");
                        String name2 = (String) map2.get("name");
                        return name1.compareTo(name2);
                    }
                });
            }
        }

        /**
         * 
         */
        private void sortByPartnerName(List list) {
            Collections.sort(list, new Comparator() {
                public int compare(Object arg0, Object arg1) {
                    Map map1 = (Map) arg0;
                    Map map2 = (Map) arg1;
                    String name1 = (String) map1.get("name");
                    String name2 = (String) map2.get("name");
                    return name1.compareTo(name2);
                }
            });
        }

        /**
         * @param name
         * @param credential
         */
        KnownUser(String name, final String credential) {
            this.fUserName = name;
            this.fCredential = credential;
        }
    }

    public class User implements Principal {

        protected List _partnerWithProvider = new ArrayList();
        
        protected String fUserName = "Anonymous";

        private User() {
            // Nothing todo.
        }

        public String getName() {
            return this.fUserName;
        }

        /**
         * Returns whether the User is autheticated or not.
         * 
         * @return True if it is authenticated otherwise false.
         */
        public boolean isAuthenticated() {
            return false;
        }

        public List getPartnerWithProvider() {
            return _partnerWithProvider;
        }
        
        public void setPartnerWithProvider(List list) {
            _partnerWithProvider = list;
        }
        
        public String toString() {
            return getName();
        }

        public boolean equals(Object obj) {
            boolean result = false;

            if (obj instanceof User) {
                User newObj = (User) obj;
                result = getName().equals(newObj.getName());
            }

            return result;
        }
    }

    /**
     * Is for authentication with jetty. Can also be used outsside from jetty.
     * 
     * @param bus
     *            A "bus" ;-).
     * @param algorithm
     *            E.g. "SHA-1".
     * @throws NoSuchAlgorithmException
     */
    public IngridRealm(final IBus bus, final String algorithm) throws NoSuchAlgorithmException {
        this.fMessageDigest = MessageDigest.getInstance(algorithm);
        this.fIBus = bus;
    }

    /**
     * Is for authentication with the jetty. Can also be used outsside from
     * jetty.
     * 
     * @param busUrl
     *            The bus url.
     * @param propertyFile
     *            A property file with the jxta configuration.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public IngridRealm(String busUrl, String propertyFile) throws NoSuchAlgorithmException, IOException {
        BusClient client = BusClient.instance();
        client.setBusUrl(busUrl);
        client.setJxtaConfigurationPath(propertyFile);
        this.fIBus = client.getBus();
        this.fMessageDigest = MessageDigest.getInstance("SHA-1");
        this.fRealmName = "Ingrid Realm";
    }

    /**
     * Is for authentication with the jetty. Can also be used outsside from
     * jetty.
     * 
     * @param realmName
     *            A name for the realm.
     * @param bus
     *            A "bus" ;-).
     * @param algorithm
     *            E.g. "SHA-1".
     * @throws NoSuchAlgorithmException
     */
    public IngridRealm(final String realmName, final IBus bus, final String algorithm) throws NoSuchAlgorithmException {
        this.fMessageDigest = MessageDigest.getInstance(algorithm);
        this.fRealmName = realmName;
        this.fIBus = bus;
    }

    public String getName() {
        return this.fRealmName;
    }

    /**
     * The user object if it is authenticated otherwise a user object for a
     * anonymous user.
     * 
     * @see org.mortbay.http.UserRealm#getPrincipal(java.lang.String)
     */
    public Principal getPrincipal(final String userName) {
        Principal result = null;

        if (this.fUser.containsKey(userName)) {
            result = (Principal) this.fUser.get(userName);
        } else {
            result = new User();
        }

        return result;
    }

    private String encode(String userName, String clearTextPassword) {
        byte value[];
        synchronized (this.fMessageDigest) {
            this.fMessageDigest.reset();
            value = this.fMessageDigest.digest(clearTextPassword.getBytes());
            this.fMessageDigest.update(userName.getBytes());
            value = this.fMessageDigest.digest(value);
        }

        return new String(new BASE64Encoder().encode(value));
    }

    /**
     * Authenticates a user with its password. If the user can be authenticated
     * it returns a user object with all partners and providers and knows the
     * roles.
     * 
     * @param userName
     *            A user name.
     * @return The principal to the username and credential.
     */
    public Principal authenticate(String userName, Object credentials, HttpRequest request) {
        Principal result = new User();

        if ((userName != null) && (credentials != null)) {
            IngridHits authData = getAuthenticationData(userName, credentials);

            if ((userName != null) && (isAuthenticated(authData))) {
                result = createPrincipal(userName, (String) credentials, authData);
            }
        }

        return result;
    }

    private Principal createPrincipal(String userName, String credentials, IngridHits authData) {
        Principal result = new User();

        List allPartnerWithProvider = getAllPartnerWithProvider();

        if (authData != null) {
            IngridHit[] hitA = authData.getHits();

            if (null != hitA) {
                KnownUser user = new KnownUser(userName, credentials);
                for (int i = 0; i < hitA.length; i++) {

                    String permission = (String) hitA[i].get("permission");
                    String[] providers = (String[]) hitA[i].getArray("provider");
                    String[] partners = (String[]) hitA[i].getArray("partner");

                    List partnerWithProvider = createHierarchieForUrlMaintenance(allPartnerWithProvider, permission, partners, providers);
                    if (partnerWithProvider != null) {
                      user.setPartnerWithProvider(partnerWithProvider);
                    }
                    
                    for (int j = 0; providers != null && j < providers.length; j++) {
                        user.addProviderToRole(permission, providers[j]);
                    }

                    for (int j = 0; partners != null && j < partners.length; j++) {
                        user.addPartnerToRole(permission, partners[j]);
                    }

                    result = user;
                    addUserToRole(permission, user);
                    this.fUser.put(userName, user);
                }
            }
        }
        return result;
    }

    private List getAllPartnerWithProvider() {
        List list = new ArrayList();
        try {
            String query = "datatype:management management_request_type:1";
            IngridQuery ingridQuery = QueryStringParser.parse(query);
            IngridHits hits = this.fIBus.search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
              IngridHit hit = hits.getHits()[0];
              list = hit.getArrayList("partner"); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List createHierarchieForUrlMaintenance(List allPartnerWithProvider, String permission, String[] partners,
            String[] providers) {

        List list = new ArrayList();
        if (ROLE_PORTAL.equals(permission)) {
            list.addAll(allPartnerWithProvider);
        } else if (ROLE_PARTNER.equals(permission)) {
            for (int i = 0; i < partners.length; i++) {
                String partnerName = partners[i];
                for (int j = 0, length = allPartnerWithProvider.size(); j < length; j++) {
                    Map partnerMap = (Map) allPartnerWithProvider.get(j);
                    String partnerId = (String) partnerMap.get("partnerid");
                    if (partnerId.equals(partnerName)) {
                        list.add(partnerMap);
                    }
                }
            }
        } else if (ROLE_PROVIDER_CATALOG.equals(permission) || ROLE_PROVIDER_INDEX.equals(permission)) {
            for (int i = 0; i < partners.length; i++) {
                String partnerName = partners[i];
                for (int j = 0, length = allPartnerWithProvider.size(); j < length; j++) {
                    Map partnerMap = (Map) allPartnerWithProvider.get(j);
                    String partnerId = (String) partnerMap.get("partnerid");
                    if (partnerId.equals(partnerName)) {
                        List allProviderFromPartner = (List) partnerMap.get("providers");
                        for (Iterator iter = allProviderFromPartner.iterator(); iter.hasNext();) {
                            Map providerMap = (Map) iter.next();
                            String providerId = (String) providerMap.get("providerid");
                            for (int k = 0; k < providers.length; k++) {
                                String providerIdFromPlug = providers[k];
                                if (providerIdFromPlug.equals("*")) {
                                    list.add(partnerMap);
                                    return list;
                                } else if (providerId.equals(providerIdFromPlug)) {
                                    if(list.isEmpty()) {
                                        list.add(new HashMap());
                                    }
                                    Map partnerMapFromHierachie = (Map) list.get(0);
                                    List providerListFromHierarchie = (List) partnerMapFromHierachie.get("providers");
                                    if (providerListFromHierarchie == null) {
                                        partnerMapFromHierachie.put("partnerid", partnerId);
                                        partnerMapFromHierachie.put("name", (String) partnerMap.get("name"));
                                        providerListFromHierarchie = new ArrayList();
                                        partnerMapFromHierachie.put("providers", providerListFromHierarchie);
                                    }
                                    Map providerMapFromHierarchie = new HashMap();
                                    providerMapFromHierarchie.put("providerid", providerId);
                                    providerMapFromHierarchie.put("name", providerMap.get("name"));
                                    providerListFromHierarchie.add(providerMapFromHierarchie);
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    private void addUserToRole(String roleName, Principal user) {
        if ((null != roleName) && (user != null)) {
            ArrayList list = null;
            if (this.fRolesToUser.containsKey(roleName)) {
                list = (ArrayList) this.fRolesToUser.get(roleName);
            } else {
                list = new ArrayList(1);
            }

            list.add(user);
            this.fRolesToUser.put(roleName, list);
        }
    }

    private IngridHits getAuthenticationData(String userName, Object credentials) {
        IngridHits result = null;

        final String digest = encode(userName, (String) credentials);

        try {
            String qury = "datatype:management management_request_type:0 login: " + userName + " digest:" + digest;
            // management_request_type:815; für testdaten 0 für echte daten
            IngridQuery query = QueryStringParser.parse(qury);

            result = this.fIBus.search(query, 1000, 0, 0, 120000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean isAuthenticated(IngridHits hits) {
        boolean result = false;

        if (hits != null) {
            IngridHit[] hitA = hits.getHits();
            if (hitA != null && hitA.length > 0) {
                boolean authenticated = hitA[0].getBoolean("authenticated");
                if (authenticated) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * @see org.mortbay.http.UserRealm#reauthenticate(java.security.Principal)
     */
    public boolean reauthenticate(Principal user) {
        return ((User) user).isAuthenticated();
    }

    /**
     * @see org.mortbay.http.UserRealm#isUserInRole(java.security.Principal,
     *      java.lang.String)
     */
    public boolean isUserInRole(final Principal principal, final String roleName) {
        boolean result = false;

        if (this.fRolesToUser.containsKey(roleName)) {
            ArrayList users = (ArrayList) this.fRolesToUser.get(roleName);
            result = users.contains(principal);
        }

        return result;
    }

    /**
     * Isn't implemented.
     * 
     * @see org.mortbay.http.UserRealm#disassociate(java.security.Principal)
     */
    public void disassociate(Principal principal) {
        // Nothing todo.
    }

    /**
     * Isn't implemented.
     * 
     * @see org.mortbay.http.UserRealm#pushRole(java.security.Principal,
     *      java.lang.String)
     */
    public Principal pushRole(Principal arg0, String arg1) {
        return null;
    }

    /**
     * Isn't implemented.
     * 
     * @see org.mortbay.http.UserRealm#popRole(java.security.Principal)
     */
    public Principal popRole(Principal arg0) {
        return null;
    }

    /**
     * Isn't implemented.
     * 
     * @see org.mortbay.http.UserRealm#logout(java.security.Principal)
     */
    public void logout(Principal arg0) {
        // Nothing todo.
    }

    /**
     * Returns all partner for a user in a given role.
     * 
     * @param principal
     *            The user.
     * @param roleName
     *            The role name.
     * @return All partners for a user in a given role.
     */
    public String[] getPartner(Principal principal, String roleName) {
        String[] result = new String[0];

        if (principal instanceof KnownUser) {
            KnownUser newPrincipal = (KnownUser) principal;
            if (newPrincipal.fRoleToPartners.containsKey(roleName)) {
                List list = (List) newPrincipal.fRoleToPartners.get(roleName);
                result = (String[]) list.toArray(new String[list.size()]);
            }
        }

        return result;
    }

    /**
     * All providers for a user in a given role.
     * 
     * @param principal
     *            The user.
     * @param roleName
     *            The role name.
     * @return All providers for a user in a given role.
     */
    public String[] getProvider(Principal principal, String roleName) {
        String[] result = new String[0];

        if (principal instanceof KnownUser) {
            KnownUser newPrincipal = (KnownUser) principal;
            if (newPrincipal.fRoleToProviders.containsKey(roleName)) {
                List list = (List) newPrincipal.fRoleToProviders.get(roleName);
                result = (String[]) list.toArray(new String[list.size()]);
            }
        }

        return result;
    }

    
    /**
     * Sets the single sign on realm.
     * 
     * @param ssoRealm
     *            Single sign on realm to use.
     */
    public void setSSORealm(SSORealm ssoRealm) {
        this._ssoRealm = ssoRealm;
    }

    public Credential getSingleSignOn(HttpRequest request, HttpResponse response) {
        return this._ssoRealm != null ? this._ssoRealm.getSingleSignOn(request, response) : null;
    }

    public void setSingleSignOn(HttpRequest request, HttpResponse response, Principal principal, Credential credential) {
        if (this._ssoRealm != null) {
            this._ssoRealm.setSingleSignOn(request, response, principal, credential);
        }
    }

    public void clearSingleSignOn(String username) {
        if (this._ssoRealm != null) {
            this._ssoRealm.clearSingleSignOn(username);
        }
    }
}
