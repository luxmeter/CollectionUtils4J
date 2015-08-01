package luxmeter.collectionutils;

import static java.util.Comparator.*;
import static java.util.Comparator.comparing;
import static luxmeter.collectionutils.ComparableWithSortOrder.*;
import static luxmeter.collectionutils.NullOrder.NULL_FIRST;
import static luxmeter.collectionutils.SortOrder.ASC;
import static luxmeter.collectionutils.SortOrder.DESC;
import static luxmeter.collectionutils.CollectionUtils.sortedByKey;
import static luxmeter.collectionutils.CollectionUtils.sortedByKeys;
import static luxmeter.collectionutils.CollectionUtils.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import com.google.common.collect.Lists;

public class CollectionUtilsTest {
    @Test
    public void shouldSortByLastNameAsComposedKeyInAscendingOrder() {
        List<Person> persons = createPersons();
        
        List<Person> expected = Lists.newArrayList();
        expected.add(new Person("Peter", null, "Dieter", 50));
        expected.add(new Person("Hans", null, "Dieter", 35));
        expected.add(new Person("Hans", "Christoph", "Dieter", null));
        expected.add(new Person("Lena", "Mira", "Eisen", 26));
        expected.add(new Person("Andreas", null, "Guardian", 35));
        expected.add(new Person("Lena", null, "Oelson", 28));
        expected.add(new Person("Andreas", null, "Pfingsten", 27));
        expected.add(new Person("Florian", null, "Uhrmacher", 30));
        expected.add(new Person("Jason", null, "Vettel", 28));

        assertNotEquals(expected, persons);

        List<Person> sorted = sortedByKeys(persons, person -> tuple(person.getLastName()));
        assertEquals(expected, sorted);
    }

    @Test
    public void shouldSortByLastNameAsSingleKeyInAscendingOrder() {
        List<Person> persons = createPersons();

        List<Person> expected = Lists.newArrayList();
        expected.add(new Person("Peter", null, "Dieter", 50));
        expected.add(new Person("Hans", null, "Dieter", 35));
        expected.add(new Person("Hans", "Christoph", "Dieter", null));
        expected.add(new Person("Lena", "Mira", "Eisen", 26));
        expected.add(new Person("Andreas", null, "Guardian", 35));
        expected.add(new Person("Lena", null, "Oelson", 28));
        expected.add(new Person("Andreas", null, "Pfingsten", 27));
        expected.add(new Person("Florian", null, "Uhrmacher", 30));
        expected.add(new Person("Jason", null, "Vettel", 28));

        assertNotEquals(expected, persons);

        List<Person> sorted = sortedByKey(persons, person -> person.getLastName());
        assertEquals(expected, sorted);
    }

    @Test
    public void shouldDoNothingOnNullValueAsSingleKey() {
        List<Person> persons = createPersons();
        List<Person> sorted = sortedByKey(persons, person -> null);
        assertEquals(persons, sorted);
    }

    @Test
    public void shouldSortByFirstNameAndLastNameInAscendingOrder() {
        List<Person> persons = createPersons();

        List<Person> expected = Lists.newArrayList();
        expected.add(new Person("Andreas", null, "Guardian", 35));
        expected.add(new Person("Andreas", null, "Pfingsten", 27));
        expected.add(new Person("Florian", null, "Uhrmacher", 30));
        expected.add(new Person("Hans", null, "Dieter", 35));
        expected.add(new Person("Hans", "Christoph", "Dieter", null));
        expected.add(new Person("Jason", null, "Vettel", 28));
        expected.add(new Person("Lena", "Mira", "Eisen", 26));
        expected.add(new Person("Lena", null, "Oelson", 28));
        expected.add(new Person("Peter", null, "Dieter", 50));

        assertNotEquals(expected, persons);

        List<Person> sorted = sortedByKeys(persons,
                person -> tuple(person.getFirstName(), person.getLastName()));
        assertEquals(expected, sorted);
    }


