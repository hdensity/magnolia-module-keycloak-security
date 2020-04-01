package it.schm.magnolia.keycloak.security;

import info.magnolia.cms.security.ExternalUserManager;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.jaas.principal.GroupListImpl;
import info.magnolia.jaas.principal.RoleListImpl;
import info.magnolia.objectfactory.Components;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.util.Collection;
import java.util.Map;

public class KeycloakUserManager extends ExternalUserManager {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserManager.class);

    private String realmName;

    @Override
    public User getUser(Subject subject) throws UnsupportedOperationException {

        throw new UnsupportedOperationException("getUser(Subject subject) is deprecated and should not be used");
    }

    @Override
    public User getUser(Map<String, String> properties, GroupList groupList, RoleList roleList) {
        throw new UnsupportedOperationException(
                "Map<String, String> properties, GroupList groupList, RoleList roleList is not supported in this implementation");
    }

    @Override
    public Collection<User> getAllUsers() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public User getUser(KeycloakPrincipal<?> principal, GroupList groups, RoleList roles) {
        KeycloakSecurityContext ctx = principal.getKeycloakSecurityContext();

        String userName = ctx.getToken().getPreferredUsername();
        // SecuritySupport cannot be injected cause it's not ready during Magnolia init phase
        GroupManager groupManager = Components.getComponent(SecuritySupport.class).getGroupManager();
        // groups must be resolved first as finding roles depends on them
        GroupList allGroups = aggregateDirectAndTransitiveGroups(groups, userName, groupManager);
        RoleList allRoles = aggregateDirectAndTransitiveRoles(roles, allGroups, userName, groupManager);

        KeycloakUser keycloakUser = new KeycloakUser(groups, allGroups, roles, allRoles);
        keycloakUser.setEmail(ctx.getToken().getEmail());
        keycloakUser.setEnabled(true);
        keycloakUser.setLanguage(ctx.getToken().getLocale());
        keycloakUser.setName(ctx.getToken().getPreferredUsername());
        keycloakUser.setFullname(ctx.getToken().getName());
        keycloakUser.setIdentifier(ctx.getToken().getId());
        keycloakUser.setToken(ctx.getTokenString());

        return keycloakUser;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    /**
     * @return a Set of all roles, sorting them (case insensitive), directly assigned and transitive for this user.
     * @see #getUser(KeycloakPrincipal, GroupList, RoleList)
     */
    protected RoleList aggregateDirectAndTransitiveRoles(
            RoleList roles, GroupList allGroups, String name, GroupManager groupManager) {
        RoleList allRoles = new RoleListImpl();
        roles.getList().forEach(allRoles::add);

        // add roles from all groups
        allGroups.getList().forEach(group -> {
            try {
                Group mgnlGroup = groupManager.getGroup(group);
                if (mgnlGroup != null) {
                    mgnlGroup.getRoles().forEach(allRoles::add);
                }
            } catch (javax.jcr.AccessDeniedException e) {
                log.debug("Skipping denied group {} for user {}", group, name, e);
            } catch (UnsupportedOperationException e) {
                log.debug("Skipping unsupported  getGroup() for group {} and user {}", group, name, e);
            }
        });

        return allRoles;
    }

    /**
     * @return a Set of all groups, directly assigned and transitive for this user.
     * @see #getUser(KeycloakPrincipal, GroupList, RoleList)
     */
    protected GroupList aggregateDirectAndTransitiveGroups(GroupList groups, String name, GroupManager groupManager) {
        GroupList allGroups = new GroupListImpl();

        // add all subgroups
        addSubgroups(allGroups, groupManager, groups.getList(), name);

        return allGroups;
    }

    /**
     * Any group from the groups is checked for the subgroups only if it is not in the allGroups yet. This is to prevent infinite loops in case of cyclic group assignment.
     */
    private void addSubgroups(GroupList allGroups, GroupManager groupManager, Collection<String> groups, String name) {
        groups.forEach(groupName -> {
            // check if this group was not already added to prevent infinite loops
            if (!allGroups.getList().contains(groupName)) {
                allGroups.add(groupName);
                try {
                    Group group = groupManager.getGroup(groupName);
                    if (group == null) {
                        return; // skips current iteration
                    }

                    Collection<String> subgroups = group.getGroups();
                    // and recursively add more subgroups
                    addSubgroups(allGroups, groupManager, subgroups, name);
                } catch (javax.jcr.AccessDeniedException e) {
                    log.debug("Skipping denied group {} for user {}", groupName, name, e);
                } catch (UnsupportedOperationException e) {
                    log.debug("Skipping unsupported getGroup() for group {} and user {}", groupName, name, e);
                }
            }
        });
    }

}
