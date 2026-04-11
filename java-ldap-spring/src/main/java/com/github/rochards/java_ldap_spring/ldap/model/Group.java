package com.github.rochards.java_ldap_spring.ldap.model;

import java.util.List;

public record Group(
        String dn,
        String cn,
        List<String> members
) {
}
