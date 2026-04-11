package com.github.rochards.java_ldap_spring.ldap.model;

public record Person(
        String uid,
        String cn,
        String sn,
        String mail
) {
}
