package com.github.rochards.java_ldap_spring.ldap;

import com.github.rochards.java_ldap_spring.ldap.model.Group;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;

import java.util.List;
import java.util.Optional;

public class GroupRepository {

    private final LdapTemplate ldapTemplate;

    public GroupRepository() {
        ldapTemplate = ReadOnlyLdapTemplateFactory.getInstance();
    }

    public List<Group> findAllGroups() {
        var query = LdapQueryBuilder.query()
                .base("ou=groups")
                .where("objectClass").is("groupOfNames");

        // I'm not using AttributesMapper here because it does not return the dn attribute
        // because it's not a regular entry attribute.
        return ldapTemplate.search(
                query,
                (ContextMapper<Group>) ctx -> {
                    var dirCtx = (DirContextAdapter) ctx;
                    return new Group(
                            dirCtx.getDn().toString(),
                            dirCtx.getStringAttribute("cn"),
                            List.of(dirCtx.getStringAttributes("member"))
                    );
                }
        );
    }
    
    public Optional<Group> findGroupByCn(String cn) {
        var query = LdapQueryBuilder.query()
                .base("ou=groups")
                .where("objectClass").is("groupOfNames")
                .and("cn").is(cn);

        return ldapTemplate.search(
                query,
                (ContextMapper<Group>) ctx -> {
                    var dirCtx = (DirContextAdapter) ctx;
                    return new Group(
                            dirCtx.getDn().toString(),
                            dirCtx.getStringAttribute("cn"),
                            List.of(dirCtx.getStringAttributes("member"))
                    );
                }
        ).stream().findFirst();
    }

    public List<Group> findGroupsOfPerson(String personDn) {
        var query = LdapQueryBuilder.query()
                .base("ou=groups")
                .where("objectClass").is("groupOfNames")
                .and("member").is(personDn);

        return ldapTemplate.search(
                query,
                (ContextMapper<Group>) ctx -> {
                    var dirCtx = (DirContextAdapter) ctx;
                    return new Group(
                            dirCtx.getDn().toString(),
                            dirCtx.getStringAttribute("cn"),
                            List.of(dirCtx.getStringAttributes("member"))
                    );
                }
        );
    }
}
