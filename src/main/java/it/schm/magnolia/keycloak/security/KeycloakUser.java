package it.schm.magnolia.keycloak.security;

import info.magnolia.cms.security.ExternalUser;
import info.magnolia.cms.security.auth.Entity;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;

import java.util.HashMap;

public class KeycloakUser extends ExternalUser {

    private static final long serialVersionUID = -289449729630988775L;

    public static final String ID_PROPERTY = "id";
    public static final String TOKEN_PROPERTY = "token";

    protected KeycloakUser(GroupList groupList, RoleList roleList) {
        super(new HashMap<>(), groupList, roleList);
    }


    public void setLanguage(String language) {
        setProperty(Entity.LANGUAGE, language);
    }

    public void setName(String name) {
        setProperty(Entity.NAME, name);
    }

    public void setEmail(String email) {
        setProperty(Entity.EMAIL, email);
    }

    public void setFullname(String name) {
        setProperty(Entity.FULL_NAME, name);
    }

    public void setToken(String token) {
        setProperty(TOKEN_PROPERTY, token);
    }

    public String getToken() {
        return getProperty(TOKEN_PROPERTY);
    }

    public void setIdentifier(String id) {
        setProperty(ID_PROPERTY, id);
    }

    @Override
    public String getIdentifier() {
        return getProperty(ID_PROPERTY);
    }

}
