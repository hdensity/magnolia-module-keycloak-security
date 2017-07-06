package org.sevensource.magnolia.keycloak;

import org.sevensource.magnolia.keycloak.security.RoleMapper;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

public class KeycloakSecurityModule implements ModuleLifecycle {
	
	private RoleMapper roleMapper;
	
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
}
