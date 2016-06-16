# CollectionUtils4J
Collection of functions I missed in Java and popular 3rd party libraries

I created this project initially because I thought neither the Java API nor 3rd party
libraries as Google Guava or Apache CollectionUtls would provide
convenient methods to sort a collection by a user defined (composed-)key.

However, I found out that the Comparator class of the Java API was updated in Java8 (people make still fun of me therefor).
Theoretically, this project could be removed but I decided against it.
I use it now as my personal playground. Feel free to join.

## Collection Utils

**Sorting of nullable keys with the Comparator API**

```java
persons.sort(comparing(Person::getFirstName)
        .thenComparing(comparing(Person::getMiddleName, nullLast(naturalOrder()))));
```

If you forget to define the nullFirst comparator, you will get a NullPointerException in case the middlename is null.
My API considers null values always as possible values for each part of the key:

**Sorting of nullable keys with the CollectionUtils API**

```java
sorted = sortedByKeys(persons, 
            person -> tuple(person.getFirstName(), person.getMiddleName()));
```

You can also define individual sorting orders:

**Individually sorting with the CollectionUtils API**

```java
sorted = sortedByKeys(persons, person ->
            tuple(differently(person.getLastName(), DESC, NULL_LAST),
                    differently(person.getAge(), ASC, NULL_FIRST)));
```

## „Lamda“ Utils

**Creating Partial Applicaple Functions**
```java
PartialFunction<Integer> partial = partial(this::sub, bind(10), free()); // type safe
int result = partial.apply(3); // 10 - 3 = 7, not type safe
```
As cool as it is, it is unfortunately neither type safe nor fast since it uses reflection underneath to make the code concise.
