package it.schm.magnolia.keycloak.security;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.jaas.principal.GroupListImpl;
import info.magnolia.jaas.principal.RoleListImpl;
import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.jaas.sp.UserAwareLoginModule;
import info.magnolia.jaas.sp.jcr.JCRAuthenticationModule;
import info.magnolia.objectfactory.Components;
import it.schm.magnolia.keycloak.KeycloakSecurityModule;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.jaas.DirectAccessGrantsLoginModule;
import org.keycloak.adapters.jaas.RolePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class KeycloakLoginModuleAdapter extends AbstractLoginModule implements UserAwareLoginModule {

    private static final Logger log = LoggerFactory.getLogger(JCRAuthenticationModule.class);
    protected User user;

    private DirectAccessGrantsLoginModule keycloakLoginModule;
    private KeycloakSecurityModule keycloakSecurityModule;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        keycloakSecurityModule = Components.getComponent(KeycloakSecurityModule.class);
        keycloakLoginModule = new DirectAccessGrantsLoginModule();

        Map<String, Object> newOptions = new HashMap<>(options);
        setupKeycloakOptionsFile(newOptions);

        super.initialize(subject, callbackHandler, sharedState, newOptions);
        keycloakLoginModule.initialize(subject, callbackHandler, sharedState, newOptions);
    }

    private void setupKeycloakOptionsFile(Map<String, Object> options) {
        if (!options.containsKey(DirectAccessGrantsLoginModule.KEYCLOAK_CONFIG_FILE_OPTION)) {
            String configFile = keycloakSecurityModule.getKeycloakConfigFile();

            if (StringUtils.isNotBlank(configFile)) {
                options.put(DirectAccessGrantsLoginModule.KEYCLOAK_CONFIG_FILE_OPTION, configFile);
            }
        }
    }

    private boolean isConfigured(Map options) {
        boolean isConfigured = options.containsKey(DirectAccessGrantsLoginModule.KEYCLOAK_CONFIG_FILE_OPTION);
        if (!isConfigured) {
            log.warn("No keycloakConfigFile specified, skipping Keycloak authentication");
        }

        return isConfigured;
    }

    @Override
    public boolean login() throws LoginException {
        if (getSkip() || !isConfigured(options)) {
            return true;
        }

        try {
            success = keycloakLoginModule.login();
            setSharedStatus(success ? STATUS_SUCCEEDED : STATUS_SKIPPED);

            return success;
        } catch (LoginException le) {
            log.warn(le.getMessage());

            FailedLoginException e = new FailedLoginException();
            e.initCause(le);

            throw e;
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!success || !keycloakLoginModule.commit()) {
            return false;
        }

        this.validateUser();

        return super.commit();
    }

    @Override
    public boolean abort() throws LoginException {
        super.abort();

        return keycloakLoginModule.abort();
    }

    @Override
    public boolean logout() throws LoginException {
        super.logout();

        return keycloakLoginModule.logout();
    }

    @Override
    public void validateUser() throws LoginException {
        initUser();

        if (user == null) {
            throw new AccountNotFoundException("User account " + name + " not found.");
        }

        if (!user.isEnabled()) {
            throw new AccountLockedException("User account " + name + " is locked.");
        }
    }

    protected void initUser() {
        Set<KeycloakPrincipal> principals = subject.getPrincipals(KeycloakPrincipal.class);

        if (principals.isEmpty()) {
            String msg = "No KeycloakPrincipal available";
            log.error(msg);
            throw new IllegalStateException(msg);
        } else if (principals.size() > 1) {
            String msg = String.format("%s KeycloakPrincipals available - which one should I choose?", principals.size());
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        KeycloakPrincipal<?> principal = principals.iterator().next();

        user = getUserManager().getUser(principal, getGroupList(principal), getRoleList());
    }

    private GroupList getGroupList(KeycloakPrincipal<?> principal) {
        Set<String> groups = new HashSet<>();

        String groupClaimKey = keycloakSecurityModule.getGroupClaimKey();
        if (StringUtils.isNotEmpty(groupClaimKey)) {
            Map<String, Object> otherClaims = principal.getKeycloakSecurityContext().getToken().getOtherClaims();
            if (otherClaims.containsKey(groupClaimKey)) {
                groups = new HashSet<>((List<String>) otherClaims.get(groupClaimKey));
            }
        }

        return mapGroups(groups);
    }

    private GroupList mapGroups(Set<String> groups) {
        Mapper groupMapper = keycloakSecurityModule.getGroupMapper();

        GroupListImpl groupList = new GroupListImpl();
        groups.stream()
                .map(this::extractLeafGroup)
                .map(groupMapper::map)
                .filter(Objects::nonNull)
                .forEach(groupList::add);

        return groupList;
    }

    /**
     * Keycloak can be configured to return a path to a nested group. This method extracts the leaf group.
     *
     * @param groupPath The path
     * @return The leaf group
     */
    private String extractLeafGroup(String groupPath) {
        return groupPath.contains("/") ? groupPath.substring(groupPath.lastIndexOf('/') + 1) : groupPath;
    }

    private RoleList getRoleList() {
        Mapper roleMapper = keycloakSecurityModule.getRoleMapper();

        RoleList roleList = new RoleListImpl();
        subject.getPrincipals(RolePrincipal.class).stream()
                .map(RolePrincipal::getName)
                .map(roleMapper::map)
                .filter(Objects::nonNull)
                .forEach(roleList::add);

        return roleList;
    }

    @Override
    public void setEntity() {
        subject.getPrincipals().add(user);
        subject.getPrincipals().add(realm);

        user.getAllRoles().forEach(this::addRoleName);
        user.getAllGroups().forEach(this::addGroupName);
    }

    @Override
    public void setACL() {
        // no-op
    }

    @Override
    public User getUser() {
        return user;
    }

    private KeycloakUserManager getUserManager() {
        UserManager userManager = Components.getComponent(SecuritySupport.class).getUserManager(realm.getName());

        if (userManager == null) {
            String msg = String.format("No UserManager found for realm %s", realm.getName());
            log.error(msg);
            throw new IllegalStateException(msg);
        } else if (!(userManager instanceof KeycloakUserManager)) {
            String msg = String.format("UserManager is of type %s, but expected %s",
                    userManager.getClass().getName(), KeycloakUserManager.class.getName());
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        return (KeycloakUserManager) userManager;
    }

}