    @Test
    public void shouldSortByFirstNameAndMiddleNameInAscendingOrderAndNullFirst() {
        List<Person> persons = createPersons();

        List<Person> expected = Lists.newArrayList();
        expected.add(new Person("Andreas", null, "Pfingsten", 27));
        expected.add(new Person("Andreas", null, "Guardian", 35));
        expected.add(new Person("Florian", null, "Uhrmacher", 30));
        expected.add(new Person("Hans", null, "Dieter", 35));
        expected.add(new Person("Hans", "Christoph", "Dieter", null));
        expected.add(new Person("Jason", null, "Vettel", 28));
        expected.add(new Person("Lena", null, "Oelson", 28));
        expected.add(new Person("Lena", "Mira", "Eisen", 26));
        expected.add(new Person("Peter", null, "Dieter", 50));

        assertNotEquals(expected, persons);

        List<Person> sorted = sortedByKeys(persons,
                person -> tuple(person.getFirstName(), person.getMiddleName()), ASC, NULL_FIRST);
        assertEquals(expected, sorted);
    }

    @Test
    public void shouldSortByFirstNameAndMiddleNameInAscendingOrderAndNullFirst2() {
        List<Person> persons = createPersons();

        List<Person> expected = Lists.newArrayList();
        expected.add(new Person("Andreas", null, "Pfingsten", 27));
        expected.add(new Person("Andreas", null, "Guardian", 35));
        expected.add(new Person("Florian", null, "Uhrmacher", 30));
        expected.add(new Person("Hans", null, "Dieter", 35));
        expected.add(new Person("Hans", "Christoph", "Dieter", null));
        expected.add(new Person("Jason", null, "Vettel", 28));
        expected.add(new Person("Lena", null, "Oelson", 28));
        expected.add(new Person("Lena", "Mira", "Eisen", 26));
        expected.add(new Person("Peter", null, "Dieter", 50));

        assertNotEquals(expected, persons);

        persons.sort(comparing(Person::getFirstName)
                .thenComparing(comparing(Person::getMiddleName, nullsFirst(naturalOrder()))));
        assertEquals(expected, persons);
    }

    @Test
    public void shouldSortByIndividualSortingOrder() {
        List<Person> persons = createPersons();

        List<Person> expected = Lists.newArrayList();
        expected.add(new Person("Jason", null, "Vettel", 28));
        expected.add(new Person("Florian", null, "Uhrmacher", 30));
        expected.add(new Person("Andreas", null, "Pfingsten", 27));
        expected.add(new Person("Lena", null, "Oelson", 28));
        expected.add(new Person("Andreas", null, "Guardian", 35));
        expected.add(new Person("Lena", "Mira", "Eisen", 26));
        expected.add(new Person("Hans", "Christoph", "Dieter", null));
        expected.add(new Person("Hans", null, "Dieter", 35));
        expected.add(new Person("Peter", null, "Dieter", 50));

        assertNotEquals(expected, persons);

        List<Person> sorted = sortedByKeys(persons, person ->
                tuple(differently(person.getLastName(), DESC),
                        differently(person.getAge(), ASC, NULL_FIRST),
                        person.getFirstName()));
        assertEquals(expected, sorted);
    }

    @Test
    public void shouldReturnEmptyListOnNull() {
        List<Person> persons = null;
        assertNotNull(sortedByKeys(persons, p -> tuple(p.getAge())));
    }

    private List<Person> createPersons() {
        List<Person> persons = Lists.newArrayList();
        persons.add(new Person("Lena", "Mira", "Eisen", 26));
        persons.add(new Person("Peter", null, "Dieter", 50));
        persons.add(new Person("Andreas", null, "Pfingsten", 27));
        persons.add(new Person("Lena", null, "Oelson", 28));
        persons.add(new Person("Hans", null, "Dieter", 35));
        persons.add(new Person("Jason", null, "Vettel", 28));
        persons.add(new Person("Florian", null, "Uhrmacher", 30));
        persons.add(new Person("Andreas", null, "Guardian", 35));
        persons.add(new Person("Hans", "Christoph", "Dieter", null));
        return persons;
    }

    public static class Person {
        private final String firstName;
        private final String middleName;
        private final String lastName;
        private final Integer age;

        public Person(String firstName, String middleName, String lastName, Integer age) {
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.age = age;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public Integer getAge() {
            return age;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Person rhs = (Person) obj;
            return new EqualsBuilder()
                    .append(this.firstName, rhs.firstName)
                    .append(this.middleName, rhs.middleName)
                    .append(this.lastName, rhs.lastName)
                    .append(this.age, rhs.age)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(firstName)
                    .append(middleName)
                    .append(lastName)
                    .append(age)
                    .toHashCode();
        }
    }
}