package luxmeter.collectionutils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static luxmeter.collectionutils.CollectionSortUtils.*;
import static luxmeter.collectionutils.CollectionUtils.*;
import static luxmeter.collectionutils.ComparableWithSortOrder.differently;
import static luxmeter.collectionutils.NullOrder.NULL_FIRST;
import static luxmeter.collectionutils.SortOrder.ASC;
import static luxmeter.collectionutils.SortOrder.DESC;
import static org.junit.Assert.*;

public class CollectionSortUtilsTest {
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

        List<Person> sorted = sortedByKey(persons, Person::getLastName);
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
    public void shouldSortByKeyUsingTheComparator() {
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

        persons.sort(byKey(person ->
                tuple(person.getFirstName(), person.getMiddleName()),
                ASC, NULL_FIRST));

        assertEquals(expected, persons);
    }

    @Test
    public void shouldEnumerateCollection() {
        List<Person> persons = createPersons();
        Iterator<ElementWithSequence<Person>> iterator = enumerate(persons).iterator();
        ElementWithSequence<Person> first = iterator.next();
        ElementWithSequence<Person> second = iterator.next();
        assertNotEquals(first, second);
        assertEquals(0, first.getSequence());
        assertEquals(1, second.getSequence());
        assertEquals(2, iterator.next().getSequence());
        assertEquals(3, iterator.next().getSequence());
        assertEquals(4, iterator.next().getSequence());
        assertEquals(5, iterator.next().getSequence());
        assertEquals(6, iterator.next().getSequence());
        assertEquals(7, iterator.next().getSequence());
        assertEquals(8, iterator.next().getSequence());
        assertTrue(!iterator.hasNext());
    }

    @Test
    public void shouldEnumerateCollectionWithOffset() {
        List<Person> persons = createPersons();
        Iterator<ElementWithSequence<Person>> iterator = enumerate(persons, 10).iterator();
        ElementWithSequence<Person> first = iterator.next();
        ElementWithSequence<Person> second = iterator.next();
        assertNotEquals(first, second);
        assertEquals(10, first.getSequence());
        assertEquals(11, second.getSequence());
        assertEquals(12, iterator.next().getSequence());
        assertEquals(13, iterator.next().getSequence());
        assertEquals(14, iterator.next().getSequence());
        assertEquals(15, iterator.next().getSequence());
        assertEquals(16, iterator.next().getSequence());
        assertEquals(17, iterator.next().getSequence());
        assertEquals(18, iterator.next().getSequence());
        assertTrue(!iterator.hasNext());
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

        @Override
        public String toString() {
            return "Person{" +
                    "firstName='" + firstName + '\'' +
                    ", middleName='" + middleName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}