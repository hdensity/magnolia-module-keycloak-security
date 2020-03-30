package it.schm.magnolia.keycloak.security;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class DefaultRoleMapper implements RoleMapper {

    private Map<String, String> mappings = new HashMap<>();
    private boolean mapUnmappedRolesAsIs = true;

    @Override
    public String mapRole(String role) {
    	String mappedRole = mappings.get(role);

        if (StringUtils.isEmpty(mappedRole)) {
            return mapUnmappedRolesAsIs ? role : null;
        } else {
            return mappedRole;
        }
    }

    public void setMappings(Map<String, String> roleMappings) {
        this.mappings = roleMappings;
    }

    public void addMapping(String source, String target) {
        this.mappings.put(source, target);
    }

    public void setMapUnmappedRolesAsIs(boolean mapUnmappedRolesAsIs) {
        this.mapUnmappedRolesAsIs = mapUnmappedRolesAsIs;
    }

}
