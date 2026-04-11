package com.github.rochards.java_ldap_spring.ldap;

import javax.naming.directory.Attributes;

public final class LdapAttributesUtils {

    private LdapAttributesUtils() {
    }

    public static String getAttribute(Attributes attributes, String name) {
        var attribute = attributes.get(name);
        try {
            return attribute != null ? (String) attribute.get() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
