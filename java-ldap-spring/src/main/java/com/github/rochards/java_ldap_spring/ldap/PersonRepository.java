package com.github.rochards.java_ldap_spring.ldap;

import com.github.rochards.java_ldap_spring.ldap.model.Person;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.Optional;

import static com.github.rochards.java_ldap_spring.ldap.LdapAttributesUtils.getAttribute;

public class PersonRepository {

    private final LdapTemplate readLdapTemplate;
    private final LdapTemplate writeLdapTemplate;

    public PersonRepository() {
        readLdapTemplate = ReadOnlyLdapTemplateFactory.getInstance();
        writeLdapTemplate = WriteLdapTemplateFactory.getInstance();
    }

    public List<String> findAllPeopleIds() {
        return readLdapTemplate.search(
                "ou=people",
                "(objectClass=inetOrgPerson)",
                (AttributesMapper<String>) attrs -> getAttribute(attrs, "uid")
        );
    }

    public List<Person> findAllPeople() {
        return readLdapTemplate.search(
                "ou=people",
                "(objectClass=inetOrgPerson)",
                (AttributesMapper<Person>) attrs -> new Person(
                        getAttribute(attrs, "uid"),
                        getAttribute(attrs, "cn"),
                        getAttribute(attrs, "sn"),
                        getAttribute(attrs, "mail")
                )
        );
    }

    public Optional<Person> findPersonByUid(String uid) {
        // Build a relative DN because the context source already provides the base dc=example,dc=org.
        var dn = LdapNameBuilder.newInstance()
                .add("ou", "people")
                .add("uid", uid)
                .build();

        try {
            var person = readLdapTemplate.lookup(
                    dn,
                    (AttributesMapper<Person> ) attrs -> new Person(
                            getAttribute(attrs, "uid"),
                            getAttribute(attrs, "cn"),
                            getAttribute(attrs, "sn"),
                            getAttribute(attrs, "mail")
                    )
            );
            return Optional.of(person);
        } catch (NameNotFoundException e) {
            return Optional.empty();
        }
    }

    public void savePerson(Person person, String password) {
        // Build a relative DN because the context source already provides the base dc=example,dc=org.
        LdapName dn = LdapNameBuilder.newInstance()
                .add("ou", "people")
                .add("uid", person.uid())
                .build();

        var context = new DirContextAdapter(dn);
        context.setAttributeValues("objectClass", new String[]{"inetOrgPerson"});
        context.setAttributeValue("uid", person.uid());
        context.setAttributeValue("cn", person.cn());
        context.setAttributeValue("sn", person.sn());
        context.setAttributeValue("mail", person.mail());
        context.setAttributeValue("userPassword", password);

        // Writes need a template bound with an account that has permission to create LDAP entries.
        writeLdapTemplate.bind(context);
    }

    /*
     * For a full replacement of an existing entry, use rebind(...):
     *
     * var context = new DirContextAdapter(dn);
     * context.setAttributeValues("objectClass", new String[]{"inetOrgPerson"});
     * context.setAttributeValue("uid", person.uid());
     * context.setAttributeValue("cn", person.cn());
     * context.setAttributeValue("sn", person.sn());
     * context.setAttributeValue("mail", person.mail());
     * context.setAttributeValue("userPassword", password);
     * ldapTemplate.rebind(context);
     *
     * For a partial update, use modifyAttributes(...) with only the changed fields:
     *
     * var context = (DirContextAdapter) ldapTemplate.lookup(dn);
     * context.setAttributeValue("mail", "new-mail@example.org");
     * context.setAttributeValue("cn", "New Common Name");
     * ldapTemplate.modifyAttributes(context);
     */

    public void deletePersonById(String uid) {
        // Build a relative DN because the context source already provides the base dc=example,dc=org.
        var dn = LdapNameBuilder.newInstance()
                .add("ou", "people")
                .add("uid", uid)
                .build();

        // Deletes also require write permission on the LDAP entry or its parent.
        writeLdapTemplate.unbind(dn);
    }
}
