package it.schm.magnolia.keycloak;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import it.schm.magnolia.keycloak.security.Mapper;

public class KeycloakSecurityModule implements ModuleLifecycle {

    private String keycloakConfigFile;
    private String groupClaimKey;
    private Mapper roleMapper;
    private Mapper groupMapper;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        // no-op
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        // no-op
    }

    public Mapper getRoleMapper() {
        return roleMapper;
    }

    public void setRoleMapper(Mapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public Mapper getGroupMapper() {
        return groupMapper;
    }

    public void setGroupMapper(Mapper groupMapper) {
        this.groupMapper = groupMapper;
    }

    public String getKeycloakConfigFile() {
        return keycloakConfigFile;
    }

    public void setKeycloakConfigFile(String keycloakConfigFile) {
        this.keycloakConfigFile = keycloakConfigFile;
    }

    public String getGroupClaimKey() {
        return groupClaimKey;
    }

    public void setGroupClaimKey(String groupClaimKey) {
        this.groupClaimKey = groupClaimKey;
    }

}
