package it.schm.magnolia.keycloak;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import it.schm.magnolia.keycloak.security.RoleMapper;

public class KeycloakSecurityModule implements ModuleLifecycle {

    private RoleMapper roleMapper;
    private String keycloakConfigFile;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        // no-op
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        // no-op
    }

    public RoleMapper getRoleMapper() {
        return roleMapper;
    }

    public void setRoleMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public String getKeycloakConfigFile() {
        return keycloakConfigFile;
    }

    public void setKeycloakConfigFile(String keycloakConfigFile) {
        this.keycloakConfigFile = keycloakConfigFile;
    }

}
