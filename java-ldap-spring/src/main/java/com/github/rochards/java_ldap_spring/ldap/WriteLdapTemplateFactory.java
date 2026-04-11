package com.github.rochards.java_ldap_spring.ldap;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

public class WriteLdapTemplateFactory {

    private static LdapTemplate instance;

    private WriteLdapTemplateFactory() {}

    public static LdapTemplate getInstance() {
        if (instance == null) {
            instance = createTemplate();
        }
        return instance;
    }

    private static LdapTemplate createTemplate() {
        var contextSource = new LdapContextSource();
        // This template binds as the LDAP admin because write operations need elevated permissions.
        // The DN and password come from the local Docker Compose LDAP setup used by this project.
        contextSource.setUrl("ldap://localhost:1389");
        contextSource.setBase("dc=example,dc=org");
        contextSource.setUserDn("cn=admin,dc=example,dc=org");
        contextSource.setPassword("adminpassword");
        contextSource.afterPropertiesSet();

        /*
         * In a production application, it is recommended to wrap the context source in a
         * PoolingContextSource so LDAP write operations can reuse connections efficiently.
         *
         * var poolingContextSource = new PoolingContextSource();
         * poolingContextSource.setContextSource(contextSource);
         * // Validates pooled LDAP contexts before they are reused.
         * poolingContextSource.setDirContextValidator(new DefaultDirContextValidator());
         * // Maximum number of active LDAP connections allowed in the pool.
         * poolingContextSource.setMaxActive(8);
         * // Maximum number of idle LDAP connections kept available.
         * poolingContextSource.setMaxIdle(8);
         * // Minimum number of idle LDAP connections kept ready for reuse.
         * poolingContextSource.setMinIdle(1);
         * // Maximum time in milliseconds to wait for a pooled connection.
         * poolingContextSource.setMaxWait(5000);
         * poolingContextSource.afterPropertiesSet();
         *
         * return new LdapTemplate(poolingContextSource);
         */

        return new LdapTemplate(contextSource);
    }
}
