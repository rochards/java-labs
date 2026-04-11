package com.github.rochards.java_ldap_spring;

import com.github.rochards.java_ldap_spring.ldap.GroupRepository;
import com.github.rochards.java_ldap_spring.ldap.PersonRepository;
import com.github.rochards.java_ldap_spring.ldap.model.Group;
import com.github.rochards.java_ldap_spring.ldap.model.Person;

import java.util.List;

public class JavaLdapSpringApplication {

    public static void main(String[] args) {
        var personRepository = new PersonRepository();
        var groupRepository = new GroupRepository();

        System.out.println("=== Person Operations ===");
        System.out.println("- findAllPeopleIds():");
        printUidList(personRepository.findAllPeopleIds());

        var person = new Person("peter", "Peter", "Parker", "parker@spider.com");
        System.out.println("\n- savePerson():");
        System.out.printf("Saving person with uid '%s'%n", person.uid());
        personRepository.savePerson(person,"@123");
        System.out.println("Status: created");

        System.out.println("\n- findAllPeople():");
        printPeople(personRepository.findAllPeople());

        System.out.println("\n- deletePersonById():");
        System.out.printf("Deleting person with uid '%s'%n", person.uid());
        personRepository.deletePersonById(person.uid());
        System.out.println("Status: deleted");

        var uid = "alice";
        System.out.println("\n- findPersonByUid():");
        System.out.printf("Searching for uid '%s'%n", uid);
        printPersonResult(personRepository.findPersonByUid(uid).orElse(null));

        // ---------------------------

        System.out.println();
        System.out.println("=== Group Operations ===");
        System.out.println("\n- findAllGroups():");
        printGroups(groupRepository.findAllGroups());

        var cn = "app-users";
        System.out.println("\n- findGroupByCn():");
        System.out.printf("Searching for cn '%s'%n", cn);
        printGroupResult(groupRepository.findGroupByCn(cn).orElse(null));

        var personDn = "uid=alice,ou=people,dc=example,dc=org";
        System.out.println("\n- findGroupsOfPerson():");
        System.out.printf("Searching groups of member '%s'%n", personDn);
        printGroups(groupRepository.findGroupsOfPerson(personDn));
    }

    private static void printUidList(List<String> uids) {
        if (uids.isEmpty()) {
            System.out.println("No results.");
            return;
        }

        for (int i = 0; i < uids.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, uids.get(i));
        }
    }

    private static void printPeople(List<Person> people) {
        if (people.isEmpty()) {
            System.out.println("No results.");
            return;
        }

        for (int i = 0; i < people.size(); i++) {
            var person = people.get(i);
            System.out.printf(
                    "%d. uid=%s, cn=%s, sn=%s, mail=%s%n",
                    i + 1,
                    person.uid(),
                    person.cn(),
                    person.sn(),
                    person.mail()
            );
        }
    }

    private static void printPersonResult(Person person) {
        if (person == null) {
            System.out.println("Result: not found");
            return;
        }

        System.out.printf(
                "Result: uid=%s, cn=%s, sn=%s, mail=%s%n",
                person.uid(),
                person.cn(),
                person.sn(),
                person.mail()
        );
    }

    private static void printGroups(List<Group> groups) {
        if (groups.isEmpty()) {
            System.out.println("No results.");
            return;
        }

        for (int i = 0; i < groups.size(); i++) {
            var group = groups.get(i);
            System.out.printf(
                    "%d. cn=%s, dn=%s, members=%s%n",
                    i + 1,
                    group.cn(),
                    group.dn(),
                    group.members()
            );
        }
    }

    private static void printGroupResult(Group group) {
        if (group == null) {
            System.out.println("Result: not found");
            return;
        }

        System.out.printf(
                "Result: cn=%s, dn=%s, members=%s%n",
                group.cn(),
                group.dn(),
                group.members()
        );
    }
}
