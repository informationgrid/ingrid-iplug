/*
 * Created on 14.08.2006
 */
package de.ingrid.iplug.web;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;

import sun.misc.BASE64Encoder;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * 
 */
public class IngridRealm implements UserRealm {

    private IBus fIBus;

    private String fRealmName;

    private MessageDigest fMessageDigest;

    private HashMap fRolesToUser = new HashMap();

    private HashMap fUser = new HashMap();

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

        /**
         * @param name
         * @param credential
         */
        KnownUser(String name, final String credential) {
            this.fUserName = name;
            this.fCredential = credential;
        }
    }

    private class User implements Principal {

        protected String fUserName = "Anonymous";

        private User() {
            // Nothing todo.
        }

        public String getName() {
            return this.fUserName;
        }

        /**
         * @return True if it is authenticated otherwise false.
         */
        public boolean isAuthenticated() {
            return false;
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
     * Is for authentication with the jetty. Can also be used outsside from jetty.
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
     * The user object if it is authenticated otherwise a user object for a anonymous user.
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
     * Authenticates a user with its password. If the user can be authenticated it returns a user object with all
     * partners and providers and knows the roles.
     * 
     * @param userName
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

        if (authData != null) {
            IngridHit[] hitA = authData.getHits();

            if (null != hitA) {
                KnownUser user = new KnownUser(userName, credentials);
                for (int i = 0; i < hitA.length; i++) {
                    String roleName = (String) hitA[i].get("permission");

                    String[] ps = (String[]) hitA[i].getArray("provider");
                    if (null != ps) {
                        for (int j = 0; j < ps.length; j++) {
                            user.addProviderToRole(roleName, ps[j]);
                        }
                    }

                    ps = (String[]) hitA[i].getArray("partner");
                    if (null != ps) {
                        for (int j = 0; j < ps.length; j++) {
                            user.addPartnerToRole(roleName, ps[j]);
                        }
                    }

                    result = user;

                    addUserToRole(roleName, user);
                    this.fUser.put(userName, user);
                }
            }
        }

        return result;
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
            //management_request_type:815; für testdaten 0 für echte daten
            IngridQuery query = QueryStringParser.parse("datatype:management management_request_type:815 login: "
                    + userName + " digest:" + digest);

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
     * @see org.mortbay.http.UserRealm#isUserInRole(java.security.Principal, java.lang.String)
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
     * @see org.mortbay.http.UserRealm#disassociate(java.security.Principal)
     */
    public void disassociate(Principal principal) {
        // Nothing todo.
    }

    /**
     * Isn't implemented.
     * @see org.mortbay.http.UserRealm#pushRole(java.security.Principal, java.lang.String)
     */
    public Principal pushRole(Principal arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Isn't implemented.
     * @see org.mortbay.http.UserRealm#popRole(java.security.Principal)
     */
    public Principal popRole(Principal arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Isn't implemented.
     * @see org.mortbay.http.UserRealm#logout(java.security.Principal)
     */
    public void logout(Principal arg0) {
        // Nothing todo.
    }

    /**
     * @param principal
     * @param roleName
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
     * @param principal
     * @param roleName
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
}
