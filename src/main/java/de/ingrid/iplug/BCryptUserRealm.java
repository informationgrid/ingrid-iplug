package de.ingrid.iplug;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.http.HashUserRealm;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class BCryptUserRealm extends HashUserRealm {
    
    Map<String, String> users = new HashMap<String, String>();
    
    public BCryptUserRealm(String realmName) {
        super.setName( realmName );
    }
    
    @Override
    public Principal authenticate(String username, Object credentials, HttpRequest request) {
        String password;
        synchronized (this)
        {
            password = (String) users.get(username);
        }
        if (password==null)
            return null;
        
        if (BCrypt.checkpw(credentials.toString(), password)) {
            return new User(username);
        }
        
        return null;
    }
    
    @Override
    public synchronized Object put(Object name, Object credentials) {
        return users.put( (String) name, credentials.toString() );
    }
    
    private class User implements Principal
    {
        private String name; 

        public User(String name) {
            this.name = name;
        }
        
        /* ------------------------------------------------------------ */
        private UserRealm getUserRealm()
        {
            return BCryptUserRealm.this;
        }
        
        public String getName()
        {
            return this.name;
        }
                
        public boolean isAuthenticated()
        {
            return true;
        }
        
        public String toString()
        {
            return getName();
        }
        
    }

}
