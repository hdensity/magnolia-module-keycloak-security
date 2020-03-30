package it.schm.magnolia.keycloak.security;

import com.google.inject.Provider;
import info.magnolia.cms.security.ExternalUserManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.jaas.principal.GroupListImpl;
import info.magnolia.jaas.principal.RoleListImpl;
import it.schm.magnolia.keycloak.KeycloakSecurityModule;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.jaas.RolePrincipal;

import javax.inject.Inject;
import javax.security.auth.Subject;

import java.util.Map;
import java.util.Set;

public class KeycloakUserManager extends ExternalUserManager {

    private final Provider<KeycloakSecurityModule> keycloakSecurityModuleProvider;
    private String realmName;

    @Inject
    public KeycloakUserManager(Provider<KeycloakSecurityModule> keycloakSecurityModuleProvider) {
        this.keycloakSecurityModuleProvider = keycloakSecurityModuleProvider;
    }

    public User getUser(Subject subject) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("getUser(Subject subject) is deprecated and should not be used");
    }

    public User getUser(Map<String, String> properties, GroupList groupList, RoleList roleList) {
        throw new UnsupportedOperationException(
                "Map<String, String> properties, GroupList groupList, RoleList roleList is not supported in this implementation");
    }

    public User getUser(KeycloakPrincipal<?> principal, Set<RolePrincipal> roles) {
        final KeycloakSecurityContext ctx = principal.getKeycloakSecurityContext();

        final KeycloakUser keycloakUser = new KeycloakUser(buildGroupList(), buildRoleList(roles));
        keycloakUser.setEmail(ctx.getToken().getEmail());
        keycloakUser.setEnabled(true);
        keycloakUser.setLanguage(ctx.getToken().getLocale());
        keycloakUser.setName(ctx.getToken().getPreferredUsername());
        keycloakUser.setFullname(ctx.getToken().getName());
        keycloakUser.setIdentifier(ctx.getToken().getId());
        keycloakUser.setToken(ctx.getTokenString());

        return keycloakUser;
    }

    protected RoleList buildRoleList(Set<RolePrincipal> roles) {

        final RoleMapper roleMapper = keycloakSecurityModuleProvider.get().getRoleMapper();

        final RoleList roleList = new RoleListImpl();

        for (RolePrincipal rolePrincipal : roles) {
            final String role = roleMapper.mapRole(rolePrincipal.getName());
            if (role != null) {
                roleList.add(role);
            }
        }

        return roleList;
    }

    protected GroupList buildGroupList() {
        return new GroupListImpl();
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

}
