# CollectionUtils4J
Collection of functions I missed in Java and popular 3rd party libraries

I created this project initially because I thought neither the Java API nor 3rd party
libraries as Google Guava or Apache CollectionUtls would provide
convenient methods to sort a collection by a user defined (composed-)key.

However, I found out that the Comparator class of the Java API was updated in Java8.
Semantically there is no difference in the API calls and I bet that you can do more with the Comparator class. 
Theoretically, this project could be removed but I decided against it.
Aside from the fact that I found my API more intuitive and less verbose,
you can feel free to add functions you were missing.
In addition, my API doesn't force you to think about null values, e.g.:

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
            tuple(differently(person.getLastName(), DESC),
                    differently(person.getAge(), ASC, NULL_FIRST)));
```


It's up to you what you prefer. Happy programming :)
