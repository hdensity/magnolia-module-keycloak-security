[![GitHub](https://img.shields.io/github/license/hdensity/magnolia-module-keycloak-security)](https://github.com/hdensity/magnolia-module-keycloak-security/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/hdensity/magnolia-module-keycloak-security.svg?branch=master)](https://travis-ci.com/hdensity/magnolia-module-keycloak-security)
[![Coverage Status](https://coveralls.io/repos/github/hdensity/magnolia-module-keycloak-security/badge.svg?branch=master)](https://coveralls.io/github/hdensity/magnolia-module-keycloak-security?branch=master)
[![Code Climate maintainability](https://img.shields.io/codeclimate/maintainability/hdensity/magnolia-module-keycloak-security)](https://codeclimate.com/github/hdensity/magnolia-module-keycloak-security)
[![Code Climate issues](https://img.shields.io/codeclimate/issues/hdensity/magnolia-module-keycloak-security)](https://codeclimate.com/github/hdensity/magnolia-module-keycloak-security/issues)
[![Active](http://img.shields.io/badge/Status-Active-green.svg)](https://github.com/hdensity/magnolia-module-keycloak-security)

# magnolia-module-keycloak-security

[Keycloak](http://www.keycloak.org/) SSO/IAM integration for [Magnolia](http://www.magnolia-cms.com) 6.2

This module delegates authentication - in addition to Magnolias builtin authentication mechanisms - to Keycloak.

**Contributions welcome!**

# Installation

* create a client in Keycloak with *Direct Access Grants* enabled
* export the configuration in *Keycloak OIDC JSON* format from the *Installation* tab
* save the configuration file into your projects classpath, i.e. `src/main/resources/keycloak.json`
* configure `src/main/webapp/WEB-INF/config/jaas.config` to include the KeycloakAuthenticationModule:
```
magnolia {
  info.magnolia.jaas.sp.jcr.JCRAuthenticationModule optional realm=system;

  it.schm.magnolia.keycloak.security.KeycloakLoginModuleAdapter requisite realm=external skip_on_previous_success=true;
  info.magnolia.jaas.sp.jcr.JCRAuthorizationModule required;
};
```

# Configuration

All additional configuration is stored in Magnolias JCR.

* login into magnolia using the `superuser` account
* go into Configurations App and navigate to `/modules/keycloak-security/config` and add your keycloakConfigFile, i.e. `classpath:keycloak.json`
* the module features a RoleMapper, which maps Keycloak roles to Magnolia roles. It is configured in `/modules/keycloak-security/config/roleMapper`.
* the module installs a UserManager into `/server/security/userManagers/external` which can be used as an extension point for customisation
